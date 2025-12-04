package app;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        super("Đăng nhập Hệ thống");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLayout(new GridLayout(3, 2, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Đăng nhập");

        add(new JLabel("Tên đăng nhập:"));
        add(usernameField);
        add(new JLabel("Mật khẩu:"));
        add(passwordField);
        add(new JLabel(""));
        add(loginButton);

        loginButton.addActionListener(e -> attemptLogin());

        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.getInstance().login(username, password);
            }

            @Override
            protected void done() {
                try {
                    String role = get();
                    dispose();
                    new MainFrame(role);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            e.getMessage(), "Lỗi Đăng nhập", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
