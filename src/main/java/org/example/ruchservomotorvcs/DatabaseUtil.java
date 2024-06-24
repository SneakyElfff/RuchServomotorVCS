package org.example.ruchservomotorvcs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://localhost:5432/VCS";
    private static final String USER = "nina";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, "");
    }
}
