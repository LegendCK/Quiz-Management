package views;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import data.DatabaseConnection;

public class LoginView extends JPanel {
    private final JTextField regNoField;
    private final JPasswordField passwordField;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public LoginView(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;

        setBackground(new Color(240, 248, 255)); // Alice Blue
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Welcome to Quiz Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Registration Number:"), gbc);
        gbc.gridx = 1;
        regNoField = new JTextField(15);
        add(regNoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        add(buttonPanel, gbc);

        loginButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(new RoundedBorder(10));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signUpButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setBorder(new RoundedBorder(10));
        signUpButton.setFocusPainted(false);
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        regNoField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });

        // Add KeyListener to passwordField
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regNo = regNoField.getText();
                String password = new String(passwordField.getPassword());
                String userType = authenticateUser(regNo, password);
                if (userType != null) {
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(LoginView.this);
                    topFrame.dispose();
                    String userName = "User";
                    navigateToDashboard(userType, userName);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials!");
                }
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "signUpPanel");
            }
        });
    }

    private String authenticateUser(String regNo, String password) {
        String sql = "SELECT role FROM User WHERE registration_number = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, regNo);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("role");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void navigateToDashboard(String userType, String name) {
        JFrame dashboardFrame = new JFrame("Dashboard");
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setSize(800, 600);

        if (userType.equals("Student")) {
            StudentDashboard studentDashboard = new StudentDashboard(name);
            dashboardFrame.add(studentDashboard);
        } else if (userType.equals("Admin")) {
            AdminDashboard adminDashboard = new AdminDashboard(name);
            dashboardFrame.add(adminDashboard);
        }

        dashboardFrame.setVisible(true);
    }

    static class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
