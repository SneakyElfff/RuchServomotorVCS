package org.example.ruchservomotorvcs;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.sql.*;
import java.util.Objects;

public class MainWindow {

    private VBox menuPanel;

    public BorderPane createMainPane(Runnable onLogout) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 30px;" // Внутренний отступ
        );

        // Создание контента для главного окна
        VBox mainBox = createMainBox();

        // Создание кнопки меню
        Button menuButton = createMenuButton();
        StackPane topRight = new StackPane(menuButton);
        topRight.setAlignment(Pos.TOP_RIGHT);

        // Создание панели меню (изначально скрытой)
        menuPanel = createMenuPanel(onLogout);
        menuPanel.setVisible(false);
        menuPanel.setManaged(false);

        // Создание StackPane для наложения меню поверх основного контента
        StackPane contentStack = new StackPane(mainBox, menuPanel);
        StackPane.setAlignment(menuPanel, Pos.TOP_RIGHT);

        // Добавление всего в корневой контейнер
        root.setTop(topRight);
        root.setCenter(contentStack);

        return root;
    }

    private Button createMenuButton() {
        Button menuButton = new Button("☰");
        menuButton.setMinWidth(40);
        menuButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #df6a1b; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        menuButton.setOnAction(e -> toggleMenuPanel());

        return menuButton;
    }

    private void toggleMenuPanel() {
        menuPanel.setVisible(!menuPanel.isVisible());
        menuPanel.setManaged(menuPanel.isVisible());
    }

    private VBox createMenuPanel(Runnable onLogout) {
        VBox panel = new VBox(10);
        panel.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-padding: 10px;" +
                        "-fx-min-width: 200px;" +
                        "-fx-max-width: 200px;"
        );
        panel.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Меню");
        titleLabel.setStyle("-fx-text-fill: #df6a1b; -fx-font-size: 18px;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #df6a1b;");

        Button logoutButton = new Button("Выйти");
        logoutButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b;" +
                        "-fx-text-fill: #04060a;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        logoutButton.setOnAction(e -> {
            toggleMenuPanel(); // Скрыть меню перед выходом
            onLogout.run();
        });

        panel.getChildren().addAll(titleLabel, separator, logoutButton);
        return panel;
    }

    private VBox createMainBox() {
        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.CENTER);

        // Создание таблицы
        TableView<ObservableList<Object>> table = new TableView<>();

        table.getStyleClass().add("edge-to-edge"); // Применяем класс стилей для рамки

        String cssPath = Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm();
        System.out.println("CSS Path: " + cssPath);
        if (cssPath == null) {
            throw new RuntimeException("Cannot find CSS file");
        }
        table.getStylesheets().add(cssPath);

        // Заполнение таблицы данными из базы данных
        try {
            ObservableList<ObservableList<Object>> data = getTable(table);
            table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mainBox.getChildren().addAll(table);

        return mainBox;
    }

    private ObservableList<ObservableList<Object>> getTable(TableView<ObservableList<Object>> table) throws SQLException {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        String query = "SELECT i.item_number, i.blueprint_number, i.project_number, " +
                "r.review_number, r.revision, r.author, r.review_date, r.review_text, " +
                "r.in_charge, r.fix_date, r.notes " +
                "FROM items i " +
                "JOIN remarks r ON i.item_number = r.item_number";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Создание столбцов таблицы на основе метаданных
            for (int i = 1; i <= columnCount; i++) {
                final int j = i;
                TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(metaData.getColumnName(i));
                column.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue().get(j - 1)));
                table.getColumns().add(column);
            }

            // Заполнение данных
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }
        }

        return data;
    }
}
