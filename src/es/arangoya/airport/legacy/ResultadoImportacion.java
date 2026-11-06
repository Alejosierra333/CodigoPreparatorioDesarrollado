package es.arangoya.airport.legacy;

public class ResultadoImportacion {

    private int vuelos;
    private int pasajeros;
    private int embarques;
    private int errores;

    public ResultadoImportacion(int vuelos, int pasajeros,
            int embarques, int errores) {

        this.vuelos = vuelos;
        this.pasajeros = pasajeros;
        this.embarques = embarques;
        this.errores = errores;
    }

    public int getVuelos() {
        return vuelos;
    }

    public int getPasajeros() {
        return pasajeros;
    }

    public int getEmbarques() {
        return embarques;
    }

    public int getErrores() {
        return errores;
    }

    @Override
    public String toString() {
        return "Vuelos: " + vuelos
                + " | Pasajeros: " + pasajeros
                + " | Embarques: " + embarques
                + " | Errores: " + errores;
    }
}
