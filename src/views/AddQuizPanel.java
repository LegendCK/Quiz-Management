package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import data.DatabaseConnection;

public class AddQuizPanel extends JPanel {
    private JComboBox<String> subjectComboBox;
    private JTextField quizTitleField;  // Field for quiz title
    private JTextField questionField;
    private JTextField[] optionFields;
    private JComboBox<Integer> correctAnswerComboBox;
    private JButton nextQuestionButton;
    private JButton finishButton;
    private List<Question> questions;

    public AddQuizPanel() {
        setLayout(new GridLayout(0, 2, 10, 10));
        setBackground(Color.WHITE);

        questions = new ArrayList<>();

        add(new JLabel("Enter Quiz Title:"));
        quizTitleField = new JTextField();
        add(quizTitleField);

        add(new JLabel("Select Subject:"));
        subjectComboBox = new JComboBox<>();
        populateSubjectComboBox();
        add(subjectComboBox);

        add(new JLabel("Enter Question:"));
        questionField = new JTextField();
        add(questionField);

        optionFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            add(new JLabel("Option " + (i + 1) + ":"));
            optionFields[i] = new JTextField();
            add(optionFields[i]);
        }

        add(new JLabel("Correct Answer (1-4):"));
        correctAnswerComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        add(correctAnswerComboBox);

        nextQuestionButton = new JButton("Next Question");
        finishButton = new JButton("Finish");
        add(nextQuestionButton);
        add(finishButton);

        nextQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCurrentQuestion();
                clearFields();
            }
        });

        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCurrentQuestion();

                String quizTitle = quizTitleField.getText().trim();
                if (!quizTitle.isEmpty()) {
                    saveQuizToDatabase(quizTitle);
                } else {
                    JOptionPane.showMessageDialog(AddQuizPanel.this, "Quiz title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void populateSubjectComboBox() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT subject_name FROM Subject";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        subjectComboBox.addItem(resultSet.getString("subject_name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCurrentQuestion() {
        String question = questionField.getText().trim();
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Question cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = new String[4];
        for (int i = 0; i < 4; i++) {
            options[i] = optionFields[i].getText().trim();
            if (options[i].isEmpty()) {
                JOptionPane.showMessageDialog(this, "Option " + (i + 1) + " cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        int correctAnswer = (int) correctAnswerComboBox.getSelectedItem();
        questions.add(new Question(question, options, correctAnswer));
    }

    private void saveQuizToDatabase(String quizTitle) {
        String subject = (String) subjectComboBox.getSelectedItem();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String quizSql = "INSERT INTO quiz (quiz_name, quiz_description, total_marks, passing_marks) VALUES (?, ?, ?, ?)";
            try (PreparedStatement quizStmt = connection.prepareStatement(quizSql, Statement.RETURN_GENERATED_KEYS)) {
                quizStmt.setString(1, quizTitle);
                quizStmt.setString(2, "Default Description");
                quizStmt.setInt(3, 100);
                quizStmt.setInt(4, 50);
                quizStmt.executeUpdate();

                ResultSet generatedKeys = quizStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int quizId = generatedKeys.getInt(1);

                    for (Question question : questions) {
                        String questionSql = "INSERT INTO questions (quiz_id, question_text, option_1, option_2, option_3, option_4, correct_option) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement questionStmt = connection.prepareStatement(questionSql)) {
                            questionStmt.setInt(1, quizId);
                            questionStmt.setString(2, question.question);
                            questionStmt.setString(3, question.options[0]);
                            questionStmt.setString(4, question.options[1]);
                            questionStmt.setString(5, question.options[2]);
                            questionStmt.setString(6, question.options[3]);
                            questionStmt.setInt(7, question.correctAnswer);
                            questionStmt.executeUpdate();
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Quiz and questions added successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving quiz: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        questionField.setText("");
        for (JTextField optionField : optionFields) {
            optionField.setText("");
        }
        correctAnswerComboBox.setSelectedIndex(0);
    }

    private static class Question {
        String question;
        String[] options;
        int correctAnswer;

        public Question(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }
}
