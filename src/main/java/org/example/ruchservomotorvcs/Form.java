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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.DatePicker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Form {
    private Stage formStage;
    private TableView<ObservableList<Object>> table;
    private MainWindow mainWindow; // ссылка на MainWindow
    private ObservableList<Object> editingRowData;

    private class InputField {
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
        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.TOP_CENTER);
        formBox.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 20px;"
        );

        List<InputField> inputFields = new ArrayList<>();

        // Шапка формы с основными идентификаторами
        HBox headline = new HBox(10);
        ImageView logo = new ImageView(new Image("file:src/main/resources/org/example/ruchservomotorvcs/images/logo.png"));
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);

        GridPane numbersRow = new GridPane();
        numbersRow.setHgap(10);
        numbersRow.setVgap(5);

        // Данные авторов и дат замечания
        GridPane authorsAndDates = new GridPane();
        authorsAndDates.setHgap(10);
        authorsAndDates.setVgap(10);

        VBox textFields = new VBox(10);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT i.Номер_изделия, i.Номер_чертежа, i.Номер_заказа, " +
                    "r.Ревизия, r.Номер_рассмотрения, r.Автор_внесения_изменения, r.Дата_внесения, r.Ответственный_за_устранение, " +
                    "r.Дата_исправления, r.Текст_изменения, r.Примечания " +
                    "FROM изделия i " +
                    "JOIN замечания r ON i.Номер_изделия = r.Номер_изделия LIMIT 1";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    // Обеспечение удобного отображения названий столбцов
                    columnName = columnName.replace("_", " ");
                    Label label = new Label(columnName);
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-text-fill: #ffffff;");

                    Node field;
                    if (columnName.startsWith("Дата")) {
                        field = createStyledDatePicker(columnName);
                    } else if (i <= 5) {
                        field = createStyledTextField(columnName);
                    } else if (i <= 9) {
                        field = createStyledTextField(columnName);
                    } else {
                        field = createStyledTextArea(columnName);
                    }

                    // Заполнение полей данными, если это редактирование
                    if (editingRowData != null && i - 1 < editingRowData.size()) {
                        Object value = editingRowData.get(i - 1);
                        switch (field) {
                            case TextField textField -> textField.setText(value != null ? value.toString() : "");
                            case TextArea textArea -> textArea.setText(value != null ? value.toString() : "");
                            case DatePicker datePicker when value instanceof Date ->
                                    datePicker.setValue(((Date) value).toLocalDate());
                            default -> {
                            }
                        }
                    }

                    // Добавление полей в форму
                    if (i <= 5) {
                        addFieldToGridPane(numbersRow, label, field, i - 1);
                    } else if (i <= 9) {
                        addFieldToGridPane(authorsAndDates, label, field, (i - 6) * 2);
                    } else {
                        addFieldToVBox(textFields, label, field);
                    }

                    // Обеспечение получения реальных названий столбцов таблицы БД
                    columnName = columnName.replace(" ", "_");
                    inputFields.add(new InputField(field, columnName));
                }

                headline.getChildren().addAll(logo, numbersRow);

                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(createActionButton(inputFields), createCloseButton());

                formBox.getChildren().addAll(headline, authorsAndDates, textFields, buttonBox);
                return formBox;
            }
        } catch (SQLException e) {
            MainWindow.showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось получить данные из базы.", e.getMessage());
        }

        return formBox;
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
        textField.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;"
        );
        return textField;
    }

    private DatePicker createStyledDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setEditable(false);
        datePicker.setPromptText(promptText);
        datePicker.setMaxWidth(200);
        datePicker.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px;" +
                        "-fx-cursor: hand;"
        );
        datePicker.getEditor().setStyle(
                "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: pointer;"
        );
        return datePicker;
    }

    private TextArea createStyledTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setPrefRowCount(4);
        textArea.setWrapText(true);
        textArea.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-control-inner-background: #04060a; " +
                        "-fx-background-color: #04060a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent; " +
                        "-fx-padding: 4px;"
        );

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
        StringBuilder itemsQueryBuilder = new StringBuilder("INSERT INTO изделия (");
        StringBuilder itemsValuesBuilder = new StringBuilder("VALUES (");
        StringBuilder remarksQueryBuilder = new StringBuilder("INSERT INTO замечания (");
        StringBuilder remarksValuesBuilder = new StringBuilder("VALUES (");

        for (int i = 0; i < 3; i++) {
            itemsQueryBuilder.append(inputFields.get(i).columnName);
            itemsValuesBuilder.append("?");
            if (i < 2) {
                itemsQueryBuilder.append(", ");
                itemsValuesBuilder.append(", ");
            }
        }
        itemsQueryBuilder.append(") ");
        itemsValuesBuilder.append(")");

        remarksQueryBuilder.append(inputFields.getFirst().columnName);
        remarksValuesBuilder.append("?");

        for (int i = 3; i < inputFields.size(); i++) {
            remarksQueryBuilder.append(", ").append(inputFields.get(i).columnName);
            remarksValuesBuilder.append(", ?");
        }

        remarksQueryBuilder.append(") ");
        remarksValuesBuilder.append(")");

        String itemsQuery = itemsQueryBuilder + itemsValuesBuilder.toString();
        String remarksQuery = remarksQueryBuilder + remarksValuesBuilder.toString();

        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
                for (int i = 0; i < 3; i++) {
                    itemsStmt.setString(i + 1, ((TextField) inputFields.get(i).node).getText());
                }
                itemsStmt.executeUpdate();
            }

            try (PreparedStatement remarksStmt = conn.prepareStatement(remarksQuery)) {
                remarksStmt.setString(1, ((TextField) inputFields.getFirst().node).getText());

                int paramIndex = 2;
                for (int i = 3; i < inputFields.size(); i++) {
                    InputField field = inputFields.get(i);
                    String columnName = field.columnName;

                    if (field.node instanceof DatePicker) {
                        LocalDate date = ((DatePicker) field.node).getValue();
                        if (date != null) {
                            remarksStmt.setDate(paramIndex++, java.sql.Date.valueOf(date));
                        } else {
                            remarksStmt.setNull(paramIndex++, Types.DATE);
                        }
                    } else if (field.node instanceof TextField) {
                        String value = ((TextField) field.node).getText();
                        if (columnName.equals("Номер_рассмотрения")) {
                            remarksStmt.setInt(paramIndex++, Integer.parseInt(value));
                        } else {
                            remarksStmt.setString(paramIndex++, value);
                        }
                    } else if (field.node instanceof TextArea) {
                        String value = ((TextArea) field.node).getText();
                        remarksStmt.setString(paramIndex++, value);
                    }
                }
                try {
                    remarksStmt.executeUpdate();
                } catch (SQLException e) {
                    // Удаление части данных при ошибке внесения остальных
                    MainWindow.showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось добавить замечание.", e.getMessage());
                    String query = "DELETE FROM изделия WHERE Номер_изделия = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setObject(1, ((TextField) inputFields.getFirst().node).getText());
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

    public void showEditForm(ObservableList<Object> rowData) {
        showForm(rowData);
    }

    private void updateRecord(List<InputField> inputFields) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Обновление таблицы items
            String updateItemsQuery = "UPDATE изделия SET Номер_чертежа = ?, Номер_заказа = ? WHERE Номер_изделия = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateItemsQuery)) {
                pstmt.setString(1, ((TextField) inputFields.get(2).node).getText());
                pstmt.setString(2, ((TextField) inputFields.get(1).node).getText());
                pstmt.setString(3, ((TextField) inputFields.get(0).node).getText());
                pstmt.executeUpdate();
            }

            // Обновление таблицы remarks
            String updateRemarksQuery = "UPDATE замечания SET "Ревизия" = ?, Автор_внесения_изменения = ?, Дата_внесения = ?, Текст_изменения = ?, Ответственный_за_устранение = ?, Дата_исправления = ?, Примечания = ? WHERE Номер_изделия = ? AND Номер_рассмотрения = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRemarksQuery)) {
                pstmt.setString(1, ((TextField) inputFields.get(3).node).getText()); // revision
                pstmt.setString(2, ((TextField) inputFields.get(5).node).getText()); // author

                LocalDate reviewDate = ((DatePicker) inputFields.get(6).node).getValue();
                if (reviewDate != null) {
                    pstmt.setDate(3, java.sql.Date.valueOf(reviewDate)); // review_date
                } else {
                    pstmt.setNull(3, Types.DATE);
                }

                pstmt.setString(4, ((TextArea) inputFields.get(9).node).getText()); // review_text
                pstmt.setString(5, ((TextField) inputFields.get(7).node).getText()); // in_charge

                LocalDate fixDate = ((DatePicker) inputFields.get(8).node).getValue();
                if (fixDate != null) {
                    pstmt.setDate(6, java.sql.Date.valueOf(fixDate)); // fix_date
                } else {
                    pstmt.setNull(6, Types.DATE);
                }

                pstmt.setString(7, ((TextArea) inputFields.get(10).node).getText()); // notes
                pstmt.setString(8, ((TextField) inputFields.get(0).node).getText()); // item_number
                pstmt.setInt(9, Integer.parseInt(((TextField) inputFields.get(4).node).getText())); // review_number
                pstmt.executeUpdate();
            }

            // Обновление данных в таблице JavaFX
            formStage.close();
            table.setItems(mainWindow.getTable("изделия", "замечания"));

        } catch (SQLException | IllegalArgumentException e) {
            MainWindow.showErrorAlert("Ошибка при обновлении данных", "Не удалось обновить данные в базе.", e.getMessage());
        }
    }
}