import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Optimizado {
    private List<Cliente> clientes;
    private List<CentroDistribucion> centros;
    private List<Ruta> rutas;
    private int costoMinimo = Integer.MAX_VALUE;
    private int[] mejorAsignacion;
    private Map<Integer, Map<Integer, Integer>> memoTransporte = new HashMap<>();

    public Optimizado() {
        this.clientes = new ArrayList<>();
        this.centros = new ArrayList<>();
        this.rutas = new ArrayList<>();
    }

    public void resolverAEstrella() {
        PriorityQueue<Nodo> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.costoEstimado));
        int[] asignacionInicial = new int[clientes.size()];
        boolean[] centrosConstruidosInicial = new boolean[centros.size()];

        pq.add(new Nodo(0, asignacionInicial, centrosConstruidosInicial, 0, calcularLimiteInferior(0, 0)));

        while (!pq.isEmpty()) {
            Nodo actual = pq.poll();
            System.out.println("Nodo actual: " + actual.clienteIndex + " " + actual.costoActual + " "
                    + actual.costoEstimado);

            if (actual.clienteIndex == clientes.size()) {
                if (actual.costoActual < costoMinimo) {
                    costoMinimo = actual.costoActual;
                    mejorAsignacion = actual.asignacion.clone();
                }
                continue;
            }

            int limiteInferior = calcularLimiteInferior(actual.clienteIndex, actual.costoActual);
            if (limiteInferior >= costoMinimo)
                continue; // Poda por límite inferior

            for (CentroDistribucion centro : obtenerCentrosPrometedores(actual.clienteIndex)) {
                int centroId = centro.id;
                if (!actual.centrosConstruidos[centroId]
                        && !esFactible(actual.clienteIndex, centroId, actual.costoActual)) {
                    continue;
                }

                int costo = calcularCosto(actual.clienteIndex, centroId);
                int[] nuevaAsignacion = actual.asignacion.clone();
                nuevaAsignacion[actual.clienteIndex] = centroId;
                boolean[] nuevosCentrosConstruidos = actual.centrosConstruidos.clone();
                nuevosCentrosConstruidos[centroId] = true;

                int nuevoCostoEstimado = actual.costoActual + costo
                        + calcularLimiteInferior(actual.clienteIndex + 1, actual.costoActual + costo);
                pq.add(new Nodo(actual.clienteIndex + 1, nuevaAsignacion, nuevosCentrosConstruidos,
                        actual.costoActual + costo, nuevoCostoEstimado));
            }
        }

        System.out.println("Costo mínimo: " + costoMinimo);
        for (int i = 0; i < mejorAsignacion.length; i++) {
            System.out.println("Cliente " + i + " asignado al Centro " + mejorAsignacion[i]);
        }
    }

    // Métodos de cálculo de costo
    private int calcularCosto(int clienteIndex, int centroIndex) {
        Cliente cliente = clientes.get(clienteIndex);
        CentroDistribucion centro = centros.get(centroIndex);
        int costoTransporte = obtenerCostoTransporte(cliente.id, centro.id);
        return (costoTransporte + centro.costoEnvioPuerto) * cliente.volumenProduccion + centro.costoFijoAnual;
    }

    private int obtenerCostoTransporte(int clienteId, int centroId) {
        if (!memoTransporte.containsKey(clienteId)) {
            memoTransporte.put(clienteId, new HashMap<>());
        }
        if (memoTransporte.get(clienteId).containsKey(centroId)) {
            return memoTransporte.get(clienteId).get(centroId);
        }

        int costo = dijkstra(clienteId, centroId);
        memoTransporte.get(clienteId).put(centroId, costo);
        System.out.println("Costo de transporte de " + clienteId + " a " + centroId + ": " + costo);
        return costo;
    }

    private int dijkstra(int origen, int destino) {
        Map<Integer, List<Ruta>> grafo = new HashMap<>();
        for (Ruta ruta : rutas) {
            grafo.putIfAbsent(ruta.origen, new ArrayList<>());
            grafo.get(ruta.origen).add(ruta);
        }

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> costos = new HashMap<>();
        for (Ruta ruta : rutas) {
            costos.put(ruta.origen, Integer.MAX_VALUE);
            costos.put(ruta.destino, Integer.MAX_VALUE);
        }
        costos.put(origen, 0);
        pq.add(new int[] { origen, 0 });

        while (!pq.isEmpty()) {
            int[] actual = pq.poll();
            int nodoActual = actual[0];
            int costoActual = actual[1];

            if (nodoActual == destino) {
                return costoActual;
            }

            for (Ruta ruta : grafo.getOrDefault(nodoActual, new ArrayList<>())) {
                int nuevoCosto = costoActual + ruta.costoUnitario;
                if (nuevoCosto < costos.getOrDefault(ruta.destino, Integer.MAX_VALUE)) {
                    costos.put(ruta.destino, nuevoCosto);
                    pq.add(new int[] { ruta.destino, nuevoCosto });
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    // Heurística y podas
    private int calcularLimiteInferior(int clienteIndex, int costoActual) {
        int limiteInferior = costoActual;
        for (int i = clienteIndex; i < clientes.size(); i++) {
            int minimoCosto = Integer.MAX_VALUE;
            for (CentroDistribucion centro : centros) {
                int costoTransporte = obtenerCostoTransporte(clientes.get(i).id, centro.id);
                if (costoTransporte > 0) {
                    int costoTotal = (costoTransporte + centro.costoEnvioPuerto) * clientes.get(i).volumenProduccion;
                    minimoCosto = Math.min(minimoCosto, costoTotal + centro.costoFijoAnual);
                }
            }
            limiteInferior += minimoCosto;
        }
        return limiteInferior;
    }

    private List<CentroDistribucion> obtenerCentrosPrometedores(int clienteIndex) {
        Cliente cliente = clientes.get(clienteIndex);
        return centros.stream()
                .sorted(Comparator.comparingInt(c -> obtenerCostoTransporte(cliente.id, c.id) + c.costoFijoAnual))
                .limit(3)
                .collect(Collectors.toList());
    }

    private boolean esFactible(int clienteIndex, int centroIndex, int costoActual) {
        Cliente cliente = clientes.get(clienteIndex);
        CentroDistribucion centro = centros.get(centroIndex);
        int costoTransporte = obtenerCostoTransporte(cliente.id, centro.id);
        if (costoTransporte == 0)
            return false;
        int costoTotalAsignacion = (costoTransporte + centro.costoEnvioPuerto) * cliente.volumenProduccion
                + centro.costoFijoAnual;
        return costoActual + costoTotalAsignacion < costoMinimo;
    }

    public void cargarClientesYCentros(String archivoClientesYCentros) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(archivoClientesYCentros));
        String linea;
        while ((linea = reader.readLine()) != null) {
            String[] partes = linea.split(",");
            if (partes.length == 3) {
                int idCentro = Integer.parseInt(partes[0].trim());
                int costoEnvioPuerto = Integer.parseInt(partes[1].trim());
                int costoFijoAnual = Integer.parseInt(partes[2].trim());
                System.out.println("datos Centro: " + idCentro + " " + costoEnvioPuerto + " " + costoFijoAnual);
                centros.add(new CentroDistribucion(idCentro, costoEnvioPuerto, costoFijoAnual));
            } else if (partes.length == 2) {
                int idCliente = Integer.parseInt(partes[0].trim());
                int volumenProduccion = Integer.parseInt(partes[1].trim());
                System.out.println("datos Cliente: " + idCliente + " " + volumenProduccion);
                clientes.add(new Cliente(idCliente, volumenProduccion));
            }
        }
        reader.close();
    }

    public void cargarRutas(String archivoRutas) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(archivoRutas));
        String linea;
        while ((linea = reader.readLine()) != null) {
            String[] partes = linea.split(",");
            if (partes.length == 3) {
                int origen = Integer.parseInt(partes[0].trim());
                int destino = Integer.parseInt(partes[1].trim());
                int costoUnitario = Integer.parseInt(partes[2].trim());
                System.out.println("datos: " + origen + " " + destino + " " + costoUnitario);
                rutas.add(new Ruta(origen, destino, costoUnitario));
            }
        }
        reader.close();
    }
}