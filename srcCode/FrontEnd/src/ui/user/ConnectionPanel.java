package ui.user;

import model.Device;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ConnectionPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Địa chỉ MAC", "Tên", "Model", "Kết nối"};

    public ConnectionPanel() {
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("GIAO DIỆN KẾT NỐI THIẾT BỊ BLUETOOTH", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 16));

        tableModel = new DefaultTableModel(columnNames, 0);
        deviceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(deviceTable);

        JButton connectButton = new JButton("📡 Bật Bluetooth và Quét Thiết bị");

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(connectButton, BorderLayout.SOUTH);

        connectButton.addActionListener(e -> scanAndLoadDevices());

        // Thêm sự kiện click nút "Kết nối" trong bảng (cần Custom Renderer/Editor)
        // Để đơn giản, giả định người dùng nhấn nút Kết nối chính

        // Giả lập tải thiết bị khi vào panel
        scanAndLoadDevices();
    }

    private void scanAndLoadDevices() {
        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                // Giả định API Backend có phương thức scanDevices()
                // return ApiClient.getInstance().scanDevices();
                return ApiClient.getInstance().getAllDevices(); // Sử dụng tạm API lấy thiết bị đã đăng ký
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    tableModel.setRowCount(0);
                    for (Device d : devices) {
                        // Thêm nút kết nối giả định
                        tableModel.addRow(new Object[]{
                                d.getMacAddress(), d.getName(), d.getModel(), "Ấn Kết nối"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ConnectionPanel.this,
                            "Lỗi khi quét thiết bị: " + e.getMessage(), "Lỗi Bluetooth/API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}