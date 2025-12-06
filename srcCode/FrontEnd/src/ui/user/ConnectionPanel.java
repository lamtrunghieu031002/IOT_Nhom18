package ui.user;

import model.Device;
import model.DevicePageResponse;
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
    private final String[] columnNames = {"Device ID", "Tên thiết bị", "Model", "Trạng thái"};

    private JButton loadButton;
    private JLabel loadingLabel;

    // Paging
    private int currentPage = 1;
    private final int pageSize = 10;

    public ConnectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ================= HEADER ====================
        JLabel header = new JLabel("🔵 DANH SÁCH THIẾT BỊ", SwingConstants.CENTER);
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

        loadButton = new JButton("🔄 Tải danh sách thiết bị");
        loadButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loadButton.setBackground(new Color(46, 204, 113));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setBorder(new EmptyBorder(10, 15, 10, 15));

        bottomPanel.add(loadButton, BorderLayout.CENTER);

        loadingLabel = new JLabel(" ", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loadingLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        loadingLabel.setForeground(new Color(100, 100, 100));
        bottomPanel.add(loadingLabel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadDevicePage());

        add(bottomPanel, BorderLayout.SOUTH);

        // Auto load page 0
        loadDevicePage();
    }

    private void loadDevicePage() {
        loadButton.setEnabled(false);
        loadingLabel.setText("⏳ Đang tải danh sách thiết bị...");

        new SwingWorker<DevicePageResponse, Void>() {
            @Override
            protected DevicePageResponse doInBackground() throws Exception {
                // status = ALL
                return ApiClient.getInstance().getDevicesPaging("ACTIVE", currentPage, pageSize);
            }

            @Override
            protected void done() {
                try {
                    DevicePageResponse page = get();
                    List<Device> devices = page.getDevices();

                    tableModel.setRowCount(0);
                    for (Device d : devices) {
                        tableModel.addRow(new Object[]{
                                d.getDeviceId(),
                                d.getName(),
                                d.getModel(),
                                d.getStatus()
                        });
                    }

                    loadingLabel.setText("✔ Trang " + (page.getPage() + 1)
                            + " / " + page.getTotalPages()
                            + " — Tổng " + page.getTotal() + " thiết bị");

                } catch (Exception ex) {
                    String msg = ex.getMessage();

                    if (msg.contains("401") || msg.contains("403")) {
                        JOptionPane.showMessageDialog(ConnectionPanel.this,
                                "❌ Bạn không có quyền hoặc token hết hạn!",
                                "Lỗi phân quyền",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ConnectionPanel.this,
                                "❌ Lỗi tải dữ liệu: " + msg,
                                "Lỗi API",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    loadingLabel.setText("❌ Lỗi tải thiết bị");
                } finally {
                    loadButton.setEnabled(true);
                }
            }
        }.execute();
    }

    // Test UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Connection Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new ConnectionPanel());
            frame.setVisible(true);
        });
    }
}
