package ui.admin;

import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddDeviceDialog extends JDialog {

    private DeviceManagementPanel parentPanel;
    private JTextField deviceIdField, nameField, modelField;

    // Constructor mới nhận thêm macAddress
    public AddDeviceDialog(DeviceManagementPanel parent, String prefilledMac) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Thêm Thiết bị Mới", true);
        this.parentPanel = parent;

        setLayout(new BorderLayout(15, 15));
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Panel chính
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        // 1. Device ID
        JPanel p1 = new JPanel(new BorderLayout(5, 5));
        p1.add(new JLabel("Mã thiết bị (MAC Address):"), BorderLayout.NORTH);
        deviceIdField = new JTextField(20);
        deviceIdField.setFont(new Font("Monospaced", Font.BOLD, 14));

        // LOGIC TỰ ĐỘNG ĐIỀN
        if (prefilledMac != null && !prefilledMac.isEmpty()) {
            deviceIdField.setText(prefilledMac);
            deviceIdField.setEditable(false); // Không cho sửa để tránh sai lệch
            deviceIdField.setBackground(new Color(230, 230, 230)); // Màu xám báo hiệu read-only
            p1.add(new JLabel("(Đã lấy từ Bluetooth)"), BorderLayout.EAST);
        }
        p1.add(deviceIdField, BorderLayout.CENTER);

        // 2. Tên thiết bị
        JPanel p2 = new JPanel(new BorderLayout(5, 5));
        p2.add(new JLabel("Tên thiết bị gợi nhớ:"), BorderLayout.NORTH);
        nameField = new JTextField(20);
        p2.add(nameField, BorderLayout.CENTER);

        // 3. Model
        JPanel p3 = new JPanel(new BorderLayout(5, 5));
        p3.add(new JLabel("Model / Phiên bản:"), BorderLayout.NORTH);
        modelField = new JTextField(20);
        modelField.setText("ESP32-MQ3-V1"); // Gợi ý mặc định
        p3.add(modelField, BorderLayout.CENTER);

        mainPanel.add(p1);
        mainPanel.add(p2);
        mainPanel.add(p3);
        add(mainPanel, BorderLayout.CENTER);

        // --- BUTTONS ---
        JButton addButton = new JButton("Lưu Thiết Bị");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);

        JButton backButton = new JButton("Hủy bỏ");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        buttonPanel.add(backButton);
        buttonPanel.add(addButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addDevice());
        backButton.addActionListener(e -> dispose());
    }

    // Giữ lại constructor cũ để tương thích (nếu cần), gọi sang constructor mới với chuỗi rỗng
    public AddDeviceDialog(DeviceManagementPanel parent) {
        this(parent, "");
    }

    private void addDevice() {
        String deviceId = deviceIdField.getText().trim();
        String name = nameField.getText().trim();
        String model = modelField.getText().trim();

        if (deviceId.isEmpty() || name.isEmpty() || model.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thiếu dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.getInstance().addDevice(deviceId, name, model);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AddDeviceDialog.this,
                                "Thiết bị đã đăng ký thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);

                        parentPanel.loadDevices();  // load lại bảng
                        dispose();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AddDeviceDialog.this,
                            "Lỗi: " + e.getMessage(),
                            "API Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}