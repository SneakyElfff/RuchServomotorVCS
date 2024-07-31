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

    private Scene scene;
    private static final String COMMON_CSS_STYLE = "-fx-background-color: #04060a; " +
            "-fx-border-color: #df6a1b; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 10px;";

    @Override
    public void start(Stage primaryStage) {
        // Создание корневого контейнера
        BorderPane root = createRootPane();

        // Создание вертикального контейнера для центрирования логотипа и кнопки
        VBox centerBox = createCenterBox(root);
        root.setCenter(centerBox);

        // Создание контейнера для копирайта и центрирование его внизу
        VBox creditsText = createCreditsText();
        root.setBottom(creditsText);

        // Создание сцены с корневым контейнером
        scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Ruch VCS");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private BorderPane createRootPane() {
        BorderPane root = new BorderPane();
        root.setStyle(COMMON_CSS_STYLE + "-fx-padding: 10px;"); // Внутренний отступ
        return root;
    }

    private VBox createCenterBox(BorderPane root) {
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(new Image(getClass().getResource("/org/example/ruchservomotorvcs/images/logo.png").toExternalForm()));
        logo.setFitWidth(200);
        logo.setPreserveRatio(true);

        Button loginButton = createLoginButton(root);

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

    private Button createLoginButton(BorderPane root) {
        Button loginButton = new Button("Войти");
        loginButton.setMinWidth(200);
        loginButton.setStyle(COMMON_CSS_STYLE +
                "-fx-font-size: 18px; " +
                        "-fx-text-fill: #df6a1b; " +
                        "-fx-cursor: hand;"
        );

        loginButton.setOnAction(_ -> showLoginFields());

        return loginButton;
    }

    private void showLoginFields() {
        LoginWindow loginWindow = new LoginWindow();
        BorderPane loginPane = loginWindow.createLoginPane(this::showStartWindow, this::showMainWindow);
        scene.setRoot(loginPane);
    }

    private void showStartWindow() {
        BorderPane root = createRootPane();

        // Создание вертикального контейнера для центрирования логотипа и кнопки
        VBox centerBox = createCenterBox(root);
        root.setCenter(centerBox);

        // Создание контейнера для копирайта и центрирование его внизу
        VBox creditsText = createCreditsText();
        root.setBottom(creditsText);

        // Установить корневой элемент обратно
        scene.setRoot(root);
    }

    private void showMainWindow() {
        MainWindow mainWindow = new MainWindow();
        BorderPane mainPane = mainWindow.createMainPane(this::showStartWindow);
        scene.setRoot(mainPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
