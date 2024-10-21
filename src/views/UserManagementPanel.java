package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import data.DatabaseConnection;

public class UserManagementPanel extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JTextField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextField registrationNumberField;

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        initializeComponents();
        loadUserData();
    }

    private void initializeComponents() {
        // Table to display users
        String[] columnNames = {"User ID", "Username", "Role", "Registration Number"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Form to add/edit user
        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JTextField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Role:"));
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Student"});
        formPanel.add(roleComboBox);

        formPanel.add(new JLabel("Registration Number:"));
        registrationNumberField = new JTextField();
        formPanel.add(registrationNumberField);

        // Buttons
        JButton addButton = new JButton("Add User");
        addButton.addActionListener(e -> addUser());
        JButton deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(e -> deleteUser());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // Adding components to the main panel
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserData() {
        tableModel.setRowCount(0); // Clear existing data
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT user_id, username, role, registration_number FROM user";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String role = resultSet.getString("role");
                String registrationNumber = resultSet.getString("registration_number");
                tableModel.addRow(new Object[]{userId, username, role, registrationNumber});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUser() {
        String username = usernameField.getText();
        String password = passwordField.getText(); // In a real application, hash this password
        String role = (String) roleComboBox.getSelectedItem();
        String registrationNumber = registrationNumberField.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO user (username, password, role, registration_number) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            statement.setString(4, registrationNumber);
            statement.executeUpdate();
            loadUserData(); // Refresh the table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM user WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, userId);
                statement.executeUpdate();
                loadUserData(); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
        }
    }
}
