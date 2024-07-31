package org.example.ruchservomotorvcs;

import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.DatePicker;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Form {
    private final Stage formStage;
    private final TableView<ObservableList<Object>> table;
    private final MainWindow mainWindow; // ссылка на MainWindow
    private ObservableList<Object> editingRowData;

    private static final String COMMON_CSS_STYLE = "-fx-background-color: #04060a; " +
            "-fx-border-color: #df6a1b; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 10px;";

    private static class InputField {
        Node node;
        String columnName;

        InputField(Node node, String columnName) {
            this.node = node;
            this.columnName = columnName;
        }
    }

    public Form(TableView<ObservableList<Object>> table, MainWindow mainWindow) {
        this.table = table;
        this.mainWindow = mainWindow; // Инициализируем MainWindow
        formStage = new Stage();
        formStage.setTitle("Добавить запись");
    }

    public void showForm() {
        showForm(null);
    }

    public void showForm(ObservableList<Object> rowData) {
        this.editingRowData = rowData;
        formStage.setTitle(rowData == null ? "Добавить запись" : "Редактировать запись");

        VBox formBox = createFormBox();
        Scene formScene = new Scene(formBox, 800, 600);
        formScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css")).toExternalForm());
        formStage.setScene(formScene);
        formStage.show();
    }

    private VBox createFormBox() {
        VBox formBox = createStyledFormBox();
        List<InputField> inputFields = new ArrayList<>();

        HBox headline = createHeadline();
        GridPane numbersRow = createGridPane(5);
        GridPane authorsAndDates = createGridPane(10);
        VBox textFields = new VBox(10);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT i.Номер_изделия, i.Номер_чертежа, i.Номер_заказа, " +
                    "r.Ревизия, r.Номер_рассмотрения, r.Автор_внесения_изменения, r.Дата_внесения, r.Статус, r.Ответственный_за_устранение, " +
                    "r.Дата_исправления, r.Текст_изменения, r.Примечания, r.Изображение " +
                    "FROM изделия i " +
                    "JOIN замечания r ON i.Номер_изделия = r.Номер_изделия LIMIT 1";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                processResultSet(rs, inputFields, numbersRow, authorsAndDates, textFields);
                headline.getChildren().addAll(createLogo(), numbersRow);

                HBox buttonBox = createButtonBox(inputFields);
                formBox.getChildren().addAll(headline, authorsAndDates, textFields, buttonBox);
                return formBox;
            }
        } catch (SQLException e) {
            MainWindow.showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось получить данные из базы.", e.getMessage());
        }

        return formBox;
    }

    private VBox createStyledFormBox() {
        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.TOP_CENTER);
        formBox.setStyle(COMMON_CSS_STYLE);
        formBox.setStyle(
                "-fx-padding: 20px;"
        );
        return formBox;
    }

    private HBox createHeadline() {
        return new HBox(10);
    }

    private GridPane createGridPane(int vgap) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(vgap);
        return gridPane;
    }

    private ImageView createLogo() {
        ImageView logo = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/org/example/ruchservomotorvcs/images/logo.png")).toExternalForm()));
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);
        return logo;
    }

    private void processResultSet(ResultSet rs, List<InputField> inputFields, GridPane numbersRow, GridPane authorsAndDates, VBox textFields) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i).replace("_", " ");
            Label label = createStyledLabel(columnName);
            Node field = createField(columnName, i);

            if (editingRowData != null && i - 1 < editingRowData.size()) {
                fillFieldWithData(field, editingRowData.get(i - 1));
            }

            if (i <= 5) {
                addFieldToGridPane(numbersRow, label, field, i - 1);
            } else if (i <= 10) {
                addFieldToGridPane(authorsAndDates, label, field, (i - 6) * 2);
            } else {
                addFieldToVBox(textFields, label, field);
            }

            inputFields.add(new InputField(field, columnName.replace(" ", "_")));
        }
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: #ffffff;");
        return label;
    }

    private Node createField(String columnName, int columnIndex) {
        if (columnName.startsWith("Дата")) {
            return createStyledDatePicker(columnName);
        } else if (columnName.equals("Статус")) {
            return createStyledComboBox(columnName);
        } else if (columnName.equals("Изображение")) {
            return createImageUploadButton();
        } else if (columnIndex <= 10) {
            return createStyledTextField(columnName);
        } else {
            return createStyledTextArea(columnName);
        }
    }

    private void fillFieldWithData(Node field, Object value) {
        if (field instanceof TextField textField) {
            textField.setText(value != null ? value.toString() : "");
        } else if (field instanceof TextArea textArea) {
            textArea.setText(value != null ? value.toString() : "");
        } else if (field instanceof DatePicker datePicker && value instanceof Date) {
            datePicker.setValue(((Date) value).toLocalDate());
        } else if (field instanceof ComboBox<?> comboBox) {
            fillComboBox(comboBox, value);
        } else if (field instanceof HBox hbox) {
            fillImageUploadButton(hbox, value);
        }
    }

    private void fillComboBox(ComboBox<?> comboBox, Object value) {
        if (value != null) {
            String stringValue = value.toString();
            @SuppressWarnings("unchecked")
            ComboBox<String> stringComboBox = (ComboBox<String>) comboBox;
            if (stringComboBox.getItems().contains(stringValue)) {
                stringComboBox.setValue(stringValue);
            } else {
                stringComboBox.getSelectionModel().clearSelection();
            }
        } else {
            comboBox.getSelectionModel().clearSelection();
        }
    }

    private void fillImageUploadButton(HBox hbox, Object value) {
        if (value instanceof byte[] imageData) {
            Button uploadButton = (Button) hbox.getChildren().get(0);
            ImageView previewImage = (ImageView) hbox.getChildren().get(1);
            uploadButton.setText("Изменить");
            uploadButton.setUserData(imageData);
            previewImage.setImage(new Image(new ByteArrayInputStream(imageData)));
        }
    }

    private HBox createButtonBox(List<InputField> inputFields) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(createActionButton(inputFields), createCloseButton());
        return buttonBox;
    }

    private Button createActionButton(List<InputField> inputFields) {
        Button actionButton = createStyledButton(editingRowData == null ? "Добавить" : "Сохранить");
        actionButton.setOnAction(_ -> {
            try {
                if (editingRowData == null) {
                    addRecord(inputFields);
                } else {
                    updateRecord(inputFields);
                }
                formStage.close();
                table.setItems(mainWindow.getTable("изделия", "замечания"));
            } catch (SQLException e) {
                MainWindow.showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось выполнить операцию.", e.getMessage());
            }
        });
        return actionButton;
    }

    private void addFieldToGridPane(GridPane gridPane, Label label, Node field, int columnIndex) {
        gridPane.add(label, columnIndex, 0);
        gridPane.add(field, columnIndex, 1);
        GridPane.setHalignment(label, HPos.CENTER);
    }

    private void addFieldToVBox(VBox vBox, Label label, Node field) {
        vBox.getChildren().addAll(label, field);
    }

    private Button createCloseButton() {
        Button closeButton = createStyledButton("Закрыть");
        closeButton.setOnAction(_ -> formStage.close());
        return closeButton;
    }

    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setMaxWidth(200);
        textField.setPromptText(promptText);
        textField.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");
        return textField;
    }

    private DatePicker createStyledDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setEditable(false);
        datePicker.setPromptText(promptText);
        datePicker.setMaxWidth(200);
        datePicker.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-cursor: hand;");

        datePicker.getEditor().setStyle(
                "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: pointer;"
        );
        return datePicker;
    }

    private ComboBox<String> createStyledComboBox(String promptText) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Внесение", "Рассмотрение", "Выполнение", "Отклонение");
        comboBox.setPromptText(promptText);
        comboBox.setMaxWidth(200);
        comboBox.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-text-fill: #ffffff; ");
        return comboBox;
    }

    private TextArea createStyledTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setPrefRowCount(4);
        textArea.setWrapText(true);
        textArea.setStyle(COMMON_CSS_STYLE + "-fx-font-size: 16px; " +
                "-fx-control-inner-background: #04060a; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-background-radius: 8px; " +
                "-fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent; " +
                "-fx-padding: 4px;");

        textArea.setFocusTraversable(false);

        // CSS для скрытия полос прокрутки и их компонентов
        textArea.getStylesheets().add("data:text/css," +
                ".text-area .scroll-bar:vertical { -fx-scale-x: 0; }" +
                ".text-area .scroll-bar:horizontal { -fx-scale-y: 0; }" +
                ".text-area .scroll-pane { -fx-background-color: transparent; }" +
                ".text-area .scroll-pane .viewport { -fx-background-color: transparent; }" +
                ".text-area .scroll-pane .content { -fx-background-color: #04060a; -fx-background-radius: 10px; }"
        );

        return textArea;
    }

    private Node createImageUploadButton() {
        HBox hbox = new HBox(10);
        Button uploadButton = createStyledButton("Выбрать изображение");

        ImageView previewImage = new ImageView();
        previewImage.setFitHeight(50);
        previewImage.setFitWidth(50);

        uploadButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(formStage);
            if (selectedFile != null) {
                uploadButton.setText("Изменить");
                uploadButton.setUserData(selectedFile);
                previewImage.setImage(new Image(selectedFile.toURI().toString()));
            }
        });

        previewImage.setOnMouseClicked(_ -> {
            if (previewImage.getImage() != null) {
                Stage imageStage = new Stage();
                ImageView fullSizeImage = new ImageView(previewImage.getImage());
                fullSizeImage.setPreserveRatio(true);
                fullSizeImage.setFitHeight(400);
                Scene scene = new Scene(new StackPane(fullSizeImage));
                imageStage.setScene(scene);
                imageStage.show();
            }
        });

        hbox.getChildren().addAll(uploadButton, previewImage);
        return hbox;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
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

    private void addRecord(List<InputField> inputFields) throws SQLException {
        String itemsQuery = buildInsertItemsQuery("изделия", inputFields.subList(0, 3));
        String remarksQuery = buildInsertRemarksQuery("замечания", inputFields);

        try (Connection conn = DatabaseUtil.getConnection()) {
            insertItemsRecord(conn, itemsQuery, inputFields);
            insertRemarksRecord(conn, remarksQuery, inputFields);
        }
    }

    private String buildInsertItemsQuery(String tableName, List<InputField> fields) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");

        for (int i = 0; i < fields.size(); i++) {
            queryBuilder.append(fields.get(i).columnName);
            valuesBuilder.append("?");
            if (i < fields.size() - 1) {
                queryBuilder.append(", ");
                valuesBuilder.append(", ");
            }
        }
        queryBuilder.append(") ");
        valuesBuilder.append(")");

        return queryBuilder.append(valuesBuilder.toString()).toString();
    }

    private String buildInsertRemarksQuery(String tableName, List<InputField> fields) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");

        queryBuilder.append(fields.getFirst().columnName);
        valuesBuilder.append("?");

        for (int i = 3; i < fields.size(); i++) {
            queryBuilder.append(", ").append(fields.get(i).columnName);
            valuesBuilder.append(", ?");
        }

        queryBuilder.append(") ");
        valuesBuilder.append(")");

        return queryBuilder.append(valuesBuilder.toString()).toString();
    }

    private void insertItemsRecord(Connection conn, String query, List<InputField> inputFields) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < 3; i++) {
                stmt.setString(i + 1, ((TextField) inputFields.get(i).node).getText());
            }
            stmt.executeUpdate();
        }
    }

    private void insertRemarksRecord(Connection conn, String query, List<InputField> inputFields) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ((TextField) inputFields.getFirst().node).getText());

            int paramIndex = 2;
            for (int i = 3; i < inputFields.size(); i++) {
                InputField field = inputFields.get(i);
                setStatementParameter(stmt, paramIndex++, field);
            }

            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                handleInsertRemarksError(conn, inputFields.getFirst(), e);
            }
        } catch (FileNotFoundException e) {
            MainWindow.showErrorAlert("Ошибка", "Не удалось загрузить файл.", e.getMessage());
        }
    }

    private void setStatementParameter(PreparedStatement stmt, int paramIndex, InputField field) throws SQLException, FileNotFoundException {
        if (field.node instanceof DatePicker) {
            LocalDate date = ((DatePicker) field.node).getValue();
            if (date != null) {
                stmt.setDate(paramIndex, java.sql.Date.valueOf(date));
            } else {
                stmt.setNull(paramIndex, Types.DATE);
            }
        } else if (field.node instanceof TextField) {
            String value = ((TextField) field.node).getText();
            if (field.columnName.equals("Номер_рассмотрения")) {
                stmt.setInt(paramIndex, Integer.parseInt(value));
            } else {
                stmt.setString(paramIndex, value);
            }
        } else if (field.node instanceof ComboBox<?> comboBox) {
            Object selectedItem = comboBox.getSelectionModel().getSelectedItem();
            stmt.setString(paramIndex, selectedItem != null ? selectedItem.toString() : null);
        } else if (field.node instanceof TextArea) {
            String value = ((TextArea) field.node).getText();
            stmt.setString(paramIndex, value);
        } else if (field.node instanceof HBox && field.columnName.equals("Изображение")) {
            setImageParameter(stmt, paramIndex, (HBox) field.node);
        }
    }

    private void setImageParameter(PreparedStatement stmt, int paramIndex, HBox hbox) throws SQLException, FileNotFoundException {
        Button uploadButton = (Button) hbox.getChildren().getFirst();
        Object userData = uploadButton.getUserData();
        if (userData instanceof File imageFile) {
            FileInputStream fis = new FileInputStream(imageFile);
            stmt.setBinaryStream(paramIndex, fis, (int) imageFile.length());
        } else if (userData instanceof byte[]) {
            stmt.setBytes(paramIndex, (byte[]) userData);
        } else {
            stmt.setNull(paramIndex, Types.BINARY);
        }
    }

    private void handleInsertRemarksError(Connection conn, InputField firstField, SQLException e) throws SQLException {
        MainWindow.showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось добавить замечание.", e.getMessage());
        String query = "DELETE FROM изделия WHERE Номер_изделия = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, ((TextField) firstField.node).getText());
            pstmt.executeUpdate();
        }
    }

    public void showEditForm(ObservableList<Object> rowData) {
        showForm(rowData);
    }

    private void updateRecord(List<InputField> inputFields) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            updateItemsTable(conn, inputFields);
            updateRemarksTable(conn, inputFields);

            // Обновление данных в таблице JavaFX
            formStage.close();
            table.setItems(mainWindow.getTable("изделия", "замечания"));

        } catch (SQLException | IllegalArgumentException | IOException e) {
            MainWindow.showErrorAlert("Ошибка при обновлении данных", "Не удалось обновить данные в базе.", e.getMessage());
        }
    }

    private void updateItemsTable(Connection conn, List<InputField> inputFields) throws SQLException {
        String updateItemsQuery = "UPDATE изделия SET Номер_чертежа = ?, Номер_заказа = ? WHERE Номер_изделия = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateItemsQuery)) {
            pstmt.setString(1, ((TextField) inputFields.get(1).node).getText());
            pstmt.setString(2, ((TextField) inputFields.get(2).node).getText());
            pstmt.setString(3, ((TextField) inputFields.get(0).node).getText());
            pstmt.executeUpdate();
        }
    }

    private void updateRemarksTable(Connection conn, List<InputField> inputFields) throws SQLException, IOException {
        String updateRemarksQuery = "UPDATE замечания SET Ревизия = ?, Автор_внесения_изменения = ?, Дата_внесения = ?, Текст_изменения = ?, Статус = ?, Ответственный_за_устранение = ?, Дата_исправления = ?, Примечания = ?, Изображение = ? WHERE Номер_изделия = ? AND Номер_рассмотрения = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateRemarksQuery)) {
            setUpdateRemarksParameters(pstmt, inputFields);
            pstmt.executeUpdate();
        }
    }

    private void setUpdateRemarksParameters(PreparedStatement pstmt, List<InputField> inputFields) throws SQLException, IOException {
        pstmt.setString(1, ((TextField) inputFields.get(3).node).getText()); // revision
        pstmt.setString(2, ((TextField) inputFields.get(5).node).getText()); // author

        LocalDate reviewDate = ((DatePicker) inputFields.get(6).node).getValue();
        if (reviewDate != null) {
            pstmt.setDate(3, java.sql.Date.valueOf(reviewDate)); // review_date
        } else {
            pstmt.setNull(3, Types.DATE);
        }

        pstmt.setString(4, ((TextArea) inputFields.get(10).node).getText()); // review_text

        if (inputFields.get(7).node instanceof ComboBox<?> comboBox) {
            Object selectedItem = comboBox.getSelectionModel().getSelectedItem();
            pstmt.setString(5, selectedItem != null ? selectedItem.toString() : null); // status
        }

        pstmt.setString(6, ((TextField) inputFields.get(8).node).getText()); // in_charge

        LocalDate fixDate = ((DatePicker) inputFields.get(9).node).getValue();
        if (fixDate != null) {
            pstmt.setDate(7, java.sql.Date.valueOf(fixDate)); // fix_date
        } else {
            pstmt.setNull(7, Types.DATE);
        }

        pstmt.setString(8, ((TextArea) inputFields.get(11).node).getText()); // notes

        setImageParameter(pstmt, 9, (HBox) inputFields.get(12).node); // image

        pstmt.setString(10, ((TextField) inputFields.get(0).node).getText()); // item_number
        pstmt.setInt(11, Integer.parseInt(((TextField) inputFields.get(4).node).getText())); // review_number
    }
}