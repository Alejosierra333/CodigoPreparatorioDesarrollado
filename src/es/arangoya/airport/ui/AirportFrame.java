package es.arangoya.airport.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import es.arangoya.airport.legacy.ImportadorDat;
import es.arangoya.airport.legacy.ResultadoImportacion;
import es.arangoya.airport.modelo.Pasajero;
import es.arangoya.airport.modelo.Vuelo;
import es.arangoya.airport.servicio.AirportService;

public class AirportFrame extends JFrame {

    private final AirportService service = new AirportService();
    private final AirportMapPanel mapa = new AirportMapPanel();

    private List<Vuelo> vuelos = new ArrayList<>();
    private final Map<Integer, Integer> ocupaciones = new HashMap<>();

    private final DefaultTableModel modeloVuelos = new DefaultTableModel(
            new Object[] {
                    "Vuelo", "Origen", "Destino", "Hora",
                    "Estado", "Puerta", "Ocupación"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaVuelos = new JTable(modeloVuelos);
    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(modeloVuelos);

    private final JTextField txtBuscar = new JTextField(15);
    private final JComboBox<String> filtroEstado = new JComboBox<>(new String[] {
            "Todos", "PROGRAMADO", "BOARDING", "TAXI", "DESPEGANDO",
            "ATERRIZANDO", "LLEGADA", "RETRASADO", "SALIDO"
    });

    private final JLabel reloj = new JLabel();

    private final JLabel statVuelos = new JLabel("0");
    private final JLabel statOperacion = new JLabel("0");
    private final JLabel statRetrasados = new JLabel("0");
    private final JLabel statOcupacion = new JLabel("0%");

    private final JLabel detalleCodigo = new JLabel("—");
    private final JLabel detalleRuta = new JLabel("Selecciona un vuelo");
    private final JLabel detalleEstado = new JLabel("SIN SELECCIÓN");
    private final JLabel detalleHora = new JLabel("—");
    private final JLabel detallePuerta = new JLabel("—");
    private final JLabel detalleAeronave = new JLabel("—");
    private final JLabel detalleOcupacion = new JLabel("—");

    private LocalDateTime sim = LocalDateTime.of(2026, 11, 12, 14, 0);
    private int velocidad = 5;
    private boolean simulacionActiva = true;

    public AirportFrame() {
        setTitle("BIO Airport Operations");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1540, 860);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);

        getContentPane().setBackground(AirportTheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);

        configurarTabla();
        configurarFiltros();
        configurarSimulacion();

        cargar();
    }

    private JPanel crearCabecera() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(AirportTheme.BACKGROUND);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AirportTheme.NAVY);
        barra.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel marca = new JPanel(new GridLayout(0, 1, 0, 2));
        marca.setOpaque(false);

        JLabel titulo = new JLabel("BIO AIRPORT OPERATIONS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(AirportTheme.font(Font.BOLD, 20));

        JLabel subtitulo = new JLabel("Panel operativo · Vuelos, puertas y embarques");
        subtitulo.setForeground(new Color(191, 208, 218));
        subtitulo.setFont(AirportTheme.font(Font.PLAIN, 11));

        marca.add(titulo);
        marca.add(subtitulo);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setOpaque(false);

        reloj.setOpaque(true);
        reloj.setBackground(AirportTheme.NAVY_SOFT);
        reloj.setForeground(Color.WHITE);
        reloj.setFont(AirportTheme.font(Font.BOLD, 12));
        reloj.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JButton btnSimulacion = new JButton("Pausar simulación");
        JButton btnImportar = new JButton("Cargar DAT");
        JButton btnRecargar = new JButton("Recargar BD");

        AirportTheme.prepararBoton(btnSimulacion, false);
        AirportTheme.prepararBoton(btnImportar, false);
        AirportTheme.prepararBoton(btnRecargar, false);

        JComboBox<String> comboVelocidad = new JComboBox<>(new String[] {
                "x1", "x5", "x20", "x60"
        });
        comboVelocidad.setSelectedItem("x5");
        comboVelocidad.setToolTipText("Velocidad del reloj y de la animación");

        btnSimulacion.addActionListener(e -> {
            simulacionActiva = !simulacionActiva;
            btnSimulacion.setText(
                    simulacionActiva ? "Pausar simulación" : "Reanudar simulación");
        });

        comboVelocidad.addActionListener(e -> {
            String seleccion = (String) comboVelocidad.getSelectedItem();
            velocidad = Integer.parseInt(seleccion.substring(1));
        });

        btnImportar.addActionListener(e -> importar());
        btnRecargar.addActionListener(e -> cargar());

        controles.add(reloj);
        controles.add(new JLabel(" "));
        controles.add(comboVelocidad);
        controles.add(btnSimulacion);
        controles.add(btnImportar);
        controles.add(btnRecargar);

        barra.add(marca, BorderLayout.WEST);
        barra.add(controles, BorderLayout.EAST);

        JPanel resumen = new JPanel(new GridLayout(1, 4, 10, 0));
        resumen.setBackground(AirportTheme.BACKGROUND);
        resumen.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        resumen.add(crearTarjetaResumen("Vuelos cargados", statVuelos));
        resumen.add(crearTarjetaResumen("En operación", statOperacion));
        resumen.add(crearTarjetaResumen("Retrasados", statRetrasados));
        resumen.add(crearTarjetaResumen("Ocupación media", statOcupacion));

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(resumen, BorderLayout.SOUTH);

        return contenedor;
    }

    private JPanel crearTarjetaResumen(String titulo, JLabel valor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 5));
        AirportTheme.prepararTarjeta(tarjeta);

        JLabel etiqueta = new JLabel(titulo.toUpperCase());
        etiqueta.setForeground(AirportTheme.MUTED);
        etiqueta.setFont(AirportTheme.font(Font.BOLD, 10));

        valor.setForeground(AirportTheme.NAVY);
        valor.setFont(AirportTheme.font(Font.BOLD, 24));

        tarjeta.add(etiqueta, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    private JSplitPane crearContenido() {
        JPanel panelVuelos = crearPanelVuelos();
        JPanel panelMapa = crearPanelMapa();
        JPanel panelDetalle = crearPanelDetalle();

        JSplitPane tablaMapa = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                panelVuelos,
                panelMapa);
        tablaMapa.setResizeWeight(0.43);
        tablaMapa.setDividerSize(6);
        tablaMapa.setBorder(null);

        JSplitPane total = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tablaMapa,
                panelDetalle);
        total.setResizeWeight(0.82);
        total.setDividerSize(6);
        total.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        total.setBackground(AirportTheme.BACKGROUND);

        return total;
    }

    private JPanel crearPanelVuelos() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        AirportTheme.prepararTarjeta(panel);

        JPanel cabecera = new JPanel(new BorderLayout(8, 0));
        cabecera.setOpaque(false);

        JPanel titulo = new JPanel(new GridLayout(0, 1));
        titulo.setOpaque(false);

        JLabel principal = new JLabel("Vuelos");
        principal.setFont(AirportTheme.font(Font.BOLD, 17));
        principal.setForeground(AirportTheme.TEXT);

        JLabel secundario = new JLabel("Selecciona un vuelo para consultar su operación");
        secundario.setFont(AirportTheme.font(Font.PLAIN, 10));
        secundario.setForeground(AirportTheme.MUTED);

        titulo.add(principal);
        titulo.add(secundario);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filtros.setOpaque(false);
        txtBuscar.setToolTipText("Buscar por vuelo, origen o destino");
        filtroEstado.setToolTipText("Filtrar por estado operativo");

        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);
        filtros.add(filtroEstado);

        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(filtros, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tablaVuelos);
        scroll.setBorder(BorderFactory.createLineBorder(AirportTheme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelMapa() {
        JPanel panel = new JPanel(new BorderLayout());
        AirportTheme.prepararTarjeta(panel);

        JLabel titulo = new JLabel("Vista operativa del aeropuerto");
        titulo.setFont(AirportTheme.font(Font.BOLD, 17));
        titulo.setForeground(AirportTheme.TEXT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(mapa, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        AirportTheme.prepararTarjeta(panel);

        JPanel superior = new JPanel(new GridLayout(0, 1, 0, 3));
        superior.setOpaque(false);

        detalleCodigo.setFont(AirportTheme.font(Font.BOLD, 28));
        detalleCodigo.setForeground(AirportTheme.NAVY);

        detalleRuta.setFont(AirportTheme.font(Font.BOLD, 14));
        detalleRuta.setForeground(AirportTheme.TEXT);

        detalleEstado.setOpaque(true);
        detalleEstado.setForeground(Color.WHITE);
        detalleEstado.setHorizontalAlignment(JLabel.CENTER);
        detalleEstado.setFont(AirportTheme.font(Font.BOLD, 11));
        detalleEstado.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        superior.add(new JLabel("OPERACIÓN DEL VUELO"));
        superior.add(detalleCodigo);
        superior.add(detalleRuta);
        superior.add(detalleEstado);

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        datos.add(crearDato("Hora programada", detalleHora), gbc);
        gbc.gridy++;
        datos.add(crearDato("Puerta", detallePuerta), gbc);
        gbc.gridy++;
        datos.add(crearDato("Aeronave", detalleAeronave), gbc);
        gbc.gridy++;
        datos.add(crearDato("Ocupación", detalleOcupacion), gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        datos.add(new JPanel(), gbc);

        JPanel acciones = new JPanel(new GridLayout(0, 1, 0, 7));
        acciones.setOpaque(false);

        JButton btnPasajeros = new JButton("Ver pasajeros");
        JButton btnEstado = new JButton("Avanzar estado del vuelo");

        AirportTheme.prepararBoton(btnPasajeros, false);
        AirportTheme.prepararBoton(btnEstado, true);

        btnPasajeros.addActionListener(e -> mostrarPasajeros());
        btnEstado.addActionListener(e -> avanzarEstado());

        acciones.add(btnPasajeros);
        acciones.add(btnEstado);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(datos, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearDato(String nombre, JLabel valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, AirportTheme.BORDER));

        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setForeground(AirportTheme.MUTED);
        etiqueta.setFont(AirportTheme.font(Font.PLAIN, 11));

        valor.setForeground(AirportTheme.TEXT);
        valor.setFont(AirportTheme.font(Font.BOLD, 13));
        valor.setHorizontalAlignment(JLabel.RIGHT);

        panel.add(etiqueta, BorderLayout.WEST);
        panel.add(valor, BorderLayout.EAST);
        return panel;
    }

    private void configurarTabla() {
        tablaVuelos.setRowSorter(sorter);
        tablaVuelos.setRowHeight(30);
        tablaVuelos.setShowVerticalLines(false);
        tablaVuelos.setGridColor(AirportTheme.BORDER);
        tablaVuelos.setSelectionBackground(new Color(219, 235, 244));
        tablaVuelos.setSelectionForeground(AirportTheme.TEXT);
        tablaVuelos.setFont(AirportTheme.font(Font.PLAIN, 11));
        tablaVuelos.getTableHeader().setFont(AirportTheme.font(Font.BOLD, 11));
        tablaVuelos.getTableHeader().setBackground(new Color(246, 248, 249));
        tablaVuelos.getTableHeader().setForeground(AirportTheme.TEXT);
        tablaVuelos.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);

        tablaVuelos.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaVuelos.getColumnModel().getColumn(1).setPreferredWidth(50);
        tablaVuelos.getColumnModel().getColumn(2).setPreferredWidth(50);
        tablaVuelos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaVuelos.getColumnModel().getColumn(4).setPreferredWidth(105);
        tablaVuelos.getColumnModel().getColumn(5).setPreferredWidth(50);
        tablaVuelos.getColumnModel().getColumn(6).setPreferredWidth(135);

        tablaVuelos.getColumnModel().getColumn(1).setCellRenderer(centro);
        tablaVuelos.getColumnModel().getColumn(2).setCellRenderer(centro);
        tablaVuelos.getColumnModel().getColumn(3).setCellRenderer(centro);
        tablaVuelos.getColumnModel().getColumn(4).setCellRenderer(new EstadoVueloRenderer());
        tablaVuelos.getColumnModel().getColumn(5).setCellRenderer(centro);
        tablaVuelos.getColumnModel().getColumn(6).setCellRenderer(new OcupacionRenderer());

        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
    }

    private void configurarFiltros() {
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltro();
            }
        });

        filtroEstado.addActionListener(e -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText().trim();
        String estado = (String) filtroEstado.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filtros = new ArrayList<>();

        if (!texto.isEmpty()) {
            filtros.add(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(texto),
                    0, 1, 2));
        }

        if (estado != null && !"Todos".equals(estado)) {
            filtros.add(RowFilter.regexFilter(
                    "^" + java.util.regex.Pattern.quote(estado) + "$",
                    4));
        }

        sorter.setRowFilter(filtros.isEmpty()
                ? null
                : RowFilter.andFilter(filtros));
    }

    private void configurarSimulacion() {
        Timer timer = new Timer(40, e -> {
            if (!simulacionActiva) {
                return;
            }

            sim = sim.plusSeconds(velocidad);
            reloj.setText(
                    "SIM "
                            + sim.format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))
                            + " · x"
                            + velocidad);

            mapa.tick(sim, factorMovimiento());
        });

        timer.start();
    }

    private double factorMovimiento() {
        return switch (velocidad) {
            case 1 -> 1.0;
            case 5 -> 2.0;
            case 20 -> 4.0;
            case 60 -> 6.5;
            default -> 1.0;
        };
    }

    private void cargar() {
        try {
            vuelos = service.vuelos();
            ocupaciones.clear();
            modeloVuelos.setRowCount(0);

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (Vuelo vuelo : vuelos) {
                int ocupacion = service.ocupacion(vuelo.getId());
                ocupaciones.put(vuelo.getId(), ocupacion);

                modeloVuelos.addRow(new Object[] {
                        vuelo.getCodigo(),
                        vuelo.getOrigen(),
                        vuelo.getDestino(),
                        vuelo.getFecha().format(formato),
                        vuelo.getEstado(),
                        vuelo.getPuerta(),
                        ocupacion + "/" + vuelo.getCapacidad()
                });
            }

            mapa.setVuelos(vuelos);
            mapa.sincronizar(sim);
            actualizarResumen();

            if (tablaVuelos.getRowCount() > 0) {
                tablaVuelos.setRowSelectionInterval(0, 0);
            } else {
                limpiarDetalle();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se han podido cargar los vuelos.\n\n"
                            + e.getMessage()
                            + "\n\nComprueba la base de datos, Connector/J y la configuración de ConexionBD.",
                    "Error de carga",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarResumen() {
        int enOperacion = 0;
        int retrasados = 0;
        int plazas = 0;
        int ocupados = 0;

        for (Vuelo vuelo : vuelos) {
            String estado = vuelo.getEstado();

            if ("BOARDING".equals(estado)
                    || "TAXI".equals(estado)
                    || "DESPEGANDO".equals(estado)
                    || "ATERRIZANDO".equals(estado)) {
                enOperacion++;
            }

            if ("RETRASADO".equals(estado)) {
                retrasados++;
            }

            plazas += vuelo.getCapacidad();
            ocupados += ocupaciones.getOrDefault(vuelo.getId(), 0);
        }

        int ocupacionMedia = plazas == 0
                ? 0
                : (int) Math.round(ocupados * 100.0 / plazas);

        statVuelos.setText(String.valueOf(vuelos.size()));
        statOperacion.setText(String.valueOf(enOperacion));
        statRetrasados.setText(String.valueOf(retrasados));
        statOcupacion.setText(ocupacionMedia + "%");
    }

    private Vuelo seleccionado() {
        int filaVista = tablaVuelos.getSelectedRow();

        if (filaVista < 0) {
            return null;
        }

        int filaModelo = tablaVuelos.convertRowIndexToModel(filaVista);
        String codigo = modeloVuelos.getValueAt(filaModelo, 0).toString();

        for (Vuelo vuelo : vuelos) {
            if (vuelo.getCodigo().equals(codigo)) {
                return vuelo;
            }
        }

        return null;
    }

    private void mostrarDetalle() {
        Vuelo vuelo = seleccionado();

        if (vuelo == null) {
            limpiarDetalle();
            return;
        }

        int ocupados = ocupaciones.getOrDefault(vuelo.getId(), 0);
        int porcentaje = vuelo.getCapacidad() == 0
                ? 0
                : (int) Math.round(ocupados * 100.0 / vuelo.getCapacidad());

        detalleCodigo.setText(vuelo.getCodigo());
        detalleRuta.setText(vuelo.getOrigen() + "  →  " + vuelo.getDestino());
        detalleHora.setText(vuelo.getFecha().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")));
        detallePuerta.setText(vuelo.getPuerta());
        detalleAeronave.setText(vuelo.getAeronave());
        detalleOcupacion.setText(
                ocupados + " / " + vuelo.getCapacidad() + " · " + porcentaje + "%");

        detalleEstado.setText(vuelo.getEstado());
        detalleEstado.setBackground(AirportTheme.colorEstado(vuelo.getEstado()));
    }

    private void limpiarDetalle() {
        detalleCodigo.setText("—");
        detalleRuta.setText("Selecciona un vuelo");
        detalleEstado.setText("SIN SELECCIÓN");
        detalleEstado.setBackground(AirportTheme.GRAY);
        detalleHora.setText("—");
        detallePuerta.setText("—");
        detalleAeronave.setText("—");
        detalleOcupacion.setText("—");
    }

    private void mostrarPasajeros() {
        Vuelo vuelo = seleccionado();

        if (vuelo == null) {
            return;
        }

        try {
            List<Pasajero> pasajeros = service.pasajeros(vuelo.getId());
            mostrarDialogoPasajeros(vuelo, pasajeros);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se han podido cargar los pasajeros.\n" + e.getMessage(),
                    "Pasajeros",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoPasajeros(Vuelo vuelo, List<Pasajero> pasajeros) {
        JDialog dialogo = new JDialog(this, "Pasajeros · " + vuelo.getCodigo(), true);
        dialogo.setSize(850, 520);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(0, 8));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(AirportTheme.NAVY);
        cabecera.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel(
                vuelo.getCodigo()
                        + " · "
                        + vuelo.getOrigen()
                        + " → "
                        + vuelo.getDestino());
        titulo.setForeground(Color.WHITE);
        titulo.setFont(AirportTheme.font(Font.BOLD, 17));

        JLabel total = new JLabel(pasajeros.size() + " pasajeros");
        total.setForeground(new Color(200, 215, 224));

        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(total, BorderLayout.EAST);

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[] { "Documento", "Nombre", "Apellidos", "Email" }, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Pasajero pasajero : pasajeros) {
            modelo.addRow(new Object[] {
                    pasajero.getDocumento(),
                    pasajero.getNombre(),
                    pasajero.getApellidos(),
                    pasajero.getEmail()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(27);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFont(AirportTheme.font(Font.PLAIN, 11));
        tabla.getTableHeader().setFont(AirportTheme.font(Font.BOLD, 11));
        tabla.setShowVerticalLines(false);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        dialogo.add(cabecera, BorderLayout.NORTH);
        dialogo.add(scroll, BorderLayout.CENTER);
        dialogo.setVisible(true);
    }

    private void avanzarEstado() {
        Vuelo vuelo = seleccionado();

        if (vuelo == null) {
            return;
        }

        String siguiente;
        boolean salida = "BIO".equals(vuelo.getOrigen());

        if (salida) {
            siguiente = switch (vuelo.getEstado()) {
                case "PROGRAMADO", "RETRASADO" -> "BOARDING";
                case "BOARDING" -> "TAXI";
                case "TAXI" -> "DESPEGANDO";
                case "DESPEGANDO" -> "SALIDO";
                default -> "BOARDING";
            };
        } else {
            siguiente = switch (vuelo.getEstado()) {
                case "ATERRIZANDO" -> "TAXI";
                case "TAXI" -> "LLEGADA";
                case "LLEGADA" -> "LLEGADA";
                default -> "ATERRIZANDO";
            };
        }

        cambiarEstado(siguiente);
    }

    private void cambiarEstado(String estado) {
        Vuelo vuelo = seleccionado();

        if (vuelo == null) {
            return;
        }

        try {
            service.estado(vuelo.getId(), estado);
            vuelo.setEstado(estado);
            cargar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cambiar el estado del vuelo.",
                    "Operación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importar() {
        ResultadoImportacion resultado = new ImportadorDat().importar(
                "datos/estado_inicial_airport.dat");

        JOptionPane.showMessageDialog(
                this,
                resultado.toString(),
                "Importación DAT",
                resultado.getErrores() == 0
                        ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.WARNING_MESSAGE);

        cargar();
    }
}
