package es.arangoya.airport.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

public final class AirportTheme {

    public static final Color BACKGROUND = new Color(239, 243, 246);
    public static final Color CARD = Color.WHITE;
    public static final Color NAVY = new Color(24, 49, 67);
    public static final Color NAVY_SOFT = new Color(43, 68, 84);
    public static final Color ACCENT = new Color(24, 115, 153);
    public static final Color TEXT = new Color(36, 47, 54);
    public static final Color MUTED = new Color(103, 119, 128);
    public static final Color BORDER = new Color(216, 224, 229);

    public static final Color BLUE = new Color(35, 118, 180);
    public static final Color ORANGE = new Color(219, 145, 31);
    public static final Color GREEN = new Color(45, 143, 87);
    public static final Color PURPLE = new Color(116, 80, 164);
    public static final Color RED = new Color(193, 67, 62);
    public static final Color GRAY = new Color(105, 118, 126);

    private AirportTheme() {
    }

    public static Font font(int style, float size) {
        return new Font("SansSerif", style, Math.round(size));
    }

    public static void prepararTarjeta(JComponent component) {
        component.setBackground(CARD);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
    }

    public static void prepararBoton(JButton button, boolean primary) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(font(Font.BOLD, 12));

        if (primary) {
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(248, 250, 251));
            button.setForeground(TEXT);
        }

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    public static Color colorEstado(String estado) {
        if ("BOARDING".equals(estado)) {
            return BLUE;
        }
        if ("TAXI".equals(estado)) {
            return ORANGE;
        }
        if ("DESPEGANDO".equals(estado)) {
            return GREEN;
        }
        if ("ATERRIZANDO".equals(estado)) {
            return PURPLE;
        }
        if ("RETRASADO".equals(estado)) {
            return RED;
        }
        return GRAY;
    }
}
