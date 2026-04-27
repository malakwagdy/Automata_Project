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
    private static final String START_STATE = "ZERO_ONES_ENDS_WITH_ONE";
    private static final String ACCEPT_STATE = "ZERO_ONES_ENDS_WITH_ZERO";

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
    private Circle ZERO_ONES_ENDS_WITH_ZERO;

    @FXML
    private Circle ZERO_ONES_ENDS_WITH_ONE;

    @FXML
    private Circle ONE_ONE_ENDS_WITH_ZERO;

    @FXML
    private Circle ONE_ONE_ENDS_WITH_ONE;

    @FXML
    private Circle TWO_ONES_ENDS_WITH_ZERO;

    @FXML
    private Circle TWO_ONES_ENDS_WITH_ONE;

    @FXML
    private void initialize() {
        stateCircleMap.put("ZERO_ONES_ENDS_WITH_ZERO", ZERO_ONES_ENDS_WITH_ZERO);
        stateCircleMap.put("ZERO_ONES_ENDS_WITH_ONE", ZERO_ONES_ENDS_WITH_ONE);
        stateCircleMap.put("ONE_ONE_ENDS_WITH_ZERO", ONE_ONE_ENDS_WITH_ZERO);
        stateCircleMap.put("ONE_ONE_ENDS_WITH_ONE", ONE_ONE_ENDS_WITH_ONE);
        stateCircleMap.put("TWO_ONES_ENDS_WITH_ZERO", TWO_ONES_ENDS_WITH_ZERO);
        stateCircleMap.put("TWO_ONES_ENDS_WITH_ONE", TWO_ONES_ENDS_WITH_ONE);

        resetStateColors();
//        highlightState(START_STATE);
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
        highlightState(START_STATE);
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
        String currentState = evaluateCurrentState(prefix);

        inputLabel.setText("Input: " + formatRemainingInput(fullInput, consumedCount));

        if (currentState == null) {
            resetStateColors();
            statusLabel.setStyle("-fx-text-fill: red;");
            dfa.setInput(prefix);
            dfa.isAccepted();
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

        dfa.setInput(prefix);
        boolean accepted = dfa.isAccepted();
        statusLabel.setStyle(accepted ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        statusLabel.setText(dfa.getMessage());
    }

    private String evaluateCurrentState(String input) {
        String state = START_STATE;
        for (int i = 0; i < input.length(); i++) {
            state = transition(state, input.charAt(i));
            if (state == null) {
                return null;
            }
        }
        return state;
    }

    private String transition(String state, char symbol) {
        switch (state) {
            case "ZERO_ONES_ENDS_WITH_ZERO":
            case "ZERO_ONES_ENDS_WITH_ONE":
                if (symbol == '0') {
                    return "ZERO_ONES_ENDS_WITH_ZERO";
                }
                if (symbol == '1') {
                    return "ONE_ONE_ENDS_WITH_ONE";
                }
                return null;
            case "ONE_ONE_ENDS_WITH_ZERO":
            case "ONE_ONE_ENDS_WITH_ONE":
                if (symbol == '0') {
                    return "ONE_ONE_ENDS_WITH_ZERO";
                }
                if (symbol == '1') {
                    return "TWO_ONES_ENDS_WITH_ONE";
                }
                return null;
            case "TWO_ONES_ENDS_WITH_ZERO":
            case "TWO_ONES_ENDS_WITH_ONE":
                if (symbol == '0') {
                    return "TWO_ONES_ENDS_WITH_ZERO";
                }
                if (symbol == '1') {
                    return "ZERO_ONES_ENDS_WITH_ONE";
                }
                return null;
            default:
                return null;
        }
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
