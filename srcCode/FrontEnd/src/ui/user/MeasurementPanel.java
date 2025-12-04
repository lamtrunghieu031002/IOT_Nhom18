package ui.user;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MeasurementPanel extends JPanel {
    private JLabel resultLabel;
    private JLabel violationLabel;
    private JTextField nameField, ageField, cccdField, hometownField;
    private JButton submitButton, redoButton;

    private Double currentMeasurementResult = 0.0;
    private String currentViolationLevel = "Chưa có";

    public MeasurementPanel() {
        setLayout(new BorderLayout(15, 15));

        // --- Phần 1: Hiển thị Kết quả ---
        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        resultLabel = new JLabel("Kết quả đo: Đang chờ...", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        violationLabel = new JLabel("Mức vi phạm: Chưa xác định", SwingConstants.CENTER);
        violationLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        resultPanel.setBorder(BorderFactory.createTitledBorder("KẾT QUẢ ĐO VÀ MỨC VI PHẠM"));
        resultPanel.add(resultLabel);
        resultPanel.add(violationLabel);
        add(resultPanel, BorderLayout.NORTH);

        // --- Phần 2: Form nhập thông tin ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN NGƯỜI ĐƯỢC ĐO"));

        nameField = new JTextField(20);
        ageField = new JTextField(20);
        cccdField = new JTextField(20);
        hometownField = new JTextField(20);

        formPanel.add(new JLabel("Họ tên:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Tuổi:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(cccdField);
        formPanel.add(new JLabel("Quê quán:"));
        formPanel.add(hometownField);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(formPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // --- Phần 3: Nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        submitButton = new JButton("Gửi Dữ liệu");
        redoButton = new JButton("Đo Lại");

        submitButton.setEnabled(false);

        buttonPanel.add(submitButton);
        buttonPanel.add(redoButton);
        add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitData());
        redoButton.addActionListener(e -> triggerRedo());

        // GỌI THỬ để test giao diện sau khi đo xong
        // this.receiveMeasurementResult(0.35, "Vi phạm Mức 2");
    }

    public void receiveMeasurementResult(double result, String violation) {
        this.currentMeasurementResult = result;
        this.currentViolationLevel = violation;

        resultLabel.setText("Kết quả đo: " + String.format("%.2f", result) + " mg/L");
        violationLabel.setText("Mức vi phạm: " + violation);
        submitButton.setEnabled(true);
    }

    private void submitData() {
        if (currentMeasurementResult == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kết quả đo để gửi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", nameField.getText());
        data.put("age", ageField.getText());
        data.put("cccd", cccdField.getText());
        data.put("hometown", hometownField.getText());
        data.put("result", currentMeasurementResult);
        data.put("violation", currentViolationLevel);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Gọi API POST /api/measurement/submit
                return ApiClient.getInstance().submitMeasurement(data);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MeasurementPanel.this, "Dữ liệu đã được gửi thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        // Reset form
                        currentMeasurementResult = null;
                        submitButton.setEnabled(false);
                        nameField.setText("");
                        ageField.setText("");
                        cccdField.setText("");
                        hometownField.setText("");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MeasurementPanel.this, "Lỗi khi gửi dữ liệu: " + e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void triggerRedo() {
        // Giả lập logic gửi yêu cầu đo lại
        JOptionPane.showMessageDialog(this, "Đang gửi yêu cầu đo lại đến thiết bị...", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        // Sau đó, chờ Backend gọi lại phương thức receiveMeasurementResult(...)
    }
}