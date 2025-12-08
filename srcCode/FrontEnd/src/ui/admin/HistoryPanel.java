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

        // =================================================================
        // BUTTONS PANEL
        // =================================================================

        // 1. Nút Tải lại (Reload Button)
        JButton reloadButton = new JButton("Tải lại");
        reloadButton.setForeground(Color.WHITE);
        reloadButton.setBackground(new Color(52, 152, 219)); // Màu xanh lá
        reloadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        reloadButton.setFocusPainted(false);
        reloadButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        reloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sự kiện click nút tải lại
        reloadButton.addActionListener(e -> loadHistory());

        // 2. Nút Thống kê (Statistics Button)
        JButton statisticsButton = new JButton("Thống kê Lịch sử Đo");
        statisticsButton.setForeground(Color.WHITE);
        statisticsButton.setBackground(new Color(52, 152, 219)); // Màu xanh dương
        statisticsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statisticsButton.setFocusPainted(false);
        statisticsButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        statisticsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        statisticsButton.addActionListener(e ->
                new StatisticsDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true)
        );

        // Panel chứa các nút (Canh phải)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        // Thêm nút vào panel
        buttonPanel.add(reloadButton);
        buttonPanel.add(statisticsButton);

        // === ADD COMPONENTS ===
        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data lần đầu
        loadHistory();
    }

    // =====================================================================================
    // LOAD HISTORY
    // =====================================================================================
    public void loadHistory() {
        // Có thể thêm hiệu ứng loading chuột nếu muốn
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<List<MeasurementHistory>, Void>() {
            @Override
            protected List<MeasurementHistory> doInBackground() throws Exception {
                // Giả lập delay mạng nhẹ nếu cần test UI (Thread.sleep(500))
                return ApiClient.getInstance().getAllHistory();
            }

            @Override
            protected void done() {
                try {
                    List<MeasurementHistory> history = get();
                    tableModel.setRowCount(0); // Xóa dữ liệu cũ

                    for (MeasurementHistory item : history) {
                        tableModel.addRow(new Object[]{
                                item.getId(),
                                item.getSubjectName(),
                                item.getAlcoholLevel(),
                                item.getViolationLevel(), // Backend đã trả về HIGH/LOW/NONE
                                item.getTestTime(),
                                item.getLocation(),
                                item.getOfficerFullName()
                        });
                    }

                    resizeColumns(historyTable);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            "Lỗi khi tải lịch sử đo: " + e.getMessage(),
                            "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Trả lại con trỏ chuột bình thường
                    setCursor(Cursor.getDefaultCursor());
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