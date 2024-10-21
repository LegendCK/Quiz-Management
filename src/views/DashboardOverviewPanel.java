package views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import data.DatabaseConnection;

public class DashboardOverviewPanel extends JPanel {
    private JLabel userCountLabel;
    private JTextArea recentActivitiesArea;
    private JTextArea notificationsArea;

    public DashboardOverviewPanel() {
        setLayout(new BorderLayout());

        JPanel userPanel = new JPanel(new FlowLayout());
        userPanel.setBackground(Color.LIGHT_GRAY);
        userCountLabel = new JLabel("Total Users: ");
        userPanel.add(userCountLabel);
        add(userPanel, BorderLayout.NORTH);

        recentActivitiesArea = new JTextArea(10, 30);
        recentActivitiesArea.setEditable(false);
        JScrollPane activitiesScroll = new JScrollPane(recentActivitiesArea);
        activitiesScroll.setBorder(BorderFactory.createTitledBorder("Recent Activities"));
        add(activitiesScroll, BorderLayout.WEST);

        notificationsArea = new JTextArea(10, 30);
        notificationsArea.setEditable(false);
        JScrollPane notificationsScroll = new JScrollPane(notificationsArea);
        notificationsScroll.setBorder(BorderFactory.createTitledBorder("Notifications"));
        add(notificationsScroll, BorderLayout.EAST);

        loadUserCount();
        loadRecentActivities();
        loadNotifications();
    }

    private void loadUserCount() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS user_count FROM user")) {
            if (resultSet.next()) {
                int userCount = resultSet.getInt("user_count");
                userCountLabel.setText("Total Users: " + userCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            userCountLabel.setText("Error fetching user count.");
        }
    }

    private void loadRecentActivities() {
        recentActivitiesArea.setText("1. Faculty A created a new quiz.\n" +
                "2. Student B registered.\n" +
                "3. Student B completed a quiz.\n");
    }

    private void loadNotifications() {
        notificationsArea.setText("1. New features will be available soon.\n");
    }
}
