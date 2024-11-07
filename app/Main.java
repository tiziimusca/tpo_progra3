import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        OptimizadorCentrosDistribucion optimizador = new OptimizadorCentrosDistribucion();
        try {
            optimizador.cargarDatos("clientesYCentros.txt", "rutas.txt");
            SolucionMinima solucion = optimizador.encontrarSolucionOptima();

            System.out.println("Centros a construir: " + solucion.centrosSeleccionados);
            System.out.println("Costo total anual: " + solucion.costoTotal);
        } catch (IOException e) {
            System.out.println("Error al cargar los datos: " + e.getMessage());
        }
    }
}
