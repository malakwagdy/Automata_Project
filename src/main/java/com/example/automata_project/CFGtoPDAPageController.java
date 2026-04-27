package com.example.automata_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGtoPDAPageController {
    private static final double STEP_DELAY_SECONDS = 1.2;
    private static List<CFGtoPDAPipeline.Transition> generatedPDA = new ArrayList<>();

    private Timeline simulationTimeline;

    @FXML
    private TextArea inputTextArea;

    @FXML
    private TextField inputTextField;

    @FXML
    private Label inputLabel;

    @FXML
    private Label stackLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label PDALabel;

    @FXML
    private void initialize() {
        if (PDALabel != null) {
            PDALabel.setText("q_start");
        }
        if (statusLabel != null && generatedPDA.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("No generated PDA found. Please generate it from the CFG page.");
        }
        if (inputLabel != null) {
            inputLabel.setText("Input: \u03B5");
        }
        if (stackLabel != null) {
            stackLabel.setText("$");
        }
    }

    @FXML
    protected void onGeneratePDAClick(ActionEvent event) throws IOException {
        String grammarInput = inputTextArea == null ? "" : inputTextArea.getText();
        ParsedGrammar parsedGrammar = parseGrammarInput(grammarInput);
        if (!parsedGrammar.isValid()) {
            return;
        }

        List<CFGtoPDAPipeline.Rule> normalizedRules = CFGtoPDAPipeline.eliminateLeftRecursion(parsedGrammar.rules());
        generatedPDA = CFGtoPDAPipeline.convertToPDA(normalizedRules, parsedGrammar.terminals());
        switchToGeneratedPDAPage(event);
    }

    @FXML
    protected void onCheckInput(ActionEvent event) {
        if (generatedPDA.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Generate a PDA first from CFG.");
            return;
        }

        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }

        String input = inputTextField.getText();
        if (input == null) {
            input = "";
        }
        input = input.replaceAll("\\s+", "");

        if (input.isEmpty()) {
            boolean accepted = CFGtoPDAPipeline.testString(generatedPDA, input);
            inputLabel.setText("Input: \u03B5");
            stackLabel.setText("$");
            if (accepted) {
                PDALabel.setText("q_accept");
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("String Accepted!");
            } else {
                PDALabel.setText("q_loop");
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("String Rejected.");
            }
            return;
        }

        runSimulation(input);
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "MainPage.fxml", "Main Page");
    }

    private void runSimulation(String input) {
        simulationTimeline = new Timeline();

        for (int i = 0; i <= input.length(); i++) {
            final int consumedCount = i;
            simulationTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(i * STEP_DELAY_SECONDS), e -> updateSimulationStep(input, consumedCount))
            );
        }

        simulationTimeline.play();
    }

    private void updateSimulationStep(String fullInput, int consumedCount) {
        String prefix = fullInput.substring(0, consumedCount);
        boolean finished = consumedCount == fullInput.length();

        inputLabel.setText("Input: " + formatRemainingInput(fullInput, consumedCount));
        stackLabel.setText("$");
        PDALabel.setText(consumedCount == 0 ? "q_start" : "q_loop");

        if (!finished) {
            statusLabel.setStyle("-fx-text-fill: black;");
            statusLabel.setText("Running...");
            return;
        }

        boolean accepted = CFGtoPDAPipeline.testString(generatedPDA, prefix);
        if (accepted) {
            PDALabel.setText("q_accept");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("String Accepted!");
        } else {
            PDALabel.setText("q_loop");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("String Rejected.");
        }
    }

    private String formatRemainingInput(String input, int consumedCount) {
        String remaining = input.substring(consumedCount);
        return remaining.isEmpty() ? "\u03B5" : remaining;
    }

    private ParsedGrammar parseGrammarInput(String rawInput) {
        List<CFGtoPDAPipeline.Rule> rules = new ArrayList<>();
        Set<String> terminals = new HashSet<>();

        if (rawInput == null || rawInput.trim().isEmpty()) {
            updateCFGError("Please enter at least one production.");
            return new ParsedGrammar(rules, terminals, false);
        }

        String[] lines = rawInput.split("\\R");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (trimmedLine.contains("$")) {
                updateCFGError("The '$' symbol is reserved and cannot be used in productions.");
                return new ParsedGrammar(rules, terminals, false);
            }

            String[] parts = trimmedLine.split("->");
            if (parts.length != 2) {
                updateCFGError("Invalid production format. Use LHS -> RHS.");
                return new ParsedGrammar(rules, terminals, false);
            }

            String lhs = parts[0].trim();
            if (lhs.isEmpty()) {
                updateCFGError("Invalid production format. Missing left-hand side.");
                return new ParsedGrammar(rules, terminals, false);
            }

            String[] derivations = parts[1].split("\\|");
            for (String derivation : derivations) {
                String rhs = derivation.trim();
                if (rhs.isEmpty()) {
                    updateCFGError("Invalid production format. Empty derivation is not allowed.");
                    return new ParsedGrammar(rules, terminals, false);
                }

                rules.add(new CFGtoPDAPipeline.Rule(lhs, rhs));
                if (!rhs.equals("e")) {
                    for (char c : rhs.toCharArray()) {
                        if (!Character.isUpperCase(c) && !Character.isWhitespace(c)) {
                            terminals.add(String.valueOf(c));
                        }
                    }
                }
            }
        }

        if (rules.isEmpty()) {
            updateCFGError("No valid productions found.");
            return new ParsedGrammar(rules, terminals, false);
        }

        return new ParsedGrammar(rules, terminals, true);
    }

    private void updateCFGError(String message) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(message);
        }
    }

    private void switchToGeneratedPDAPage(ActionEvent event) throws IOException {
        String absolutePath = SceneController.path + "GeneratedPDAPage.fxml";
        FXMLLoader loader = new FXMLLoader(new java.io.File(absolutePath).toURI().toURL());
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Generated Push Down Automaton");
        stage.show();
    }

    private record ParsedGrammar(List<CFGtoPDAPipeline.Rule> rules, Set<String> terminals, boolean isValid) {}
}
