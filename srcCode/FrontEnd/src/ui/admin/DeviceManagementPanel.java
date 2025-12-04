package ui.admin;

import model.Device;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeviceManagementPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Địa chỉ MAC", "Tên thiết bị", "Model", "Ngày thêm", "Trạng thái"};

    public DeviceManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(columnNames, 0);
        deviceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(deviceTable);

        JButton addButton = new JButton("➕ Thêm Thiết bị");
        JButton deleteButton = new JButton("➖ Xóa Thiết bị");
        JButton refreshButton = new JButton("🔄 Tải lại");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> loadDevices());
        deleteButton.addActionListener(e -> deleteSelectedDevice());
        addButton.addActionListener(e -> new AddDeviceDialog(this).setVisible(true));

        loadDevices();
    }

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
                                d.getMacAddress(), d.getName(), d.getModel(), d.getDateAdded(), d.getStatus()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                            "Lỗi khi tải danh sách thiết bị: " + e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void deleteSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow != -1) {
            String macAddress = (String) tableModel.getValueAt(selectedRow, 0);

            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa thiết bị MAC: " + macAddress + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return ApiClient.getInstance().deleteDevice(macAddress);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(DeviceManagementPanel.this, "Đã xóa thành công.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                                loadDevices();
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(DeviceManagementPanel.this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thiết bị muốn xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}
