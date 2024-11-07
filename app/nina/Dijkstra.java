package nina;

import java.util.*;

public class Dijkstra {
    public static double[] dijkstra(int origen, int numNodos, List<List<Ruta>> grafo) {
        double[] distancias = new double[numNodos];
        Arrays.fill(distancias, Double.MAX_VALUE);
        distancias[origen] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.add(new int[] { origen, 0 });

        while (!pq.isEmpty()) {
            int[] actual = pq.poll();
            int nodo = actual[0];
            double costo = actual[1];

            if (costo > distancias[nodo])
                continue;

            for (Ruta ruta : grafo.get(nodo)) {
                int vecino = ruta.destino;
                double nuevoCosto = costo + ruta.costoTransporte;

                if (nuevoCosto < distancias[vecino]) {
                    distancias[vecino] = nuevoCosto;
                    pq.add(new int[] { vecino, (int) nuevoCosto });
                }
            }
        }

        return distancias;
    }
}