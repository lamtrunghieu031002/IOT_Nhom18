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
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận mật khẩu không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("username", usernameField.getText());
        data.put("password", password);
        data.put("fullName", fullNameField.getText());
        data.put("email", emailField.getText());
        data.put("role", (String) roleComboBox.getSelectedItem());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.getInstance().addUser(data);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AddAccountDialog.this, "Thêm tài khoản thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        parentPanel.loadAccounts();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(AddAccountDialog.this, "Lỗi khi thêm tài khoản.", "Lỗi API", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AddAccountDialog.this, "Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
