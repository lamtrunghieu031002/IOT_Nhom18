package ui.admin;

import model.Device;
import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ScanDeviceDialog extends JDialog {

    private DeviceManagementPanel parentPanel;
    private DefaultListModel<DeviceListItem> listModel;
    private JList<DeviceListItem> deviceList;
    private JLabel statusLabel;
    private JButton selectButton;
    private JButton rescanButton;
    private JButton manualButton; // Nút nhập tay nếu không quét được

    public ScanDeviceDialog(DeviceManagementPanel parent) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Quét thiết bị Bluetooth", true);
        this.parentPanel = parent;

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- HEADER ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        statusLabel = new JLabel("Đang chuẩn bị quét...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(41, 128, 185));
        topPanel.add(statusLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- LIST ---
        listModel = new DefaultListModel<>();
        deviceList = new JList<>(listModel);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        deviceList.setFixedCellHeight(30);

        // Custom Renderer để hiển thị đẹp hơn
        deviceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                DeviceListItem item = (DeviceListItem) value;
                setText(" " + item.getDisplayString());
                if (isSelected) {
                    setBackground(new Color(52, 152, 219));
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        add(new JScrollPane(deviceList), BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        manualButton = new JButton("Nhập thủ công");
        rescanButton = new JButton("Quét lại");
        selectButton = new JButton("Chọn thiết bị");
        selectButton.setBackground(new Color(46, 204, 113));
        selectButton.setForeground(Color.WHITE);
        selectButton.setEnabled(false); // Chỉ enable khi chọn item

        buttonPanel.add(manualButton);
        buttonPanel.add(rescanButton);
        buttonPanel.add(selectButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- EVENTS ---
        deviceList.addListSelectionListener(e -> {
            selectButton.setEnabled(!deviceList.isSelectionEmpty());
        });

        rescanButton.addActionListener(e -> startScanning());

        selectButton.addActionListener(e -> {
            DeviceListItem selected = deviceList.getSelectedValue();
            if (selected != null) {
                openAddDialog(selected.macAddress);
            }
        });

        manualButton.addActionListener(e -> openAddDialog("")); // Mở form trống

        // Bắt đầu quét ngay khi mở dialog
        startScanning();
    }

    private void startScanning() {
        listModel.clear();
        selectButton.setEnabled(false);
        rescanButton.setEnabled(false);
        manualButton.setEnabled(false);
        statusLabel.setText("Đang quét thiết bị xung quanh... (Vui lòng đợi)");
        statusLabel.setForeground(new Color(230, 126, 34)); // Màu cam

        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return ApiClient.getInstance().scanDevices();
            }

            @Override
            protected void done() {
                rescanButton.setEnabled(true);
                manualButton.setEnabled(true);
                try {
                    List<Device> devices = get();
                    if (devices.isEmpty()) {
                        statusLabel.setText("Không tìm thấy thiết bị nào.");
                        statusLabel.setForeground(Color.RED);
                    } else {
                        statusLabel.setText("Tìm thấy " + devices.size() + " thiết bị.");
                        statusLabel.setForeground(new Color(39, 174, 96)); // Xanh lá

                        for (Device d : devices) {
                            listModel.addElement(new DeviceListItem(d));
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("Lỗi quét: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void openAddDialog(String macAddress) {
        this.dispose(); // Đóng dialog quét
        // Mở form thêm mới với dữ liệu đã điền
        new AddDeviceDialog(parentPanel, macAddress).setVisible(true);
    }

    // Helper class để hiển thị trong JList
    private static class DeviceListItem {
        String macAddress;
        String name;
        String status;

        public DeviceListItem(Device d) {
            this.macAddress = d.getDeviceId();
            this.name = d.getName();
            this.status = d.getStatus();
        }

        public String getDisplayString() {
            // Format: [MAC] Tên - (Trạng thái)
            return String.format("[%s] %-15s (%s)", macAddress, name, status);
        }
    }
}