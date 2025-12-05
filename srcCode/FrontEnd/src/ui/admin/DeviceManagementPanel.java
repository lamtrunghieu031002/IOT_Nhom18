package ui.admin;

import model.Device;
import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class DeviceManagementPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Địa chỉ MAC", "Tên thiết bị", "Model", "Ngày thêm", "Trạng thái"};

    public DeviceManagementPanel() {

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250)); // tone xám hiện đại

        // ======================================================
        //                    HEADER BUTTONS
        // ======================================================
        JButton addButton = createStyledButton("➕ Thêm Thiết bị", new Color(46, 204, 113));
        JButton deleteButton = createStyledButton("➖ Xóa Thiết bị", new Color(231, 76, 60));
        JButton refreshButton = createStyledButton("🔄 Tải lại", new Color(52, 152, 219));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // ======================================================
        //                    TABLE STYLING
        // ======================================================
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // khóa không cho edit
            }
        };

        deviceTable = new JTable(tableModel);
        deviceTable.setRowHeight(28);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceTable.setFillsViewportHeight(true);
        deviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceTable.setGridColor(new Color(220, 220, 220));

        // Header đẹp hơn
        JTableHeader header = deviceTable.getTableHeader();
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setOpaque(true);

        // Alternating rows
        deviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(new Color(41, 128, 185));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ======================================================
        //                  EVENT HANDLERS
        // ======================================================
        refreshButton.addActionListener(e -> loadDevices());
        deleteButton.addActionListener(e -> deleteSelectedDevice());
        addButton.addActionListener(e -> new AddDeviceDialog(this).setVisible(true));

        loadDevices();
    }

    // ======================================================
    //                  BUTTON STYLE FUNCTION
    // ======================================================
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Hover effect
        btn.addChangeListener(e -> {
            if (btn.getModel().isRollover())
                btn.setBackground(color.darker());
            else
                btn.setBackground(color);
        });

        return btn;
    }

    // ======================================================
    //                LOAD DEVICES
    // ======================================================
    public void loadDevices() {
        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllDevices();
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    tableModel.setRowCount(0);
                    for (Device d : devices) {
                        tableModel.addRow(new Object[]{
                                d.getMacAddress(),
                                d.getName(),
                                d.getModel(),
                                d.getDateAdded(),
                                d.getStatus()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                            "Lỗi khi tải danh sách thiết bị: " + e.getMessage(),
                            "Lỗi API",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ======================================================
    //                DELETE DEVICE
    // ======================================================
    private void deleteSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thiết bị muốn xóa.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String macAddress = (String) tableModel.getValueAt(selectedRow, 0);

        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa thiết bị MAC: " + macAddress + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ApiClient.getInstance().deleteDevice(macAddress);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                                    "Đã xóa thành công!", "Hoàn tất",
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadDevices();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                                "Lỗi khi xóa: " + e.getMessage(),
                                "Lỗi API", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}
