package ui.user;

import model.Device;
import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ConnectionPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Device ID", "Tên thiết bị", "Model", "Trạng thái", "Ngày tạo"};

    private JButton scanButton;
    private JLabel statusLabel;

    public ConnectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ================= HEADER ====================
        JLabel header = new JLabel("🔵 KẾT NỐI THIẾT BỊ BLUETOOTH", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(52, 152, 219));
        header.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(header, BorderLayout.NORTH);

        // ================ TABLE ======================
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        deviceTable = new JTable(tableModel);
        deviceTable.setRowHeight(28);
        deviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        deviceTable.getTableHeader().setBackground(new Color(230, 230, 230));

        deviceTable.setSelectionBackground(new Color(52, 152, 219));
        deviceTable.setSelectionForeground(Color.WHITE);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Renderer để row chẵn lẻ đẹp mắt
        deviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.setBorder(new EmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        // ================== FOOTER =====================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));

        scanButton = new JButton("📡 Quét & Kiểm tra Bluetooth");
        scanButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scanButton.setBackground(new Color(46, 204, 113));
        scanButton.setForeground(Color.WHITE);
        scanButton.setFocusPainted(false);
        scanButton.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Thêm sự kiện bấm nút
        scanButton.addActionListener(e -> scanBluetoothDevices());

        bottomPanel.add(scanButton, BorderLayout.CENTER);

        statusLabel = new JLabel("Nhấn nút để bắt đầu quét thiết bị xung quanh...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        statusLabel.setForeground(new Color(100, 100, 100));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void scanBluetoothDevices() {
        scanButton.setEnabled(false);
        scanButton.setBackground(Color.GRAY);
        statusLabel.setText("⏳ Đang quét môi trường xung quanh (Client Scanning)...");
        statusLabel.setForeground(new Color(230, 126, 34));

        tableModel.setRowCount(0);

        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return ApiClient.getInstance().scanAndCheckDevices();
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();

                    if (devices.isEmpty()) {
                        statusLabel.setText("Không tìm thấy thiết bị nào khớp với hệ thống.");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(ConnectionPanel.this,
                                "Đã quét xong nhưng không có thiết bị nào trong danh sách được đăng ký trên Server.",
                                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        for (Device d : devices) {
                            tableModel.addRow(new Object[]{
                                    d.getDeviceId(), d.getName(), d.getModel(), d.getStatus(), d.getCreatedAt()
                            });
                        }
                        statusLabel.setText("Tìm thấy " + devices.size() + " thiết bị hợp lệ.");
                        statusLabel.setForeground(new Color(46, 204, 113));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String msg = ex.getMessage();
                    statusLabel.setText("Lỗi: " + msg);
                    JOptionPane.showMessageDialog(ConnectionPanel.this, "Lỗi: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    scanButton.setEnabled(true);
                    scanButton.setBackground(new Color(46, 204, 113));
                }
            }
        }.execute();
    }

    // Hàm Main để test nhanh giao diện này
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Bluetooth Scan Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new ConnectionPanel());
            frame.setVisible(true);
        });
    }
}