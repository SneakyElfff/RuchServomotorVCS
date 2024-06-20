package org.example.ruchservomotorvcs;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RuchVCS extends Application {

    @Override
    public void start(Stage primaryStage) {
        // создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #04060a;");

        // создание вертикального контейнера для центрирования логотипа и кнопки
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(new Image("file:src/main/resources/org/example/ruchservomotorvcs/logo.png"));
        logo.setFitWidth(200);
        logo.setPreserveRatio(true); // сохранение пропорций

        Button loginButton = getLoginButton();

        centerBox.getChildren().addAll(logo, loginButton);

        // создание контейнера с отступом в 1 см для рамки
        VBox paddedBox = new VBox(centerBox);
        paddedBox.setAlignment(Pos.CENTER);
        paddedBox.setStyle(
                "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; "
        );

        root.setCenter(paddedBox);

        // создание контейнера для копирайта и центрирование его внизу
        VBox bottomBox = new VBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setStyle("-fx-background-color: #04060a;");
        Text copyrightText = new Text("© 2024 Developed by Nina Alhimovich");
        copyrightText.setStyle("-fx-fill: white;");
        bottomBox.getChildren().add(copyrightText);
        root.setBottom(bottomBox);

        // создание сцены с корневым контейнером
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Ruch VCS");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private static Button getLoginButton() {
        Button loginButton = new Button("Войти");
        loginButton.setMinWidth(200);
        loginButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #df6a1b; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; "
        );
        return loginButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
