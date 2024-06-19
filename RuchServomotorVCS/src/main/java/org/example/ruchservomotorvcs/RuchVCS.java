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
        // Создание корневого контейнера BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #04060a;");

        // Создание вертикального контейнера для центрирования логотипа и кнопки
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        // Добавление логотипа
        ImageView logo = new ImageView(new Image("file:src/main/resources/org/example/ruchservomotorvcs/logo.png"));
        logo.setFitWidth(200); // Ширина логотипа
        logo.setPreserveRatio(true); // Сохранение пропорций

        // Добавление кнопки "Войти"
        Button loginButton = new Button("Войти");
        loginButton.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #df6a1b; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );

        // Добавление логотипа и кнопки в контейнер для центрирования
        centerBox.getChildren().addAll(logo, loginButton);

        // Добавление центрального контейнера в BorderPane по центру
        root.setCenter(centerBox);

        // Создание контейнера для копирайта
        Text copyrightText = new Text("© 2024 Developed by Nina Alhimovich");
        copyrightText.setStyle("-fx-fill: white;");

        // Добавление копирайта в BorderPane в нижнюю часть
        root.setBottom(copyrightText);

        // Создание сцены с корневым контейнером
        Scene scene = new Scene(root, 800, 600);

        // Установка заголовка окна
        primaryStage.setTitle("Ruch VCS");
        // Установка сцены в окно
        primaryStage.setScene(scene);
        // Отображение окна
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
