package ui.admin;


import model.MeasurementHistory;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
public class HistoryPanel extends JPanel {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"ID", "Tên", "CCCD", "Quê quán", "Thời gian đo", "Thiết bị đo", "Kết quả đo"};

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        JButton statisticsButton = new JButton("📈 Thống kê Lịch sử Đo");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(statisticsButton);

        add(new JLabel("LỊCH SỬ CÁC LẦN ĐO NỒNG ĐỘ CỒN", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        statisticsButton.addActionListener(e -> {
            // Mở cửa sổ thống kê
            new StatisticsDialog(this).setVisible(true);
        });

        loadHistory();
    }

    public void loadHistory() {
        new SwingWorker<List<MeasurementHistory>, Void>() {
            @Override
            protected List<MeasurementHistory> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllHistory();
            }

            @Override
            protected void done() {
                try {
                    List<MeasurementHistory> history = get();
                    tableModel.setRowCount(0);
                    for (MeasurementHistory item : history) {
                        tableModel.addRow(new Object[]{
                                item.getId(), item.getName(), item.getCccd(), item.getHometown(),
                                item.getTime(), item.getDevice(), item.getResult()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            "Lỗi khi tải lịch sử đo: " + e.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
