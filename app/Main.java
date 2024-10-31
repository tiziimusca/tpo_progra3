import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            List<Cliente> clientes = new ArrayList<>();
            List<CentroDistribucion> centros = new ArrayList<>();
            List<Ruta> rutas = new ArrayList<>();

            Optimizado optimizacion = new Optimizado(clientes, centros, rutas);
            
            optimizacion.cargarClientesYCentros("clientesYCentros.txt");
            optimizacion.cargarRutas("rutas.txt");
            
            optimizacion.resolver();
        } catch (IOException e) {
            System.out.println("Error al leer los archivos: " + e.getMessage());
        }
    }
}