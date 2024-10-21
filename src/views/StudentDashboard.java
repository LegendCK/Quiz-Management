package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import data.DatabaseConnection;

public class StudentDashboard extends JPanel {
    private final JLabel greetingLabel;
    private final JPanel mainContent;
    private final CardLayout cardLayout;

    public StudentDashboard(String name) {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        greetingLabel = new JLabel("Welcome, " + name + "!", SwingConstants.CENTER);
        greetingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        greetingLabel.setForeground(new Color(50, 50, 50));

        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new GridLayout(0, 1));
        sideMenu.setPreferredSize(new Dimension(200, 0));
        sideMenu.setBackground(Color.WHITE);

        JButton quizzesButton = createNavButton("Quizzes");
        JButton performanceReportButton = createNavButton("Performance Report");
        JButton logoutButton = createNavButton("Logout");

        quizzesButton.addActionListener(_ -> showQuizzesPanel());
        performanceReportButton.addActionListener(_ -> showPerformanceReport());
        logoutButton.addActionListener(_ -> logout());

        sideMenu.add(quizzesButton);
        sideMenu.add(performanceReportButton);
        sideMenu.add(logoutButton);

        mainContent = new JPanel();
        cardLayout = new CardLayout();
        mainContent.setLayout(cardLayout);
        mainContent.setBackground(Color.WHITE);

        JPanel dashboardPanel = createDashboardPanel();
        JPanel performancePanel = createPerformancePanel();

        mainContent.add(dashboardPanel, "Dashboard");
        mainContent.add(performancePanel, "PerformanceReport");

        add(sideMenu, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(70, 70, 70));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(230, 230, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(240, 240, 240));
            }
        });

        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.add(greetingLabel, BorderLayout.NORTH);

        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new GridLayout(0, 2, 10, 10));

        // Fetch subjects dynamically from the database
        ArrayList<String> subjects = fetchSubjectsFromDatabase();

        for (String subject : subjects) {
            JPanel card = createQuizCard(subject);
            quizPanel.add(card);
        }

        dashboardPanel.add(quizPanel, BorderLayout.CENTER);
        return dashboardPanel;
    }

    private JPanel createPerformancePanel() {
        JPanel performancePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Performance Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        performancePanel.add(titleLabel, BorderLayout.NORTH);

        String[] performanceData = {
                "Coming Soon!"
        };
        JList<String> performanceList = new JList<>(performanceData);
        JScrollPane scrollPane = new JScrollPane(performanceList);
        performancePanel.add(scrollPane, BorderLayout.CENTER);

        return performancePanel;
    }

    private JPanel createQuizCard(String subject) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        card.setPreferredSize(new Dimension(150, 150));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                card.setBackground(new Color(230, 230, 250));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });

        JLabel subjectLabel = new JLabel(subject, SwingConstants.CENTER);
        subjectLabel.setFont(new Font("Arial", Font.BOLD, 16));
        subjectLabel.setForeground(new Color(70, 70, 70));

        JButton quizButton = new JButton("Start Quiz");
        quizButton.setBackground(new Color(60, 120, 240));
        quizButton.setForeground(Color.blue);
        quizButton.setFocusPainted(false);
        quizButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quizButton.addActionListener(e -> startQuiz(subject));  // Use startQuiz method when clicked

        card.add(subjectLabel, BorderLayout.CENTER);
        card.add(quizButton, BorderLayout.SOUTH);

        return card;
    }

    private void showDashboard() {
        cardLayout.show(mainContent, "Dashboard");
    }

    private void showPerformanceReport() {
        cardLayout.show(mainContent, "PerformanceReport");
    }

    private void showQuizzesPanel() {
        cardLayout.show(mainContent, "Quizzes");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "You want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void startQuiz(String subject) {
        JOptionPane.showMessageDialog(this, "Starting quiz for subject: " + subject);
        // Create and display QuizGUI window
        SwingUtilities.invokeLater(() -> {
            QuizGUI quizWindow = new QuizGUI();  // Instantiate QuizGUI
            JFrame quizFrame = new JFrame("Quiz - " + subject);  // New frame for quiz
            quizFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Close only this window
            quizFrame.add(quizWindow);  // Add QuizGUI to the frame
            quizFrame.pack();
            quizFrame.setSize(800, 600);
            quizFrame.setLocationRelativeTo(null);  // Center the quiz frame
            quizFrame.setVisible(true);  // Display the frame
        });
    }

    private ArrayList<String> fetchSubjectsFromDatabase() {
        ArrayList<String> subjects = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT subject_name FROM subject");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(rs.getString("subject_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return subjects;
    }
}
