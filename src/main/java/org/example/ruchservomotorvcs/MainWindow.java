package org.example.ruchservomotorvcs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.sql.*;
import java.util.Objects;

public class MainWindow {

    private VBox menuPanel;
    private TableView<ObservableList<Object>> table;
    private ComboBox<String> columnComboBox;
    private ObservableList<ObservableList<Object>> originalData; // Добавлено для хранения исходных данных

    public BorderPane createMainPane(Runnable onLogout) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 20px;"
        );

        VBox mainBox = createMainBox();
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

        menuButton.setOnAction(_ -> toggleMenuPanel());

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

        logoutButton.setOnAction(_ -> {
            toggleMenuPanel(); // Скрыть меню перед выходом
            onLogout.run();
        });

        panel.getChildren().addAll(titleLabel, separator, logoutButton);
        return panel;
    }

    private VBox createMainBox() {
        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Добавить запись");
        Button editButton = new Button("Редактировать запись");
        Button deleteButton = new Button("Удалить запись");

        // Стили для кнопок
        String buttonStyle =
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b; " +
                        "-fx-text-fill: #04060a; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;";

        addButton.setStyle(buttonStyle);
        editButton.setStyle(buttonStyle);
        deleteButton.setStyle(buttonStyle);

        addButton.setOnAction(_ -> showAddRecordForm());
        editButton.setOnAction(_ -> showEditRecordForm());
        deleteButton.setOnAction(_ -> showDeleteConfirmationDialog());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        table = new TableView<>();
        table.getStyleClass().add("edge-to-edge"); // Применяем класс стилей для рамки

        String cssPath = Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm();
        if (cssPath == null) {
            throw new RuntimeException("Cannot find CSS file");
        }
        table.getStylesheets().add(cssPath);

        columnComboBox = new ComboBox<>();
        columnComboBox.setMaxWidth(200);
        columnComboBox.setPromptText("Выберите столбец");
        columnComboBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());

        // Заполнение таблицы данными из базы данных
        try {
            originalData = getTable("items", "remarks"); // Сохраняем исходные данные
            table.setItems(originalData);
        } catch (SQLException e) {
            showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось получить данные из базы.", e.getMessage());
        }

        VBox filterBox = createFilterBox();

        mainBox.getChildren().addAll(buttonBox, table, filterBox);

        return mainBox;
    }

    private VBox createFilterBox() {
        VBox filterBox = new VBox(10);
        filterBox.setAlignment(Pos.CENTER);

        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER);

        Label filterLabel = new Label("Фильтр по значению:");
        filterLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        TextField filterField = new TextField();
        filterField.setMaxWidth(200);
        filterField.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;"
        );

        Button filterButton = new Button("Найти");
        Button resetButton = new Button("Сброс");

        String filterButtonStyle =
                "-fx-font-size: 18px; " +
                        "-fx-background-color: #df6a1b; " +
                        "-fx-text-fill: #04060a; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;";

        filterButton.setStyle(filterButtonStyle);
        resetButton.setStyle(filterButtonStyle);

        filterButton.setOnAction(_ -> {
            String filterValue = filterField.getText().trim();
            String selectedColumn = columnComboBox.getValue();
            if (selectedColumn != null && !selectedColumn.isEmpty()) {
                filterTable(selectedColumn, filterValue);
            } else {
                showWarningAlert("Пожалуйста, выберите столбец для фильтрации.");
            }
        });

        resetButton.setOnAction(_ -> {
            filterField.clear();

            columnComboBox.getSelectionModel().clearSelection();
            columnComboBox.setPromptText("Выберите столбец");

            table.setItems(originalData); // Сброс данных на исходные
        });

        filterRow.getChildren().addAll(filterLabel, filterField, columnComboBox, filterButton, resetButton); // Добавлено
        filterBox.getChildren().add(filterRow);

        return filterBox;
    }

    private void filterTable(String columnName, String filterValue) {
        ObservableList<ObservableList<Object>> filteredData = FXCollections.observableArrayList();

        // Получение индекса столбца по имени
        int columnIndex = -1;
        for (int i = 0; i < table.getColumns().size(); i++) {
            if (table.getColumns().get(i).getText().equals(columnName)) {
                columnIndex = i;
                break;
            }
        }

        // Поиск нужной строки
        if (columnIndex != -1) {
            if (columnIndex > 2)
                columnIndex++;

            for (ObservableList<Object> row : table.getItems()) {
                Object cell = row.get(columnIndex);
                if (cell != null && cell.toString().contains(filterValue)) {
                    filteredData.add(row);
                }
            }
        }

        table.setItems(filteredData);
    }

    private void showAddRecordForm() {
        Form form = new Form(table, this);
        form.showForm();
    }

    private void showEditRecordForm() {
        ObservableList<Object> selectedRow = table.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            ObservableList<Object> selectedRowCopy = FXCollections.observableArrayList(selectedRow);

            // Индекс столбца, который нужно исключить (например, 3)
            int excludeIndex = 3;
            selectedRowCopy.remove(excludeIndex);

            Form form = new Form(table, this);
            form.showEditForm(selectedRowCopy);
        } else {
            showWarningAlert("Пожалуйста, выберите запись для редактирования.");
        }
    }

    public ObservableList<ObservableList<Object>> getTable(String... tableNames) throws SQLException {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        // Динамический SQL-запрос
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        for (int i = 0; i < tableNames.length; i++) {
            queryBuilder.append(tableNames[i]).append(".*");
            if (i < tableNames.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(" FROM ").append(tableNames[0]);

        // JOIN для остальных таблиц, если они есть
        for (int i = 1; i < tableNames.length; i++) {
            queryBuilder.append(" JOIN ").append(tableNames[i])
                    .append(" ON ").append(tableNames[0])
                    .append(".item_number = ").append(tableNames[i])
                    .append(".item_number");
        }

        String query = queryBuilder.toString();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Очистка текущих столбцов
            table.getColumns().clear();

            int counter = 0;
            // Установка названий столбцов в ComboBox
            ObservableList<String> columnNames = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);

                // Избежание дублирования столбца
                if (Objects.equals(columnName, "item_number")) {
                    counter++;
                }
                if (counter == 2) {
                    counter = 0;
                    continue;
                }

                columnNames.add(columnName);
            }
            columnComboBox.setItems(columnNames);

            counter = 0;
            // Создание столбцов таблицы на основе метаданных
            for (int i = 1; i <= columnCount; i++) {
                final int j = i;

                String columnName = metaData.getColumnName(i);
                // Избежание дублирования столбца
                if (Objects.equals(columnName, "item_number")) {
                    counter++;
                }
                if (counter == 2) {
                    counter = 0;
                    continue;
                }
                TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(columnName);
                column.setCellValueFactory(param ->
                        new SimpleObjectProperty<>(param.getValue().get(j - 1))
                );

                // Устанавливаем ширину столбца по длине названия столбца
                double charWidth = 10; // Примерная ширина одного символа в пикселях
                column.setPrefWidth(charWidth * columnName.length());

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

    private void showDeleteConfirmationDialog() {
        ObservableList<Object> selectedRow = table.getSelectionModel().getSelectedItem();

        if (selectedRow != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить эту запись?");

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());
            dialogPane.getStyleClass().add("root");

            ButtonType buttonTypeYes = new ButtonType("Да", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("Нет", ButtonBar.ButtonData.NO);

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            Button noButtonNode = (Button) alert.getDialogPane().lookupButton(buttonTypeNo);
            noButtonNode.getStyleClass().add("no-button");

            alert.showAndWait().ifPresent(response -> {
                if (response == buttonTypeYes) {
                    deleteSelectedRecord();
                }
            });
        } else {
            showWarningAlert("Пожалуйста, выберите запись для удаления.");
        }
    }

    private void deleteSelectedRecord() {
        ObservableList<Object> selectedRow = table.getSelectionModel().getSelectedItem();

        if (selectedRow != null) {
            // четвертый столбец - primary key таблицы remarks (например, review_number)
            Object primaryKey = selectedRow.get(3);

            try (Connection conn = DatabaseUtil.getConnection()) {
//                String query = "DELETE FROM items WHERE item_number = ?";
//                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
//                    pstmt.setObject(1, primaryKey);
//                    pstmt.executeUpdate();
//                }

                String query = "DELETE FROM remarks WHERE item_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setObject(1, primaryKey);
                    pstmt.executeUpdate();
                }

                table.getItems().remove(selectedRow);
            } catch (SQLException e) {
                showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось удалить данные из базы.", e.getMessage());
            }
        }
    }

    private void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(MainWindow.class.getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }
}