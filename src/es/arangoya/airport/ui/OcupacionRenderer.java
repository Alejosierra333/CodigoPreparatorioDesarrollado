package es.arangoya.airport.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class OcupacionRenderer extends JProgressBar implements TableCellRenderer {

    public OcupacionRenderer() {
        setMinimum(0);
        setMaximum(100);
        setStringPainted(true);
        setBorderPainted(false);
        setFont(AirportTheme.font(Font.BOLD, 10));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        String texto = value == null ? "0/0" : value.toString();
        int ocupados = 0;
        int capacidad = 0;

        try {
            String[] partes = texto.split("/");
            ocupados = Integer.parseInt(partes[0].trim());
            capacidad = Integer.parseInt(partes[1].trim());
        } catch (Exception e) {
        }

        int porcentaje = capacidad == 0
                ? 0
                : (int) Math.round(ocupados * 100.0 / capacidad);

        setValue(porcentaje);
        setString(ocupados + " / " + capacidad + " · " + porcentaje + "%");
        setBackground(new java.awt.Color(229, 235, 238));
        setForeground(porcentaje >= 85
                ? AirportTheme.ORANGE
                : AirportTheme.ACCENT);

        return this;
    }
}
