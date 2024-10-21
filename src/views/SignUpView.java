package views;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import data.DatabaseConnection;

public class SignUpView extends JPanel {
    private JTextField newRegNoField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField newPasswordField;
    private JRadioButton studentButton;
    private JRadioButton adminButton;
    private JButton submitButton;
    private JButton backButton;

    public SignUpView(CardLayout cardLayout, JPanel mainPanel) {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 240, 240));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Registration Number:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        newRegNoField = new JTextField(15);
        inputPanel.add(newRegNoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        firstNameField = new JTextField(15);
        inputPanel.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        lastNameField = new JTextField(15);
        inputPanel.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        newPasswordField = new JPasswordField(15);
        inputPanel.add(newPasswordField, gbc);

        JPanel userTypePanel = new JPanel();
        userTypePanel.setBackground(new Color(240, 240, 240));
        userTypePanel.setLayout(new FlowLayout());

        studentButton = new JRadioButton("Student");
        adminButton = new JRadioButton("Admin");

        ButtonGroup userTypeGroup = new ButtonGroup();
        userTypeGroup.add(studentButton);
        userTypeGroup.add(adminButton);

        userTypePanel.add(studentButton);
        userTypePanel.add(adminButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(userTypePanel, gbc);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));

        submitButton = new JButton("Submit");
        backButton = new JButton("Back to Login");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regNo = newRegNoField.getText();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String password = new String(newPasswordField.getPassword());
                String userType = studentButton.isSelected() ? "Student" : "Admin";

                if (insertUser(regNo, firstName, lastName, password, userType)) {
                    JOptionPane.showMessageDialog(null, "Sign-Up Successful as " + userType + "!");
                } else {
                    JOptionPane.showMessageDialog(null, "Sign-Up Failed!");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "loginPanel");
            }
        });

        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        JLabel titleLabel = new JLabel("Create a New Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
    }

    private boolean insertUser(String regNo, String firstName, String lastName, String password, String userType) {
        String sql = "INSERT INTO User (username, password, registration_number, role) VALUES (?, ?, ?, ?)";


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, firstName); // Assuming firstName is used as username
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, regNo);
            preparedStatement.setString(4, userType);


            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
