package es.arangoya.airport.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import es.arangoya.airport.modelo.Pasajero;

public class PasajeroDAO {

    public List<Pasajero> porVuelo(int vueloId) throws SQLException {
        List<Pasajero> pasajeros = new ArrayList<>();

        try(Connection conexion = ConexionBD.abrir()){

        String sql = "SELECT p.* "
                + "FROM pasajeros p "
                + "JOIN embarques e ON e.pasajero_id=p.id "
                + "WHERE e.vuelo_id=" + vueloId + " "
                + "ORDER BY p.apellidos,p.nombre";

        PreparedStatement statement = conexion.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery(sql);

        
        while (rs.next()) {
            Pasajero pasajero = new Pasajero(
                    rs.getInt("id"),
                    rs.getString("documento"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("email"));

            pasajeros.add(pasajero);
        }

        rs.close();
        statement.close();
        conexion.close();

        return pasajeros;
        
    }catch (SQLException e) {
    	System.err.println(e.getMessage());
    	return null;
    }
    }
}
