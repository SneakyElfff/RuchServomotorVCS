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
        formScene.getStylesheets().add(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css").toExternalForm());
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
            String query = "SELECT i.item_number, i.blueprint_number, i.project_number, " +
                    "r.revision, r.review_number, r.author, r.review_date, r.in_charge, " +
                    "r.fix_date, r.review_text, r.notes " +
                    "FROM items i " +
                    "JOIN remarks r ON i.item_number = r.item_number LIMIT 1";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Label label = new Label(columnName);
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-text-fill: #ffffff;");

                    Node field;
                    if (columnName.endsWith("date")) {
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
                        if (field instanceof TextField) {
                            ((TextField) field).setText(value != null ? value.toString() : "");
                        } else if (field instanceof TextArea) {
                            ((TextArea) field).setText(value != null ? value.toString() : "");
                        } else if (field instanceof DatePicker && value instanceof Date) {
                            ((DatePicker) field).setValue(((Date) value).toLocalDate());
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
            showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось получить данные из базы.", e.getMessage());
        }

        return formBox;
    }

    private Button createActionButton(List<InputField> inputFields) {
        Button actionButton = createStyledButton(editingRowData == null ? "Добавить" : "Сохранить");
        actionButton.setOnAction(event -> {
            try {
                if (editingRowData == null) {
                    addRecord(inputFields);
                } else {
                    updateRecord(inputFields);
                }
                formStage.close();
                table.setItems(mainWindow.getTable("items", "remarks"));
            } catch (SQLException e) {
                showErrorAlert("Ошибка взаимодействия с базой данных", "Не удалось выполнить операцию.", e.getMessage());
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
        closeButton.setOnAction(event -> formStage.close());
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
        StringBuilder itemsQueryBuilder = new StringBuilder("INSERT INTO items (");
        StringBuilder itemsValuesBuilder = new StringBuilder("VALUES (");
        StringBuilder remarksQueryBuilder = new StringBuilder("INSERT INTO remarks (");
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

        String itemsQuery = itemsQueryBuilder.toString() + itemsValuesBuilder.toString();
        String remarksQuery = remarksQueryBuilder.toString() + remarksValuesBuilder.toString();

        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
                for (int i = 0; i < 3; i++) {
                    itemsStmt.setString(i + 1, ((TextField) inputFields.get(i).node).getText());
                }
                itemsStmt.executeUpdate();
            }

            try (PreparedStatement remarksStmt = conn.prepareStatement(remarksQuery)) {
                remarksStmt.setString(1, ((TextField) inputFields.get(0).node).getText());

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
                        switch (columnName) {
                            case "review_number":
                                remarksStmt.setInt(paramIndex++, Integer.parseInt(value));
                                break;
                            default:
                                remarksStmt.setString(paramIndex++, value);
                                break;
                        }
                    } else if (field.node instanceof TextArea) {
                        String value = ((TextArea) field.node).getText();
                        remarksStmt.setString(paramIndex++, value);
                    }
                }
                remarksStmt.executeUpdate();
            }
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    public void showEditForm(ObservableList<Object> rowData) {
        showForm(rowData);
    }

    private void updateRecord(List<InputField> inputFields) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Обновление таблицы items
            String updateItemsQuery = "UPDATE items SET blueprint_number = ?, project_number = ? WHERE item_number = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateItemsQuery)) {
                pstmt.setString(1, ((TextField) inputFields.get(2).node).getText());
                pstmt.setString(2, ((TextField) inputFields.get(1).node).getText());
                pstmt.setString(3, ((TextField) inputFields.get(0).node).getText());
                pstmt.executeUpdate();
            }

            // Обновление таблицы remarks
            String updateRemarksQuery = "UPDATE remarks SET revision = ?, author = ?, review_date = ?, review_text = ?, in_charge = ?, fix_date = ?, notes = ? WHERE item_number = ? AND review_number = ?";
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
            table.setItems(mainWindow.getTable("items", "remarks"));

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            // Показать сообщение об ошибке пользователю
            showErrorAlert("Ошибка при обновлении данных: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }
}