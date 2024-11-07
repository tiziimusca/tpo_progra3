import java.io.*;
import java.util.*;

public class OptimizacionCentrosDistribucion {

    private static double[][] costoTransporte;

    private static List<CentroDeDistribucion> centros = new ArrayList<>();
    private static List<Cliente> clientes = new ArrayList<>();
    private static List<List<Ruta>> grafo = new ArrayList<>();
    private static double mejorCosto = Double.MAX_VALUE;
    private static List<Integer> mejorSolucion = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        cargarDatos("clientesYCentros.txt", "rutas.txt");
        if (centros.isEmpty() || clientes.isEmpty()) {
            System.out.println("Error: No se han cargado centros o clientes correctamente.");
            return;
        }

        calcularCostoTransporte();

        List<Integer> centrosPosibles = new ArrayList<>();
        for (int i = 0; i < centros.size(); i++) {
            centrosPosibles.add(i);
        }

        buscarSoluciones(centrosPosibles, new ArrayList<>(), 0);

        // Mostrar la mejor solución encontrada
        System.out.println("Mejor solución encontrada con costo: " + mejorCosto);
        System.out.println("Centros de distribución a construir: " + mejorSolucion);

        // Imprimir a qué centro se asigna cada cliente
        asignarClientesACentros();
    }

    public static void cargarDatos(String clientesYCentrosPath, String rutasPath) throws IOException {
        // Carga clientes y centros de distribución
        BufferedReader br = new BufferedReader(new FileReader(clientesYCentrosPath));
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",");
            int id = Integer.parseInt(partes[0]);
            if (partes.length == 3) {
                // Centro de Distribución
                int costoUnitarioPuerto = Integer.parseInt(partes[1].trim());  // Eliminar espacios
                int costoFijoAnual = Integer.parseInt(partes[2].trim());  // Eliminar espacios

                centros.add(new CentroDeDistribucion(id, costoUnitarioPuerto, costoFijoAnual));
            } else if (partes.length == 2) {
                // Cliente
                int volumenProduccion = Integer.parseInt(partes[1].trim());
                clientes.add(new Cliente(id, volumenProduccion));
            }
        }
        br.close();

        // Verificación para asegurarse que centros y clientes no estén vacíos
        if (centros.isEmpty()) {
            System.out.println("Error: No se encontraron centros de distribución en el archivo.");
            return;
        }
        if (clientes.isEmpty()) {
            System.out.println("Error: No se encontraron clientes en el archivo.");
            return;
        }

        // Inicializar el grafo con listas vacías para cada nodo
        int totalNodes = centros.size() + clientes.size();
        for (int i = 0; i < totalNodes; i++) {
            grafo.add(new ArrayList<>());
        }

        // Cargar las rutas entre nodos
        br = new BufferedReader(new FileReader(rutasPath));
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",");
            int origen = Integer.parseInt(partes[0]);
            int destino = Integer.parseInt(partes[1]);
            int costo = Integer.parseInt(partes[2]);

            // Asegurarse de que los índices de origen y destino sean válidos
            if (origen >= totalNodes || destino >= totalNodes) {
                System.out.println("Error: Ruta inválida entre nodos " + origen + " y " + destino);
                continue;
            }

            // Agregar la ruta al grafo (bidireccional)
            grafo.get(origen).add(new Ruta(origen, destino, costo));
            grafo.get(destino).add(new Ruta(destino, origen, costo));
        }
        br.close();
    }

    private static void calcularCostoTransporte() {
        int numClientes = clientes.size();
        int numCentros = centros.size();

        // Verificación para evitar índices fuera de límites
        if (numClientes == 0 || numCentros == 0) {
            System.out.println("Error: No hay clientes o centros para calcular los costos de transporte.");
            return;
        }

        costoTransporte = new double[numClientes][numCentros];

        for (int i = 0; i < numClientes; i++) {
            double[] distancias = Dijkstra.dijkstra(i, grafo.size(), grafo);

            for (int j = 0; j < numCentros; j++) {
                // Verificación de que los índices sean correctos
                if (i < numClientes && j < numCentros) {
                    costoTransporte[i][j] = distancias[numClientes + j];  // Costos de cliente i a centro j
                }
            }
        }
    }

    private static void buscarSoluciones(List<Integer> centrosPosibles, List<Integer> solucionActual, int idx) {
        if (idx == centrosPosibles.size()) {
            double costoTotal = calcularCostoTotal(solucionActual);
            if (costoTotal < mejorCosto) {
                mejorCosto = costoTotal;
                mejorSolucion = new ArrayList<>(solucionActual);
            }
            return;
        }

        if (solucionActual.size() > 0) {
            double costoParcial = calcularCostoTotal(solucionActual);
            if (costoParcial > mejorCosto) {
                return;
            }
        }

        solucionActual.add(centrosPosibles.get(idx));
        buscarSoluciones(centrosPosibles, solucionActual, idx + 1);

        solucionActual.remove(solucionActual.size() - 1);
        buscarSoluciones(centrosPosibles, solucionActual, idx + 1);
    }

    private static double calcularCostoTotal(List<Integer> centrosSeleccionados) {
        double costoTotal = 0;

        for (int idx : centrosSeleccionados) {
            costoTotal += centros.get(idx).costoOperacionAnual;
        }

        for (int i = 0; i < clientes.size(); i++) {
            double costoMinimoCliente = Double.MAX_VALUE;

            for (int idx : centrosSeleccionados) {
                double costoTransporteClienteCentro = costoTransporte[i][idx];
                costoMinimoCliente = Math.min(costoMinimoCliente, costoTransporteClienteCentro);
            }

            costoTotal += costoMinimoCliente * clientes.get(i).volumenProduccionAnual;
        }

        return costoTotal;
    }

    private static void asignarClientesACentros() {
        // Asignar cada cliente al centro de distribución más cercano
        System.out.println("\nAsignación de clientes a centros de distribución:");
        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.get(i);
            double costoMinimo = Double.MAX_VALUE;
            int centroAsignado = -1;

            // Buscar el centro más cercano para el cliente
            for (int j = 0; j < mejorSolucion.size(); j++) {
                int centroIdx = mejorSolucion.get(j);
                double costoTransporteClienteCentro = costoTransporte[i][centroIdx];
                if (costoTransporteClienteCentro < costoMinimo) {
                    costoMinimo = costoTransporteClienteCentro;
                    centroAsignado = centroIdx;
                }
            }

            // Imprimir la asignación
            if (centroAsignado != -1) {
                System.out.println("Cliente " + cliente.id + " asignado al centro de distribución " + centros.get(centroAsignado).id);
            }
        }
    }
}