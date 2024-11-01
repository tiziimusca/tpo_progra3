import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {

            Optimizado optimizacion = new Optimizado();

            optimizacion.cargarClientesYCentros("clientesYCentros.txt");
            optimizacion.cargarRutas("rutas.txt");

            optimizacion.resolverAEstrella();
        } catch (IOException e) {
            System.out.println("Error al leer los archivos: " + e.getMessage());
        }
    }
}