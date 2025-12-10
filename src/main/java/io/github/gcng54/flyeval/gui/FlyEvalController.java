package io.github.gcng54.flyeval.gui;

import javafx.fxml.FXML;

public class FlyEvalController {


    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Window window = source.getScene().getWindow();
        window.hide();
    }

    @FXML
    public void initialize() {
        // The tabs are already loaded by the radeval_mainform-view.fxml via <fx:include>.
        // Manually loading them here again was causing the application to fail during startup.
        // This method can be left empty.
    }

}
