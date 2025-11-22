module cs151.application {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Needed for graphics/native access

    requires com.dlsc.formsfx;

    opens cs151.application to javafx.fxml;
    exports cs151.application;
}