package ui.admin;

import model.MeasurementStatics;
import service.ApiClient;

import javax.swing.*;
import java.awt.*;

public class StatisticsDialog extends JDialog {

    private JLabel totalTestsLbl;
    private JLabel violationsLbl;
    private JLabel avgLevelLbl;
    private JLabel highLbl;
    private JLabel lowLbl;
    private JLabel noneLbl;

    private JTextField startDateField;
    private JTextField endDateField;
    private JButton loadBtn;

    public StatisticsDialog(Window owner) {
        super(owner, "📊 Thống kê lịch sử đo", ModalityType.APPLICATION_MODAL);
        setSize(450, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // ==== PANEL NHẬP NGÀY ====
        JPanel datePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        datePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        startDateField = new JTextField("2025-01-01");
        endDateField = new JTextField("2025-12-31");

        datePanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("Đến ngày (yyyy-MM-dd):"));
        datePanel.add(endDateField);

        add(datePanel, BorderLayout.NORTH);

        // ==== PANEL HIỂN THỊ THỐNG KÊ ====
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalTestsLbl = new JLabel();
        violationsLbl = new JLabel();
        avgLevelLbl = new JLabel();
        highLbl = new JLabel();
        lowLbl = new JLabel();
        noneLbl = new JLabel();

        panel.add(totalTestsLbl);
        panel.add(violationsLbl);
        panel.add(avgLevelLbl);
        panel.add(new JLabel("------- Mức Vi Phạm -------"));
        panel.add(highLbl);
        panel.add(lowLbl);
        panel.add(noneLbl);

        add(panel, BorderLayout.CENTER);

        // ==== NÚT TẢI THỐNG KÊ ====
        loadBtn = new JButton("Tải thống kê");
        loadBtn.addActionListener(e -> loadStatistics());
        add(loadBtn, BorderLayout.SOUTH);
    }

    private void loadStatistics() {
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();

        // Kiểm tra định dạng
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}") ||
                !end.matches("\\d{4}-\\d{2}-\\d{2}")) {

            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày phải là yyyy-MM-dd",
                    "Sai định dạng ngày",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Chuyển thành ISO8601
        String startIso = start + "T00:00:00.000Z";
        String endIso = end + "T23:59:59.999Z";

        new SwingWorker<MeasurementStatics, Void>() {
            @Override
            protected MeasurementStatics doInBackground() throws Exception {
                return ApiClient.getInstance().getStatistics(startIso, endIso);
            }

            @Override
            protected void done() {
                try {
                    MeasurementStatics stats = get();

                    totalTestsLbl.setText("Tổng số lần đo: " + stats.data.totalTests);
                    violationsLbl.setText("Số lần vi phạm: " + stats.data.violations);
                    avgLevelLbl.setText("Mức cồn trung bình: " + stats.data.averageLevel);

                    highLbl.setText("Mức cao (high): " + stats.data.byLevel.high);
                    lowLbl.setText("Mức thấp (low): " + stats.data.byLevel.low);
                    noneLbl.setText("Không vi phạm (none): " + stats.data.byLevel.none);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StatisticsDialog.this,
                            "Lỗi tải thống kê: " + ex.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
