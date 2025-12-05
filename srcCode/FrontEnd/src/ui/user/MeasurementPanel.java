package ui.user;

import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250)); // xám nhẹ hiện đại
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // =========================================================
        //       HEADER – KẾT QUẢ ĐO
        // =========================================================
        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        resultPanel.setBackground(new Color(52, 152, 219)); // xanh dương
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        resultLabel = new JLabel("Kết quả đo: Đang chờ...", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        resultLabel.setForeground(Color.WHITE);

        violationLabel = new JLabel("Mức vi phạm: Chưa xác định", SwingConstants.CENTER);
        violationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        violationLabel.setForeground(new Color(235, 235, 235));

        resultPanel.add(resultLabel);
        resultPanel.add(violationLabel);

        add(resultPanel, BorderLayout.NORTH);

        // =========================================================
        //       FORM NHẬP THÔNG TIN
        // =========================================================
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

        // =========================================================
        //       BUTTONS
        // =========================================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        submitButton = new JButton("📤 Gửi dữ liệu");
        redoButton = new JButton("🔄 Đo lại");

        styleButton(submitButton, new Color(46, 204, 113), new Color(39, 174, 96));
        styleButton(redoButton, new Color(230, 126, 34), new Color(211, 84, 0));

        submitButton.setEnabled(false);

        buttonPanel.add(submitButton);
        buttonPanel.add(redoButton);

        add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitData());
        redoButton.addActionListener(e -> triggerRedo());
    }

    // ====== STYLE BUTTONS ======
    private void styleButton(JButton btn, Color normal, Color hover) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addChangeListener(e -> {
            if (btn.getModel().isRollover())
                btn.setBackground(hover);
            else
                btn.setBackground(normal);
        });
    }

    public void receiveMeasurementResult(double result, String violation) {
        currentMeasurementResult = result;
        currentViolationLevel = violation;

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
                return ApiClient.getInstance().submitMeasurement(data);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MeasurementPanel.this,
                                "Dữ liệu đã được gửi thành công!", "Hoàn tất",
                                JOptionPane.INFORMATION_MESSAGE);

                        resetForm();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MeasurementPanel.this,
                            "Lỗi khi gửi dữ liệu: " + e.getMessage(), "Lỗi API",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void resetForm() {
        currentMeasurementResult = null;
        submitButton.setEnabled(false);
        nameField.setText("");
        ageField.setText("");
        cccdField.setText("");
        hometownField.setText("");
        resultLabel.setText("Kết quả đo: Đang chờ...");
        violationLabel.setText("Mức vi phạm: Chưa xác định");
    }

    private void triggerRedo() {
        JOptionPane.showMessageDialog(this,
                "Đang gửi yêu cầu đo lại đến thiết bị...",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
