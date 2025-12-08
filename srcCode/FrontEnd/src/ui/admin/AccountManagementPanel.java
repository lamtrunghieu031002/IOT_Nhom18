package ui.admin;

import model.User;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AccountManagementPanel extends JPanel {

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames =
            {"ID", "Username", "Họ tên", "Email", "Role", "Ngày tạo"};

    public AccountManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        setUIFont(new Font("Segoe UI", Font.PLAIN, 14));

        // === TABLE MODEL ===
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa bảng
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setRowHeight(28);
        accountTable.setShowGrid(true);
        accountTable.setGridColor(new Color(220, 220, 220));
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Căn giữa ID + Role
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        accountTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        accountTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // === HEADER STYLE ===
        JTableHeader header = accountTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        header.setBackground(new Color(0, 122, 204));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // === BUTTONS ===
        JButton addButton = buildButton("Thêm Tài khoản", new Color(46, 204, 113));
        JButton editButton = buildButton("Cập nhật Tài khoản", new Color(52, 152, 219));
        JButton deleteButton = buildButton("Xóa Tài khoản", new Color(231, 76, 60));
        JButton refreshButton = buildButton("Tải lại", new Color(52, 152, 219));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Event
        addButton.addActionListener(e -> new AddAccountDialog(this).setVisible(true));
        editButton.addActionListener(e -> editSelectedAccount());
        deleteButton.addActionListener(e -> deleteSelectedAccount());
        refreshButton.addActionListener(e -> loadAccounts());

        loadAccounts();
    }

    // ===================================================================
    // Tải danh sách tài khoản
    // ===================================================================
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
                                user.getId(),
                                user.getUsername(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole(),
                                user.getDateCreated()
                        });
                    }

                    resizeTableColumns(accountTable);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AccountManagementPanel.this,
                            "Lỗi khi tải danh sách tài khoản: " + e.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void editSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản muốn cập nhật!",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Number idNumber = (Number) tableModel.getValueAt(selectedRow, 0);
        long userId = idNumber.longValue();  // hoặc int userId = idNumber.intValue();
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String fullName = (String) tableModel.getValueAt(selectedRow, 2);
        String email = (String) tableModel.getValueAt(selectedRow, 3);
        String roleVi = (String) tableModel.getValueAt(selectedRow, 4); // "Quản lý" hoặc "Người đo"

        new EditAccountDialog(this, userId, username, fullName, email, roleVi).setVisible(true);
    }

    // ===================================================================
    // Xóa tài khoản
    // ===================================================================
    private void deleteSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản muốn xóa.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa tài khoản ID: " + userId + " (" + username + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ApiClient.getInstance().deleteUser(userId);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(AccountManagementPanel.this,
                                    "Đã xóa tài khoản thành công.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            loadAccounts();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AccountManagementPanel.this,
                                "Lỗi khi xóa tài khoản: " + e.getMessage(),
                                "Lỗi API", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // ===================================================================
    // Utils: Tạo nút đẹp
    // ===================================================================
    private JButton buildButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Tự resize cột
    private void resizeTableColumns(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int width = 75; // min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                width = Math.max(comp.getPreferredSize().width + 20, width);
            }
            columnModel.getColumn(col).setPreferredWidth(width);
        }
    }

    // Set font UI toàn panel
    private static void setUIFont(Font f) {
        UIManager.getDefaults().entrySet().forEach(entry -> {
            Object value = entry.getValue();
            if (value != null && value instanceof Font) {
                UIManager.put(entry.getKey(), f);
            }
        });
    }
}
