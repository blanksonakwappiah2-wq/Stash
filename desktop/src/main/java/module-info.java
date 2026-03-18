module com.quickbite.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;

    opens com.quickbite.desktop to javafx.fxml, javafx.graphics, com.google.gson;
}