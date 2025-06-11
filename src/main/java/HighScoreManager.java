import java.sql.*;

public class HighScoreManager {
    private static final String DB_URL = "jdbc:sqlite:tetris_highscores.db";

    public HighScoreManager() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS highscores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "score INTEGER NOT NULL)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHighScore() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(score) AS max_score FROM highscores")) {
            if (rs.next()) {
                return rs.getInt("max_score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void saveScore(int score) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO highscores(score) VALUES (?)")) {
            ps.setInt(1, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetHighScore() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM highscores");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
