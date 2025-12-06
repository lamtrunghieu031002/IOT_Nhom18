package ui.admin;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;

public class AddDeviceDialog extends JDialog {

    private DeviceManagementPanel parentPanel;
    private JTextField deviceIdField, nameField, modelField;

    public AddDeviceDialog(DeviceManagementPanel parent) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Thêm Thiết bị", true);
        this.parentPanel = parent;

        setLayout(new BorderLayout(10, 10));
        setSize(420, 260);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        deviceIdField = new JTextField(20);
        nameField = new JTextField(20);
        modelField = new JTextField(20);

        formPanel.add(new JLabel("Device ID:"));
        formPanel.add(deviceIdField);

        formPanel.add(new JLabel("Tên thiết bị:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Model:"));
        formPanel.add(modelField);

        add(formPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Thêm");
        JButton backButton = new JButton("Hủy");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addDevice());
        backButton.addActionListener(e -> dispose());
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
