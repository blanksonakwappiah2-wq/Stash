package com.quickbite.desktop;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        // Authenticate user
        System.out.println("Logging in with " + email);
    }
}