// ManagementPanel.java
// Handles all management and view screens:
//   1. Dashboard  — stats, room availability cards, quick actions
//   2. All Rooms  — full room grid with availability status
//   3. My Bookings — full list of all reservations
//   4. Cancel Booking — lookup by ID and cancel with refund calc
//   5. About Hotel — hotel info and project details

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManagementPanel {

    private Hotel        hotel;
    private JPanel       contentArea;
    private BookingPanel bookingPanel;

    public ManagementPanel(Hotel hotel, JPanel contentArea, BookingPanel bookingPanel) {
        this.hotel        = hotel;
        this.contentArea  = contentArea;
        this.bookingPanel = bookingPanel;
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────

    public void showDashboard() {
        JPanel p = scrollPanel();
        p.add(pageTitle("Dashboard", "Welcome to The Grand Horizon Management System"));
        p.add(gap(20));

        // Stats row — 4 cards showing key metrics
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        stats.add(statCard("Total Rooms",   String.valueOf(hotel.getTotalRooms()),
            "All inventory",      UITheme.ACCENT_BLUE,   "🏨"));
        stats.add(statCard("Available",     String.valueOf(hotel.getAvailableCount()),
            "Ready to book",      UITheme.ACCENT_GREEN,  "✅"));
        stats.add(statCard("Occupied",      String.valueOf(hotel.getOccupiedCount()),
            "Currently occupied", UITheme.ACCENT_ORANGE, "🔴"));
        stats.add(statCard("Total Revenue",
            "Rs." + String.format("%,.0f", hotel.getTotalRevenue()),
            "All-time earnings",  UITheme.ACCENT_GOLD, "💰"));

        p.add(stats);
        p.add(gap(24));

        // Room availability by type
        p.add(sectionLabel("Room Availability by Category"));
        p.add(gap(12));

        JPanel typeGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        typeGrid.setOpaque(false);
        typeGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        for (Room.Type type : Room.Type.values())
            typeGrid.add(roomTypeCard(type));

        p.add(typeGrid);
        p.add(gap(24));

        // Quick action buttons
        p.add(sectionLabel("Quick Actions"));
        p.add(gap(10));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.add(actionBtn("Search & Book",  UITheme.ACCENT_BLUE,   () -> bookingPanel.showSearch()));
        actions.add(actionBtn("View Bookings",  UITheme.ACCENT_GREEN,  this::showReservations));
        actions.add(actionBtn("Cancel Booking", UITheme.ACCENT_RED,    this::showCancelScreen));
        actions.add(actionBtn("All Rooms",      UITheme.ACCENT_PURPLE, this::showAllRooms));

        p.add(actions);
        setContent(scroll(p));
    }

    // ── ALL ROOMS ─────────────────────────────────────────────────────────

    public void showAllRooms() {
        JPanel p = scrollPanel();
        p.add(pageTitle("All Rooms", "Complete room inventory and availability status"));
        p.add(gap(20));

        for (Room.Type type : Room.Type.values()) {
            Color tc = UITheme.getRoomTypeColor(type);

            JLabel typeHeader = new JLabel("  " + type.icon + " " + type.label +
                " Rooms  —  Rs." + String.format("%,.0f", type.basePrice) + "/night");
            typeHeader.setFont(UITheme.FONT_SUBHEADING);
            typeHeader.setForeground(tc);
            typeHeader.setBorder(new EmptyBorder(0, 0, 8, 0));
            p.add(typeHeader);

            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            grid.setOpaque(false);
            grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

            for (Room room : hotel.getRoomsByType(type))
                grid.add(miniRoomCard(room));

            p.add(grid);
            p.add(gap(16));
        }

        setContent(scroll(p));
    }

    // Small tile showing one room's number, floor and status
    private JPanel miniRoomCard(Room room) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(room.isAvailable() ? Color.WHITE : new Color(255, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(room.isAvailable()
                    ? UITheme.BORDER_LIGHT : new Color(254, 202, 202));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(100, 80));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel num = new JLabel(String.valueOf(room.getRoomNumber()));
        num.setFont(UITheme.FONT_BOLD);
        num.setForeground(UITheme.TEXT_PRIMARY);
        num.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel flr = new JLabel("Floor " + room.getFloor());
        flr.setFont(UITheme.FONT_SMALL);
        flr.setForeground(UITheme.TEXT_MUTED);
        flr.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color sc = room.isAvailable() ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED;
        JLabel st = new JLabel(room.isAvailable() ? "Available" : "Occupied");
        st.setFont(new Font("Segoe UI", Font.BOLD, 10));
        st.setForeground(sc);
        st.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(num); card.add(gap(2)); card.add(flr); card.add(gap(4)); card.add(st);
        return card;
    }

    // ── MY BOOKINGS ───────────────────────────────────────────────────────

    public void showReservations() {
        JPanel p = scrollPanel();
        p.add(pageTitle("My Bookings", "All reservations — active and past"));
        p.add(gap(20));

        List<Reservation> all = hotel.getAllReservations();

        if (all.isEmpty()) {
            JLabel empty = new JLabel(
                "  No bookings yet. Search and book a room to get started!");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_SECONDARY);
            p.add(empty);
        } else {
            // Most recent booking shown first
            for (int i = all.size() - 1; i >= 0; i--) {
                p.add(reservationRow(all.get(i)));
                p.add(gap(10));
            }
        }

        setContent(scroll(p));
    }

    // One row card for a single reservation
    private JPanel reservationRow(Reservation res) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true),
            new EmptyBorder(14, 20, 14, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        Color sc = UITheme.getStatusColor(res.getStatus());

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Status badge
        JLabel statusLbl = new JLabel(" " + res.getStatus().name() + " ");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusLbl.setForeground(sc);
        statusLbl.setOpaque(true);
        statusLbl.setBackground(new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), 20));
        statusLbl.setBorder(BorderFactory.createLineBorder(
            new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), 70), 1, true));

        JLabel idLbl = new JLabel(res.getBookingId() + "  ·  " +
            res.getRoom().getType().label + "  ·  Room " + res.getRoom().getRoomNumber());
        idLbl.setFont(UITheme.FONT_BOLD);
        idLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel guestLbl = new JLabel(res.getGuest().getName() +
            "  |  " + res.getGuest().getPhone());
        guestLbl.setFont(UITheme.FONT_SMALL);
        guestLbl.setForeground(UITheme.TEXT_SECONDARY);

        String df = "dd MMM yyyy";
        JLabel datesLbl = new JLabel(
            res.getCheckIn().format(DateTimeFormatter.ofPattern(df)) +
            "  to  " +
            res.getCheckOut().format(DateTimeFormatter.ofPattern(df)) +
            "  (" + res.getNights() + " nights)");
        datesLbl.setFont(UITheme.FONT_SMALL);
        datesLbl.setForeground(UITheme.TEXT_SECONDARY);

        left.add(statusLbl); left.add(gap(6));
        left.add(idLbl);     left.add(gap(4));
        left.add(guestLbl);  left.add(gap(4));
        left.add(datesLbl);

        // Right — total amount
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(160, 0));

        JLabel amtLbl = new JLabel("Rs." + String.format("%,.2f", res.getTotalAmount()));
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        amtLbl.setForeground(UITheme.ACCENT_BLUE);
        amtLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel payLbl = new JLabel(res.getPaymentMethod().name().replace("_", " "));
        payLbl.setFont(UITheme.FONT_SMALL);
        payLbl.setForeground(UITheme.TEXT_MUTED);
        payLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(Box.createVerticalGlue());
        right.add(amtLbl); right.add(gap(4));
        right.add(payLbl);
        right.add(Box.createVerticalGlue());

        card.add(left,  BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ── CANCEL BOOKING ────────────────────────────────────────────────────

    public void showCancelScreen() {
        JPanel p = scrollPanel();
        p.add(pageTitle("Cancel Booking",
            "Enter your Booking ID to cancel and receive a refund"));
        p.add(gap(20));

        JPanel form = bookingPanel.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel policy = new JLabel(
            "<html><b>Cancellation Policy: </b>" +
            "2+ days before check-in = 80% refund  |  " +
            "1 day before = 50% refund  |  Same day = No refund</html>");
        policy.setFont(UITheme.FONT_SMALL);
        policy.setForeground(UITheme.TEXT_SECONDARY);
        policy.setBorder(new EmptyBorder(0, 0, 16, 0));
        form.add(policy);

        JLabel idLbl = new JLabel("Booking ID:");
        idLbl.setFont(UITheme.FONT_LABEL);
        idLbl.setForeground(UITheme.TEXT_SECONDARY);

        JTextField idField = bookingPanel.field("Enter Booking ID  (e.g. BK12345)");
        idField.setMaximumSize(new Dimension(360, 44));

        form.add(idLbl);
        form.add(gap(4));
        form.add(idField);
        form.add(gap(14));

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setOpaque(false);

        JButton findBtn = bookingPanel.colorBtn("Find Booking", UITheme.ACCENT_BLUE);
        findBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(findBtn);
        form.add(gap(16));
        form.add(resultPanel);

        findBtn.addActionListener(e -> {
            resultPanel.removeAll();
            String id  = idField.getText().trim();
            Reservation res = hotel.findReservationById(id);

            if (res == null) {
                JLabel nf = new JLabel("Booking ID not found. Please check and try again.");
                nf.setFont(UITheme.FONT_BODY);
                nf.setForeground(UITheme.ACCENT_RED);
                resultPanel.add(nf);

            } else if (res.getStatus() == Reservation.Status.CANCELLED) {
                JLabel already = new JLabel("This booking is already cancelled.");
                already.setFont(UITheme.FONT_BODY);
                already.setForeground(UITheme.TEXT_SECONDARY);
                resultPanel.add(already);

            } else {
                double refund = res.calculateRefund();
                String df     = "dd MMM yyyy";

                JLabel found = new JLabel("Booking Found:");
                found.setFont(UITheme.FONT_BOLD);
                found.setForeground(UITheme.TEXT_PRIMARY);

                JLabel info = new JLabel(
                    "<html><b>" + res.getBookingId() + "</b>  |  " +
                    res.getGuest().getName() + "  |  Room " +
                    res.getRoom().getRoomNumber() +
                    " (" + res.getRoom().getType().label + ")<br>" +
                    res.getCheckIn().format(DateTimeFormatter.ofPattern(df)) +
                    "  to  " +
                    res.getCheckOut().format(DateTimeFormatter.ofPattern(df)) +
                    "  |  Paid: Rs." + String.format("%,.2f", res.getTotalAmount()) + "<br>" +
                    "<font color='#16a34a'><b>Refund if cancelled now: Rs." +
                    String.format("%,.2f", refund) + "</b></font></html>");
                info.setFont(UITheme.FONT_BODY);
                info.setForeground(UITheme.TEXT_SECONDARY);
                info.setBorder(new EmptyBorder(8, 0, 12, 0));

                JButton cancelBtn = bookingPanel.colorBtn(
                    "Confirm Cancellation — Refund Rs." + String.format("%,.2f", refund),
                    UITheme.ACCENT_RED);

                cancelBtn.addActionListener(ev -> {
                    double actual = hotel.cancelBooking(res.getBookingId());
                    if (actual >= 0) {
                        JOptionPane.showMessageDialog(null,
                            "Booking cancelled successfully!\n" +
                            "Refund of Rs." + String.format("%,.2f", actual) +
                            " will be credited within 5-7 business days.",
                            "Booking Cancelled",
                            JOptionPane.INFORMATION_MESSAGE);
                        showReservations();
                    }
                });

                resultPanel.add(found);
                resultPanel.add(info);
                resultPanel.add(cancelBtn);
            }

            resultPanel.revalidate();
            resultPanel.repaint();
        });

        p.add(form);
        setContent(scroll(p));
    }

    // ── ABOUT HOTEL ───────────────────────────────────────────────────────

    public void showAbout() {
        JPanel p = scrollPanel();
        p.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel stars = new JLabel("★★★★★");
        stars.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        stars.setForeground(UITheme.ACCENT_GOLD);
        stars.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("The Grand Horizon");
        name.setFont(UITheme.FONT_TITLE);
        name.setForeground(UITheme.TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Where Luxury Meets Comfort");
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        tagline.setForeground(UITheme.TEXT_SECONDARY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(stars); p.add(gap(8));
        p.add(name);  p.add(gap(8));
        p.add(tagline); p.add(gap(32));

        JPanel infoCard = bookingPanel.card();
        infoCard.setLayout(new GridLayout(0, 2, 8, 12));
        infoCard.setMaximumSize(new Dimension(680, Integer.MAX_VALUE));

        bookingPanel.detailRow(infoCard, "Location",    "Marine Drive, Mumbai, India");
        bookingPanel.detailRow(infoCard, "Total Rooms", "30 Rooms across 8 Floors");
        bookingPanel.detailRow(infoCard, "Room Types",  "Standard | Deluxe | Suite | Presidential");
        bookingPanel.detailRow(infoCard, "Check-in",    "12:00 PM");
        bookingPanel.detailRow(infoCard, "Check-out",   "11:00 AM");
        bookingPanel.detailRow(infoCard, "Phone",       "+91 98765 43210");
        bookingPanel.detailRow(infoCard, "Email",       "reservations@grandhorizon.in");
        bookingPanel.detailRow(infoCard, "Project",     "CodeAlpha Java Internship — Task 4");

        p.add(infoCard);
        setContent(scroll(p));
    }

    // ── PRIVATE UI HELPERS ────────────────────────────────────────────────

    private void setContent(JComponent c) {
        contentArea.removeAll();
        contentArea.add(c, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel scrollPanel() { return bookingPanel.scrollPanel(); }
    private JScrollPane scroll(JPanel p) { return bookingPanel.scroll(p); }
    private Component gap(int h) { return bookingPanel.gap(h); }

    private JPanel pageTitle(String title, String sub) {
        return bookingPanel.pageTitle(title, sub);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SUBHEADING);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return l;
    }

    private JPanel statCard(String label, String value, String sub,
                             Color color, String icon) {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(16, 20, 16, 20));
        c.setOpaque(false);

        JLabel ic  = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 24));
        val.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sl  = new JLabel(sub);
        sl.setFont(UITheme.FONT_SMALL);
        sl.setForeground(UITheme.TEXT_MUTED);

        c.add(ic); c.add(gap(8));
        c.add(val); c.add(gap(2));
        c.add(lbl); c.add(gap(2));
        c.add(sl);
        return c;
    }

    private JPanel roomTypeCard(Room.Type type) {
        Color tc  = UITheme.getRoomTypeColor(type);
        int avail = hotel.getAvailableByType(type);
        int total = hotel.getRoomsByType(type).size();

        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        c.setLayout(new BorderLayout(0, 8));
        c.setBorder(new EmptyBorder(16, 20, 16, 20));
        c.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel typeName = new JLabel(type.icon + "  " + type.label);
        typeName.setFont(UITheme.FONT_SUBHEADING);
        typeName.setForeground(tc);

        JLabel price = new JLabel("Rs." + String.format("%,.0f", type.basePrice) + "/night");
        price.setFont(UITheme.FONT_BOLD);
        price.setForeground(UITheme.TEXT_SECONDARY);

        top.add(typeName, BorderLayout.WEST);
        top.add(price,    BorderLayout.EAST);

        // Availability progress bar
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 5, getWidth(), 10, 10, 10);
                int w = total > 0 ? (int)(getWidth() * avail / (double) total) : 0;
                g2.setColor(tc);
                if (w > 0) g2.fillRoundRect(0, 5, w, 10, 10, 10);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 20));
        bar.setOpaque(false);

        JLabel availLbl = new JLabel(avail + " of " + total + " rooms available");
        availLbl.setFont(UITheme.FONT_SMALL);
        availLbl.setForeground(UITheme.TEXT_MUTED);

        c.add(top,      BorderLayout.NORTH);
        c.add(bar,      BorderLayout.CENTER);
        c.add(availLbl, BorderLayout.SOUTH);
        return c;
    }

    private JButton actionBtn(String text, Color color, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}