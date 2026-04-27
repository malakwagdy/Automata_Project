package com.example.automata_project;

import java.util.*;

public class CFGtoPDAPipeline {

    // --- 1. DATA STRUCTURES ---
    static class Rule {
        String lhs;
        String rhs;
        Rule(String lhs, String rhs) {
            this.lhs = lhs;
            // We strip spaces here so the left-recursion detection math
            // doesn't accidentally fail if the user types "E -> E + T" instead of "E->E+T"
            this.rhs = rhs.replaceAll("\\s+", "");
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

    // --- 2. PRE-PROCESSOR: Eliminates Direct Left Recursion ---
    public static List<Rule> eliminateLeftRecursion(List<Rule> rules) {
        List<Rule> newRules = new ArrayList<>();
        Map<String, List<String>> grammarMap = new LinkedHashMap<>(); // Preserves insertion order
        Set<String> usedVariables = new HashSet<>();

        // Group derivations by their left-hand side and map all used variables
        for (Rule r : rules) {
            grammarMap.computeIfAbsent(r.lhs, k -> new ArrayList<>()).add(r.rhs);
            usedVariables.add(r.lhs);
            for(char c : r.rhs.toCharArray()) {
                if (Character.isUpperCase(c)) usedVariables.add(String.valueOf(c));
            }
        }

        // Process each variable
        for (String lhs : grammarMap.keySet()) {
            List<String> derivations = grammarMap.get(lhs);
            List<String> alphas = new ArrayList<>(); // Left-recursive parts (e.g., "+T")
            List<String> betas = new ArrayList<>();  // Non-recursive parts (e.g., "T")

            for (String rhs : derivations) {
                if (rhs.startsWith(lhs)) alphas.add(rhs.substring(lhs.length()));
                else betas.add(rhs);
            }

            if (alphas.isEmpty()) {
                // No left recursion, keep it exactly as it is
                for (String rhs : derivations) newRules.add(new Rule(lhs, rhs));
            } else {
                // Left recursion detected! Grab a fresh, unused variable.
                String prime = getUnusedVariable(usedVariables);
                usedVariables.add(prime);
                System.out.println("  [Auto-Fix] Rewrote left-recursive rule for '" + lhs + "'. Using '" + prime + "' as the new helper variable.");

                if (betas.isEmpty()) betas.add("e"); // Failsafe if grammar is malformed

                // Rule 1: A -> beta A'
                for (String beta : betas) {
                    newRules.add(new Rule(lhs, beta.equals("e") ? prime : beta + prime));
                }
                // Rule 2: A' -> alpha A' | e
                for (String alpha : alphas) {
                    newRules.add(new Rule(prime, alpha + prime));
                }
                newRules.add(new Rule(prime, "e"));
            }
        }
        return newRules;
    }

    // Helper method to find an unused uppercase letter (starts at Z and works backward)
    private static String getUnusedVariable(Set<String> used) {
        for (char c = 'Z'; c >= 'A'; c--) {
            if (!used.contains(String.valueOf(c))) return String.valueOf(c);
        }
        return "X"; // Fallback
    }

    // --- 3. THE CONVERTER (CFG -> PDA) ---
    public static List<Transition> convertToPDA(List<Rule> rules, Set<String> terminals) {
        List<Transition> pda = new ArrayList<>();
        if (rules.isEmpty()) return pda;

        String startSymbol = rules.get(0).lhs;

        // Step A: Initialization (Push Start Symbol and '$' marker)
        pda.add(new Transition("q_start", "e", "e", "q_loop", startSymbol + "$"));

        // Step B: Rule Substitution
        for (Rule r : rules) {
            pda.add(new Transition("q_loop", "e", r.lhs, "q_loop", r.rhs));
        }

        // Step C: Terminal Matching
        for (String t : terminals) {
            pda.add(new Transition("q_loop", t, t, "q_loop", "e"));
        }

        // Step D: Acceptance
        pda.add(new Transition("q_loop", "e", "$", "q_accept", "e"));

        return pda;
    }

    // --- 4. THE SIMULATOR (Test String -> Accept/Reject) ---
    public static boolean testString(List<Transition> pda, String inputString) {
        Stack<String> initialStack = new Stack<>();
        // Stripping spaces from the input string so it matches the stripped rules
        return explorePath("q_start", inputString.replaceAll("\\s+", ""), initialStack, pda, 0);
    }

    private static boolean explorePath(String state, String remainingInput, Stack<String> stack, List<Transition> pda, int depth) {
        // --- THE INFINITE LOOP SAFEGUARD ---
        // Prevents StackOverflowError if the parser gets stuck in a cycle (e.g., A -> B, B -> A)
        if (depth > 1000) return false;

        // Base case: If we reach accept state and input is empty
        if (state.equals("q_accept")) {
            return remainingInput.isEmpty();
        }

        // Try every possible transition
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

            // Recursive call passing depth + 1
            if (explorePath(t.nextState, nextInput, nextStack, pda, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    // --- 5. MAIN EXECUTION PIPELINE ---
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Rule> grammar = new ArrayList<>();
        Set<String> terminals = new HashSet<>();

        System.out.println("=== CFG to PDA Converter ===");
        System.out.println("Enter your Context-Free Grammar rules.");
        System.out.println("Format: S -> aSa | bSb | e  (Use 'e' for epsilon)");
        System.out.println("Type 'DONE' when you are finished entering rules.\n");

        while (true) {
            System.out.print("Enter rule: ");
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("DONE")) {
                break;
            }

            if (line.isEmpty()) {
                continue;
            }

            if (line.contains("$")) {
                System.out.println("  [!] Error: The '$' symbol is reserved for the internal PDA stack marker. Please do not use it in your grammar.");
                continue;
            }

            String[] parts = line.split("->");
            if (parts.length != 2) {
                System.out.println("  [!] Invalid format. Please use '->' to separate left and right sides.");
                continue;
            }

            String lhs = parts[0].trim();
            String[] derivations = parts[1].split("\\|");

            for (String derivation : derivations) {
                String rhs = derivation.trim();
                grammar.add(new Rule(lhs, rhs));

                if (!rhs.equals("e")) {
                    for (char c : rhs.toCharArray()) {
                        if (!Character.isUpperCase(c) && !Character.isWhitespace(c)) {
                            terminals.add(String.valueOf(c));
                        }
                    }
                }
            }
        }

        if (grammar.isEmpty()) {
            System.out.println("No grammar entered. Exiting...");
            scanner.close();
            return;
        }

        // --- NEW: Intercept and Fix Left Recursion ---
        System.out.println("\nChecking for Left Recursion...");
        grammar = eliminateLeftRecursion(grammar);

        System.out.println("Detected Terminals: " + terminals);

        System.out.println("Converting CFG to PDA...");
        List<Transition> pda = convertToPDA(grammar, terminals);
        System.out.println("PDA successfully generated with " + pda.size() + " transitions.\n");

        while (true) {
            System.out.print("Enter a string to test against the PDA (or type 'exit'): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) break;

            boolean isAccepted = testString(pda, input);

            if (isAccepted) {
                System.out.println("-> Result: ACCEPTED\n");
            } else {
                System.out.println("-> Result: REJECTED\n");
            }
        }
        scanner.close();
    }
}