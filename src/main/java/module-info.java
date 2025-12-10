module io.github.gcng54.flyeval {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.jetbrains.annotations;

    exports io.github.gcng54.flyeval.gui;
    opens io.github.gcng54.flyeval.gui to javafx.fxml;
}