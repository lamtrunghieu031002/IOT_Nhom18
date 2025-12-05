package ui.admin;

import model.Device;
import model.DevicePageResponse;
import service.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class DeviceManagementPanel extends JPanel {

    private JTable deviceTable;
    private DefaultTableModel tableModel;

    private final String[] columnNames = {
            "Mã thiết bị", "Tên thiết bị", "Model", "Ngày thêm", "Trạng thái"
    };

    // Paging state
    private int currentPage = 1;      // 0-based page index
    private int pageSize = 10;

    // Paging controls
    private JButton prevBtn;
    private JButton nextBtn;
    private JLabel pageInfoLabel;
    private JComboBox<Integer> pageSizeCombo;

    public DeviceManagementPanel() {

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        // ======================================================
        //                    HEADER BUTTONS
        // ======================================================
        JButton addButton = createStyledButton("➕ Thêm Thiết bị", new Color(46, 204, 113));
        JButton deleteButton = createStyledButton("➖ Xóa Thiết bị", new Color(231, 76, 60));
        JButton refreshButton = createStyledButton("🔄 Tải lại", new Color(52, 152, 219));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // ======================================================
        //                    TABLE STYLING
        // ======================================================
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        deviceTable = new JTable(tableModel);
        deviceTable.setRowHeight(28);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceTable.setFillsViewportHeight(true);
        deviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceTable.setGridColor(new Color(220, 220, 220));

        JTableHeader header = deviceTable.getTableHeader();
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setOpaque(true);

        deviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(new Color(41, 128, 185));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ======================================================
        //                   PAGING CONTROLS
        // ======================================================
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pagingPanel.setBackground(new Color(245, 247, 250));

        prevBtn = new JButton("⟨ Prev");
        nextBtn = new JButton("Next ⟩");
        pageInfoLabel = new JLabel("Trang 0 / 0");
        pageSizeCombo = new JComboBox<>(new Integer[]{10, 20, 50});

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        pageSizeCombo.setSelectedItem(pageSize);

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadDevices();
            }
        });

        nextBtn.addActionListener(e -> {
            currentPage++;
            loadDevices();
        });

        pageSizeCombo.addActionListener(e -> {
            Integer sel = (Integer) pageSizeCombo.getSelectedItem();
            if (sel != null && sel != pageSize) {
                pageSize = sel;
                currentPage = 1; // reset to first page
                loadDevices();
            }
        });

        pagingPanel.add(new JLabel("Kích thước:"));
        pagingPanel.add(pageSizeCombo);
        pagingPanel.add(Box.createHorizontalStrut(12));
        pagingPanel.add(prevBtn);
        pagingPanel.add(pageInfoLabel);
        pagingPanel.add(nextBtn);

        add(pagingPanel, BorderLayout.SOUTH);

        // ======================================================
        //                  EVENT HANDLERS
        // ======================================================
        refreshButton.addActionListener(e -> {
            currentPage = 1;
            loadDevices();
        });
        deleteButton.addActionListener(e -> deleteSelectedDevice());
        addButton.addActionListener(e -> new AddDeviceDialog(this).setVisible(true));

        // initial load
        loadDevices();
    }

    // ======================================================
    //                  BUTTON STYLE FUNCTION
    // ======================================================
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addChangeListener(e -> {
            if (btn.getModel().isRollover())
                btn.setBackground(color.darker());
            else
                btn.setBackground(color);
        });

        return btn;
    }

    // ======================================================
    //                LOAD DEVICES (with paging)
    // ======================================================
    public void loadDevices() {
        // ensure valid page index
        if (currentPage < 0) currentPage = 1;

        // disable controls while loading
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        pageInfoLabel.setText("Đang tải...");

        new SwingWorker<DevicePageResponse, Void>() {
            @Override
            protected DevicePageResponse doInBackground() throws Exception {
                // NOTE: if your backend expects 1-based pages, call getDevicesPaging(..., currentPage + 1, pageSize)
                return ApiClient.getInstance().getDevicesPaging("ACTIVE", currentPage, pageSize);
            }

            @Override
            protected void done() {
                try {
                    DevicePageResponse page = get();

                    // clear table
                    tableModel.setRowCount(0);

                    List<Device> devices = page.getDevices(); // assumes getter exists
                    for (Device d : devices) {
                        tableModel.addRow(new Object[]{
                                d.getDeviceId(),
                                d.getName(),
                                d.getModel(),
                                d.getStatus(),
                                d.getCreatedAt(), // Ngày thêm

                        });
                    }

                    int totalPages = page.getTotalPages();
                    int total = page.getTotal();
                    int pageIndex = page.getPage(); // assumes getter returns 0-based index
                    // keep currentPage synced with returned page (safer)
                    currentPage = pageIndex;

                    // update paging controls
                    pageInfoLabel.setText("Trang " + (pageIndex ) + " / " + Math.max(1, totalPages) + " — Tổng " + total);
                    prevBtn.setEnabled(pageIndex > 1);
                    nextBtn.setEnabled(pageIndex < totalPages);

                } catch (Exception e) {
                    // show error and reset paging label
                    JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                            "Lỗi khi tải danh sách thiết bị: " + e.getMessage(),
                            "Lỗi API",
                            JOptionPane.ERROR_MESSAGE);

                    pageInfoLabel.setText("Lỗi tải dữ liệu");
                }
            }
        }.execute();
    }


    // ======================================================
    //                DELETE DEVICE
    // ======================================================
    private void deleteSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thiết bị muốn xóa.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String deviceId = (String) tableModel.getValueAt(selectedRow, 0);

        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa thiết bị: " + deviceId + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // disable buttons while deleting
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ApiClient.getInstance().deleteDevice(deviceId);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                                    "Đã xóa thiết bị thành công!",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);

                            // reload current page (if current page becomes empty, backend should adjust)
                            loadDevices();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(DeviceManagementPanel.this,
                                "Lỗi khi xóa: " + e.getMessage(),
                                "Lỗi API", JOptionPane.ERROR_MESSAGE);
                        // re-enable paging controls (best effort)
                        loadDevices();
                    }
                }
            }.execute();
        }
    }
}
