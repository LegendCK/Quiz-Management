import java.awt.*;
import javax.swing.*;
import views.LoginView;
import views.SignUpView;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Quiz Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        LoginView loginView = new LoginView(cardLayout, mainPanel);
        SignUpView signUpView = new SignUpView(cardLayout, mainPanel);

        mainPanel.add(loginView, "loginPanel");
        mainPanel.add(signUpView, "signUpPanel");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

}

