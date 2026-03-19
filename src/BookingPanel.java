// BookingPanel.java
// Handles all booking-related screens:
//   1. Search and filter available rooms
//   2. Booking form (guest details entry)
//   3. Payment screen (card / UPI / net banking)
//   4. Booking confirmation with all details

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingPanel {

    private Hotel           hotel;
    private JPanel          contentArea;
    private ManagementPanel managementPanel; // linked after construction

    // Booking state shared between screens
    private LocalDate selectedCheckIn;
    private LocalDate selectedCheckOut;
    private int       selectedGuests;

    public BookingPanel(Hotel hotel, JPanel contentArea) {
        this.hotel       = hotel;
        this.contentArea = contentArea;
    }

    // Called after both panels are created to avoid circular constructor dependency
    public void setManagementPanel(ManagementPanel mp) {
        this.managementPanel = mp;
    }

    // ── SEARCH SCREEN ─────────────────────────────────────────────────────

    public void showSearch() {
        JPanel p = scrollPanel();
        p.add(pageTitle("Search Rooms", "Find the perfect room for your stay"));
        p.add(gap(20));

        JPanel form = card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        JComboBox<String> typeBox = combo(
            "All Types", "Standard", "Deluxe", "Suite", "Presidential");
        JTextField checkInF  = field("Check-in  (dd-MM-yyyy)");
        JTextField checkOutF = field("Check-out (dd-MM-yyyy)");
        JComboBox<String> guestsBox = combo(
            "1 Guest","2 Guests","3 Guests","4 Guests","5 Guests","6 Guests");
        JTextField priceF = field("Max price per night (0 = no limit)");

        formRow(form, g, 0, "Room Type:",  typeBox);
        formRow(form, g, 1, "Check-in:",   checkInF);
        formRow(form, g, 2, "Check-out:",  checkOutF);
        formRow(form, g, 3, "Guests:",     guestsBox);
        formRow(form, g, 4, "Max Price:",  priceF);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        g.insets = new Insets(16, 8, 8, 8);
        JButton searchBtn = primaryBtn("  SEARCH AVAILABLE ROOMS  ");
        form.add(searchBtn, g);

        p.add(form);
        p.add(gap(20));

        // Search results will be added here dynamically
        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setOpaque(false);

        searchBtn.addActionListener(e -> {
            results.removeAll();

            // Parse check-in and check-out dates
            LocalDate today    = LocalDate.now();
            LocalDate checkIn  = today.plusDays(1);
            LocalDate checkOut = today.plusDays(2);

            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String ci = checkInF.getText().trim();
                String co = checkOutF.getText().trim();
                if (!ci.isEmpty() && !ci.contains("dd")) checkIn  = LocalDate.parse(ci, fmt);
                if (!co.isEmpty() && !co.contains("dd")) checkOut = LocalDate.parse(co, fmt);
            } catch (Exception ex) {
                showErr("Invalid date format. Please use dd-MM-yyyy"); return;
            }

            if (!checkOut.isAfter(checkIn)) {
                showErr("Check-out must be after check-in date."); return;
            }

            // Parse room type filter
            Room.Type type = null;
            int ti = typeBox.getSelectedIndex();
            if (ti > 0) type = Room.Type.values()[ti - 1];

            int    guests   = guestsBox.getSelectedIndex() + 1;
            double maxPrice = 0;
            try {
                String pt = priceF.getText().trim();
                if (!pt.isEmpty() && !pt.contains("Max")) maxPrice = Double.parseDouble(pt);
            } catch (Exception ignored) {}

            // Cache for the booking form screen
            selectedCheckIn  = checkIn;
            selectedCheckOut = checkOut;
            selectedGuests   = guests;

            List<Room> found = hotel.searchRooms(
                new SearchEngine.SearchCriteria(type, checkIn, checkOut, guests, maxPrice));

            if (found.isEmpty()) {
                JLabel none = new JLabel("  No rooms found. Try adjusting your filters.");
                none.setFont(UITheme.FONT_BODY);
                none.setForeground(UITheme.TEXT_SECONDARY);
                results.add(none);
            } else {
                JLabel cnt = new JLabel("  Found " + found.size() + " available rooms:");
                cnt.setFont(UITheme.FONT_BOLD);
                cnt.setForeground(UITheme.TEXT_PRIMARY);
                results.add(cnt);
                results.add(gap(12));

                LocalDate fin = checkIn, fout = checkOut;
                for (Room rm : found) {
                    results.add(roomResultCard(rm, fin, fout));
                    results.add(gap(10));
                }
            }
            results.revalidate();
            results.repaint();
        });

        p.add(results);
        setContent(scroll(p));
    }

    // Single room card shown in search results with Book Now button
    private JPanel roomResultCard(Room room, LocalDate checkIn, LocalDate checkOut) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 20, 16, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        Color tc = UITheme.getRoomTypeColor(room.getType());

        // Left side — room type badge, details, amenities
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel badge = new JLabel("  " + room.getType().icon + " " + room.getType().label + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(tc);
        badge.setOpaque(true);
        badge.setBackground(new Color(tc.getRed(), tc.getGreen(), tc.getBlue(), 20));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(tc.getRed(), tc.getGreen(), tc.getBlue(), 80), 1, true),
            new EmptyBorder(3, 8, 3, 8)));

        JLabel roomLbl = new JLabel("Room " + room.getRoomNumber() +
            "  ·  Floor " + room.getFloor() + "  ·  " + room.getView());
        roomLbl.setFont(UITheme.FONT_BOLD);
        roomLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel amenLbl = new JLabel(String.join("  ·  ",
            room.getAmenities().subList(0, Math.min(4, room.getAmenities().size()))));
        amenLbl.setFont(UITheme.FONT_SMALL);
        amenLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel capLbl = new JLabel("Up to " + room.getMaxGuests() + " guests");
        capLbl.setFont(UITheme.FONT_SMALL);
        capLbl.setForeground(UITheme.TEXT_MUTED);

        left.add(badge);
        left.add(gap(6));
        left.add(roomLbl);
        left.add(gap(4));
        left.add(amenLbl);
        left.add(gap(4));
        left.add(capLbl);

        // Right side — price and book button
        long   nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double total  = room.getPricePerNight() * nights * 1.18;

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(185, 0));

        JLabel priceLbl = new JLabel("Rs." + String.format("%,.0f", room.getPricePerNight()) + "/night");
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLbl.setForeground(UITheme.ACCENT_BLUE);
        priceLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel totalLbl = new JLabel("Rs." + String.format("%,.0f", total) +
            " total (" + nights + "N)");
        totalLbl.setFont(UITheme.FONT_SMALL);
        totalLbl.setForeground(UITheme.TEXT_SECONDARY);
        totalLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton bookBtn = colorBtn("Book Now", UITheme.ACCENT_BLUE);
        bookBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bookBtn.addActionListener(e -> showBookingForm(room, checkIn, checkOut));

        right.add(Box.createVerticalGlue());
        right.add(priceLbl);
        right.add(gap(4));
        right.add(totalLbl);
        right.add(gap(12));
        right.add(bookBtn);
        right.add(Box.createVerticalGlue());

        card.add(left,  BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ── BOOKING FORM ──────────────────────────────────────────────────────

    public void showBookingForm(Room room, LocalDate checkIn, LocalDate checkOut) {
        JPanel p = scrollPanel();
        long   nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double base   = room.getPricePerNight() * nights;
        double tax    = base * 0.18;
        double total  = base + tax;

        p.add(pageTitle("Complete Your Booking", "Fill in guest details to proceed to payment"));
        p.add(gap(20));

        // Summary card showing room and price breakdown
        JPanel summary = card();
        summary.setLayout(new BorderLayout(20, 0));

        JPanel roomInfo = new JPanel();
        roomInfo.setLayout(new BoxLayout(roomInfo, BoxLayout.Y_AXIS));
        roomInfo.setOpaque(false);
        summaryRow(roomInfo, "Room",      "Room " + room.getRoomNumber() + " — " + room.getType().label);
        summaryRow(roomInfo, "View",      room.getView() + "  ·  Floor " + room.getFloor());
        summaryRow(roomInfo, "Check-in",  checkIn.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        summaryRow(roomInfo, "Check-out", checkOut.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        summaryRow(roomInfo, "Duration",  nights + " night" + (nights > 1 ? "s" : ""));

        JPanel priceInfo = new JPanel();
        priceInfo.setLayout(new BoxLayout(priceInfo, BoxLayout.Y_AXIS));
        priceInfo.setOpaque(false);
        priceInfo.setPreferredSize(new Dimension(220, 0));
        summaryRow(priceInfo, "Base Amount", "Rs." + String.format("%,.2f", base));
        summaryRow(priceInfo, "GST (18%)",   "Rs." + String.format("%,.2f", tax));

        JLabel totalKey = new JLabel("Total Amount");
        totalKey.setFont(UITheme.FONT_BOLD);
        totalKey.setForeground(UITheme.TEXT_SECONDARY);
        JLabel totalVal = new JLabel("Rs." + String.format("%,.2f", total));
        totalVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalVal.setForeground(UITheme.ACCENT_BLUE);
        priceInfo.add(gap(8));
        priceInfo.add(totalKey);
        priceInfo.add(totalVal);

        summary.add(roomInfo,  BorderLayout.CENTER);
        summary.add(priceInfo, BorderLayout.EAST);
        p.add(summary);
        p.add(gap(16));

        // Guest information form
        JPanel guestForm = card();
        guestForm.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        JLabel gTitle = new JLabel("Guest Information");
        gTitle.setFont(UITheme.FONT_SUBHEADING);
        gTitle.setForeground(UITheme.TEXT_PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        guestForm.add(gTitle, g);
        g.gridwidth = 1;

        JTextField nameF   = field("Full Name");
        JTextField emailF  = field("Email Address");
        JTextField phoneF  = field("Phone Number");
        JComboBox<String> idBox = combo(
            "Aadhaar Card", "Passport", "PAN Card", "Driving License");
        JTextField idNumF  = field("ID Number");
        JComboBox<String> payBox = combo(
            "Credit Card", "Debit Card", "UPI", "Net Banking");
        JTextField specF   = field("Any special requests (optional)");

        formRow(guestForm, g, 1, "Full Name *",       nameF);
        formRow(guestForm, g, 2, "Email *",           emailF);
        formRow(guestForm, g, 3, "Phone *",           phoneF);
        formRow(guestForm, g, 4, "ID Type *",         idBox);
        formRow(guestForm, g, 5, "ID Number *",       idNumF);
        formRow(guestForm, g, 6, "Payment Method:",   payBox);
        formRow(guestForm, g, 7, "Special Requests:", specF);

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(16, 8, 8, 8);
        JButton proceedBtn = primaryBtn("  PROCEED TO PAYMENT  ");
        guestForm.add(proceedBtn, g);

        p.add(guestForm);

        proceedBtn.addActionListener(e -> {
            String nm  = nameF.getText().trim();
            String em  = emailF.getText().trim();
            String ph  = phoneF.getText().trim();
            String idn = idNumF.getText().trim();

            if (nm.isEmpty() || em.isEmpty() || ph.isEmpty() || idn.isEmpty()) {
                showErr("Please fill all required fields (*)"); return;
            }
            if (!em.contains("@"))  { showErr("Invalid email address."); return; }
            if (ph.length() < 10)   { showErr("Enter a valid 10-digit phone number."); return; }

            String[] idTypes = {"Aadhaar Card","Passport","PAN Card","Driving License"};
            String[] methods = {"CREDIT_CARD","DEBIT_CARD","UPI","NET_BANKING"};
            Reservation.PaymentMethod pm =
                Reservation.PaymentMethod.valueOf(methods[payBox.getSelectedIndex()]);

            showPaymentScreen(
                new Guest(nm, em, ph, idTypes[idBox.getSelectedIndex()], idn),
                room, checkIn, checkOut, selectedGuests, pm,
                specF.getText().trim(), total);
        });

        setContent(scroll(p));
    }

    // ── PAYMENT SCREEN ────────────────────────────────────────────────────

    private void showPaymentScreen(Guest guest, Room room,
                                    LocalDate checkIn, LocalDate checkOut,
                                    int guests, Reservation.PaymentMethod payMethod,
                                    String specialReq, double totalAmount) {
        JPanel p = scrollPanel();
        p.add(pageTitle("Secure Payment",
            "Amount to pay: Rs." + String.format("%,.2f", totalAmount)));
        p.add(gap(20));

        JPanel payCard = card();
        payCard.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        // Show total amount prominently at the top
        JLabel amtKey = new JLabel("Total Amount");
        amtKey.setFont(UITheme.FONT_LABEL);
        amtKey.setForeground(UITheme.TEXT_SECONDARY);
        JLabel amtVal = new JLabel("Rs." + String.format("%,.2f", totalAmount));
        amtVal.setFont(new Font("Segoe UI", Font.BOLD, 30));
        amtVal.setForeground(UITheme.ACCENT_BLUE);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; payCard.add(amtKey, g);
        g.gridy = 1; payCard.add(amtVal, g);
        g.gridwidth = 1;

        boolean isCard = payMethod == Reservation.PaymentMethod.CREDIT_CARD ||
                         payMethod == Reservation.PaymentMethod.DEBIT_CARD;
        boolean isUPI  = payMethod == Reservation.PaymentMethod.UPI;

        String methodName = isCard
            ? (payMethod == Reservation.PaymentMethod.CREDIT_CARD ? "Credit Card" : "Debit Card")
            : isUPI ? "UPI Payment" : "Net Banking";

        JLabel mLbl = new JLabel(methodName);
        mLbl.setFont(UITheme.FONT_SUBHEADING);
        mLbl.setForeground(UITheme.TEXT_PRIMARY);
        g.gridy = 2; g.gridwidth = 2; g.insets = new Insets(16, 8, 8, 8);
        payCard.add(mLbl, g);
        g.gridwidth = 1; g.insets = new Insets(6, 8, 6, 8);

        // Show different input fields based on the payment method chosen
        JTextField f1, f2 = new JTextField(), f3 = new JTextField(), f4 = new JTextField();

        if (isCard) {
            f1 = field("Card Number (16 digits)");
            f2 = field("MM/YY");
            f3 = field("CVV");
            f4 = field("Cardholder Name");
            formRow(payCard, g, 3, "Card Number:", f1);
            formRow(payCard, g, 4, "Expiry:",      f2);
            formRow(payCard, g, 5, "CVV:",         f3);
            formRow(payCard, g, 6, "Name:",        f4);
        } else if (isUPI) {
            f1 = field("UPI ID  (e.g. name@upi)");
            formRow(payCard, g, 3, "UPI ID:", f1);
        } else {
            f1 = field("Bank Name");
            formRow(payCard, g, 3, "Bank:", f1);
        }

        g.gridx = 0; g.gridy = 9; g.gridwidth = 2;
        g.insets = new Insets(20, 8, 8, 8);
        JButton payBtn = primaryBtn("  PAY Rs." + String.format("%,.2f", totalAmount) +
            "  — CONFIRM BOOKING  ");
        payCard.add(payBtn, g);

        g.gridy = 10; g.insets = new Insets(4, 8, 8, 8);
        JLabel secure = new JLabel(
            "Secured by 256-bit SSL. This is a simulation — no real payment is made.");
        secure.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        secure.setForeground(UITheme.TEXT_MUTED);
        payCard.add(secure, g);

        p.add(payCard);

        // Keep references for the lambda below
        final JTextField ff1 = f1, ff2 = f2, ff3 = f3, ff4 = f4;

        payBtn.addActionListener(e -> {
            payBtn.setText("Processing...");
            payBtn.setEnabled(false);

            // Run on a background thread so UI stays responsive during the simulated delay
            new Thread(() -> {
                PaymentProcessor.PaymentResponse resp;

                if (isCard)
                    resp = hotel.getPayment().processCard(
                        ff1.getText(), ff2.getText(), ff3.getText(), ff4.getText(), totalAmount);
                else if (isUPI)
                    resp = hotel.getPayment().processUPI(ff1.getText(), totalAmount);
                else
                    resp = hotel.getPayment().processNetBanking(ff1.getText(), totalAmount);

                SwingUtilities.invokeLater(() -> {
                    if (resp.isSuccess()) {
                        Reservation res = hotel.createBooking(
                            guest, room, checkIn, checkOut, guests, payMethod, specialReq);
                        showConfirmation(res, resp.transactionId);
                    } else {
                        payBtn.setText("  PAY Rs." + String.format("%,.2f", totalAmount) + "  ");
                        payBtn.setEnabled(true);
                        showErr("Payment Failed: " + resp.message);
                    }
                });
            }).start();
        });

        setContent(scroll(p));
    }

    // ── BOOKING CONFIRMATION ──────────────────────────────────────────────

    private void showConfirmation(Reservation res, String txnId) {
        JPanel p = scrollPanel();
        p.setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel tick = new JLabel("✅");
        tick.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        tick.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Booking Confirmed!");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLbl = new JLabel("Booking ID: " + res.getBookingId());
        idLbl.setFont(UITheme.FONT_SUBHEADING);
        idLbl.setForeground(UITheme.TEXT_SECONDARY);
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(tick);
        p.add(gap(12));
        p.add(title);
        p.add(gap(8));
        p.add(idLbl);
        p.add(gap(28));

        // Full booking details grid
        JPanel details = card();
        details.setLayout(new GridLayout(0, 2, 8, 10));
        details.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        String df = "dd MMM yyyy";
        detailRow(details, "Guest Name",     res.getGuest().getName());
        detailRow(details, "Email",          res.getGuest().getEmail());
        detailRow(details, "Phone",          res.getGuest().getPhone());
        detailRow(details, "Room",           "Room " + res.getRoom().getRoomNumber() +
            " — " + res.getRoom().getType().label);
        detailRow(details, "Floor & View",   "Floor " + res.getRoom().getFloor() +
            "  ·  " + res.getRoom().getView());
        detailRow(details, "Check-in",       res.getCheckIn()
            .format(DateTimeFormatter.ofPattern(df)));
        detailRow(details, "Check-out",      res.getCheckOut()
            .format(DateTimeFormatter.ofPattern(df)));
        detailRow(details, "Duration",       res.getNights() + " nights");
        detailRow(details, "Guests",         String.valueOf(res.getGuests()));
        detailRow(details, "Base Amount",    "Rs." + String.format("%,.2f", res.getBaseAmount()));
        detailRow(details, "GST (18%)",      "Rs." + String.format("%,.2f", res.getTaxAmount()));
        detailRow(details, "Total Paid",     "Rs." + String.format("%,.2f", res.getTotalAmount()));
        detailRow(details, "Payment",        res.getPaymentMethod().name().replace("_", " "));
        detailRow(details, "Transaction ID", txnId);
        detailRow(details, "Booking Date",   res.getBookingDate());
        detailRow(details, "Status",         "CONFIRMED");

        p.add(details);
        p.add(gap(24));

        JButton doneBtn = primaryBtn("  Back to Dashboard  ");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneBtn.addActionListener(e -> managementPanel.showDashboard());
        p.add(doneBtn);

        setContent(scroll(p));
    }

    // ── SHARED HELPERS (package-accessible so ManagementPanel can reuse) ──

    void setContent(JComponent c) {
        contentArea.removeAll();
        contentArea.add(c, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    JPanel scrollPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        return p;
    }

    JScrollPane scroll(JPanel p) {
        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UITheme.BG_MAIN);
        sp.setBackground(UITheme.BG_MAIN);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    JPanel pageTitle(String title, String sub) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_HEADING);
        t.setForeground(UITheme.TEXT_PRIMARY);
        JLabel s = new JLabel(sub);
        s.setFont(UITheme.FONT_SMALL);
        s.setForeground(UITheme.TEXT_MUTED);
        p.add(t); p.add(Box.createVerticalStrut(4)); p.add(s);
        return p;
    }

    Component gap(int h) { return Box.createVerticalStrut(h); }

    JPanel card() {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(20, 24, 20, 24));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    JTextField field(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(UITheme.FONT_BODY);
        f.setForeground(UITheme.TEXT_MUTED);
        f.setBackground(UITheme.BG_INPUT);
        f.setBorder(UITheme.inputBorder());
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText(""); f.setForeground(UITheme.TEXT_PRIMARY);
                }
                f.setBorder(UITheme.inputBorderFocused());
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder); f.setForeground(UITheme.TEXT_MUTED);
                }
                f.setBorder(UITheme.inputBorder());
            }
        });
        return f;
    }

    JComboBox<String> combo(String... items) {
        JComboBox<String> b = new JComboBox<>(items);
        b.setFont(UITheme.FONT_BODY);
        b.setBackground(UITheme.BG_INPUT);
        b.setForeground(UITheme.TEXT_PRIMARY);
        return b;
    }

    GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;
        return g;
    }

    void formRow(JPanel panel, GridBagConstraints g,
                  int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(l, g);
        g.gridx = 1; g.weightx = 1;
        field.setPreferredSize(new Dimension(300, 40));
        panel.add(field, g);
    }

    void summaryRow(JPanel p, String key, String value) {
        JLabel k = new JLabel(key + ":");
        k.setFont(UITheme.FONT_LABEL);
        k.setForeground(UITheme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY);
        v.setForeground(UITheme.TEXT_PRIMARY);
        p.add(k); p.add(Box.createVerticalStrut(2));
        p.add(v); p.add(Box.createVerticalStrut(8));
    }

    void detailRow(JPanel p, String key, String value) {
        JLabel k = new JLabel(key);
        k.setFont(UITheme.FONT_LABEL);
        k.setForeground(UITheme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY);
        v.setForeground(UITheme.TEXT_PRIMARY);
        p.add(k); p.add(v);
    }

    JButton colorBtn(String text, Color color) {
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
        return btn;
    }

    JButton primaryBtn(String text) { return colorBtn(text, UITheme.ACCENT_BLUE); }

    void showErr(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}