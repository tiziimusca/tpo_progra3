import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Optimizado {
    private List<Cliente> clientes;
    private List<CentroDistribucion> centros;
    private List<Ruta> rutas;
    private int costoMinimo = Integer.MAX_VALUE;
    private int[] mejorAsignacion;

    public Optimizado(List<Cliente> clientes, List<CentroDistribucion> centros, List<Ruta> rutas) {
        this.clientes = clientes;
        this.centros = centros;
        this.rutas = rutas;
        this.mejorAsignacion = new int[clientes.size()];
    }

    public void resolver() {
        int[] asignacionActual = new int[clientes.size()];
        boolean[] centrosConstruidos = new boolean[centros.size()];
        buscarSolucion(0, asignacionActual, centrosConstruidos, 0);
        
        // Imprimir la mejor asignación y el costo mínimo
        System.out.println("Costo mínimo: " + costoMinimo);
        for (int i = 0; i < mejorAsignacion.length; i++) {
            System.out.println("Cliente " + i + " asignado al Centro " + mejorAsignacion[i]);
        }
    }

    private void buscarSolucion(int clienteIndex, int[] asignacionActual, boolean[] centrosConstruidos, int costoActual) {
        if (clienteIndex == clientes.size()) {
            if (costoActual < costoMinimo) {
                costoMinimo = costoActual;
                mejorAsignacion = asignacionActual.clone();
            }
            return;
        }

        for (int centro = 0; centro < centros.size(); centro++) {
            if (!centrosConstruidos[centro] && !esFactible(clienteIndex, centro)) continue;

            int costo = calcularCosto(clienteIndex, centro);
            asignacionActual[clienteIndex] = centro;
            centrosConstruidos[centro] = true;

            buscarSolucion(clienteIndex + 1, asignacionActual, centrosConstruidos, costoActual + costo);

            // Backtracking
            centrosConstruidos[centro] = false;
            asignacionActual[clienteIndex] = -1;
        }
    }

    private int calcularCosto(int clienteIndex, int centroIndex) {
        Cliente cliente = clientes.get(clienteIndex);
        CentroDistribucion centro = centros.get(centroIndex);
        
        // Calcular el costo unitario de transporte para el cliente y el centro seleccionado
        // Esto se debe basar en las rutas y el volumen de producción del cliente
        int costoTransporte = obtenerCostoTransporte(cliente.id, centro.id);
        return (costoTransporte + centro.costoEnvioPuerto) * cliente.volumenProduccion + centro.costoFijoAnual;
    }

    private int obtenerCostoTransporte(int clienteId, int centroId) {
        // Buscar el costo mínimo de transporte entre el cliente y el centro de distribución
        int costoMin = Integer.MAX_VALUE;
        for (Ruta ruta : rutas) {
            if ((ruta.origen == clienteId && ruta.destino == centroId) || (ruta.origen == centroId && ruta.destino == clienteId)) {
                costoMin = Math.min(costoMin, ruta.costoUnitario);
            }
        }
        return costoMin == Integer.MAX_VALUE ? 0 : costoMin;
    }

    private boolean esFactible(int clienteIndex, int centroIndex) {
        // Implementa lógica para verificar si un centro es accesible desde un cliente
        // Aquí puedes considerar factores como si existe una ruta directa entre el cliente y el centro
        return obtenerCostoTransporte(clientes.get(clienteIndex).id, centros.get(centroIndex).id) > 0;
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
                centros.add(new CentroDistribucion(idCentro, costoEnvioPuerto, costoFijoAnual));
            } else if (partes.length == 2) {
                int idCliente = Integer.parseInt(partes[0].trim());
                int volumenProduccion = Integer.parseInt(partes[1].trim());
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
            if(partes.length == 3){
            int origen = Integer.parseInt(partes[0].trim());
            int destino = Integer.parseInt(partes[1].trim());
            int costoUnitario = Integer.parseInt(partes[2].trim());
            rutas.add(new Ruta(origen, destino, costoUnitario));
            }
        }
        reader.close();
    }
}