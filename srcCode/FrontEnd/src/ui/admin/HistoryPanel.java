package ui.admin;

import model.MeasurementHistory;
import service.ApiClient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames =
            {"ID", "Người đo", "Mức cồn", "Mức vi phạm", "Thời gian đo", "Địa điểm", "Cảnh sát đo"};

    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;

    private JLabel pageInfoLabel;
    private JButton prevBtn, nextBtn;

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        setUIFont(new Font("Segoe UI", Font.PLAIN, 14));

        // === TITLE HEADER ===
        JLabel title = new JLabel("LỊCH SỬ CÁC LẦN ĐO NỒNG ĐỘ CỒN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setOpaque(true);
        title.setBackground(new Color(0, 122, 204));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        // === TABLE MODEL ===
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(30);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(new Color(220, 220, 220));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Căn giữa ID và Kết quả đo
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(center);
        historyTable.getColumnModel().getColumn(6).setCellRenderer(center);

        // Header style
        JTableHeader header = historyTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        header.setBackground(new Color(0, 122, 204));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // === STATISTICS BUTTON ===
        JButton statisticsButton = new JButton("📈 Thống kê Lịch sử Đo");
        statisticsButton.setForeground(Color.WHITE);
        statisticsButton.setBackground(new Color(52, 152, 219));
        statisticsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statisticsButton.setFocusPainted(false);
        statisticsButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        statisticsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(statisticsButton);

        statisticsButton.addActionListener(e ->
                new StatisticsDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true)
        );
        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadHistory();
    }

    // =====================================================================================
    // LOAD HISTORY
    // =====================================================================================
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
                                item.getId(),
                                item.getSubjectName(),
                                item.getAlcoholLevel(),
                                item.getViolationLevel(),
                                item.getTestTime(),
                                item.getLocation(),
                                item.getOfficerFullName()
                        });

                    }

                    resizeColumns(historyTable);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            "Lỗi khi tải lịch sử đo: " + e.getMessage(),
                            "Lỗi API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =====================================================================================
    // Resize table columns
    // =====================================================================================
    private void resizeColumns(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int width = 75;
            for (int row = 0; row < table.getRowCount(); row++) {
                Component comp = table.prepareRenderer(
                        table.getCellRenderer(row, col), row, col);
                width = Math.max(width, comp.getPreferredSize().width + 16);
            }
            columnModel.getColumn(col).setPreferredWidth(width);
        }
    }

    // =====================================================================================
    // UI Font Setter
    // =====================================================================================
    private static void setUIFont(Font f) {
        UIManager.getDefaults().entrySet().forEach(entry -> {
            if (entry.getValue() instanceof Font) {
                UIManager.put(entry.getKey(), f);
            }
        });
    }
}
