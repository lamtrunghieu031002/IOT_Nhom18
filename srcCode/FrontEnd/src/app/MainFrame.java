package app;

import service.BluetoothClientScanner; // <--- THÊM IMPORT NÀY
import ui.admin.AccountManagementPanel;
import ui.admin.DeviceManagementPanel;
import ui.admin.HistoryPanel;
import ui.user.ConnectionPanel;
import ui.user.MeasurementPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(String role) {
        super("Hệ thống Đo Nồng độ Cồn - Vai trò: " + (role.equals("ADMIN") ? "Quản lý" : "Người đo"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Thanh thông tin và nút Đăng xuất
        JPanel topPanel = new JPanel(new BorderLayout());

        // Tạo padding cho đẹp hơn
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel infoLabel = new JLabel("Xin chào, " + (role.equals("ADMIN") ? "Người quản lý" : "Người đo") + " | ", SwingConstants.RIGHT);
        JButton logoutButton = new JButton("Đăng xuất");

        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Phân quyền Tab
        if ("OFFICER".equals(role)) {
            tabbedPane.addTab("🔗 Kết nối Thiết bị", new ConnectionPanel());
            tabbedPane.addTab("🔬 Đo Nồng độ Cồn", new MeasurementPanel());
        } else if ("ADMIN".equals(role)) {
            tabbedPane.addTab("⚙️ Quản lý Thiết bị", new DeviceManagementPanel());
            tabbedPane.addTab("👥 Quản lý Tài khoản", new AccountManagementPanel());
            tabbedPane.addTab("📜 Lịch sử Đo", new HistoryPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 1. Ngắt kết nối Bluetooth (quan trọng để giải phóng tài nguyên)
                System.out.println("Đang đăng xuất... Ngắt kết nối Bluetooth.");
                BluetoothClientScanner.getInstance().disconnect();

                dispose();

                new LoginFrame();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}