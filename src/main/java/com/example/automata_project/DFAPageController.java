package com.example.automata_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DFAPageController {
    private static final double STEP_DELAY_SECONDS = 1.0;
    private static final String ACCEPT_STATE = "ZERO_ONES";

    private final DFA dfa = new DFA();
    private final Map<String, Circle> stateCircleMap = new HashMap<>();
    private Timeline simulationTimeline;

    @FXML
    private TextField inputTextField;

    @FXML
    private Label inputLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Circle START;

    @FXML
    private Circle ZERO_ONES;

    @FXML
    private Circle ONE_ONE;

    @FXML
    private Circle TWO_ONES;

    @FXML
    private void initialize() {
        stateCircleMap.put("START", START);
        stateCircleMap.put("ZERO_ONES", ZERO_ONES);
        stateCircleMap.put("ONE_ONE", ONE_ONE);
        stateCircleMap.put("TWO_ONES", TWO_ONES);

        resetStateColors();
        inputLabel.setText("Input:  -");
        statusLabel.setText("");
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
        runSimulation(input);
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }
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
        dfa.setInput(prefix);
        boolean acceptedNow = dfa.isAccepted();

        String currentState = dfa.getCurrentState();
        inputLabel.setText("Input: " + formatRemainingInput(fullInput, consumedCount));

        if (currentState == null) {
            resetStateColors();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(dfa.getMessage());
            if (simulationTimeline != null) {
                simulationTimeline.stop();
            }
            return;
        }

        highlightState(currentState);

        boolean finished = consumedCount == fullInput.length();
        if (!finished) {
            statusLabel.setStyle("-fx-text-fill: black;");
            statusLabel.setText("Running...");
            return;
        }

        statusLabel.setStyle(acceptedNow ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        statusLabel.setText(dfa.getMessage());
    }

    private void highlightState(String stateName) {
        resetStateColors();
        Circle stateCircle = stateCircleMap.get(stateName);
        if (stateCircle == null) {
            return;
        }
        boolean isAcceptState = ACCEPT_STATE.equals(stateName);
        stateCircle.setFill(isAcceptState ? Color.LIGHTGREEN : Color.LIGHTBLUE);
    }

    private void resetStateColors() {
        for (Circle circle : stateCircleMap.values()) {
            circle.setFill(Color.LIGHTGRAY);
        }
    }

    private String formatRemainingInput(String input, int consumedCount) {
        String remaining = input.substring(consumedCount);
        return remaining.isEmpty() ? " - " : remaining;
    }
}
