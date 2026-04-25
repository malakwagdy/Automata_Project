module com.example.automata_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.automata_project to javafx.fxml;
    exports com.example.automata_project;
}