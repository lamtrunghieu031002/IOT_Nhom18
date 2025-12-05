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
    private final String[] columnNames = {"Địa chỉ MAC", "Tên thiết bị", "Model", "Kết nối"};

    private JButton connectButton;
    private JLabel loadingLabel;

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
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa ô
            }
        };

        deviceTable = new JTable(tableModel);
        deviceTable.setRowHeight(28);
        deviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        deviceTable.getTableHeader().setBackground(new Color(230, 230, 230));
        deviceTable.setGridColor(new Color(220, 220, 220));

        /// Thiết lập màu chọn mặc định của table (thích hợp cho look & feel)
        deviceTable.setSelectionBackground(new Color(52, 152, 219));
        deviceTable.setSelectionForeground(Color.WHITE);
        deviceTable.setRowSelectionAllowed(true);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer an toàn: luôn set cả background lẫn foreground
        deviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    // màu nền xen kẽ
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    // phục hồi màu chữ mặc định của table (tránh để lại màu cũ)
                    c.setForeground(table.getForeground());
                }

                // canh lề/format nếu cần
                setBorder(null);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.setBorder(new EmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        // ================== FOOTER =====================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));

        connectButton = new JButton("📡 Bật Bluetooth & Quét thiết bị");
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectButton.setBackground(new Color(46, 204, 113));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Khi hover đổi màu
        connectButton.addChangeListener(e -> {
            if (connectButton.getModel().isRollover()) {
                connectButton.setBackground(new Color(39, 174, 96));
            } else {
                connectButton.setBackground(new Color(46, 204, 113));
            }
        });

        bottomPanel.add(connectButton, BorderLayout.CENTER);

        loadingLabel = new JLabel("", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loadingLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        loadingLabel.setForeground(new Color(100, 100, 100));

        bottomPanel.add(loadingLabel, BorderLayout.SOUTH);

        connectButton.addActionListener(e -> scanAndLoadDevices());

        add(bottomPanel, BorderLayout.SOUTH);

        // Auto load khi khởi động
        scanAndLoadDevices();
    }

    private void scanAndLoadDevices() {
        connectButton.setEnabled(false);
        loadingLabel.setText("⏳ Đang quét thiết bị Bluetooth...");

        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                Thread.sleep(1000); // hiệu ứng loading cho mượt
                return ApiClient.getInstance().getAllDevices();
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    tableModel.setRowCount(0);

                    for (Device d : devices) {
                        tableModel.addRow(new Object[]{
                                d.getMacAddress(), d.getName(), d.getModel(), "Kết nối"
                        });
                    }

                    loadingLabel.setText("✔ Tìm thấy " + devices.size() + " thiết bị");

                } catch (Exception e) {
                    loadingLabel.setText("❌ Lỗi khi quét thiết bị!");
                    JOptionPane.showMessageDialog(ConnectionPanel.this,
                            "Lỗi khi quét thiết bị: " + e.getMessage(), "Lỗi Bluetooth/API",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    connectButton.setEnabled(true);
                }
            }
        }.execute();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Connection Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);

            frame.add(new ConnectionPanel()); // ← Gắn panel vào frame

            frame.setVisible(true);
        });
    }
}
