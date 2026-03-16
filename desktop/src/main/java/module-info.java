module com.quickbite.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.quickbite.desktop to javafx.fxml, javafx.graphics;
}