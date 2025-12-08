package ui.user;

import service.ApiClient;
import service.BluetoothClientScanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasurementPanel extends JPanel implements BluetoothClientScanner.BluetoothDataListener {

    private JLabel resultLabel;
    private JLabel violationLabel;
    private JLabel statusConnectionLabel;
    private JTextField nameField, ageField, cccdField, hometownField;
    private JButton submitButton, redoButton;

    private Double currentMeasurementResult = null;
    private String currentViolationLevel = "Chưa xác định";

    public MeasurementPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Đăng ký lắng nghe dữ liệu từ Bluetooth Scanner
        BluetoothClientScanner.getInstance().addDataListener(this);

        // ================= HEADER ====================
        JPanel resultPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        resultPanel.setBackground(new Color(52, 152, 219));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        statusConnectionLabel = new JLabel("Đang kiểm tra kết nối...", SwingConstants.CENTER);
        statusConnectionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusConnectionLabel.setOpaque(true);
        statusConnectionLabel.setBackground(new Color(41, 128, 185));
        statusConnectionLabel.setForeground(Color.WHITE);

        resultLabel = new JLabel("Đang chờ dữ liệu...", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        resultLabel.setForeground(Color.WHITE);

        violationLabel = new JLabel("Mức vi phạm: ---", SwingConstants.CENTER);
        violationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        violationLabel.setForeground(new Color(235, 235, 235));

        resultPanel.add(statusConnectionLabel);
        resultPanel.add(resultLabel);
        resultPanel.add(violationLabel);

        add(resultPanel, BorderLayout.NORTH);

        // ================= FORM NHẬP LIỆU ====================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JPanel fields = new JPanel(new GridLayout(4, 2, 12, 15));
        fields.setBackground(Color.WHITE);

        nameField = new JTextField(20);
        ageField = new JTextField(20);
        cccdField = new JTextField(20);
        hometownField = new JTextField(20);

        fields.add(new JLabel("Họ và tên:"));
        fields.add(nameField);
        fields.add(new JLabel("Tuổi:"));
        fields.add(ageField);
        fields.add(new JLabel("CCCD:"));
        fields.add(cccdField);
        fields.add(new JLabel("Quê quán:"));
        fields.add(hometownField);

        formPanel.add(fields);
        add(formPanel, BorderLayout.CENTER);

        // ================= BUTTONS ====================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        submitButton = new JButton("Gửi lên Server");
        redoButton = new JButton("Yêu cầu đo lại");

        styleButton(submitButton, new Color(46, 204, 113), new Color(39, 174, 96));
        styleButton(redoButton, new Color(230, 126, 34), new Color(211, 84, 0));

        submitButton.setEnabled(false);
        redoButton.setEnabled(false); // Mặc định tắt nếu chưa kết nối

        buttonPanel.add(submitButton);
        buttonPanel.add(redoButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        submitButton.addActionListener(e -> submitData());
        redoButton.addActionListener(e -> triggerRedo());

        // ================= TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI =================
        // Khi tab này hiển thị, cập nhật ngay trạng thái kết nối
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateConnectionUI();
            }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });

        // Chạy lần đầu
        updateConnectionUI();
    }

    // Hàm cập nhật giao diện dựa trên trạng thái Scanner hiện tại
    private void updateConnectionUI() {
        BluetoothClientScanner scanner = BluetoothClientScanner.getInstance();
        boolean isConnected = scanner.isConnected();

        if (isConnected) {
            // Lấy tên thiết bị trực tiếp từ Scanner (Scanner bạn gửi trả về String)
            String deviceName = scanner.getCurrentDeviceName();

            statusConnectionLabel.setText("Đang kết nối với: " + (deviceName != null ? deviceName : "Unknown"));
            statusConnectionLabel.setBackground(new Color(39, 174, 96)); // Xanh lá
            redoButton.setEnabled(true);
        } else {
            statusConnectionLabel.setText("Chưa kết nối thiết bị Bluetooth");
            statusConnectionLabel.setBackground(new Color(231, 76, 60)); // Đỏ
            redoButton.setEnabled(false);
        }
    }

    // ================= XỬ LÝ DỮ LIỆU NHẬN ĐƯỢC =================
    @Override
    public void onDataReceived(String rawData) {
        SwingUtilities.invokeLater(() -> {
            // 1. Nhận tín hiệu bắt đầu đo
            if (rawData.contains("StartMesuring")) {
                resultLabel.setText("Đang đo... (Thổi vào cảm biến)");
                resultLabel.setForeground(Color.YELLOW);
                violationLabel.setText("Vui lòng đợi 5 giây...");
                submitButton.setEnabled(false);
            }
            // 2. Nhận kết quả đo
            else if (rawData.startsWith("GetAlcohol|")) {
                parseAndDisplayResult(rawData);
            }
        });
    }

    private void parseAndDisplayResult(String rawData) {
        try {
            // Tách chuỗi JSON sau dấu |
            String jsonPart = rawData.substring(rawData.indexOf("|") + 1);

            // Dùng Regex lấy giá trị
            Pattern pLevel = Pattern.compile("\"alcohol_level\":([0-9.]+)");
            Matcher mLevel = pLevel.matcher(jsonPart);

            Pattern pStatus = Pattern.compile("\"status\":\"([^\"]+)\"");
            Matcher mStatus = pStatus.matcher(jsonPart);

            if (mLevel.find() && mStatus.find()) {
                double level = Double.parseDouble(mLevel.group(1));
                String status = mStatus.group(1);

                this.currentMeasurementResult = level;

                // Hiển thị cảnh báo
                if (status.equalsIgnoreCase("HIGH")) {
                    this.currentViolationLevel = "Vượt mức (Cảnh báo)";
                    resultLabel.setForeground(new Color(255, 100, 100)); // Đỏ nhạt
                } else {
                    this.currentViolationLevel = "Bình thường (An toàn)";
                    resultLabel.setForeground(Color.WHITE);
                }

                resultLabel.setText("Kết quả: " + String.format("%.3f", level) + " mg/L");
                violationLabel.setText(this.currentViolationLevel);

                submitButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Đã nhận kết quả đo mới!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Lỗi đọc dữ liệu!");
        }
    }

    // ================= CÁC CHỨC NĂNG KHÁC =================

    private void triggerRedo() {
        if (!BluetoothClientScanner.getInstance().isConnected()) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối thiết bị!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Reset giao diện
        resultLabel.setText("Đang yêu cầu đo lại...");
        resultLabel.setForeground(Color.WHITE);
        violationLabel.setText("---");
        submitButton.setEnabled(false);

        // Gửi lệnh xuống ESP32 (Hàm này cần được thêm vào Scanner ở BƯỚC 1)
        BluetoothClientScanner.getInstance().sendData("GetAlcohol");
    }

    private void submitData() {
        if (currentMeasurementResult == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", nameField.getText());
        data.put("age", ageField.getText());
        data.put("cccd", cccdField.getText());
        data.put("hometown", hometownField.getText());
        data.put("result", currentMeasurementResult);
        data.put("violation", currentViolationLevel);
        data.put("timestamp", System.currentTimeMillis());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.getInstance().submitMeasurement(data);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MeasurementPanel.this, "Lưu dữ liệu thành công!");
                        nameField.setText("");
                        ageField.setText("");
                        cccdField.setText("");
                        hometownField.setText("");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MeasurementPanel.this, "Lỗi gửi API: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Hủy đăng ký listener khi Panel bị đóng
    @Override
    public void removeNotify() {
        BluetoothClientScanner.getInstance().removeDataListener(this);
        super.removeNotify();
    }

    private void styleButton(JButton btn, Color normal, Color hover) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(normal); }
        });
    }
}