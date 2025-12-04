package ui.admin;

import service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
// Cần thêm thư viện JFreeChart vào project để lớp này hoạt động
// import org.jfree.chart.*;
// import org.jfree.data.category.DefaultCategoryDataset;

public class StatisticsDialog extends JDialog {

    public StatisticsDialog(JPanel parent) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Thống kê Nồng độ Cồn theo Độ tuổi", true);

        setLayout(new BorderLayout());
        setSize(800, 500);
        setLocationRelativeTo(parent);

        JLabel loadingLabel = new JLabel("Đang tải dữ liệu thống kê...", SwingConstants.CENTER);
        add(loadingLabel, BorderLayout.CENTER);

        loadStatisticsData();
    }

    private void loadStatisticsData() {
        new SwingWorker<Map<String, Double>, Void>() {
            @Override
            protected Map<String, Double> doInBackground() throws Exception {
                return ApiClient.getInstance().getAgeBasedStatistics();
            }

            @Override
            protected void done() {
                remove(getComponent(0)); // Xóa loading label
                try {
                    Map<String, Double> data = get();

                    // --- BƯỚC VẼ BIỂU ĐỒ (Cần JFreeChart) ---
                    // DefaultCategoryDataset dataset = createDataset(data);
                    // JFreeChart lineChart = ChartFactory.createLineChart(...);
                    // ChartPanel chartPanel = new ChartPanel(lineChart);
                    // add(chartPanel, BorderLayout.CENTER);

                    // Thay thế bằng một JLabel đơn giản nếu không có JFreeChart:
                    JTextArea resultArea = new JTextArea("Dữ liệu thống kê đã tải:\n");
                    data.forEach((age, avg) -> resultArea.append(age + " tuổi: " + String.format("%.2f", avg) + " mg/L\n"));
                    add(new JScrollPane(resultArea), BorderLayout.CENTER);
                    // --- END BƯỚC VẼ BIỂU ĐỒ ---

                    revalidate();
                    repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StatisticsDialog.this,
                            "Lỗi khi tải dữ liệu thống kê: " + e.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        }.execute();
    }

    // Phương thức tạo Dataset (Nếu sử dụng JFreeChart)
    /*
    private DefaultCategoryDataset createDataset(Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Nồng độ trung bình", entry.getKey());
        }
        return dataset;
    }
    */
}
