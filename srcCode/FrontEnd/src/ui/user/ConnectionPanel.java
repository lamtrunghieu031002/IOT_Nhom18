package ui.user;

import model.Device;
import service.ApiClient;
import service.BluetoothClientScanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Device ID", "Tên thiết bị", "Model", "Trạng thái", "Ngày tạo"};

    // Lưu danh sách thiết bị hiện tại để mapping từ dòng chọn -> object Device
    private List<Device> currentDeviceList = new ArrayList<>();

    private JButton btnScan;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JLabel statusLabel;

    public ConnectionPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ================= HEADER ====================
        JLabel header = new JLabel("🔵 QUẢN LÝ KẾT NỐI BLUETOOTH", SwingConstants.CENTER);
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
        deviceTable.setRowHeight(30);
        deviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Renderer để highlight trạng thái
        deviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                // Logic màu nền xen kẽ
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                }

                // Highlight chữ "Connected" màu xanh lá
                if (col == 3 && value != null && value.toString().equalsIgnoreCase("Connected")) {
                    c.setForeground(new Color(39, 174, 96));
                    if(isSelected) c.setForeground(Color.WHITE); // Nếu đang chọn thì vẫn chữ trắng
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });

        // Sự kiện chọn dòng để bật tắt nút Connect/Disconnect
        deviceTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.setBorder(new EmptyBorder(10, 15, 0, 15));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ================== FOOTER (BUTTONS) =====================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonContainer.setOpaque(false);

        btnScan = createStyledButton("📡 Quét thiết bị", new Color(46, 204, 113));
        btnConnect = createStyledButton("🔗 Kết nối", new Color(52, 152, 219));
        btnDisconnect = createStyledButton("❌ Ngắt kết nối", new Color(231, 76, 60));

        btnScan.addActionListener(e -> scanBluetoothDevices());
        btnConnect.addActionListener(e -> connectDevice());
        btnDisconnect.addActionListener(e -> disconnectDevice());

        buttonContainer.add(btnScan);
        buttonContainer.add(btnConnect);
        buttonContainer.add(btnDisconnect);

        bottomPanel.add(buttonContainer, BorderLayout.CENTER);

        statusLabel = new JLabel("Hệ thống sẵn sàng.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Khởi tạo trạng thái nút ban đầu
        updateButtonState();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateButtonState() {
        boolean hasSelection = deviceTable.getSelectedRow() != -1;
        boolean isServiceConnected = BluetoothClientScanner.getInstance().isConnected();

        btnConnect.setEnabled(hasSelection && !isServiceConnected);
        btnDisconnect.setEnabled(isServiceConnected); // Chỉ cho ngắt khi đang kết nối thật

        // Nếu đã kết nối, hiển thị trên label
        if (isServiceConnected) {
            String deviceName = BluetoothClientScanner.getInstance().getCurrentDeviceName();
            statusLabel.setText("Đang kết nối với: " + deviceName);
            statusLabel.setForeground(new Color(39, 174, 96));
        } else {
            // Nếu chưa kết nối, label hiển thị bình thường
            if (!statusLabel.getText().contains("Quét")) { // Giữ nguyên nếu đang scan
                statusLabel.setText("Chưa kết nối.");
                statusLabel.setForeground(Color.GRAY);
            }
        }
    }

    // ================= CHỨC NĂNG THỰC TẾ =================

    // 1. Quét Thiết bị (Kết hợp ApiClient và Scanner)
    private void scanBluetoothDevices() {
        btnScan.setEnabled(false);
        btnConnect.setEnabled(false);
        btnDisconnect.setEnabled(false);
        statusLabel.setText("⏳ Đang quét môi trường xung quanh (Vui lòng đợi)...");
        statusLabel.setForeground(new Color(230, 126, 34));

        tableModel.setRowCount(0);
        currentDeviceList.clear();

        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                // Bước 1: Gọi API Client để lấy danh sách thiết bị HỢP LỆ (đã đăng ký DB)
                // Hàm này bên trong có thể đã gọi BluetoothClientScanner.scan() để lấy MAC thật
                // và so sánh với DB.
                return ApiClient.getInstance().scanAndCheckDevices();
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    currentDeviceList = devices; // Lưu vào list cục bộ

                    if (devices == null || devices.isEmpty()) {
                        statusLabel.setText("Không tìm thấy thiết bị nào khớp với hệ thống.");
                        statusLabel.setForeground(Color.RED);
                    } else {
                        for (Device d : devices) {
                            // Kiểm tra xem thiết bị này có phải là thiết bị đang kết nối không
                            String displayStatus = d.getStatus();
                            BluetoothClientScanner scanner = BluetoothClientScanner.getInstance();
                            if (scanner.isConnected() &&
                                    scanner.getCurrentDeviceName().equals(d.getName())) {
                                displayStatus = "Connected";
                            }

                            tableModel.addRow(new Object[]{
                                    d.getDeviceId(), d.getName(), d.getModel(), displayStatus, d.getCreatedAt()
                            });
                        }
                        statusLabel.setText("Tìm thấy " + devices.size() + " thiết bị.");
                        statusLabel.setForeground(new Color(46, 204, 113));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Lỗi khi quét: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ConnectionPanel.this,
                            "Lỗi quét: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnScan.setEnabled(true);
                    updateButtonState();
                }
            }
        }.execute();
    }

    // 2. Kết nối (GỌI THỰC TẾ VÀO SERVICE)
    private void connectDevice() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow == -1) return;

        // Lấy Object Device từ list tương ứng với dòng chọn
        Device device = currentDeviceList.get(selectedRow);

        // GIẢ ĐỊNH: Class Device có phương thức getMacAddress().
        // Nếu chưa có, bạn cần thêm field 'macAddress' vào model.Device
        String macAddress = device.getDeviceId();
        String deviceName = device.getName();

        if (macAddress == null || macAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Thiết bị này không có địa chỉ MAC trong dữ liệu!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnConnect.setEnabled(false); // Chặn bấm liên tục
        statusLabel.setText("Đang kết nối tới " + deviceName + "...");
        statusLabel.setForeground(new Color(52, 152, 219));

        // Dùng SwingWorker để không treo giao diện khi socket đang connect
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Gọi vào Service Singleton thực tế
                BluetoothClientScanner.getInstance().connect(macAddress, deviceName);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Kiểm tra ngoại lệ

                    // Thành công
                    tableModel.setValueAt("Connected", selectedRow, 3);
                    statusLabel.setText("Đã kết nối thành công: " + deviceName);
                    statusLabel.setForeground(new Color(39, 174, 96));
                    JOptionPane.showMessageDialog(ConnectionPanel.this,
                            "Kết nối thành công với " + deviceName, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    // Thất bại
                    e.printStackTrace();
                    statusLabel.setText("Kết nối thất bại!");
                    statusLabel.setForeground(Color.RED);
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ConnectionPanel.this,
                            "Không thể kết nối:\n" + msg, "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                } finally {
                    updateButtonState();
                }
            }
        }.execute();
    }

    // 3. Ngắt kết nối (GỌI THỰC TẾ VÀO SERVICE)
    private void disconnectDevice() {
        if (!BluetoothClientScanner.getInstance().isConnected()) {
            return;
        }

        String currentName = BluetoothClientScanner.getInstance().getCurrentDeviceName();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn muốn ngắt kết nối với " + currentName + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Thực hiện ngắt kết nối ngay lập tức (thường nhanh nên không cần SwingWorker,
            // nhưng nếu kỹ tính có thể dùng)
            BluetoothClientScanner.getInstance().disconnect();

            // Cập nhật lại UI
            statusLabel.setText("Đã ngắt kết nối.");
            statusLabel.setForeground(Color.RED);

            // Reset trạng thái trên bảng (tìm dòng đang "Connected" để sửa lại)
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if ("Connected".equals(tableModel.getValueAt(i, 3))) {
                    tableModel.setValueAt("Available", i, 3);
                }
            }

            updateButtonState();
        }
    }
}