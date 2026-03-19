// HotelGUI.java
// Main application window — builds the frame, header and sidebar
// Navigation is handled here; actual screen content is in
// BookingPanel.java and ManagementPanel.java

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HotelGUI extends JFrame {

    private Hotel            hotel;
    private JPanel           contentArea;
    private JPanel           activeSidebarBtn;

    // These two panels handle all the screens
    private BookingPanel     bookingPanel;
    private ManagementPanel  managementPanel;

    public HotelGUI() {
        hotel = new Hotel();
        initWindow();
        buildLayout();

        // Give both panels a reference to the content area so they can swap screens
        bookingPanel    = new BookingPanel(hotel, contentArea);
        managementPanel = new ManagementPanel(hotel, contentArea, bookingPanel);

        // Cross-link so booking panel can navigate to reservations after confirming
        bookingPanel.setManagementPanel(managementPanel);

        // Start on the dashboard
        managementPanel.showDashboard();
    }

    // ── WINDOW SETUP ──────────────────────────────────────────────────────

    private void initWindow() {
        setTitle("The Grand Horizon — Hotel Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_MAIN);
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private void buildLayout() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_MAIN);
        add(contentArea, BorderLayout.CENTER);
    }

    // ── HEADER BAR ────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        left.setOpaque(false);

        JLabel star = new JLabel("★");
        star.setFont(new Font("Segoe UI", Font.BOLD, 22));
        star.setForeground(UITheme.ACCENT_GOLD);

        JLabel name = new JLabel("THE GRAND HORIZON");
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(Color.WHITE);

        JLabel sub = new JLabel("  ·  Luxury Hotel & Resorts");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(148, 163, 184));

        left.add(star);
        left.add(name);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
        right.setOpaque(false);
        JLabel date = new JLabel("Today: " + LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setFont(UITheme.FONT_SMALL);
        date.setForeground(new Color(148, 163, 184));
        right.add(date);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── SIDEBAR NAVIGATION ────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(UITheme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBorder(new EmptyBorder(16, 0, 16, 0));

        sb.add(sidebarLabel("MAIN"));
        sb.add(sidebarBtn("Home",           () -> managementPanel.showDashboard()));
        sb.add(sidebarBtn("Search Rooms",   () -> bookingPanel.showSearch()));
        sb.add(sidebarBtn("My Bookings",    () -> managementPanel.showReservations()));

        sb.add(Box.createVerticalStrut(8));
        sb.add(sidebarLabel("MANAGEMENT"));
        sb.add(sidebarBtn("All Rooms",      () -> managementPanel.showAllRooms()));
        sb.add(sidebarBtn("Cancel Booking", () -> managementPanel.showCancelScreen()));

        sb.add(Box.createVerticalGlue());
        sb.add(sidebarLabel("INFO"));
        sb.add(sidebarBtn("About Hotel",    () -> managementPanel.showAbout()));

        return sb;
    }

    private JLabel sidebarLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(71, 85, 105));
        l.setBorder(new EmptyBorder(10, 16, 4, 16));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(220, 28));
        return l;
    }

    private JPanel sidebarBtn(String label, Runnable action) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        btn.setOpaque(true);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(new Color(148, 163, 184));
        btn.add(lbl);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeSidebarBtn) {
                    btn.setBackground(new Color(30, 41, 59));
                    lbl.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeSidebarBtn) {
                    btn.setBackground(UITheme.BG_SIDEBAR);
                    lbl.setForeground(new Color(148, 163, 184));
                }
            }
            public void mouseClicked(MouseEvent e) {
                // Reset previous active button
                if (activeSidebarBtn != null) {
                    activeSidebarBtn.setBackground(UITheme.BG_SIDEBAR);
                    for (Component c : activeSidebarBtn.getComponents())
                        if (c instanceof JLabel jl)
                            jl.setForeground(new Color(148, 163, 184));
                }
                // Set this button as active
                activeSidebarBtn = btn;
                btn.setBackground(new Color(30, 58, 138, 80));
                lbl.setForeground(new Color(96, 165, 250));
                action.run();
            }
        });
        return btn;
    }
}