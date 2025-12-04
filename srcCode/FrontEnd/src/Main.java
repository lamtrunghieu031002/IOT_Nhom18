import app.LoginFrame;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        // Đặt Look and Feel của hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Khởi chạy ứng dụng trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
           new LoginFrame().setVisible(true);
        });
    }
}