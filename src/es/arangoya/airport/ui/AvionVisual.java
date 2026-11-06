package es.arangoya.airport.ui;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import es.arangoya.airport.modelo.Vuelo;

public class AvionVisual {

    private final Vuelo vuelo;

    private double x;
    private double y;
    private double angulo = -Math.PI / 2.0;

    private final List<Point2D.Double> ruta = new ArrayList<>();
    private int puntoActual;

    private String estadoRuta = "";
    private double velocidadBase = 2.0;

    public AvionVisual(Vuelo vuelo, double x, double y) {
        this.vuelo = vuelo;
        this.x = x;
        this.y = y;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngulo() {
        return angulo;
    }

    public String getEstadoRuta() {
        return estadoRuta;
    }

    public void setEstadoRuta(String estadoRuta) {
        this.estadoRuta = estadoRuta;
    }

    public void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setAngulo(double angulo) {
        this.angulo = angulo;
    }

    public void setVelocidadBase(double velocidadBase) {
        this.velocidadBase = velocidadBase;
    }

    public void setRuta(List<Point2D.Double> puntos) {
        ruta.clear();
        ruta.addAll(puntos);
        puntoActual = 0;

        if (!ruta.isEmpty()) {
            actualizarAngulo(ruta.get(0));
        }
    }

    public void tick(double factorMovimiento) {

        if (puntoActual >= ruta.size()) {
            return;
        }

        Point2D.Double objetivo = ruta.get(puntoActual);

        double dx = objetivo.x - x;
        double dy = objetivo.y - y;
        double distancia = Math.hypot(dx, dy);

        double paso = velocidadBase * factorMovimiento;

        if (distancia <= paso + 0.5) {
            x = objetivo.x;
            y = objetivo.y;
            puntoActual++;

            if (puntoActual < ruta.size()) {
                actualizarAngulo(ruta.get(puntoActual));
            }
            return;
        }

        angulo = Math.atan2(dy, dx);

        x += dx / distancia * paso;
        y += dy / distancia * paso;
    }

    private void actualizarAngulo(Point2D.Double objetivo) {

        double dx = objetivo.x - x;
        double dy = objetivo.y - y;

        if (Math.abs(dx) > 0.001 || Math.abs(dy) > 0.001) {
            angulo = Math.atan2(dy, dx);
        }
    }
}
