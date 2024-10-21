package views;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JPanel {
    private JPanel sideMenu;
    private JPanel mainContent;
    private JButton logoutButton;

    public AdminDashboard(String adminName) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        sideMenu = new JPanel();
        sideMenu.setLayout(new GridLayout(0, 1));
        sideMenu.setBackground(new Color(30, 30, 30));
        sideMenu.setPreferredSize(new Dimension(200, 0));

        JButton dashboardButton = createMenuButton("Dashboard Overview");
        JButton quizButton = createMenuButton("Quiz Management");
        JButton userButton = createMenuButton("User Management");
        logoutButton = createMenuButton("Logout");

        // Set button actions
        dashboardButton.addActionListener(e -> showDashboard());
        quizButton.addActionListener(e -> showQuizManagement());
        userButton.addActionListener(e -> showUserManagement());
        logoutButton.addActionListener(e -> logout());

        // Add buttons to the side menu
        sideMenu.add(dashboardButton);
        sideMenu.add(quizButton);
        sideMenu.add(userButton);
        sideMenu.add(logoutButton);

        add(sideMenu, BorderLayout.WEST);

        mainContent = new JPanel();
        mainContent.setLayout(new BorderLayout());
        mainContent.setBackground(Color.LIGHT_GRAY);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBackground(Color.LIGHT_GRAY);
        JLabel greetingLabel = new JLabel("Welcome, Admin " + adminName + "!");
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(greetingLabel);
        headerPanel.add(logoutButton);

        mainContent.add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        showDashboard();
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(45, 45, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 30));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 70, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(45, 45, 45));
            }
        });
        return button;
    }

    private void showDashboard() {
        mainContent.removeAll();
        mainContent.add(new DashboardOverviewPanel(), BorderLayout.CENTER);  // Add Dashboard Overview
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void showQuizManagement() {
        mainContent.removeAll();
        mainContent.add(new QuizManagementPanel(mainContent), BorderLayout.CENTER);  // Pass mainContent as the parent panel
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void showUserManagement() {
        mainContent.removeAll();
        mainContent.add(new UserManagementPanel(), BorderLayout.CENTER);  // Link to UserManagementPanel
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void logout() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose();
    }
}
