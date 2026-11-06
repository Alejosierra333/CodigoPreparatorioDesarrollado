package es.arangoya.airport.servicio;

import java.util.List;

import es.arangoya.airport.jdbc.PasajeroDAO;
import es.arangoya.airport.jdbc.VueloDAO;
import es.arangoya.airport.modelo.Pasajero;
import es.arangoya.airport.modelo.Vuelo;

public class AirportService {

    private VueloDAO vuelos = new VueloDAO();
    private PasajeroDAO pasajeros = new PasajeroDAO();

    public List<Vuelo> vuelos() throws Exception {
        return vuelos.listar();
    }

    public int ocupacion(int id) throws Exception {
        return vuelos.ocupacion(id);
    }

    public List<Pasajero> pasajeros(int id) throws Exception {
        return pasajeros.porVuelo(id);
    }

    public void estado(int id, String estado) throws Exception {
        vuelos.estado(id, estado);
    }
}
