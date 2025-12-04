package ui.admin;



import model.User;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
public class AccountManagementPanel extends JPanel {

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"ID", "Username", "Họ tên", "Email", "Role", "Ngày tạo"};

    public AccountManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(columnNames, 0);
        accountTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(accountTable);

        JButton addButton = new JButton("➕ Thêm Tài khoản");
        JButton deleteButton = new JButton("➖ Xóa Tài khoản");
        JButton refreshButton = new JButton("🔄 Tải lại");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> new AddAccountDialog(this).setVisible(true));
        deleteButton.addActionListener(e -> deleteSelectedAccount());
        refreshButton.addActionListener(e -> loadAccounts());

        loadAccounts();
    }

    public void loadAccounts() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User user : users) {
                        tableModel.addRow(new Object[]{
                                user.getId(), user.getUsername(), user.getFullName(),
                                user.getEmail(), user.getRole(), user.getDateCreated()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AccountManagementPanel.this,
                            "Lỗi khi tải danh sách tài khoản: " + e.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void deleteSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow != -1) {
            Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String username = (String) tableModel.getValueAt(selectedRow, 1);

            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa tài khoản ID: " + userId + " (" + username + ")?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return ApiClient.getInstance().deleteUser(userId);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(AccountManagementPanel.this, "Đã xóa tài khoản thành công.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                                loadAccounts();
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(AccountManagementPanel.this, "Lỗi khi xóa tài khoản: " + e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản muốn xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}