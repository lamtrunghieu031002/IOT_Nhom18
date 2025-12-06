package ui.admin;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditAccountDialog extends JDialog {

    private final AccountManagementPanel parentPanel;
    private final long userId;
    private JTextField fullNameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleComboBox;

    public EditAccountDialog(AccountManagementPanel parent, long userId, String username,
                             String fullName, String email, String roleVi) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Cập nhật Tài khoản", true);
        this.parentPanel = parent;
        this.userId = userId;

        setSize(460, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        form.add(new JLabel("Username:"));
        JLabel lblUsername = new JLabel(username);
        lblUsername.setFont(lblUsername.getFont().deriveFont(Font.BOLD));
        form.add(lblUsername);

        form.add(new JLabel("Họ tên:"));
        fullNameField = new JTextField(fullName, 20);
        form.add(fullNameField);

        form.add(new JLabel("Email:"));
        JLabel lblEmail = new JLabel(email);
        lblEmail.setForeground(Color.GRAY);
        form.add(lblEmail);

        form.add(new JLabel("Mật khẩu mới:"));
        passwordField = new JPasswordField(20);
        form.add(passwordField);

        form.add(new JLabel("Xác nhận MK:"));
        confirmPasswordField = new JPasswordField(20);
        form.add(confirmPasswordField);

        form.add(new JLabel("Vai trò:"));
        roleComboBox = new JComboBox<>(new String[]{"Người đo", "Quản lý"});
        roleComboBox.setSelectedItem(roleVi);
        form.add(roleComboBox);

        add(form, BorderLayout.CENTER);

        // Nút
        JButton saveBtn = new JButton("Lưu thay đổi");
        JButton cancelBtn = new JButton("Hủy");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> updateAccount());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void updateAccount() {
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String roleVi = (String) roleComboBox.getSelectedItem();

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.isEmpty() && password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải ít nhất 6 ký tự!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("role", roleVi);
        if (!password.isEmpty()) {
            data.put("password", password);
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.getInstance().updateUser((int)userId, data);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(EditAccountDialog.this,
                                "Cập nhật tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        parentPanel.loadAccounts();
                        dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EditAccountDialog.this,
                            "Lỗi cập nhật: " + ex.getMessage(), "Thất bại", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
