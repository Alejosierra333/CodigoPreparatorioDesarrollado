package es.arangoya.airport.modelo;

import java.time.LocalDateTime;

public class Vuelo {

    private int id;
    private String codigo;
    private String origen;
    private String destino;
    private LocalDateTime fecha;
    private String estado;
    private String puerta;
    private String aeronave;
    private int capacidad;

    public Vuelo(int id, String codigo, String origen, String destino,
            LocalDateTime fecha, String estado, String puerta,
            String aeronave, int capacidad) {

        this.id = id;
        this.codigo = codigo;
        this.origen = origen;
        this.destino = destino;
        this.fecha = fecha;
        this.estado = estado;
        this.puerta = puerta;
        this.aeronave = aeronave;
        this.capacidad = capacidad;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getPuerta() {
        return puerta;
    }

    public String getAeronave() {
        return aeronave;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return codigo + " · " + origen + " → " + destino;
    }
}
