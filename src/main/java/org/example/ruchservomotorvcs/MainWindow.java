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

    private static String userRole;
    private VBox menuPanel;
    private TableView<ObservableList<Object>> table;
    private ComboBox<String> columnComboBox;
    private ObservableList<ObservableList<Object>> originalData; // Добавлено для хранения исходных данных

    private static final String COMMON_CSS_STYLE = "-fx-background-color: #04060a; " +
            "-fx-border-color: #df6a1b; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 10px;";

    public static void setUserRole(String role) {
        userRole = role;
    }

    public BorderPane createMainPane(Runnable onLogout) {
        // Создание корневого контейнера
        BorderPane root = new BorderPane();
        root.setStyle(COMMON_CSS_STYLE + "-fx-padding: 20px;");

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
        menuButton.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 18px; " +
                "-fx-text-fill: #df6a1b; " +
                "-fx-cursor: hand;");

        menuButton.setOnAction(_ -> toggleMenuPanel());

        return menuButton;
    }

    private void toggleMenuPanel() {
        menuPanel.setVisible(!menuPanel.isVisible());
        menuPanel.setManaged(menuPanel.isVisible());
    }

    private VBox createMenuPanel(Runnable onLogout) {
        VBox panel = new VBox(10);
        panel.setStyle(COMMON_CSS_STYLE + "-fx-padding: 10px;" +
                "-fx-min-width: 200px;" +
                "-fx-max-width: 200px;");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMaxHeight(200);

        Label titleLabel = new Label("Меню");
        titleLabel.setStyle("-fx-text-fill: #df6a1b; -fx-font-size: 18px;");

        Separator separator1 = createSeparator();
        Separator separator2 = createSeparator();

        Button logoutButton = createLogoutButton(onLogout);

        Button addUserButton = createStyledButton("Добавить пользователя");
        addUserButton.setOnAction(_ -> showAddUserDialog());

        Button deleteUserButton = createStyledButton("Удалить пользователя");
        deleteUserButton.setOnAction(_ -> showDeleteUserDialog());

        if (!"администратор".equalsIgnoreCase(userRole)) {
            addUserButton.setVisible(false);
            deleteUserButton.setVisible(false);
        }

        panel.getChildren().addAll(titleLabel, separator1, logoutButton, separator2, addUserButton, deleteUserButton);
        return panel;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #df6a1b;");

        return separator;
    }

    private Button createLogoutButton (Runnable onLogout) {
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

        return logoutButton;
    }

    private Button createStyledButton (String buttonName) {
        Button button = new Button(buttonName);
        button.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 18px; " +
                "-fx-text-fill: #df6a1b; " +
                "-fx-cursor: hand;");

        return button;
    }

    private void showAddUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавить пользователя");

        DialogPane dialogPane = dialog.getDialogPane();
        createStyledDialogPane(dialogPane);

        TextField usernameField = createStyledTextField("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("пользователь", "администратор");
        roleComboBox.setPromptText("Роль");

        VBox signInFields = new VBox(10, usernameField, passwordField, roleComboBox);
        signInFields.setAlignment(Pos.CENTER_LEFT);

        dialogPane.setContent(signInFields);

        ButtonType okButtonType = createButtonsOfDialogPane(dialogPane);

        dialog.showAndWait().ifPresent(response -> {
            if (response == okButtonType) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String role = roleComboBox.getValue();

                if (!username.isEmpty() && !password.isEmpty() && role != null) {
                    try (Connection conn = DatabaseUtil.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                                 "INSERT INTO пользователи (Логин, Пароль, Роль) VALUES (?, ?, ?)")) {
                        pstmt.setString(1, username);
                        pstmt.setString(2, password);
                        pstmt.setString(3, role);
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        showErrorAlert("Ошибка", "Не удалось добавить пользователя.", e.getMessage());
                    }
                } else {
                    showWarningAlert("Пожалуйста, заполните все поля.");
                }
            }
        });
    }

    private void showDeleteUserDialog() {
        TextInputDialog loginFiled = new TextInputDialog();
        loginFiled.setTitle("Удалить пользователя");
        loginFiled.setHeaderText("Введите логин пользователя для удаления:");

        DialogPane dialogPane = loginFiled.getDialogPane();
        createStyledDialogPane(dialogPane);

        TextField inputField = loginFiled.getEditor();
        inputField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");

        createButtonsOfDialogPane(dialogPane);

        loginFiled.showAndWait().ifPresent(username -> {
            if (!username.isEmpty()) {
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM пользователи WHERE Логин = ?")) {
                    pstmt.setString(1, username);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected == 0) {
                        showWarningAlert("Пользователь с логином " + username + " не найден.");
                    }
                } catch (SQLException e) {
                    showErrorAlert("Ошибка", "Не удалось удалить пользователя.", e.getMessage());
                }
            } else {
                showWarningAlert("Пожалуйста, введите логин.");
            }
        });
    }

    private static void createStyledDialogPane(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(MainWindow.class.getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("root");

    }

    private TextField createStyledTextField(String textFieldName) {
        TextField textField = new TextField();
        textField.setPromptText(textFieldName);
        textField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");

        return textField;
    }

    private ButtonType createButtonsOfDialogPane(DialogPane dialogPane) {
        ButtonType okButtonType = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(okButtonType, closeButtonType);

        Button okButton = (Button) dialogPane.lookupButton(okButtonType);
        okButton.setDefaultButton(true);

        ButtonBar buttonBar = (ButtonBar) dialogPane.lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
        buttonBar.getButtons().setAll(okButton, (Button) dialogPane.lookupButton(closeButtonType));

        return okButtonType;
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
            originalData = getTable("изделия", "замечания"); // Сохраняем исходные данные
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
        filterField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");

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
                try {
                    filterTable(selectedColumn, filterValue);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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

    private void filterTable(String columnName, String filterValue) throws SQLException {
        ObservableList<ObservableList<Object>> filteredData = FXCollections.observableArrayList();

        originalData = getTable("изделия", "замечания"); // Сохраняем исходные данные

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
                    .append(".Номер_изделия = ").append(tableNames[i])
                    .append(".Номер_изделия");
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
                // Обеспечение удобного отображения названий столбцов
                columnName = columnName.replace("_", " ");

                // Избежание дублирования столбца
                if (Objects.equals(columnName, "Номер изделия")) {
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
                // Обеспечение удобного отображения названий столбцов
                columnName = columnName.replace("_", " ");
                // Избежание дублирования столбца
                if (Objects.equals(columnName, "Номер изделия")) {
                    counter++;
                }
                if (counter == 2) {
                    counter = 0;
                    continue;
                }
                TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(columnName);

                if (columnName.equals("Изображение")) {
                    column.setCellValueFactory(param -> {
                        Object value = param.getValue().get(j - 1);
                        if (value instanceof byte[] && ((byte[]) value).length > 0) {
                            return new SimpleObjectProperty<>("️\u2398");
                        } else {
                            return new SimpleObjectProperty<>("");
                        }
                    });
                } else {
                    column.setCellValueFactory(param ->
                            new SimpleObjectProperty<>(param.getValue().get(j - 1))
                    );
                }

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
            createStyledDialogPane(dialogPane);

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
                String query = "DELETE FROM замечания WHERE Номер_изделия = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setObject(1, primaryKey);
                    pstmt.executeUpdate();
                }

                query = "DELETE FROM изделия WHERE Номер_изделия = ?";
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
        createStyledDialogPane(dialogPane);

        alert.showAndWait();
    }

    static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        createStyledDialogPane(dialogPane);

        alert.showAndWait();
    }
}