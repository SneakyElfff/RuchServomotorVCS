package org.example.ruchservomotorvcs;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private MainWindow mainWindow; // Добавляем ссылку на MainWindow

    public Form(TableView<ObservableList<Object>> table, MainWindow mainWindow) {
        this.table = table;
        this.mainWindow = mainWindow; // Инициализируем MainWindow
        formStage = new Stage();
        formStage.setTitle("Добавить запись");
    }

    public void showForm() {
        VBox formBox = createFormBox();
        Scene formScene = new Scene(formBox, 600, 700);
        formScene.getStylesheets().add(getClass().getResource("/org/example/ruchservomotorvcs/css/styles.css").toExternalForm());
        formStage.setScene(formScene);
        formStage.show();
    }

    private VBox createFormBox() {
        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle(
                "-fx-background-color: #04060a;" +
                        "-fx-border-color: #df6a1b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px;"
        );

        List<InputField> inputFields = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT i.item_number, i.blueprint_number, i.project_number, " +
                    "r.review_number, r.revision, r.author, r.review_date, r.review_text, " +
                    "r.in_charge, r.fix_date, r.notes " +
                    "FROM items i " +
                    "JOIN remarks r ON i.item_number = r.item_number LIMIT 1";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Label label = new Label(columnName);
                    formBox.getChildren().add(label);

                    if (columnName.equals("review_date") || columnName.equals("fix_date")) {
                        DatePicker datePicker = new DatePicker();
                        datePicker.setEditable(false);
                        datePicker.setPromptText(columnName);
                        datePicker.setMaxWidth(200);

                        // Применяем стили к DatePicker
                        datePicker.setStyle(
                                "-fx-font-size: 18px; " +
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
                                        "-fx-font-size: 18px;" +
                                        "-fx-background-radius: 10px;" +
                                        "-fx-cursor: pointer;"
                        );

                        formBox.getChildren().add(datePicker);
                        inputFields.add(new InputField(datePicker, columnName));
                    } else {
                        TextField textField = new TextField();
                        textField.setMaxWidth(200);
                        textField.setPromptText(columnName);
                        textField.setStyle(
                                "-fx-font-size: 18px; " +
                                        "-fx-background-color: #04060a; " +
                                        "-fx-text-fill: #ffffff; " +
                                        "-fx-border-color: #df6a1b; " +
                                        "-fx-border-width: 2px; " +
                                        "-fx-border-radius: 10px;"
                        );

                        formBox.getChildren().add(textField);
                        inputFields.add(new InputField(textField, columnName));
                    }
                }

                Button addButton = new Button("Добавить");
                addButton.setMinWidth(200);
                addButton.setStyle(
                        "-fx-font-size: 18px; " +
                                "-fx-background-color: #df6a1b; " +
                                "-fx-text-fill: #04060a; " +
                                "-fx-background-radius: 10px;" +
                                "-fx-cursor: hand;"
                );

                addButton.setOnAction(event -> {
                    try {
                        addRecord(inputFields);
                        formStage.close();
                        table.setItems(mainWindow.getTable("items", "remarks"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                formBox.getChildren().add(addButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return formBox;
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
                    }
                }
                remarksStmt.executeUpdate();
            }
        }
    }

    private class InputField {
        Node node;
        String columnName;

        InputField(Node node, String columnName) {
            this.node = node;
            this.columnName = columnName;
        }
    }
}
