package es.arangoya.airport.legacy;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import es.arangoya.airport.jdbc.ConexionBD;

public class ImportadorDat {

    public ResultadoImportacion importar(String ruta) {
        int vuelosInsertados = 0;
        int pasajerosInsertados = 0;
        int embarquesInsertados = 0;
        int errores = 0;

        DataInputStream in = null;
        Connection conexion = null;

        try {
            in = new DataInputStream(new FileInputStream(ruta));
            conexion = ConexionBD.abrir();

            int numeroVuelos = in.readInt();

            for (int i = 0; i < numeroVuelos; i++) {
                int id = in.readInt();
                String codigo = in.readUTF();
                String origen = in.readUTF();
                String destino = in.readUTF();
                long epoch = in.readLong();
                String estado = in.readUTF();
                String puerta = in.readUTF();
                String aeronave = in.readUTF();
                int capacidad = in.readInt();

                LocalDateTime fecha = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(epoch),
                        ZoneId.systemDefault());

                Statement statement = conexion.createStatement();

                String sql = "INSERT INTO vuelos(" 
                        + "id,codigo,origen,destino,fecha_programada,estado,puerta,aeronave,capacidad" 
                        + ") VALUES ("
                        + id + ",'"
                        + codigo + "','"
                        + origen + "','"
                        + destino + "','"
                        + fecha.toString().replace("T", " ") + "','"
                        + estado + "','"
                        + puerta + "','"
                        + aeronave + "',"
                        + capacidad + ")";

                try {
                    statement.executeUpdate(sql);
                    vuelosInsertados++;
                } catch (Exception e) {
                    errores++;
                    System.out.println("Error vuelo " + codigo);
                }

                statement.close();
            }

            int numeroPasajeros = in.readInt();

            for (int i = 0; i < numeroPasajeros; i++) {
                int id = in.readInt();
                String documento = in.readUTF();
                String nombre = in.readUTF();
                String apellidos = in.readUTF();
                String email = in.readUTF();

                Statement statement = conexion.createStatement();

                String sql = "INSERT INTO pasajeros VALUES ("
                        + id + ",'"
                        + documento + "','"
                        + nombre + "','"
                        + apellidos + "','"
                        + email + "')";

                try {
                    statement.executeUpdate(sql);
                    pasajerosInsertados++;
                } catch (Exception e) {
                    errores++;
                }

                statement.close();
            }

            int numeroEmbarques = in.readInt();

            for (int i = 0; i < numeroEmbarques; i++) {
                int vueloId = in.readInt();
                int pasajeroId = in.readInt();
                String asiento = in.readUTF();
                String clase = in.readUTF();
                String estado = in.readUTF();
                boolean equipaje = in.readBoolean();
                double peso = in.readDouble();

                Statement statement = conexion.createStatement();

                String sql = "INSERT INTO embarques(" 
                        + "vuelo_id,pasajero_id,asiento,clase,estado,equipaje,peso_equipaje" 
                        + ") VALUES ("
                        + vueloId + ","
                        + pasajeroId + ",'"
                        + asiento + "','"
                        + clase + "','"
                        + estado + "',"
                        + equipaje + ","
                        + peso + ")";

                try {
                    statement.executeUpdate(sql);
                    embarquesInsertados++;
                } catch (Exception e) {
                    errores++;
                }

                statement.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            errores++;

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }

            try {
                if (conexion != null) {
                    conexion.close();
                }
            } catch (Exception e) {
            }
        }

        return new ResultadoImportacion(
                vuelosInsertados,
                pasajerosInsertados,
                embarquesInsertados,
                errores);
    }
}
