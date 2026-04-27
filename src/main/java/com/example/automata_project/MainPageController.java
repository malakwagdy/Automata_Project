package com.example.automata_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainPageController {
    @FXML
    protected void onCFGtoPDAClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "CFGtoPDAPage.fxml", "Context Free Grammar to Push Down Automaton");
    }

    @FXML
    protected void onDFAClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "DFAPage.fxml", "Deterministic Finite Automaton");
    }

    @FXML
    protected void onPDAClick(ActionEvent event) throws IOException {
        SceneController.switchScene(event, "PDAPage.fxml", "Push Down Automaton");
    }
}
