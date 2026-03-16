package com.quickbite.desktop;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String BACKEND_URL = "http://localhost:8080/api/users/";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    // Allow letters, digits, and common special characters (min 8 chars, must contain upper + lower + digit)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("QuickBite - Modern Food Ordering");

        // Login Scene
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.getStyleClass().add("grid-pane");

        Label titleLabel = new Label("Welcome to QuickBite");
        titleLabel.getStyleClass().add("title-label");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        TextField passwordTextField = new TextField();
        passwordTextField.setPromptText("Enter your password");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        CheckBox showPasswordCheck = new CheckBox("Show Password");
        showPasswordCheck.getStyleClass().add("show-password-check");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("register-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        loginGrid.add(titleLabel, 0, 0, 2, 1);
        loginGrid.add(emailLabel, 0, 1);
        loginGrid.add(emailField, 1, 1);
        loginGrid.add(passwordLabel, 0, 2);
        loginGrid.add(passwordField, 1, 2);
        loginGrid.add(passwordTextField, 1, 2);
        loginGrid.add(showPasswordCheck, 1, 3);
        loginGrid.add(loginButton, 0, 4);
        loginGrid.add(registerButton, 1, 4);
        loginGrid.add(messageLabel, 0, 5, 2, 1);

        Scene loginScene = new Scene(loginGrid, 500, 400);
        loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Register Scene
        GridPane registerGrid = new GridPane();
        registerGrid.setAlignment(Pos.CENTER);
        registerGrid.setHgap(10);
        registerGrid.setVgap(10);
        registerGrid.setPadding(new Insets(25, 25, 25, 25));
        registerGrid.getStyleClass().add("grid-pane");

        Label regTitleLabel = new Label("Register New Account");
        regTitleLabel.getStyleClass().add("title-label");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");

        Label regEmailLabel = new Label("Email:");
        TextField regEmailField = new TextField();
        regEmailField.setPromptText("Enter a valid email");

        Label regPasswordLabel = new Label("Password:");
        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password (min 8 chars, upper, lower, digit, symbols OK)");

        TextField regPasswordTextField = new TextField();
        regPasswordTextField.setPromptText("Password (min 8 chars, upper, lower, digit)");
        regPasswordTextField.setManaged(false);
        regPasswordTextField.setVisible(false);

        CheckBox regShowPasswordCheck = new CheckBox("Show Password");
        regShowPasswordCheck.getStyleClass().add("show-password-check");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");

        TextField confirmPasswordTextField = new TextField();
        confirmPasswordTextField.setPromptText("Confirm your password");
        confirmPasswordTextField.setManaged(false);
        confirmPasswordTextField.setVisible(false);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CUSTOMER", "RESTAURANT_OWNER", "DELIVERY_AGENT");
        roleCombo.setValue("CUSTOMER");

        Button regRegisterButton = new Button("Register");
        regRegisterButton.getStyleClass().add("register-button");

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("back-button");

        Label regMessageLabel = new Label();
        regMessageLabel.getStyleClass().add("message-label");

        registerGrid.add(regTitleLabel, 0, 0, 2, 1);
        registerGrid.add(nameLabel, 0, 1);
        registerGrid.add(nameField, 1, 1);
        registerGrid.add(regEmailLabel, 0, 2);
        registerGrid.add(regEmailField, 1, 2);
        registerGrid.add(regPasswordLabel, 0, 3);
        registerGrid.add(regPasswordField, 1, 3);
        registerGrid.add(regPasswordTextField, 1, 3);
        registerGrid.add(regShowPasswordCheck, 1, 4);
        registerGrid.add(confirmPasswordLabel, 0, 5);
        registerGrid.add(confirmPasswordField, 1, 5);
        registerGrid.add(confirmPasswordTextField, 1, 5);
        registerGrid.add(new Label("Role:"), 0, 6);
        registerGrid.add(roleCombo, 1, 6);
        registerGrid.add(regRegisterButton, 0, 7);
        registerGrid.add(backButton, 1, 7);
        registerGrid.add(regMessageLabel, 0, 8, 2, 1);

        Scene registerScene = new Scene(registerGrid, 550, 500);
        registerScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Menu Scene
        VBox menuVBox = new VBox(15);
        menuVBox.setAlignment(Pos.CENTER);
        menuVBox.setPadding(new Insets(25, 25, 25, 25));
        menuVBox.getStyleClass().add("menu-vbox");

        Label menuTitle = new Label("QuickBite Menu");
        menuTitle.getStyleClass().add("menu-title");

        Button browseButton = new Button("Browse Restaurants");
        browseButton.getStyleClass().add("menu-button");

        Button orderButton = new Button("Place Order");
        orderButton.getStyleClass().add("menu-button");

        Button trackButton = new Button("Track Delivery");
        trackButton.getStyleClass().add("menu-button");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        menuVBox.getChildren().addAll(menuTitle, browseButton, orderButton, trackButton, logoutButton);

        Scene menuScene = new Scene(menuVBox, 500, 450);
        menuScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Event Handlers
        showPasswordCheck.setOnAction(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordTextField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                passwordTextField.setVisible(true);
                passwordTextField.setManaged(true);
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        });

        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (passwordTextField.isVisible()) {
                passwordTextField.setText(newText);
            }
        });

        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (passwordField.isVisible()) {
                passwordField.setText(newText);
            }
        });

        regShowPasswordCheck.setOnAction(e -> {
            if (regShowPasswordCheck.isSelected()) {
                regPasswordTextField.setText(regPasswordField.getText());
                confirmPasswordTextField.setText(confirmPasswordField.getText());
                regPasswordField.setVisible(false);
                regPasswordField.setManaged(false);
                confirmPasswordField.setVisible(false);
                confirmPasswordField.setManaged(false);
                regPasswordTextField.setVisible(true);
                regPasswordTextField.setManaged(true);
                confirmPasswordTextField.setVisible(true);
                confirmPasswordTextField.setManaged(true);
            } else {
                regPasswordField.setText(regPasswordTextField.getText());
                confirmPasswordField.setText(confirmPasswordTextField.getText());
                regPasswordTextField.setVisible(false);
                regPasswordTextField.setManaged(false);
                confirmPasswordTextField.setVisible(false);
                confirmPasswordTextField.setManaged(false);
                regPasswordField.setVisible(true);
                regPasswordField.setManaged(true);
                confirmPasswordField.setVisible(true);
                confirmPasswordField.setManaged(true);
            }
        });

        regPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (regPasswordTextField.isVisible()) {
                regPasswordTextField.setText(newText);
            }
        });

        regPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (regPasswordField.isVisible()) {
                regPasswordField.setText(newText);
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (confirmPasswordTextField.isVisible()) {
                confirmPasswordTextField.setText(newText);
            }
        });

        confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (confirmPasswordField.isVisible()) {
                confirmPasswordField.setText(newText);
            }
        });

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                messageLabel.setText("Invalid entry: Please enter a valid email.");
                return;
            }
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                messageLabel.setText("Invalid entry: Password must be at least 8 characters with upper, lower, and digit.");
                return;
            }
            if (login(email, password)) {
                messageLabel.setText("Login successful!");
                primaryStage.setScene(menuScene);
            } else {
                messageLabel.setText("Login failed. Check credentials.");
            }
        });

        registerButton.setOnAction(e -> primaryStage.setScene(registerScene));

        regRegisterButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = regEmailField.getText().trim();
            String password = regShowPasswordCheck.isSelected() ? regPasswordTextField.getText() : regPasswordField.getText();
            String confirm = regShowPasswordCheck.isSelected() ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
            String role = roleCombo.getValue();

            if (name.isEmpty() || !EMAIL_PATTERN.matcher(email).matches() || !PASSWORD_PATTERN.matcher(password).matches() || !password.equals(confirm)) {
                regMessageLabel.setText("Invalid entry: Check all fields and password match.");
                return;
            }
            if (register(name, email, password, role)) {
                regMessageLabel.setText("Registration successful! Please login.");
                primaryStage.setScene(loginScene);
            } else {
                regMessageLabel.setText("Registration failed. Email may already exist.");
            }
        });

        backButton.setOnAction(e -> primaryStage.setScene(loginScene));

        browseButton.setOnAction(e -> showAlert("Browsing restaurants..."));
        orderButton.setOnAction(e -> showAlert("Placing order..."));
        trackButton.setOnAction(e -> showAlert("Tracking delivery..."));
        logoutButton.setOnAction(e -> primaryStage.setScene(loginScene));

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("QuickBite");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean login(String email, String password) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && !response.body().equals("null");
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private boolean register(String name, String email, String password, String role) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"role\":\"" + role + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}