module org.example.ruchservomotorvcs {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.ruchservomotorvcs to javafx.fxml;
    exports org.example.ruchservomotorvcs;
}