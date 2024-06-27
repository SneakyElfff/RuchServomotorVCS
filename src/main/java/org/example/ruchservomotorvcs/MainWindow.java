package org.example.ruchservomotorvcs;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.sql.*;
import java.util.Objects;

public class MainWindow {

    private VBox menuPanel;
    private TableView<ObservableList<Object>> table;

    public BorderPane createMainPane(Runnable onLogout) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 20px;" // Внутренний отступ
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

        // Создание кнопки "Добавить запись"
        Button addButton = new Button("Добавить запись");
        addButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b; " +
                        "-fx-text-fill: #04060a; " +
                        "-fx-border-radius: 10px;" +
                        "-fx-cursor: hand;"
        );

        addButton.setOnAction(event -> showAddRecordForm());

        // Создание таблицы
        table = new TableView<>();
        table.getStyleClass().add("edge-to-edge"); // Применяем класс стилей для рамки

        String cssPath = Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm();
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

        mainBox.getChildren().addAll(addButton, table);

        return mainBox;
    }

    private void showAddRecordForm() {
        // Создание нового окна (Stage) для формы
        Stage formStage = new Stage();
        formStage.setTitle("Добавить запись");

        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px;"
        );

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT i.item_number, i.blueprint_number, i.project_number, " +
                    "r.review_number, r.revision, r.author, r.review_date, r.review_text, " +
                    "r.in_charge, r.fix_date, r.notes " +
                    "FROM items i " +
                    "JOIN remarks r ON i.item_number = r.item_number LIMIT 1";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                TextField[] inputFields = new TextField[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    TextField textField = new TextField();
                    textField.setPromptText(columnName);
                    inputFields[i - 1] = textField;
                    formBox.getChildren().add(new Label(columnName));
                    formBox.getChildren().add(textField);
                }

                // Кнопка для добавления записи
                Button addButton = new Button("Добавить");
                addButton.setStyle(
                        "-fx-font-size: 18px; " +
                                "-fx-background-color: #df6a1b; " +
                                "-fx-text-fill: #04060a; " +
                                "-fx-border-radius: 10px;" +
                                "-fx-cursor: hand;"
                );

                addButton.setOnAction(event -> {
                    try {
                        addRecord(inputFields);
                        formStage.close();
                        table.setItems(getTable(table)); // Обновить данные в таблице
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                formBox.getChildren().add(addButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Scene formScene = new Scene(formBox, 400, 500);
        formStage.setScene(formScene);
        formStage.show();
    }

    private void addRecord(TextField[] inputFields) throws SQLException {
        StringBuilder itemsQueryBuilder = new StringBuilder("INSERT INTO items (");
        StringBuilder itemsValuesBuilder = new StringBuilder("VALUES (");
        StringBuilder remarksQueryBuilder = new StringBuilder("INSERT INTO remarks (");
        StringBuilder remarksValuesBuilder = new StringBuilder("VALUES (");

        // Первые три значения идут в таблицу items
        for (int i = 1; i <= 3; i++) {
            itemsQueryBuilder.append(inputFields[i - 1].getPromptText());
            itemsValuesBuilder.append("?");
            if (i < 3) {
                itemsQueryBuilder.append(", ");
                itemsValuesBuilder.append(", ");
            }
        }
        itemsQueryBuilder.append(") ");
        itemsValuesBuilder.append(")");

        // Добавление первого значения
        remarksQueryBuilder.append(inputFields[0].getPromptText());
        remarksValuesBuilder.append("?");

        // Добавление остальных значений, начиная с четвертого
        for (int i = 4; i <= inputFields.length; i++) {
            remarksQueryBuilder.append(", ").append(inputFields[i - 1].getPromptText());
            remarksValuesBuilder.append(", ?");
        }

        remarksQueryBuilder.append(") ");
        remarksValuesBuilder.append(")");

        // Объединяем строки для окончательных запросов
        String itemsQuery = itemsQueryBuilder.toString() + itemsValuesBuilder.toString();
        String remarksQuery = remarksQueryBuilder.toString() + remarksValuesBuilder.toString();

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Вставка данных в таблицу items
            try (PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
                for (int i = 0; i < 3; i++) {
                    itemsStmt.setString(i + 1, inputFields[i].getText());
                }
                itemsStmt.executeUpdate();
            }

            // Вставка данных в таблицу remarks
            try (PreparedStatement remarksStmt = conn.prepareStatement(remarksQuery)) {
                remarksStmt.setString(1, inputFields[0].getText()); // item_number

                int paramIndex = 2; // Начинаем со второго параметра для remarksStmt
                for (int i = 3; i < inputFields.length; i++) {
                    String columnName = inputFields[i].getPromptText();
                    String value = inputFields[i].getText();

                    switch (columnName) {
                        case "review_number":
                            remarksStmt.setInt(paramIndex++, Integer.parseInt(value));
                            break;
                        case "review_date":
                        case "fix_date":
                            if (value != null && !value.isEmpty()) {
                                remarksStmt.setDate(paramIndex++, Date.valueOf(value));
                            } else {
                                remarksStmt.setNull(paramIndex++, Types.DATE);
                            }
                            break;
                        default:
                            remarksStmt.setString(paramIndex++, value);
                            break;
                    }
                }
                remarksStmt.executeUpdate();
            }
        }
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

            // Очистка текущих столбцов
            table.getColumns().clear();

            // Создание столбцов таблицы на основе метаданных
            for (int i = 1; i <= columnCount; i++) {
                final int j = i;
                TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(metaData.getColumnName(i));
                column.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue().get(j - 1)));
                table.getColumns().add(column);
            }

            // Очистка текущих данных
            data.clear();

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
