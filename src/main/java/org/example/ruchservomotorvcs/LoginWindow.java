package org.example.ruchservomotorvcs;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginWindow {
    private static final String COMMON_CSS_STYLE = "-fx-background-color: #04060a; " +
            "-fx-border-color: #df6a1b; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 10px;";

    public BorderPane createLoginPane(Runnable onBack, Runnable onLoginSuccess) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(COMMON_CSS_STYLE + "-fx-padding: 10px;"); // Внутренний отступ

        // Создание полей ввода и кнопок
        VBox loginBox = createLoginBox(onLoginSuccess);
        root.setCenter(loginBox);

        // Создание кнопки назад
        HBox topBox = createBackButton(onBack);
        root.setTop(topBox);

        return root;
    }

    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setMaxWidth(200);
        textField.setPromptText(promptText);
        textField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 18px; " +
                "-fx-text-fill: #ffffff; ");
        return textField;
    }

    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(200);
        passwordField.setPromptText("Пароль");
        passwordField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 18px; " +
                "-fx-text-fill: #ffffff; ");
        return passwordField;
    }

    private Button createStyledButton(String buttonName) {
        Button button = new Button(buttonName);
        button.setMinWidth(200);
        button.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b; " +
                        "-fx-text-fill: #04060a; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        return button;
    }

    private VBox createLoginBox(Runnable onLoginSuccess) {
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);

        TextField usernameField = createStyledTextField("Логин");
        PasswordField passwordField = createPasswordField();

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button submitButton = createStyledButton("Войти");
        submitButton.setOnAction(event -> handleLogin(usernameField, passwordField, errorLabel, onLoginSuccess));

        usernameField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);

        loginBox.getChildren().addAll(usernameField, passwordField, submitButton, errorLabel);
        return loginBox;
    }

    private void handleLogin(TextField usernameField, PasswordField passwordField, Label errorLabel, Runnable onLoginSuccess) {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            errorLabel.setText("Пожалуйста, заполните все поля.");
        } else {
            boolean isValidUser = DatabaseUtil.validateUser(usernameField.getText(), passwordField.getText());
            if (isValidUser) {
                errorLabel.setText("");
                onLoginSuccess.run();
            } else {
                errorLabel.setText("Неверный логин или пароль.");
            }
        }
    }

    private HBox createButtonBox(Button backButton) {
        HBox topBox = new HBox(backButton);
        topBox.setAlignment(Pos.TOP_LEFT);
        topBox.setStyle("-fx-padding: 10px;"); // Внешний отступ
        return topBox;
    }

    private HBox createBackButton(Runnable onBack) {
        Button backButton = new Button("<");
        backButton.setMinWidth(40);
        backButton.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 18px; " +
                "-fx-text-fill: #df6a1b; " +
                "-fx-cursor: hand;");

        backButton.setOnAction(_ -> onBack.run());

        return createButtonBox(backButton);
    }
}
