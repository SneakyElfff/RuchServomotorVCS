package org.example.ruchservomotorvcs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginWindow {

    public BorderPane createLoginPane(Runnable onBack, Runnable onLoginSuccess) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px;" // Внутренний отступ
        );

        // Создание полей ввода и кнопок
        VBox loginBox = createLoginBox(onLoginSuccess);
        root.setCenter(loginBox);

        // Создание кнопки назад
        HBox topBox = createBackButton(onBack);
        root.setTop(topBox);

        return root;
    }

    private VBox createLoginBox(Runnable onLoginSuccess) {
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setMaxWidth(200);
        usernameField.setPromptText("Логин");
        usernameField.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;"
        );

        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(200);
        passwordField.setPromptText("Пароль");
        passwordField.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;"
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button submitButton = new Button("Войти");
        submitButton.setMinWidth(200);
        submitButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b; " +
                        "-fx-text-fill: #04060a; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        submitButton.setOnAction(_ -> {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                errorLabel.setText("Пожалуйста, заполните все поля.");
            } else {
                errorLabel.setText("");
                onLoginSuccess.run();
            }
        });

        usernameField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);

        loginBox.getChildren().addAll(usernameField, passwordField, submitButton, errorLabel);
        return loginBox;
    }

    private HBox createBackButton(Runnable onBack) {
        Button backButton = new Button("<");
        backButton.setMinWidth(40);
        backButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #df6a1b; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(_ -> onBack.run());

        HBox topBox = new HBox(backButton);
        topBox.setAlignment(Pos.TOP_LEFT);
        topBox.setStyle("-fx-padding: 10px;"); // Внешний отступ
        return topBox;
    }
}
