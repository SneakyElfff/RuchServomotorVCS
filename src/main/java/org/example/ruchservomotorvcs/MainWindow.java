package org.example.ruchservomotorvcs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class MainWindow {

    public BorderPane createMainPane() {
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
        root.setCenter(mainBox);

        return root;
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
