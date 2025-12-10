package io.github.gcng54.flyeval.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FlyEvalApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FlyEvalApplication.class.getResource("/io/github/gcng54/flyeval/gui/flyeval-main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        stage.setTitle("Flight Evaluator Application.[GC.2025]");
        stage.setScene(scene);
        stage.show();
    }
}
