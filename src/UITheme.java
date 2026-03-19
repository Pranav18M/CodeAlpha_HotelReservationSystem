// UITheme.java
// Central design system — all colors, fonts, and borders in one place
// Changing a color here updates the entire application

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UITheme {

    // ── Colors ────────────────────────────────────────────────────────────
    public static final Color BG_MAIN        = new Color(245, 247, 252);
    public static final Color BG_SIDEBAR     = new Color( 15,  23,  42);
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_HEADER      = new Color( 15,  23,  42);
    public static final Color BG_INPUT       = new Color(248, 250, 255);

    public static final Color ACCENT_GOLD    = new Color(212, 160,  23);
    public static final Color ACCENT_BLUE    = new Color( 37,  99, 235);
    public static final Color ACCENT_GREEN   = new Color( 22, 163,  74);
    public static final Color ACCENT_RED     = new Color(220,  38,  38);
    public static final Color ACCENT_ORANGE  = new Color(234,  88,  12);
    public static final Color ACCENT_PURPLE  = new Color(124,  58, 237);

    public static final Color COLOR_STANDARD     = new Color( 59, 130, 246);
    public static final Color COLOR_DELUXE       = new Color(168,  85, 247);
    public static final Color COLOR_SUITE        = new Color(245, 158,  11);
    public static final Color COLOR_PRESIDENTIAL = new Color(220,  38,  38);

    public static final Color TEXT_PRIMARY   = new Color( 15,  23,  42);
    public static final Color TEXT_SECONDARY = new Color( 71,  85, 105);
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);
    public static final Color BORDER_LIGHT   = new Color(226, 232, 240);
    public static final Color BORDER_MEDIUM  = new Color(203, 213, 225);

    // ── Fonts ─────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD       = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD,  11);

    // ── Borders ───────────────────────────────────────────────────────────
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_MEDIUM, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    public static Border inputBorderFocused() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 2, true),
            BorderFactory.createEmptyBorder(7, 11, 7, 11)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    public static Color getRoomTypeColor(Room.Type type) {
        return switch (type) {
            case STANDARD     -> COLOR_STANDARD;
            case DELUXE       -> COLOR_DELUXE;
            case SUITE        -> COLOR_SUITE;
            case PRESIDENTIAL -> COLOR_PRESIDENTIAL;
        };
    }

    public static Color getStatusColor(Reservation.Status status) {
        return switch (status) {
            case CONFIRMED   -> ACCENT_GREEN;
            case CANCELLED   -> ACCENT_RED;
            case CHECKED_IN  -> ACCENT_BLUE;
            case CHECKED_OUT -> TEXT_MUTED;
        };
    }
}