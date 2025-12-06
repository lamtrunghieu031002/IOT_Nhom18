package app;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        super("Đăng nhập Hệ thống");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // ==================== Panel chính chia 2 bên ====================
        JPanel mainPanel = new JPanel(new BorderLayout());

        // ------------------ Bên trái: Hình nền ------------------
        JLabel leftImageLabel = new JLabel();
        // Thay đường dẫn này bằng ảnh thực tế của bạn (hoặc dùng URL nếu có)
        URL imageUrl = getClass().getClassLoader().getResource("images/login.jpg");
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(450, 500, Image.SCALE_SMOOTH);
            leftImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // Nếu không tìm thấy ảnh thì để màu nền thay thế
            leftImageLabel.setBackground(new Color(240, 248, 255));
            leftImageLabel.setOpaque(true);
        }
        leftImageLabel.setPreferredSize(new Dimension(450, 500));
        mainPanel.add(leftImageLabel, BorderLayout.WEST);

        // ------------------ Bên phải: Form đăng nhập ------------------
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề "Sign in"
        JLabel titleLabel = new JLabel("Sign in", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(50, 50, 50));

        JLabel userLabel = new JLabel("Tên đăng nhập");
        JTextField usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(300, 45));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JLabel passLabel = new JLabel("Mật khẩu");
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(300, 45));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Nút Đăng nhập (xanh dương)
        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(new Color(0, 122, 255)); // Màu xanh giống ảnh
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(140, 45));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Nút Quay lại (xám)
        JButton backButton = new JButton("Quay lại");
        backButton.setBackground(new Color(220, 220, 220));
        backButton.setForeground(new Color(80, 80, 80));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setPreferredSize(new Dimension(140, 45));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> System.exit(0)); // Thoát chương trình hoặc quay về màn trước

        // Panel chứa 2 nút
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(backButton);

        // Đặt các thành phần vào rightPanel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        rightPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        rightPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        rightPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(30, 10, 10, 10);
        rightPanel.add(buttonPanel, gbc);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Thêm mainPanel vào frame
        add(mainPanel);

        // ==================== Xử lý đăng nhập ====================
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        setVisible(true);
    }

    private void attemptLogin(String username, char[] passwordChars) {
        String password = new String(passwordChars);

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
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            ex.getMessage(),
                            "Lỗi Đăng nhập",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Để chạy thử
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}