package views;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import data.DatabaseConnection;

public class QuizDatabaseHandler {

    public boolean insertQuestion(String questionText, String[] options, int correctOption) {
        String sql = "INSERT INTO Question (question_text, option_1, option_2, option_3, option_4, correct_option) VALUES ( ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, questionText);
            preparedStatement.setString(2, options[0]);
            preparedStatement.setString(3, options[1]);
            preparedStatement.setString(4, options[2]);
            preparedStatement.setString(5, options[3]);
            preparedStatement.setInt(6, correctOption);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
