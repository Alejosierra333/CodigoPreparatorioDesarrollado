package es.arangoya.airport.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import es.arangoya.airport.modelo.Vuelo;

public class AirportMapPanel extends JPanel {

    private static final String AEROPUERTO_LOCAL = "BIO";
    private static final int MAX_AVIONES_VISIBLES = 9;

    private final AirportLayout layout = new AirportLayout();

    private final List<Vuelo> todosLosVuelos = new ArrayList<>();
    private final Map<Integer, AvionVisual> aviones = new LinkedHashMap<>();

    private LocalDateTime horaSimulada;
    private long ultimoMinuto = Long.MIN_VALUE;

    public AirportMapPanel() {
        setBackground(new Color(226, 232, 235));
    }

    public void setVuelos(List<Vuelo> vuelos) {
        todosLosVuelos.clear();
        todosLosVuelos.addAll(vuelos);
        ultimoMinuto = Long.MIN_VALUE;
    }

    public void sincronizar(LocalDateTime hora) {
        horaSimulada = hora;
        reconstruirAviones();
        repaint();
    }

    public void tick(LocalDateTime hora, double factorMovimiento) {
        horaSimulada = hora;

        long minuto =
                hora.getDayOfYear() * 24L * 60L
                + hora.getHour() * 60L
                + hora.getMinute();

        if (minuto != ultimoMinuto) {
            ultimoMinuto = minuto;
            reconstruirAviones();
        }

        for (AvionVisual avion : aviones.values()) {
            avion.tick(factorMovimiento);
        }

        repaint();
    }

    private void reconstruirAviones() {

        if (horaSimulada == null) {
            return;
        }

        List<Vuelo> candidatos =
                new ArrayList<>(todosLosVuelos);

        candidatos.removeIf(
                vuelo -> "SALIDO".equals(vuelo.getEstado()));

        candidatos.sort(
                Comparator.comparingLong(
                        vuelo -> Math.abs(
                                Duration.between(
                                        horaSimulada,
                                        vuelo.getFecha())
                                        .toMinutes()
                        )
                )
        );

        List<Vuelo> visibles = new ArrayList<>();

        for (Vuelo vuelo : candidatos) {

            long diferencia =
                    Duration.between(
                            horaSimulada,
                            vuelo.getFecha())
                            .toMinutes();

            if (diferencia >= -45 && diferencia <= 90) {
                visibles.add(vuelo);
            }

            if (visibles.size() == MAX_AVIONES_VISIBLES) {
                break;
            }
        }

        if (visibles.isEmpty() && !candidatos.isEmpty()) {
            visibles.addAll(
                    candidatos.subList(
                            0,
                            Math.min(
                                    7,
                                    candidatos.size())));
        }

        Map<Integer, AvionVisual> nuevos =
                new LinkedHashMap<>();

        for (Vuelo vuelo : visibles) {

            AvionVisual avion =
                    aviones.get(vuelo.getId());

            int gateIndex =
                    indicePuerta(vuelo.getPuerta());

            if (avion == null) {

                Point2D.Double posicion =
                        posicionInicial(
                                vuelo,
                                gateIndex);

                avion =
                        new AvionVisual(
                                vuelo,
                                posicion.x,
                                posicion.y);
            }

            String claveRuta =
                    vuelo.getEstado()
                            + "-"
                            + vuelo.getPuerta();

            if (!claveRuta.equals(avion.getEstadoRuta())) {

                avion.setEstadoRuta(claveRuta);

                configurarRuta(
                        avion,
                        vuelo,
                        gateIndex);
            }

            nuevos.put(
                    vuelo.getId(),
                    avion);
        }

        aviones.clear();
        aviones.putAll(nuevos);
    }

    private boolean esSalida(Vuelo vuelo) {
        return AEROPUERTO_LOCAL.equals(
                vuelo.getOrigen());
    }

    private boolean esLlegada(Vuelo vuelo) {
        return AEROPUERTO_LOCAL.equals(
                vuelo.getDestino());
    }

    private int indicePuerta(String puerta) {

        if (puerta != null
                && puerta.matches("[A-Z]\\d{2}")) {

            try {
                return Math.max(
                        0,
                        Math.min(
                                7,
                                Integer.parseInt(
                                        puerta.substring(1)) - 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    private Point2D.Double posicionInicial(
            Vuelo vuelo,
            int gateIndex) {

        return switch (vuelo.getEstado()) {
            case "DESPEGANDO" ->
                    layout.departureRunwayStart();

            case "ATERRIZANDO" ->
                    layout.arrivalApproachStart();

            case "TAXI" -> esLlegada(vuelo)
                    ? layout.arrivalRunwayExit()
                    : layout.gate(gateIndex);

            default ->
                    layout.gate(gateIndex);
        };
    }

    private void configurarRuta(
            AvionVisual avion,
            Vuelo vuelo,
            int gateIndex) {

        List<Point2D.Double> ruta =
                new ArrayList<>();

        String estado =
                vuelo.getEstado();

        if ("TAXI".equals(estado)) {

            avion.setVelocidadBase(2.0);

            if (esLlegada(vuelo)) {
                configurarTaxiLlegada(
                        avion,
                        ruta,
                        gateIndex);
            } else {
                configurarTaxiSalida(
                        avion,
                        ruta,
                        gateIndex);
            }

        } else if ("DESPEGANDO".equals(estado)) {

            configurarDespegue(
                    avion,
                    ruta);

        } else if ("ATERRIZANDO".equals(estado)) {

            configurarAterrizaje(
                    avion,
                    ruta);

        } else {

            estacionarEnPuerta(
                    avion,
                    ruta,
                    gateIndex);
        }

        avion.setRuta(ruta);
    }

    private void configurarTaxiSalida(
            AvionVisual avion,
            List<Point2D.Double> ruta,
            int gateIndex) {

        Point2D.Double gate =
                layout.gate(gateIndex);

        double conector =
                layout.connectorForGate(gateIndex);

        avion.setPosicion(
                gate.x,
                gate.y);

        avion.setAngulo(
                -Math.PI / 2.0);

        ruta.add(new Point2D.Double(
                gate.x,
                layout.apronTaxiY()));

        ruta.add(new Point2D.Double(
                conector,
                layout.apronTaxiY()));

        ruta.add(new Point2D.Double(
                conector,
                layout.taxiwayY()));

        ruta.add(new Point2D.Double(
                layout.runwayEntryX(),
                layout.taxiwayY()));

        ruta.add(new Point2D.Double(
                layout.runwayEntryX(),
                layout.runwayY() + 48));
    }

    private void configurarTaxiLlegada(
            AvionVisual avion,
            List<Point2D.Double> ruta,
            int gateIndex) {

        Point2D.Double salida =
                layout.arrivalRunwayExit();

        Point2D.Double gate =
                layout.gate(gateIndex);

        double conector =
                layout.connectorForGate(gateIndex);

        avion.setPosicion(
                salida.x,
                salida.y);

        ruta.add(new Point2D.Double(
                layout.runwayExitX(),
                layout.taxiwayY()));

        ruta.add(new Point2D.Double(
                conector,
                layout.taxiwayY()));

        ruta.add(new Point2D.Double(
                conector,
                layout.apronTaxiY()));

        ruta.add(new Point2D.Double(
                gate.x,
                layout.apronTaxiY()));

        ruta.add(gate);
    }

    private void configurarDespegue(
            AvionVisual avion,
            List<Point2D.Double> ruta) {

        Point2D.Double inicio =
                layout.departureRunwayStart();

        avion.setPosicion(
                inicio.x,
                inicio.y);

        avion.setVelocidadBase(4.0);

        ruta.add(new Point2D.Double(
                430,
                layout.runwayY()));

        ruta.add(new Point2D.Double(
                900,
                layout.runwayY()));

        ruta.add(new Point2D.Double(
                1080,
                layout.runwayY() - 90));
    }

    private void configurarAterrizaje(
            AvionVisual avion,
            List<Point2D.Double> ruta) {

        Point2D.Double aproximacion =
                layout.arrivalApproachStart();

        avion.setPosicion(
                aproximacion.x,
                aproximacion.y);

        avion.setVelocidadBase(3.5);

        ruta.add(new Point2D.Double(
                90,
                layout.runwayY()));

        ruta.add(new Point2D.Double(
                510,
                layout.runwayY()));

        ruta.add(
                layout.arrivalRunwayExit());
    }

    private void estacionarEnPuerta(
            AvionVisual avion,
            List<Point2D.Double> ruta,
            int gateIndex) {

        Point2D.Double gate =
                layout.gate(gateIndex);

        avion.setPosicion(
                gate.x,
                gate.y);

        avion.setAngulo(
                -Math.PI / 2.0);

        avion.setVelocidadBase(1.4);

        ruta.add(gate);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D pantalla =
                (Graphics2D) g.create();

        pantalla.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        pantalla.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        dibujarFondoPanel(pantalla);

        AffineTransform transform =
                crearTransformacionEscalada();

        Graphics2D q =
                (Graphics2D) pantalla.create();

        q.transform(transform);

        dibujarTerreno(q);
        dibujarRunway(q);
        dibujarTaxiways(q);
        dibujarApron(q);
        dibujarTerminal(q);
        dibujarLeyenda(q);

        for (AvionVisual avion : aviones.values()) {
            dibujarAvion(q, avion);
        }

        q.dispose();
        pantalla.dispose();
    }

    private void dibujarFondoPanel(Graphics2D g) {

        g.setColor(new Color(226, 232, 235));
        g.fillRect(
                0,
                0,
                getWidth(),
                getHeight());
    }

    private AffineTransform crearTransformacionEscalada() {

        double margen = 16.0;

        double anchoDisponible =
                Math.max(
                        1,
                        getWidth() - margen * 2);

        double altoDisponible =
                Math.max(
                        1,
                        getHeight() - margen * 2);

        double escala =
                Math.min(
                        anchoDisponible / AirportLayout.WIDTH,
                        altoDisponible / AirportLayout.HEIGHT);

        double anchoEscalado =
                AirportLayout.WIDTH * escala;

        double altoEscalado =
                AirportLayout.HEIGHT * escala;

        double offsetX =
                (getWidth() - anchoEscalado) / 2.0;

        double offsetY =
                (getHeight() - altoEscalado) / 2.0;

        AffineTransform transform =
                new AffineTransform();

        transform.translate(
                offsetX,
                offsetY);

        transform.scale(
                escala,
                escala);

        return transform;
    }

    private void dibujarTerreno(Graphics2D q) {

        q.setColor(new Color(178, 197, 174));
        q.fillRoundRect(
                0,
                0,
                (int) AirportLayout.WIDTH,
                (int) AirportLayout.HEIGHT,
                22,
                22);

        q.setColor(new Color(167, 187, 163));

        for (int x = -250;
                x < AirportLayout.WIDTH;
                x += 75) {

            q.drawLine(
                    x,
                    0,
                    x + 280,
                    (int) AirportLayout.HEIGHT);
        }

        q.setColor(new Color(65, 78, 81));
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13));

        q.drawString(
                "BIO · AIRSIDE MAP",
                28,
                30);
    }

    private void dibujarRunway(Graphics2D q) {

        Rectangle2D.Double runway =
                layout.getRunway();

        q.setColor(new Color(48, 53, 56));
        q.fillRoundRect(
                (int) runway.x,
                (int) runway.y,
                (int) runway.width,
                (int) runway.height,
                12,
                12);

        q.setColor(new Color(239, 239, 234));

        for (int x = 180;
                x <= 760;
                x += 105) {

            q.fillRect(
                    x,
                    (int) layout.runwayY() - 3,
                    55,
                    6);
        }

        dibujarUmbral(
                q,
                72,
                true);

        dibujarUmbral(
                q,
                895,
                false);

        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        17));

        q.drawString(
                "12",
                105,
                (int) layout.runwayY() + 7);

        q.drawString(
                "30",
                870,
                (int) layout.runwayY() + 7);

        q.setColor(new Color(55, 65, 68));
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11));

        q.drawString(
                "RWY 12 / 30 · ACTIVA 12",
                58,
                60);
    }

    private void dibujarUmbral(
            Graphics2D q,
            int x,
            boolean izquierda) {

        int direccion =
                izquierda ? 1 : -1;

        for (int i = 0; i < 6; i++) {

            int px =
                    x + direccion * i * 10;

            q.fillRect(
                    px,
                    86,
                    5,
                    22);
        }
    }

    private void dibujarTaxiways(Graphics2D q) {

        q.setStroke(
                new BasicStroke(
                        30f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

        q.setColor(new Color(88, 96, 94));

        q.drawLine(
                95,
                (int) layout.taxiwayY(),
                905,
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.runwayEntryX(),
                160,
                (int) layout.runwayEntryX(),
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.runwayExitX(),
                160,
                (int) layout.runwayExitX(),
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.leftApronConnectorX(),
                (int) layout.taxiwayY(),
                (int) layout.leftApronConnectorX(),
                (int) layout.apronTaxiY());

        q.drawLine(
                (int) layout.rightApronConnectorX(),
                (int) layout.taxiwayY(),
                (int) layout.rightApronConnectorX(),
                (int) layout.apronTaxiY());

        q.setStroke(
                new BasicStroke(
                        2.2f));

        q.setColor(new Color(235, 187, 37));

        q.drawLine(
                95,
                (int) layout.taxiwayY(),
                905,
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.runwayEntryX(),
                160,
                (int) layout.runwayEntryX(),
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.runwayExitX(),
                160,
                (int) layout.runwayExitX(),
                (int) layout.taxiwayY());

        q.drawLine(
                (int) layout.leftApronConnectorX(),
                (int) layout.taxiwayY(),
                (int) layout.leftApronConnectorX(),
                (int) layout.apronTaxiY());

        q.drawLine(
                (int) layout.rightApronConnectorX(),
                (int) layout.taxiwayY(),
                (int) layout.rightApronConnectorX(),
                (int) layout.apronTaxiY());

        q.setColor(new Color(65, 72, 73));
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        10));

        q.drawString(
                "TAXIWAY A",
                70,
                260);
    }

    private void dibujarApron(Graphics2D q) {

        Rectangle2D.Double apron =
                layout.getApron();

        q.setColor(new Color(151, 158, 159));
        q.fillRoundRect(
                (int) apron.x,
                (int) apron.y,
                (int) apron.width,
                (int) apron.height,
                18,
                18);

        q.setColor(new Color(119, 126, 127));
        q.setStroke(new BasicStroke(1.2f));

        q.drawLine(
                130,
                (int) layout.apronTaxiY(),
                870,
                (int) layout.apronTaxiY());

        q.setColor(new Color(233, 190, 42));
        q.setStroke(new BasicStroke(1.7f));

        for (int i = 0; i < 8; i++) {

            Point2D.Double gate =
                    layout.gate(i);

            q.drawLine(
                    (int) gate.x,
                    (int) layout.apronTaxiY(),
                    (int) gate.x,
                    (int) gate.y + 20);

            q.drawLine(
                    (int) gate.x - 15,
                    (int) gate.y + 18,
                    (int) gate.x + 15,
                    (int) gate.y + 18);
        }

        q.setColor(new Color(68, 75, 77));
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        10));

        q.drawString(
                "APRON A",
                120,
                405);
    }

    private void dibujarTerminal(Graphics2D q) {

        Rectangle2D.Double terminal =
                layout.getTerminal();

        q.setColor(new Color(75, 87, 96));
        q.fillRoundRect(
                (int) terminal.x,
                (int) terminal.y,
                (int) terminal.width,
                (int) terminal.height,
                14,
                14);

        q.setColor(new Color(115, 151, 172));

        for (int x = 170;
                x <= 820;
                x += 38) {

            q.fillRect(
                    x,
                    605,
                    22,
                    20);
        }

        for (int i = 0; i < 8; i++) {

            Point2D.Double gate =
                    layout.gate(i);

            q.setColor(new Color(91, 100, 104));

            q.fillRoundRect(
                    (int) gate.x - 16,
                    557,
                    32,
                    32,
                    6,
                    6);

            q.setColor(new Color(235, 198, 52));
            q.setStroke(new BasicStroke(2f));

            q.drawLine(
                    (int) gate.x,
                    557,
                    (int) gate.x,
                    (int) gate.y + 28);

            q.setColor(new Color(43, 50, 52));
            q.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            11));

            q.drawString(
                    "A0" + (i + 1),
                    (int) gate.x - 11,
                    548);
        }

        q.setColor(Color.WHITE);
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        15));

        q.drawString(
                "TERMINAL A",
                165,
                650);

        dibujarTorre(q);
    }

    private void dibujarTorre(Graphics2D q) {

        Point2D.Double tower =
                layout.tower();

        q.setColor(new Color(103, 111, 114));

        q.fillRect(
                (int) tower.x - 10,
                (int) tower.y - 5,
                20,
                75);

        q.setColor(new Color(56, 69, 74));

        q.fillOval(
                (int) tower.x - 26,
                (int) tower.y - 22,
                52,
                30);

        q.setColor(Color.WHITE);
        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        9));

        q.drawString(
                "TWR",
                (int) tower.x - 11,
                (int) tower.y - 3);
    }

    private void dibujarLeyenda(Graphics2D q) {

        String[] estados = {
                "BOARDING",
                "TAXI",
                "DESPEGANDO",
                "ATERRIZANDO",
                "RETRASADO"
        };

        int x = 300;
        int y = 31;

        for (String estado : estados) {

            q.setColor(
                    colorEstado(
                            estado));

            q.fillOval(
                    x,
                    y - 8,
                    9,
                    9);

            q.setColor(
                    new Color(
                            55,
                            63,
                            65));

            q.setFont(
                    new Font(
                            "SansSerif",
                            Font.PLAIN,
                            9));

            q.drawString(
                    estado,
                    x + 14,
                    y);

            x += 115;
        }
    }

    private void dibujarAvion(
            Graphics2D q,
            AvionVisual avion) {

        AffineTransform anterior =
                q.getTransform();

        q.translate(
                avion.getX(),
                avion.getY());

        q.rotate(
                avion.getAngulo());

        Shape silueta =
                crearSiluetaAvion();

        q.setColor(new Color(248, 249, 247));
        q.fill(silueta);

        q.setColor(new Color(48, 55, 58));
        q.setStroke(new BasicStroke(1.3f));
        q.draw(silueta);

        q.setColor(
                colorEstado(
                        avion.getVuelo()
                                .getEstado()));

        q.fillRect(
                -17,
                -4,
                8,
                8);

        q.setTransform(anterior);

        dibujarEtiquetaAvion(
                q,
                avion);
    }

    private void dibujarEtiquetaAvion(
            Graphics2D q,
            AvionVisual avion) {

        int x =
                (int) avion.getX();

        int y =
                (int) avion.getY();

        String codigo =
                avion.getVuelo()
                        .getCodigo();

        q.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        9));

        int ancho =
                q.getFontMetrics()
                        .stringWidth(codigo);

        q.setColor(
                new Color(
                        30,
                        36,
                        38,
                        220));

        q.fillRoundRect(
                x - ancho / 2 - 5,
                y + 23,
                ancho + 10,
                16,
                7,
                7);

        q.setColor(Color.WHITE);

        q.drawString(
                codigo,
                x - ancho / 2,
                y + 34);
    }

    private Shape crearSiluetaAvion() {

        Path2D.Double p =
                new Path2D.Double();

        p.moveTo(25, 0);
        p.curveTo(20, -4, 15, -5, 9, -5);

        p.lineTo(2, -5);
        p.lineTo(-3, -21);
        p.lineTo(-8, -21);
        p.lineTo(-6, -5);

        p.lineTo(-14, -4);
        p.lineTo(-20, -10);
        p.lineTo(-23, -10);
        p.lineTo(-20, 0);

        p.lineTo(-23, 10);
        p.lineTo(-20, 10);
        p.lineTo(-14, 4);

        p.lineTo(-6, 5);
        p.lineTo(-8, 21);
        p.lineTo(-3, 21);
        p.lineTo(2, 5);

        p.lineTo(9, 5);
        p.curveTo(15, 5, 20, 4, 25, 0);

        p.closePath();

        return p;
    }

    private Color colorEstado(String estado) {

        if ("BOARDING".equals(estado)) {
            return new Color(31, 111, 179);
        }

        if ("TAXI".equals(estado)) {
            return new Color(222, 150, 28);
        }

        if ("DESPEGANDO".equals(estado)) {
            return new Color(38, 144, 84);
        }

        if ("ATERRIZANDO".equals(estado)) {
            return new Color(112, 77, 161);
        }

        if ("RETRASADO".equals(estado)) {
            return new Color(194, 61, 57);
        }

        return new Color(88, 100, 106);
    }
}
