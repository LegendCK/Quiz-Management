package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import data.DatabaseConnection;

public class QuizGUI extends JPanel {
    private JFrame frame;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup group;
    private JButton submitButton;
    private JButton nextButton;
    private JButton backButton;
    private JButton resetButton;
    private JLabel timerLabel;
    private int timeLeft = 30; // 30 seconds for each question
    private int currentQuestion = 0;
    private String[] questions;
    private String[][] optionsArray;
    private String[] correctAnswers;
    private int score = 0; // Initialize score to 0
    private Timer timer;

    public QuizGUI() {
        frame = new JFrame("Quiz");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the window

        // Initialize header, options panel, buttons, and result label
        initHeaderPanel();
        initOptionsPanel();
        initButtonsPanel();

        // Fetch questions to start the quiz (Assuming quiz ID = 1 for now)
        fetchQuestions(1);

        frame.setVisible(true);
    }

    private void initHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(0xF0F0F0)); // Light gray background

        timerLabel = new JLabel("Time left: " + timeLeft + " seconds", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(timerLabel, BorderLayout.EAST);

        questionLabel = new JLabel("Welcome to the Quiz! Click Start to begin.");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(questionLabel, BorderLayout.CENTER);
        frame.add(headerPanel, BorderLayout.NORTH);
    }

    private void initOptionsPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(4, 1, 10, 10)); // Vertical grid layout for 4 options
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        options = new JRadioButton[4];
        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 18));
            options[i].setBackground(new Color(0xFFFFFF)); // White background
            options[i].setOpaque(true); // Make the background opaque
            group.add(options[i]);
            optionsPanel.add(options[i]);
        }
        frame.add(optionsPanel, BorderLayout.CENTER);
    }

    private void initButtonsPanel() {
        JPanel buttonsAndResultPanel = new JPanel();
        buttonsAndResultPanel.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        submitButton = createButton("Submit", "Submit your answer");
        nextButton = createButton("Next", "Go to the next question");
        backButton = createButton("Back", "Go back to the previous question");
        resetButton = createButton("Reset", "Reset the quiz");

        submitButton.addActionListener(new SubmitButtonListener());
        nextButton.addActionListener(new NextButtonListener());
        backButton.addActionListener(new BackButtonListener());
        resetButton.addActionListener(new ResetButtonListener());

        buttonsPanel.add(submitButton);
        buttonsPanel.add(nextButton);
        buttonsPanel.add(backButton);
        resetButton.setEnabled(false);
        buttonsPanel.add(resetButton);
        buttonsAndResultPanel.add(buttonsPanel, BorderLayout.NORTH);

        frame.add(buttonsAndResultPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(0x007BFF)); // Bootstrap primary color
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setFocusable(false);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void fetchQuestions(int quizId) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            Connection conn = db.getConnection();
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            String query = "SELECT question_text, option_1, option_2, option_3, option_4, correct_option FROM questions WHERE quiz_id = " + quizId;
            ResultSet rs = stmt.executeQuery(query);

            int questionCount = getRowCount(rs);
            questions = new String[questionCount];
            optionsArray = new String[questionCount][4];
            correctAnswers = new String[questionCount];

            int index = 0;
            while (rs.next()) {
                questions[index] = rs.getString("question_text");
                optionsArray[index][0] = rs.getString("option_1");
                optionsArray[index][1] = rs.getString("option_2");
                optionsArray[index][2] = rs.getString("option_3");
                optionsArray[index][3] = rs.getString("option_4");
                // Assuming correct_option is an integer indicating the correct option index (1-4)
                int correctOptionIndex = rs.getInt("correct_option") - 1; // Convert to 0-based index
                correctAnswers[index] = optionsArray[index][correctOptionIndex]; // Store the actual option text
                index++;
            }

            rs.close();
            stmt.close();
            conn.close();

            startQuiz();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching questions.");
        }
    }

    private int getRowCount(ResultSet rs) throws SQLException {
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
        return rowCount;
    }

    private void startQuiz() {
        currentQuestion = 0;
        score = 0; // Reset score at the start of the quiz
        updateQuestion();
        startTimer();
    }

    private void updateQuestion() {
        questionLabel.setText(questions[currentQuestion]);
        options[0].setText(optionsArray[currentQuestion][0]);
        options[1].setText(optionsArray[currentQuestion][1]);
        options[2].setText(optionsArray[currentQuestion][2]);
        options[3].setText(optionsArray[currentQuestion][3]);
        group.clearSelection();
        submitButton.setEnabled(true);
        timeLeft = 30;
        timerLabel.setText("Time left: " + timeLeft + " seconds");
    }

    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time left: " + timeLeft + " seconds");

                if (timeLeft <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(frame, "Time's up! Moving to the next question.");
                    moveToNextQuestion();
                }
            }
        });
        timer.start();
    }

    private void moveToNextQuestion() {
        if (currentQuestion < questions.length - 1) {
            currentQuestion++;
            updateQuestion();
            timer.restart();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        timer.stop();
        submitButton.setEnabled(false);
        nextButton.setEnabled(false);
        backButton.setEnabled(false);
        resetButton.setEnabled(true);
        JOptionPane.showMessageDialog(frame, "Quiz finished! Your score: " + score + " out of " + questions.length);
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedOption = null;
            for (JRadioButton option : options) {
                if (option.isSelected()) {
                    selectedOption = option.getText();
                    break;
                }
            }

            if (selectedOption == null) {
                JOptionPane.showMessageDialog(frame, "Please select an option.");
            } else {
                if (selectedOption.equals(correctAnswers[currentQuestion])) {
                    score++;
                }
                submitButton.setEnabled(false);
            }
        }
    }

    private class NextButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            moveToNextQuestion();
        }
    }

    private class BackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentQuestion > 0) {
                currentQuestion--;
                updateQuestion();
            }
        }
    }

    private class ResetButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetQuiz();
        }
    }

    private void resetQuiz() {
        currentQuestion = 0;
        score = 0;
        resetButton.setEnabled(false);
        submitButton.setEnabled(true);
        nextButton.setEnabled(true);
        backButton.setEnabled(true);
        updateQuestion();
    }

}
