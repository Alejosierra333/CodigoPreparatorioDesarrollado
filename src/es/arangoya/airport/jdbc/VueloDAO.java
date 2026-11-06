package es.arangoya.airport.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import es.arangoya.airport.modelo.Vuelo;

public class VueloDAO {

    public List<Vuelo> listar() throws Exception {
        List<Vuelo> vuelos = new ArrayList<>();

        Connection conexion = ConexionBD.abrir();
        Statement statement = conexion.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT * FROM vuelos ORDER BY fecha_programada");

        while (rs.next()) {
            Vuelo vuelo = new Vuelo(
                    rs.getInt("id"),
                    rs.getString("codigo"),
                    rs.getString("origen"),
                    rs.getString("destino"),
                    rs.getTimestamp("fecha_programada").toLocalDateTime(),
                    rs.getString("estado"),
                    rs.getString("puerta"),
                    rs.getString("aeronave"),
                    rs.getInt("capacidad"));

            vuelos.add(vuelo);
        }

        rs.close();
        statement.close();
        conexion.close();

        return vuelos;
    }

    public int ocupacion(int vueloId) throws Exception {
        Connection conexion = ConexionBD.abrir();
        Statement statement = conexion.createStatement();

        ResultSet rs = statement.executeQuery(
                "SELECT COUNT(*) total FROM embarques WHERE vuelo_id=" + vueloId);

        int total = 0;

        if (rs.next()) {
            total = rs.getInt("total");
        }

        rs.close();
        statement.close();
        conexion.close();

        return total;
    }

    public void estado(int id, String estado) throws Exception {
        Connection conexion = ConexionBD.abrir();
        Statement statement = conexion.createStatement();

        statement.executeUpdate(
                "UPDATE vuelos SET estado='" + estado + "' WHERE id=" + id);

        statement.close();
        conexion.close();
    }
}
