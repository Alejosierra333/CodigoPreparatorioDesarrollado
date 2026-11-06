package es.arangoya.airport;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import es.arangoya.airport.ui.AirportFrame;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing utiliza su aspecto por defecto.
        }

        SwingUtilities.invokeLater(() -> {
            AirportFrame frame = new AirportFrame();
            frame.setVisible(true);
        });
    }
}
