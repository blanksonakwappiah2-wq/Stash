package com.quickbite.desktop;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main extends Application {
    private static final String BACKEND_URL = "http://localhost:8080/api/users/";
    private static final String RESTAURANT_URL = "http://localhost:8080/api/restaurants";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    
    private Gson gson = new Gson();
    private User currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("QuickBite - Modern Food Ordering");

        // --- LOGIN SCENE ---
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.getStyleClass().add("grid-pane");

        Label titleLabel = new Label("Welcome to QuickBite");
        titleLabel.getStyleClass().add("title-label");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        TextField passwordTextField = new TextField();
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
        loginGrid.add(new Label("Email:"), 0, 1);
        loginGrid.add(emailField, 1, 1);
        loginGrid.add(new Label("Password:"), 0, 2);
        loginGrid.add(passwordField, 1, 2);
        loginGrid.add(passwordTextField, 1, 2);
        loginGrid.add(showPasswordCheck, 1, 3);
        loginGrid.add(loginButton, 0, 4);
        loginGrid.add(registerButton, 1, 4);
        loginGrid.add(messageLabel, 0, 5, 2, 1);

        Scene loginScene = new Scene(loginGrid, 500, 400);
        loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- REGISTER SCENE ---
        GridPane registerGrid = new GridPane();
        registerGrid.setAlignment(Pos.CENTER);
        registerGrid.setHgap(10);
        registerGrid.setVgap(10);
        registerGrid.setPadding(new Insets(25, 25, 25, 25));
        registerGrid.getStyleClass().add("grid-pane");

        TextField regNameField = new TextField();
        regNameField.setPromptText("Enter your full name");

        TextField regEmailField = new TextField();
        regEmailField.setPromptText("Enter a valid email");

        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CUSTOMER", "RESTAURANT_OWNER", "DELIVERY_AGENT", "MANAGER");
        roleCombo.setValue("CUSTOMER");

        Button regRegisterButton = new Button("Register");
        regRegisterButton.getStyleClass().add("register-button");

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("back-button");

        Label regMessageLabel = new Label();
        regMessageLabel.getStyleClass().add("message-label");

        registerGrid.add(new Label("Register New Account"), 0, 0, 2, 1);
        registerGrid.add(new Label("Name:"), 0, 1);
        registerGrid.add(regNameField, 1, 1);
        registerGrid.add(new Label("Email:"), 0, 2);
        registerGrid.add(regEmailField, 1, 2);
        registerGrid.add(new Label("Password:"), 0, 3);
        registerGrid.add(regPasswordField, 1, 3);
        registerGrid.add(new Label("Confirm:"), 0, 4);
        registerGrid.add(confirmPasswordField, 1, 4);
        registerGrid.add(new Label("Role:"), 0, 5);
        registerGrid.add(roleCombo, 1, 5);
        registerGrid.add(regRegisterButton, 0, 6);
        registerGrid.add(backButton, 1, 6);
        registerGrid.add(regMessageLabel, 0, 7, 2, 1);

        Scene registerScene = new Scene(registerGrid, 550, 500);
        registerScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- CUSTOMER MENU SCENE ---
        VBox menuVBox = new VBox(15);
        menuVBox.setAlignment(Pos.CENTER);
        menuVBox.setPadding(new Insets(25, 25, 25, 25));
        menuVBox.getStyleClass().add("menu-vbox");

        Button browseButton = new Button("Browse Restaurants");
        browseButton.getStyleClass().add("menu-button");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        menuVBox.getChildren().addAll(new Label("QuickBite Menu"), browseButton, logoutButton);
        Scene menuScene = new Scene(menuVBox, 500, 450);
        menuScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- MANAGER DASHBOARD SCENE ---
        GridPane managerGrid = new GridPane();
        managerGrid.setAlignment(Pos.CENTER);
        managerGrid.setHgap(10);
        managerGrid.setVgap(10);
        managerGrid.setPadding(new Insets(25, 25, 25, 25));
        managerGrid.getStyleClass().add("grid-pane");

        TextField restNameField = new TextField();
        TextField restAddressField = new TextField();
        TextField restContactField = new TextField();
        TextField restWebsiteField = new TextField();

        Button addRestButton = new Button("Add Restaurant");
        addRestButton.getStyleClass().add("login-button");

        Button managerLogoutButton = new Button("Logout");
        managerLogoutButton.getStyleClass().add("back-button");

        managerGrid.add(new Label("Manager Dashboard"), 0, 0, 2, 1);
        managerGrid.add(new Label("Rest. Name:"), 0, 1);
        managerGrid.add(restNameField, 1, 1);
        managerGrid.add(new Label("Address:"), 0, 2);
        managerGrid.add(restAddressField, 1, 2);
        managerGrid.add(new Label("Contact:"), 0, 3);
        managerGrid.add(restContactField, 1, 3);
        managerGrid.add(new Label("Website:"), 0, 4);
        managerGrid.add(restWebsiteField, 1, 4);
        managerGrid.add(addRestButton, 0, 5);
        managerGrid.add(managerLogoutButton, 1, 5);

        Scene managerSceneLocal = new Scene(managerGrid, 550, 500);
        managerSceneLocal.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- RESTAURANTS LIST SCENE ---
        VBox restListVBox = new VBox(10);
        restListVBox.setAlignment(Pos.TOP_CENTER);
        restListVBox.setPadding(new Insets(20));
        
        ScrollPane restScroll = new ScrollPane(restListVBox);
        restScroll.setFitToWidth(true);
        
        VBox restRoot = new VBox(10, new Label("Available Restaurants"), restScroll);
        restRoot.setAlignment(Pos.CENTER);
        restRoot.setPadding(new Insets(20));
        
        Button backToMenuBtn = new Button("Back to Menu");
        backToMenuBtn.getStyleClass().add("back-button");
        restRoot.getChildren().add(backToMenuBtn);

        Scene restaurantsSceneLocal = new Scene(restRoot, 600, 500);
        restaurantsSceneLocal.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- EVENT HANDLERS ---
        Runnable logoutAction = () -> {
            currentUser = null;
            emailField.clear();
            passwordField.clear();
            passwordTextField.clear();
            primaryStage.setScene(loginScene);
        };

        logoutButton.setOnAction(e -> logoutAction.run());
        managerLogoutButton.setOnAction(e -> logoutAction.run());
        registerButton.setOnAction(e -> primaryStage.setScene(registerScene));
        backButton.setOnAction(e -> primaryStage.setScene(loginScene));
        backToMenuBtn.setOnAction(e -> primaryStage.setScene(menuScene));

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();
            
            messageLabel.setText("");
            User user = login(email, password);
            if (user != null) {
                currentUser = user;
                if ("MANAGER".equals(user.role)) {
                    primaryStage.setScene(managerSceneLocal);
                } else {
                    primaryStage.setScene(menuScene);
                }
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });

        regRegisterButton.setOnAction(e -> {
            if (register(regNameField.getText(), regEmailField.getText(), regPasswordField.getText(), roleCombo.getValue())) {
                primaryStage.setScene(loginScene);
                showAlert("Registration Success!");
            }
        });

        addRestButton.setOnAction(e -> {
            if (addRestaurant(restNameField.getText(), restAddressField.getText(), restContactField.getText(), restWebsiteField.getText())) {
                showAlert("Restaurant Added!");
                restNameField.clear(); restAddressField.clear(); restContactField.clear(); restWebsiteField.clear();
            }
        });

        browseButton.setOnAction(e -> {
            List<Restaurant> list = fetchRestaurants();
            restListVBox.getChildren().clear();
            for (Restaurant r : list) {
                VBox card = new VBox(5);
                card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 10; -fx-background-radius: 5;");
                Label name = new Label(r.name);
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #6366f1;");
                Label info = new Label(r.address + " | " + r.contact);
                info.setStyle("-fx-font-size: 11;");
                restListVBox.getChildren().addAll(card);
                card.getChildren().addAll(name, info);
                if (r.website != null && !r.website.isEmpty()) {
                    Button webBtn = new Button("Visit Website");
                    webBtn.setOnAction(ev -> getHostServices().showDocument(r.website));
                    card.getChildren().add(webBtn);
                }
            }
            primaryStage.setScene(restaurantsSceneLocal);
        });

        showPasswordCheck.setOnAction(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordTextField.setText(passwordField.getText());
                passwordField.setVisible(false); passwordField.setManaged(false);
                passwordTextField.setVisible(true); passwordTextField.setManaged(true);
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordTextField.setVisible(false); passwordTextField.setManaged(false);
                passwordField.setVisible(true); passwordField.setManaged(true);
            }
        });

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message);
        a.showAndWait();
    }

    private User login(String email, String password) {
        try {
            String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), User.class);
            }
        } catch (Exception e) {}
        return null;
    }

    private boolean register(String name, String email, String password, String role) {
        try {
            String json = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}", name, email, password, role);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }

    private boolean addRestaurant(String name, String address, String contact, String website) {
        try {
            String json = String.format("{\"name\":\"%s\",\"address\":\"%s\",\"contact\":\"%s\",\"website\":\"%s\",\"owner\":{\"id\":%d}}", 
                                        name, address, contact, website, currentUser.id);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESTAURANT_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }

    private List<Restaurant> fetchRestaurants() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(RESTAURANT_URL)).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<ArrayList<Restaurant>>(){}.getType());
        } catch (Exception e) { return new ArrayList<>(); }
    }

    private static class User { Long id; String role; }
    private static class Restaurant { String name, address, contact, website; }
}