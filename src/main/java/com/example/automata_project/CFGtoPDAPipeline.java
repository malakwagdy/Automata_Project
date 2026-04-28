package com.example.automata_project;

import java.util.*;

public class CFGtoPDAPipeline {

    // EXPOSED VARIABLES FOR GUI
    // Req 3 & 4: These public static variables allow the GUI to grab the simulation status and stack anytime.
    public static String simulationMessage = "";
    public static Stack<String> currentSimulationStack = new Stack<>();

    //DATA STRUCTURE FOR GUI FUNCTION
    //A wrapper object to return everything the main function generated back to the GUI
    public static class PipelineResult {
        public List<Rule> grammar;
        public Set<String> terminals;
        public List<Transition> pda;
        public String message;
    }

    // 1. DATA STRUCTURES
    static class Rule {
        String lhs;
        String rhs;
        Rule(String lhs, String rhs) {
            this.lhs = lhs;
            // Support Greek epsilon.
            // We replace both lowercase and uppercase unicode epsilons with "e"
            // so the internal logic handles it seamlessly without crashing.
            this.rhs = rhs.replaceAll("\\s+", "")
                    .replace("\u03B5", "e")
                    .replace("\u03b5", "e");
        }
    }

    static class Transition {
        String currentState, input, pop, nextState, push;
        Transition(String currentState, String input, String pop, String nextState, String push) {
            this.currentState = currentState;
            this.input = input;
            this.pop = pop;
            this.nextState = nextState;
            this.push = push;
        }
    }

    // GUI ACCESSIBLE MAIN FUNCTION EQUIVALENT
    // This function mirrors exactly what the console `main` loop does,
    // but takes a raw multi-line string and returns an accessible object.
    public static PipelineResult processGrammar(String rawInput) {
        PipelineResult result = new PipelineResult();
        List<Rule> grammar = new ArrayList<>();
        Set<String> terminals = new HashSet<>();

        String[] lines = rawInput.split("\\R");
        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.equalsIgnoreCase("DONE")) continue;

            if (line.contains("$")) {
                result.message = "Error: The '$' symbol is reserved for the internal PDA stack marker. Please do not use it.";
                return result;
            }

            String[] parts = line.split("->");
            if (parts.length != 2) continue;

            String lhs = parts[0].trim();
            String[] derivations = parts[1].split("\\|");

            for (String derivation : derivations) {
                String rhs = derivation.trim();
                Rule newRule = new Rule(lhs, rhs);
                grammar.add(newRule);

                // Check against "e" because the Rule constructor automatically normalized ε to e
                if (!newRule.rhs.equals("e")) {
                    for (char c : newRule.rhs.toCharArray()) {
                        if (!Character.isUpperCase(c) && !Character.isWhitespace(c)) {
                            terminals.add(String.valueOf(c));
                        }
                    }
                }
            }
        }

        if (grammar.isEmpty()) {
            result.message = "No valid grammar entered.";
            return result;
        }

        // Apply logic
        grammar = eliminateLeftRecursion(grammar);
        result.grammar = grammar;
        result.terminals = terminals;
        result.pda = convertToPDA(grammar, terminals);
        result.message = "PDA successfully generated with " + result.pda.size() + " transitions.";

        return result;
    }

    // 2. PRE-PROCESSOR: Eliminates Direct Left Recursion
    public static List<Rule> eliminateLeftRecursion(List<Rule> rules) {
        List<Rule> newRules = new ArrayList<>();
        Map<String, List<String>> grammarMap = new LinkedHashMap<>();
        Set<String> usedVariables = new HashSet<>();

        for (Rule r : rules) {
            grammarMap.computeIfAbsent(r.lhs, k -> new ArrayList<>()).add(r.rhs);
            usedVariables.add(r.lhs);
            for(char c : r.rhs.toCharArray()) {
                if (Character.isUpperCase(c)) usedVariables.add(String.valueOf(c));
            }
        }

        for (String lhs : grammarMap.keySet()) {
            List<String> derivations = grammarMap.get(lhs);
            List<String> alphas = new ArrayList<>();
            List<String> betas = new ArrayList<>();

            for (String rhs : derivations) {
                if (rhs.startsWith(lhs)) alphas.add(rhs.substring(lhs.length()));
                else betas.add(rhs);
            }

            if (alphas.isEmpty()) {
                for (String rhs : derivations) newRules.add(new Rule(lhs, rhs));
            } else {
                String prime = getUnusedVariable(usedVariables);
                usedVariables.add(prime);
                System.out.println("  [Auto-Fix] Rewrote left-recursive rule for '" + lhs + "'. Using '" + prime + "' as the new helper variable.");

                if (betas.isEmpty()) betas.add("e");

                for (String beta : betas) {
                    newRules.add(new Rule(lhs, beta.equals("e") ? prime : beta + prime));
                }
                for (String alpha : alphas) {
                    newRules.add(new Rule(prime, alpha + prime));
                }
                newRules.add(new Rule(prime, "e"));
            }
        }
        return newRules;
    }

    private static String getUnusedVariable(Set<String> used) {
        for (char c = 'Z'; c >= 'A'; c--) {
            if (!used.contains(String.valueOf(c))) return String.valueOf(c);
        }
        return "X";
    }

    // 3. THE CONVERTER (CFG -> PDA)
    public static List<Transition> convertToPDA(List<Rule> rules, Set<String> terminals) {
        List<Transition> pda = new ArrayList<>();
        if (rules.isEmpty()) return pda;

        String startSymbol = rules.get(0).lhs;

        pda.add(new Transition("q_start", "e", "e", "q_loop", startSymbol + "$"));

        for (Rule r : rules) {
            pda.add(new Transition("q_loop", "e", r.lhs, "q_loop", r.rhs));
        }

        for (String t : terminals) {
            pda.add(new Transition("q_loop", t, t, "q_loop", "e"));
        }

        pda.add(new Transition("q_loop", "e", "$", "q_accept", "e"));

        return pda;
    }

    // 4. THE SIMULATOR (Test String -> Accept/Reject)
    public static boolean testString(List<Transition> pda, String inputString) {
        Stack<String> initialStack = new Stack<>();

        // Reset GUI variables at the start of a new test
        currentSimulationStack.clear();
        simulationMessage = "Running...";

        // Strip spaces and also normalize Greek epsilon if the user inputs it in the test string
        String cleanInput = inputString.replaceAll("\\s+", "")
                .replace("\u03B5", "e")
                .replace("\u03b5", "e");

        // If the user literally just passed an epsilon string, it means empty string
        if (cleanInput.equals("e")) cleanInput = "";

        boolean accepted = explorePath("q_start", cleanInput, initialStack, pda, 0);

        // Populate the GUI accessible message variable
        if (accepted) {
            simulationMessage = "String Accepted !";
        } else {
            simulationMessage = "String Rejected!";
        }

        return accepted;
    }

    private static boolean explorePath(String state, String remainingInput, Stack<String> stack, List<Transition> pda, int depth) {
        // Expose the stack contents so GUI can read them.
        // This takes a snapshot of the current path's stack.
        currentSimulationStack = (Stack<String>) stack.clone();

        if (depth > 1000) return false;

        if (state.equals("q_accept")) {
            return remainingInput.isEmpty();
        }

        for (Transition t : pda) {
            if (!t.currentState.equals(state)) continue;

            boolean isEpsilonInput = t.input.equals("e");
            if (!isEpsilonInput && (remainingInput.isEmpty() || !remainingInput.startsWith(t.input))) continue;

            boolean isEpsilonPop = t.pop.equals("e");
            if (!isEpsilonPop && (stack.isEmpty() || !stack.peek().equals(t.pop))) continue;

            Stack<String> nextStack = (Stack<String>) stack.clone();

            if (!isEpsilonPop) nextStack.pop();

            if (!t.push.equals("e")) {
                for (int i = t.push.length() - 1; i >= 0; i--) {
                    nextStack.push(String.valueOf(t.push.charAt(i)));
                }
            }

            String nextInput = isEpsilonInput ? remainingInput : remainingInput.substring(1);

            if (explorePath(t.nextState, nextInput, nextStack, pda, depth + 1)) {
                // Keep the winning stack state intact for the GUI to display
                currentSimulationStack = nextStack;
                return true;
            }
        }
        return false;
    }

    // 5. MAIN EXECUTION PIPELINE (Console Test)
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder rawGrammarBuilder = new StringBuilder();

        System.out.println("=== CFG to PDA Converter ===");
        System.out.println("Enter your Context-Free Grammar rules.");
        System.out.println("Format: S -> aSa | bSb | e  (Use 'e' or '\u03B5' for epsilon)");
        System.out.println("Type 'DONE' when you are finished entering rules.\n");

        while (true) {
            System.out.print("Enter rule: ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("DONE")) break;
            rawGrammarBuilder.append(line).append("\n");
        }

        // Test the newly created GUI wrapper function
        PipelineResult result = processGrammar(rawGrammarBuilder.toString());

        System.out.println("\nChecking for Left Recursion...");
        System.out.println(result.message);

        if (result.grammar == null || result.grammar.isEmpty()) {
            scanner.close();
            return;
        }

        System.out.println("Detected Terminals: " + result.terminals);
        System.out.println("PDA successfully generated with " + result.pda.size() + " transitions.\n");

        while (true) {
            System.out.print("Enter a string to test against the PDA (or type 'exit'): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) break;

            boolean isAccepted = testString(result.pda, input);

            // Testing the new static message variable
            System.out.println("-> Result: " + simulationMessage);
            // Testing the new static stack exposure
            System.out.println("-> Final Stack State: " + currentSimulationStack + "\n");
        }
        scanner.close();
    }
}