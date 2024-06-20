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

public class StartWindow extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Создание корневого контейнера
        BorderPane root = createRootPane();

        // Создание вертикального контейнера для центрирования логотипа и кнопки
        VBox centerBox = createCenterBox();
        root.setCenter(centerBox);

        // Создание контейнера для копирайта и центрирование его внизу
        VBox creditsText = createCreditsText();
        root.setBottom(creditsText);

        // Создание сцены с корневым контейнером
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Ruch VCS");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private BorderPane createRootPane() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #04060a;");

        return root;
    }

    private VBox createCenterBox() {
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle(
                "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; "
        );

        ImageView logo = new ImageView(new Image("file:src/main/resources/org/example/ruchservomotorvcs/logo.png"));
        logo.setFitWidth(200);
        logo.setPreserveRatio(true);

        Button loginButton = createLoginButton();

        centerBox.getChildren().addAll(logo, loginButton);

        return centerBox;
    }

    private VBox createCreditsText() {
        VBox creditsText = new VBox();
        creditsText.setAlignment(Pos.CENTER);
        creditsText.setStyle("-fx-background-color: #04060a;");

        Text copyrightText = new Text("© 2024 Developed by Nina Alhimovich");
        copyrightText.setStyle("-fx-fill: white;");
        creditsText.getChildren().add(copyrightText);

        return creditsText;
    }

    private Button createLoginButton() {
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
