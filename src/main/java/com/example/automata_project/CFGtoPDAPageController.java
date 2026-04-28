package com.example.automata_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CFGtoPDAPageController {
    private static List<CFGtoPDAPipeline.Transition> generatedPDA = new ArrayList<>();

    @FXML
    private TextArea inputTextArea;

    @FXML
    private TextField inputTextField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button testStringBtn;

    @FXML
    protected void onGeneratePDAClick(ActionEvent event) throws IOException {
        String grammarInput = inputTextArea == null ? "" : inputTextArea.getText();
        CFGtoPDAPipeline.PipelineResult result = CFGtoPDAPipeline.processGrammar(grammarInput);

        if (result.pda == null || result.pda.isEmpty()) {
            showParseError(result.message == null ? "Invalid grammar input." : result.message);
            return;
        }

        generatedPDA = result.pda;
        inputTextField.setVisible(true);
        testStringBtn.setVisible(true);
    }

    @FXML
    protected void onCheckInput(ActionEvent event) {
        if (generatedPDA.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Generate a PDA first from CFG.");
            return;
        }

        String input = inputTextField.getText();
        if (input == null) {
            input = "";
        }
        input = input.replaceAll("\\s+", "");

        boolean accepted = CFGtoPDAPipeline.testString(generatedPDA, input);
        if (accepted) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(CFGtoPDAPipeline.simulationMessage);
        } else {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(CFGtoPDAPipeline.simulationMessage);
        }
        return;
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "MainPage.fxml", "Main Page");
    }

    private void showParseError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid CFG");
        alert.setHeaderText("Could not generate PDA");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
