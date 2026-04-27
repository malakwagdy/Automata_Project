package com.example.automata_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Stack;

public class PDAPageController {
    private final PushDownAutomaton pda = new PushDownAutomaton();
    private Timeline simulationTimeline;
    private static final double STEP_DELAY_SECONDS = 1.2;

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
        PDALabel.setText("p");
    }

    @FXML
    protected void onCheckInput(ActionEvent event) {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }

        String input = inputTextField.getText();
        if (input == null) {
            input = "";
        }

        if (input.isEmpty()) {
            pda.setInput(input);
            pda.isAccepted();
            inputLabel.setText("Input: \u03B5");
            stackLabel.setText(formatStackVertical(pda.stack));
            PDALabel.setText("f");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(pda.getMessage());
            return;
        }

        runSimulation(input);
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "MainPage.fxml", "Main Page");
    }

    private String formatRemainingInput(String input, int consumedCount) {
        String remaining = input.substring(consumedCount);
        return remaining.isEmpty() ? "\u03B5" : remaining;
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
        pda.setInput(prefix);
        boolean prefixAccepted = pda.isAccepted();
        String message = pda.getMessage();
        boolean finished = consumedCount == fullInput.length();

        inputLabel.setText("Input: " + formatRemainingInput(fullInput, consumedCount));
        stackLabel.setText(formatStackVertical(pda.stack));

        if (!finished && shouldStopEarly(message)) {
            PDALabel.setText(hasConsumedB(prefix) ? "q" : "p");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(message);
            if (simulationTimeline != null) {
                simulationTimeline.stop();
            }
            return;
        }

        if (!finished) {
            PDALabel.setText(hasConsumedB(prefix) ? "q" : "p");
            statusLabel.setStyle("-fx-text-fill: black;");
            statusLabel.setText("Running...");
            return;
        }

        if (prefixAccepted) {
            PDALabel.setText("f");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(message);
        } else {
            PDALabel.setText(hasConsumedB(prefix) ? "q" : "p");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(message);
        }
    }

    private boolean hasConsumedB(String s) {
        return s.indexOf('b') >= 0;
    }

    private boolean shouldStopEarly(String message) {
        return !("String Rejected: more a's than b's".equals(message) || "String Accepted!".equals(message));
    }

    private String formatStackVertical(Stack<Character> stack) {
        StringBuilder sb = new StringBuilder();
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (i < stack.size() - 1) {
                sb.append('\n');
            }
            sb.append(stack.get(i));
        }
        return sb.toString();
    }
}
