package es.arangoya.airport.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class AirportLayout {

    public static final double WIDTH = 1000.0;
    public static final double HEIGHT = 700.0;

    private static final int NUM_GATES = 8;

    private final Rectangle2D.Double runway =
            new Rectangle2D.Double(55, 72, 890, 86);

    private final Rectangle2D.Double apron =
            new Rectangle2D.Double(105, 382, 790, 185);

    private final Rectangle2D.Double terminal =
            new Rectangle2D.Double(145, 585, 710, 82);

    public Rectangle2D.Double getRunway() {
        return runway;
    }

    public Rectangle2D.Double getApron() {
        return apron;
    }

    public Rectangle2D.Double getTerminal() {
        return terminal;
    }

    public double runwayY() {
        return runway.getCenterY();
    }

    public double taxiwayY() {
        return 286;
    }

    public double apronTaxiY() {
        return 355;
    }

    public double runwayEntryX() {
        return 120;
    }

    public double runwayExitX() {
        return 865;
    }

    public double leftApronConnectorX() {
        return 250;
    }

    public double rightApronConnectorX() {
        return 750;
    }

    public Point2D.Double departureRunwayStart() {
        return new Point2D.Double(
                runwayEntryX(),
                runwayY());
    }

    public Point2D.Double arrivalApproachStart() {
        return new Point2D.Double(
                -85,
                runwayY());
    }

    public Point2D.Double arrivalRunwayExit() {
        return new Point2D.Double(
                runwayExitX(),
                runwayY());
    }

    public Point2D.Double gate(int index) {

        int gate =
                Math.max(
                        0,
                        Math.min(
                                NUM_GATES - 1,
                                index));

        double left = 185;
        double right = 815;
        double step = (right - left) / (NUM_GATES - 1);

        return new Point2D.Double(
                left + gate * step,
                515);
    }

    public double connectorForGate(int gateIndex) {

        return gateIndex <= 3
                ? leftApronConnectorX()
                : rightApronConnectorX();
    }

    public Point2D.Double tower() {
        return new Point2D.Double(
                885,
                525);
    }
}
