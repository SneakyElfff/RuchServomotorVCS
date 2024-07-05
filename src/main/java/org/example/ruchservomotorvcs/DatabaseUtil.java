package org.example.ruchservomotorvcs;

import java.sql.*;

public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://localhost:5432/VCS";
    private static final String USER = "nina";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, "");
    }

    public static boolean validateUser(String username, String password) {
        String query = "SELECT COUNT(*) FROM пользователи WHERE Логин = ? AND Пароль = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            MainWindow.showErrorAlert("Ошибка", "Не удалось проверить наличие пользователя.", e.getMessage());
        }
        return false;
    }
}
