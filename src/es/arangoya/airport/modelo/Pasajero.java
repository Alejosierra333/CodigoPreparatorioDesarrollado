package es.arangoya.airport.modelo;

public class Pasajero {

    private int id;
    private String documento;
    private String nombre;
    private String apellidos;
    private String email;

    public Pasajero(int id, String documento, String nombre,
            String apellidos, String email) {

        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
}
