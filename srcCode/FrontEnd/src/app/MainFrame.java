package app;

import ui.admin.AccountManagementPanel;
import ui.admin.DeviceManagementPanel;
import ui.admin.HistoryPanel;
import ui.user.ConnectionPanel;
import ui.user.MeasurementPanel;

import javax.swing.*;
import java.awt.*;
public class MainFrame extends JFrame {

    public MainFrame(String role) {
        super("Hệ thống Đo Nồng độ Cồn - Vai trò: " + (role.equals("admin") ? "Quản lý" : "Người đo"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Thanh thông tin và nút Đăng xuất
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("Xin chào, " + (role.equals("admin") ? "Người quản lý" : "Người đo") + " | ", SwingConstants.RIGHT);
        JButton logoutButton = new JButton("Đăng xuất");
        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);


        if ("user".equals(role)) {
            tabbedPane.addTab("🔗 Kết nối Thiết bị", new ConnectionPanel());
            tabbedPane.addTab("🔬 Đo Nồng độ Cồn", new MeasurementPanel());
        } else if ("admin".equals(role)) {
            tabbedPane.addTab("⚙️ Quản lý Thiết bị", new DeviceManagementPanel());
            tabbedPane.addTab("👥 Quản lý Tài khoản", new AccountManagementPanel());
            tabbedPane.addTab("📜 Lịch sử Đo", new HistoryPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
