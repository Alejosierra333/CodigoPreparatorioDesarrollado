package es.arangoya.airport.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class EstadoVueloRenderer extends DefaultTableCellRenderer {

    public EstadoVueloRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
        setFont(AirportTheme.font(Font.BOLD, 11));
        setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        String estado = value == null ? "" : value.toString();
        setText(estado);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(AirportTheme.colorEstado(estado));
            setForeground(java.awt.Color.WHITE);
        }

        return this;
    }
}
