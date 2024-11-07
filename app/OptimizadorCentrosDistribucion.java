import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

class OptimizadorCentrosDistribucion {

    List<Cliente> clientes = new ArrayList<>();
    List<CentroDistribucion> centros = new ArrayList<>();
    Map<Integer, Map<Integer, Integer>> grafo = new HashMap<>();
    private SolucionMinima mejorSolucion = new SolucionMinima(new ArrayList<>(), Integer.MAX_VALUE);
    private Map<Integer, Integer> mejorAsignacionClientes = new HashMap<>();

    public void cargarDatos(String clientesYCentrosPath, String rutasPath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(clientesYCentrosPath));
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",");
            int id = Integer.parseInt(partes[0]);
            if (partes.length == 3) {
                int costoUnitarioPuerto = Integer.parseInt(partes[1]);
                int costoFijoAnual = Integer.parseInt(partes[2]);
                centros.add(new CentroDistribucion(id, costoUnitarioPuerto, costoFijoAnual));
            } else if (partes.length == 2) {
                int volumenProduccion = Integer.parseInt(partes[1]);
                clientes.add(new Cliente(id, volumenProduccion));
            }

        }
        br.close();

        br = new BufferedReader(new FileReader(rutasPath));
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",");
            int origen = Integer.parseInt(partes[0]);
            int destino = Integer.parseInt(partes[1]);
            int costo = Integer.parseInt(partes[2]);

            grafo.computeIfAbsent(origen, k -> new HashMap<>()).put(destino, costo);
            grafo.computeIfAbsent(destino, k -> new HashMap<>()).put(origen, costo);
        }
        br.close();

        System.out.println("Grafo:");
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : grafo.entrySet()) {
            System.out.println("Nodo " + entry.getKey() + " conexiones: " + entry.getValue());
        }

    }

    private Map<Integer, Integer> calcularCaminosMinimos(int inicio) {
        Map<Integer, Integer> distancias = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        pq.offer(new int[] { inicio, 0 });
        distancias.put(inicio, 0);

        while (!pq.isEmpty()) {
            int[] actual = pq.poll();
            int nodo = actual[0], distanciaActual = actual[1];

            System.out.println("Visitando nodo: " + nodo + " con distancia actual: " + distanciaActual);

            if (distanciaActual > distancias.getOrDefault(nodo, Integer.MAX_VALUE))
                continue;

            if (grafo.containsKey(nodo)) {
                for (Map.Entry<Integer, Integer> vecino : grafo.get(nodo).entrySet()) {
                    int siguiente = vecino.getKey();
                    int nuevaDistancia = distanciaActual + vecino.getValue();
                    System.out.println("Evaluando vecino: " + siguiente + " con nueva distancia: " + nuevaDistancia);

                    if (nuevaDistancia < distancias.getOrDefault(siguiente, Integer.MAX_VALUE)) {
                        distancias.put(siguiente, nuevaDistancia);
                        pq.offer(new int[] { siguiente, nuevaDistancia });
                    }
                }
            }
        }
        return distancias;
    }

    public SolucionMinima encontrarSolucionOptima() {
        backtrack(new ArrayList<>(), 0);

        System.out.println("Asignación de clientes a centros:");
        for (Map.Entry<Integer, Integer> entry : mejorAsignacionClientes.entrySet()) {
            System.out.println("Cliente " + entry.getKey() + " asignado al Centro " + entry.getValue());
        }
        return mejorSolucion;
    }

    private void backtrack(List<Integer> centrosSeleccionados, int indice) {
        if (indice == centros.size()) {
            Map<Integer, Integer> asignacionClientes = new HashMap<>();
            int costoTotal = calcularCostoTotal(centrosSeleccionados, asignacionClientes);
            if (costoTotal < mejorSolucion.costoTotal) {
                mejorSolucion = new SolucionMinima(new ArrayList<>(centrosSeleccionados), costoTotal);
                mejorAsignacionClientes = new HashMap<>(asignacionClientes);
            }
            return;
        }

        backtrack(centrosSeleccionados, indice + 1);
        centrosSeleccionados.add(centros.get(indice).id);
        backtrack(centrosSeleccionados, indice + 1);
        centrosSeleccionados.remove(centrosSeleccionados.size() - 1);
    }

    private int calcularCostoTotal(List<Integer> centrosSeleccionados, Map<Integer, Integer> asignacionClientes) {
        int costoTotal = 0;

        for (Cliente cliente : clientes) {
            int costoMinimoCliente = Integer.MAX_VALUE;
            int centroAsignado = -1;

            Map<Integer, Integer> distancias = calcularCaminosMinimos(cliente.id);

            if (distancias.isEmpty()) {
                System.out.println("Error: No hay caminos calculados para el cliente " + cliente.id);
                return Integer.MAX_VALUE;
            }

            for (int centroId : centrosSeleccionados) {
                CentroDistribucion centro = centros.stream().filter(c -> c.id == centroId).findFirst().orElse(null);
                if (centro != null) {
                    if (distancias.containsKey(centro.id)) {
                        int costoClienteCentro = distancias.get(centro.id);
                        int costoCliente = (costoClienteCentro + centro.costoUnitarioPuerto)
                                * cliente.volumenProduccion;
                        if (costoCliente < costoMinimoCliente) {
                            costoMinimoCliente = costoCliente;
                            centroAsignado = centro.id;
                        }
                    }
                }
            }

            if (costoMinimoCliente == Integer.MAX_VALUE) {
                System.out.println("Error: No hay un camino válido para el cliente " + cliente.id);
                return Integer.MAX_VALUE;
            }
            costoTotal += costoMinimoCliente;
            asignacionClientes.put(cliente.id, centroAsignado);
        }

        for (int centroId : centrosSeleccionados) {
            CentroDistribucion centro = centros.stream().filter(c -> c.id == centroId).findFirst().orElse(null);
            if (centro != null) {
                costoTotal += centro.costoFijoAnual;
            }
        }
        return costoTotal;
    }
}