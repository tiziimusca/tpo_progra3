class Nodo {
    int clienteIndex;
    int[] asignacion;
    boolean[] centrosConstruidos;
    int costoActual;
    int costoEstimado;

    Nodo(int clienteIndex, int[] asignacion, boolean[] centrosConstruidos, int costoActual, int costoEstimado) {
        this.clienteIndex = clienteIndex;
        this.asignacion = asignacion;
        this.centrosConstruidos = centrosConstruidos;
        this.costoActual = costoActual;
        this.costoEstimado = costoEstimado;
    }
}