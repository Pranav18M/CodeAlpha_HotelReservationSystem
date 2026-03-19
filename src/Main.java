// Main.java
// Entry point — configures rendering hints and launches the hotel GUI

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("ScrollBar.width", 8);
                UIManager.put("ScrollBar.thumb",
                    new javax.swing.plaf.ColorUIResource(
                        new Color(203, 213, 225)));
                UIManager.put("ScrollBar.track",
                    new javax.swing.plaf.ColorUIResource(
                        new Color(241, 245, 249)));
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception ignored) {}

            new HotelGUI().setVisible(true);
        });
    }
}