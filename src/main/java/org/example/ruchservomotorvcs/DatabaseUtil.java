package org.example.ruchservomotorvcs;

import java.sql.*;

public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.lchxrjzjnwbbvrlahfix";
    private static final String PASSWORD = "wobso4-riqfYm-rinnon";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean validateUser(String username, String password) {
        String query = "SELECT COUNT(*), Роль FROM пользователи WHERE Логин = ? AND Пароль = ? GROUP BY Роль";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    MainWindow.setUserRole(rs.getString("Роль"));
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            MainWindow.showErrorAlert("Ошибка", "Не удалось получить информацию о пользователе.", e.getMessage());
        }
        return false;
    }
}
