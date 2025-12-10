package io.github.gcng54.flyeval.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FlyEvalController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
