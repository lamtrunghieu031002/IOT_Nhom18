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

    // Cập nhật các biến nhập liệu
    private JTextField nameField, ageField, cccdField, locationField; // locationField thay cho hometownField
    private JComboBox<String> genderComboBox; // Thêm ComboBox giới tính

    private JButton submitButton, redoButton;

    private Double currentMeasurementResult = null;
    private String currentViolationLevel = "Chưa xác định";

    public MeasurementPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Đăng ký lắng nghe dữ liệu từ Bluetooth Scanner
        BluetoothClientScanner.getInstance().addDataListener(this);

        // ================= HEADER (GIỮ NGUYÊN) ====================
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

        // ================= FORM NHẬP LIỆU (ĐÃ SỬA) ====================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Tăng số hàng lên 5 để chứa thêm Giới tính
        JPanel fields = new JPanel(new GridLayout(5, 2, 12, 15));
        fields.setBackground(Color.WHITE);

        // Khởi tạo các component
        nameField = new JTextField(20);
        cccdField = new JTextField(20);

        // ComboBox Giới tính
        String[] genders = {"Nam", "Nữ", "Khác"};
        genderComboBox = new JComboBox<>(genders);
        genderComboBox.setBackground(Color.WHITE);

        ageField = new JTextField(20);
        locationField = new JTextField(20); // Thay thế hometownField

        // Thêm vào Panel theo thứ tự hợp lý
        fields.add(new JLabel("Họ và tên:"));
        fields.add(nameField);

        fields.add(new JLabel("CCCD:"));
        fields.add(cccdField);

        fields.add(new JLabel("Giới tính:")); // Mới
        fields.add(genderComboBox);

        fields.add(new JLabel("Tuổi:"));
        fields.add(ageField);

        fields.add(new JLabel("Nơi đo:")); // Đổi từ Quê quán thành Nơi đo
        fields.add(locationField);

        formPanel.add(fields);
        add(formPanel, BorderLayout.CENTER);

        // ================= BUTTONS (GIỮ NGUYÊN) ====================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        submitButton = new JButton("Gửi lên Server");
        redoButton = new JButton("Yêu cầu đo lại");

        styleButton(submitButton, new Color(46, 204, 113), new Color(39, 174, 96));
        styleButton(redoButton, new Color(230, 126, 34), new Color(211, 84, 0));

        submitButton.setEnabled(false);
        redoButton.setEnabled(false);

        buttonPanel.add(submitButton);
        buttonPanel.add(redoButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        submitButton.addActionListener(e -> submitData());
        redoButton.addActionListener(e -> triggerRedo());

        // ================= TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI =================
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateConnectionUI();
            }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });

        updateConnectionUI();
    }

    // Hàm cập nhật giao diện kết nối
    private void updateConnectionUI() {
        BluetoothClientScanner scanner = BluetoothClientScanner.getInstance();
        boolean isConnected = scanner.isConnected();

        if (isConnected) {
            String deviceName = scanner.getCurrentDeviceName();
            statusConnectionLabel.setText("Đang kết nối với: " + (deviceName != null ? deviceName : "Unknown"));
            statusConnectionLabel.setBackground(new Color(39, 174, 96));
            redoButton.setEnabled(true);
        } else {
            statusConnectionLabel.setText("Chưa kết nối thiết bị Bluetooth");
            statusConnectionLabel.setBackground(new Color(231, 76, 60));
            redoButton.setEnabled(false);
        }
    }

    // ================= XỬ LÝ DỮ LIỆU NHẬN ĐƯỢC =================
    @Override
    public void onDataReceived(String rawData) {
        SwingUtilities.invokeLater(() -> {
            if (rawData.contains("StartMesuring")) {
                resultLabel.setText("Đang đo... (Thổi vào cảm biến)");
                resultLabel.setForeground(Color.YELLOW);
                violationLabel.setText("Vui lòng đợi 5 giây...");
                submitButton.setEnabled(false);
            }
            else if (rawData.startsWith("GetAlcohol|")) {
                parseAndDisplayResult(rawData);
            }
        });
    }

    private void parseAndDisplayResult(String rawData) {
        try {
            String jsonPart = rawData.substring(rawData.indexOf("|") + 1);
            Pattern pLevel = Pattern.compile("\"alcohol_level\":([0-9.]+)");
            Matcher mLevel = pLevel.matcher(jsonPart);

            Pattern pStatus = Pattern.compile("\"status\":\"([^\"]+)\"");
            Matcher mStatus = pStatus.matcher(jsonPart);

            if (mLevel.find() && mStatus.find()) {
                double level = Double.parseDouble(mLevel.group(1));
                String status = mStatus.group(1);

                this.currentMeasurementResult = level;

                if (status.equalsIgnoreCase("HIGH")) {
                    this.currentViolationLevel = "Vượt mức (Cảnh báo)";
                    resultLabel.setForeground(new Color(255, 100, 100));
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
        resultLabel.setText("Đang yêu cầu đo lại...");
        resultLabel.setForeground(Color.WHITE);
        violationLabel.setText("---");
        submitButton.setEnabled(false);
        BluetoothClientScanner.getInstance().sendData("GetAlcohol");
    }

    private void submitData() {
        if (currentMeasurementResult == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kết quả đo!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Validate dữ liệu đầu vào cơ bản
        String name = nameField.getText().trim();
        String cccd = cccdField.getText().trim();
        String ageStr = ageField.getText().trim();
        String location = locationField.getText().trim();

        if (name.isEmpty() || cccd.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 16 || age > 100) {
                JOptionPane.showMessageDialog(this, "Tuổi phải từ 16 đến 100.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tuổi phải là một số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Lấy Device ID từ Bluetooth Scanner (Giả sử Scanner lưu địa chỉ MAC hoặc ID)
        String deviceId = BluetoothClientScanner.getInstance().getCurrentDevice().getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "UNKNOWN_DEVICE";
             JOptionPane.showMessageDialog(this, "Không xác định được ID thiết bị!", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // 3. Chuẩn bị Map dữ liệu
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", deviceId);                    // String (Required)
        data.put("subjectName", name);                     // String (Required)
        data.put("subjectId", cccd);                       // String (Required)
        data.put("subjectAge", age);                       // Integer (Required, min 16)
        data.put("subjectGender", genderComboBox.getSelectedItem().toString()); // String
        data.put("alcoholLevel", currentMeasurementResult); // Double
        data.put("location", location);                    // String
        data.put("locationCoordinates", "");               // String (Optional, để trống nếu chưa có GPS)

        // 4. Gửi dữ liệu qua Worker
        submitButton.setEnabled(false); // Khóa nút để tránh bấm nhiều lần

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Gọi ApiClient
                return ApiClient.getInstance().submitMeasurement(data);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MeasurementPanel.this, "Gửi dữ liệu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        // Reset form sau khi gửi thành công
                        nameField.setText("");
                        cccdField.setText("");
                        ageField.setText("");
                        locationField.setText("");
                        genderComboBox.setSelectedIndex(0);

                        // Reset kết quả đo để chuẩn bị cho lượt mới
                        resultLabel.setText("Đang chờ dữ liệu...");
                        violationLabel.setText("---");
                        currentMeasurementResult = null;
                    } else {
                        JOptionPane.showMessageDialog(MeasurementPanel.this, "Gửi thất bại! Vui lòng kiểm tra Server.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MeasurementPanel.this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

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