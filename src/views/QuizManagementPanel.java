package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import data.DatabaseConnection;

public class QuizManagementPanel extends JPanel {
    private JPanel parentPanel;
    private JComboBox<String> subjectComboBox;
    private JTable quizTable;
    private DefaultTableModel quizTableModel;
    private JButton deleteSubjectButton;
    private JButton deleteQuizButton;
    private JButton createQuizButton;
    private JButton addSubjectButton; // New button to add subject
    private List<Integer> quizIds;
    private List<Integer> subjectIds;

    public QuizManagementPanel(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(Color.LIGHT_GRAY);

        subjectComboBox = new JComboBox<>();
        subjectComboBox.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(new JLabel("Select Subject:"));

        controlPanel.add(subjectComboBox);

        // Button to add new subject
        addSubjectButton = new JButton("Add Subject");
        controlPanel.add(addSubjectButton);

        deleteSubjectButton = new JButton("Delete Subject");
        controlPanel.add(deleteSubjectButton);

        deleteQuizButton = new JButton("Delete Selected Quiz");
        controlPanel.add(deleteQuizButton);

        add(controlPanel, BorderLayout.NORTH);

        quizTableModel = new DefaultTableModel(new String[]{"Quiz ID", "Quiz Title", "Description"}, 0);
        quizTable = new JTable(quizTableModel);
        quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(quizTable), BorderLayout.CENTER);

        createQuizButton = new JButton("Create Quiz");
        createQuizButton.setPreferredSize(new Dimension(150, 30));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        buttonPanel.add(createQuizButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadSubjectsWithQuizzes();

        subjectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadQuizzesForSelectedSubject();
            }
        });

        deleteSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedSubject();
            }
        });

        deleteQuizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedQuiz();
            }
        });

        createQuizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentPanel.removeAll();
                parentPanel.add(new AddQuizPanel(), BorderLayout.CENTER);
                parentPanel.revalidate();
                parentPanel.repaint();
            }
        });

        // Action listener for adding a new subject
        addSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewSubject();
            }
        });
    }

    private void loadSubjectsWithQuizzes() {
        subjectComboBox.removeAllItems();
        subjectIds = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.subject_id, s.subject_name FROM Subject s JOIN quiz q ON s.subject_id = q.quiz_id GROUP BY s.subject_id, s.subject_name";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    subjectComboBox.addItem(resultSet.getString("subject_name"));
                    subjectIds.add(resultSet.getInt("subject_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQuizzesForSelectedSubject() {
        quizTableModel.setRowCount(0);
        quizIds = new ArrayList<>();

        int selectedSubjectIndex = subjectComboBox.getSelectedIndex();
        if (selectedSubjectIndex == -1) return;

        int selectedSubjectId = subjectIds.get(selectedSubjectIndex);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT quiz_id, quiz_name, quiz_description FROM quiz WHERE quiz_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, selectedSubjectId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int quizId = resultSet.getInt("quiz_id");
                        String quizTitle = resultSet.getString("quiz_name");
                        String quizDescription = resultSet.getString("quiz_description");
                        quizTableModel.addRow(new Object[]{quizId, quizTitle, quizDescription});
                        quizIds.add(quizId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading quizzes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to add a new subject
    private void addNewSubject() {
        String subjectName = JOptionPane.showInputDialog(this, "Enter new subject name:");
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Subject (subject_name) VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, subjectName);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Subject added successfully.");
                    loadSubjectsWithQuizzes();  // Refresh the subject list
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding subject: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Subject name cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedSubject() {
        String selectedSubject = (String) subjectComboBox.getSelectedItem();
        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "No subject selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the subject and its quizzes?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                // Delete all quizzes under this subject first
                String deleteQuizzesSql = "DELETE FROM quiz WHERE quiz_id IN (SELECT quiz_id FROM Subject WHERE subject_name = ?)";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuizzesSql)) {
                    statement.setString(1, selectedSubject);
                    statement.executeUpdate();
                }

                // Delete the subject
                String deleteSubjectSql = "DELETE FROM Subject WHERE subject_name = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteSubjectSql)) {
                    statement.setString(1, selectedSubject);
                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "Subject deleted successfully.");
                        loadSubjectsWithQuizzes();  // Reload subjects
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting subject: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedQuiz() {
        int selectedRow = quizTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No quiz selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quizIdToDelete = quizIds.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this quiz?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM quiz WHERE quiz_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, quizIdToDelete);
                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "Quiz deleted successfully.");
                        loadQuizzesForSelectedSubject();  // Reload quizzes
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting quiz: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
