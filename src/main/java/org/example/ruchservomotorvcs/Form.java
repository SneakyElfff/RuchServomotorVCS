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
    private MainWindow mainWindow; // Добавляем ссылку на MainWindow

    public Form(TableView<ObservableList<Object>> table, MainWindow mainWindow) {
        this.table = table;
        this.mainWindow = mainWindow; // Инициализируем MainWindow
        formStage = new Stage();
        formStage.setTitle("Добавить запись");
    }

    public void showForm() {
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
            String query = "SELECT i.item_number, i.project_number, i.blueprint_number, " +
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
                formBox.getChildren().addAll(headline, authorsAndDates, textFields, createAddButton(inputFields));

                return formBox;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return formBox;
    }

    private void addFieldToGridPane(GridPane gridPane, Label label, Node field, int columnIndex) {
        gridPane.add(label, columnIndex, 0);
        gridPane.add(field, columnIndex, 1);
        GridPane.setHalignment(label, HPos.CENTER);
    }

    private void addFieldToVBox(VBox vBox, Label label, Node field) {
        vBox.getChildren().addAll(label, field);
    }

    private Button createAddButton(List<InputField> inputFields) {
        Button addButton = createStyledButton("Добавить");
        addButton.setOnAction(event -> {
            try {
                addRecord(inputFields);
                formStage.close();
                table.setItems(mainWindow.getTable("items", "remarks"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return addButton;
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
