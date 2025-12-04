package ui.admin;


import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
public class AddDeviceDialog extends JDialog {

    private DeviceManagementPanel parentPanel;
    private JTextField macField, nameField, modelField;

    public AddDeviceDialog(DeviceManagementPanel parent) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Thêm Thông tin Thiết bị", true);
        this.parentPanel = parent;

        setLayout(new BorderLayout(10, 10));
        setSize(400, 250);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        macField = new JTextField(20);
        macField.setEditable(false); // Giả định MAC được quét và cố định
        nameField = new JTextField(20);
        modelField = new JTextField(20);

        // Giả lập MAC đã được quét
        macField.setText("AA:BB:CC:DD:12:34");

        formPanel.add(new JLabel("Địa chỉ MAC:"));
        formPanel.add(macField);
        formPanel.add(new JLabel("Tên:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Model:"));
        formPanel.add(modelField);

        add(formPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Thêm");
        JButton backButton = new JButton("Quay lại");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addDevice());
        backButton.addActionListener(e -> dispose());
    }

    private void addDevice() {
        Map<String, String> data = new HashMap<>();
        data.put("macAddress", macField.getText());
        data.put("name", nameField.getText());
        data.put("model", modelField.getText());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Giả định API Backend có endpoint đăng ký thiết bị mới
                // return ApiClient.getInstance().addDevice(data);
                Thread.sleep(1000);
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AddDeviceDialog.this, "Đăng ký thiết bị thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        parentPanel.loadDevices();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(AddDeviceDialog.this, "Lỗi khi thêm thiết bị.", "Lỗi API", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AddDeviceDialog.this, "Lỗi kết nối: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}