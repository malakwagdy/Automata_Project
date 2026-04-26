package com.example.automata_project;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Node;

import java.io.IOException;

public class SceneController {
    static String path = "/Users/Malak/D/UNI/Senior-1 Year/Semester 8/Automata and Computability/Project/Project/Automata_Project/src/main/resources/com/example/automata_project/";
    public static void switchScene(ActionEvent event, String fileName, String title) throws IOException {
        // Convert to URL and load
        String absolutePath = path + fileName;
        FXMLLoader fxmlLoader = new FXMLLoader(new java.io.File(absolutePath).toURI().toURL());
        Parent root = fxmlLoader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

//    public static void Popup(ActionEvent event, String fileName, String title) throws IOException {
//        try {
//            String absolutePath = path + fileName;
//            FXMLLoader loader = new FXMLLoader(new java.io.File(absolutePath).toURI().toURL());
//            Parent root = loader.load();
//            Stage popupStage = new Stage();
//            popupStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
//            popupStage.setTitle(title);
//            popupStage.setScene(new Scene(root));
//            popupStage.showAndWait(); // Wait until the popup is closed
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void refreshPage(ActionEvent event, String fileName) throws IOException {
        // Refresh the page
        String absolutePath = path + fileName;

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(new java.io.File(absolutePath).toURI().toURL());
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}
