package ui.admin;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AddAccountDialog extends JDialog {

    private AccountManagementPanel parentPanel;
    private JTextField usernameField, fullNameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleComboBox;

    public AddAccountDialog(AccountManagementPanel parent) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Thêm Tài khoản Mới", true);
        this.parentPanel = parent;

        setLayout(new BorderLayout(10, 10));
        setSize(450, 350);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        fullNameField = new JTextField(20);
        emailField = new JTextField(20);
        roleComboBox = new JComboBox<>(new String[]{"Người đo", "Quản lý"});

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Mật khẩu:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Xác nhận MK:"));
        formPanel.add(confirmPasswordField);
        formPanel.add(new JLabel("Họ tên:"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Vai trò (Role):"));
        formPanel.add(roleComboBox);

        add(formPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Thêm");
        JButton backButton = new JButton("Quay lại");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addAccount());
        backButton.addActionListener(e -> dispose());
    }

    private void addAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String roleDisplay = (String) roleComboBox.getSelectedItem(); // "Quản lý" hoặc "Người đo"

        // === Validate ===
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải ít nhất 6 ký tự.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("fullName", fullName);
        data.put("email", email);
        data.put("role", roleDisplay); // Gửi đúng: "Quản lý" hoặc "Người đo"

        // === Gửi API ===
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.getInstance().addUser(data);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AddAccountDialog.this,
                                "Thêm tài khoản thành công!\nUsername: " + username,
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        parentPanel.loadAccounts(); // Tải lại bảng
                        dispose(); // Đóng dialog
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddAccountDialog.this,
                            "Lỗi khi thêm tài khoản:\n" + ex.getMessage(),
                            "Thất bại", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
