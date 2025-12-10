module io.github.gcng54.flightevaluator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens io.github.gcng54.flightevaluator to javafx.fxml;
    exports io.github.gcng54.flightevaluator;
}