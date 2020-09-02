module org.baratta {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires Shared;

    opens org.baratta to javafx.fxml;
    exports org.baratta;
}