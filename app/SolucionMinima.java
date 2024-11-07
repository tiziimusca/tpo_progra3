import java.util.List;
import java.util.ArrayList;

class SolucionMinima {
    List<Integer> centrosSeleccionados;
    int costoTotal;

    public SolucionMinima(List<Integer> centrosSeleccionados, int costoTotal) {
        this.centrosSeleccionados = new ArrayList<>(centrosSeleccionados);
        this.costoTotal = costoTotal;
    }
}