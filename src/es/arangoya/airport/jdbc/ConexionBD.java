package es.arangoya.airport.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    public static String URL = "jdbc:mysql://localhost:3306/airport_ops";
    public static String USUARIO = "root";
    public static String PASSWORD = "root";

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
