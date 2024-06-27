package org.example.ruchservomotorvcs;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

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
        Scene formScene = new Scene(formBox, 400, 500);
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
                        table.setItems(mainWindow.getTable("items", "remarks")); // Обновить данные в таблице, вызывая метод getTable из MainWindow
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

    private void addRecord(TextField[] inputFields) throws SQLException {
        StringBuilder itemsQueryBuilder = new StringBuilder("INSERT INTO items (");
        StringBuilder itemsValuesBuilder = new StringBuilder("VALUES (");
        StringBuilder remarksQueryBuilder = new StringBuilder("INSERT INTO remarks (");
        StringBuilder remarksValuesBuilder = new StringBuilder("VALUES (");

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

        remarksQueryBuilder.append(inputFields[0].getPromptText());
        remarksValuesBuilder.append("?");

        for (int i = 4; i <= inputFields.length; i++) {
            remarksQueryBuilder.append(", ").append(inputFields[i - 1].getPromptText());
            remarksValuesBuilder.append(", ?");
        }

        remarksQueryBuilder.append(") ");
        remarksValuesBuilder.append(")");

        String itemsQuery = itemsQueryBuilder.toString() + itemsValuesBuilder.toString();
        String remarksQuery = remarksQueryBuilder.toString() + remarksValuesBuilder.toString();

        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
                for (int i = 0; i < 3; i++) {
                    itemsStmt.setString(i + 1, inputFields[i].getText());
                }
                itemsStmt.executeUpdate();
            }

            try (PreparedStatement remarksStmt = conn.prepareStatement(remarksQuery)) {
                remarksStmt.setString(1, inputFields[0].getText());

                int paramIndex = 2;
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
}
