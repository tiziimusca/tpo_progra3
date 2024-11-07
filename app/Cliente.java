import java.util.*;

class Cliente {
    int id;
    int volumenProduccion;
    Map<Integer, Integer> rutas = new HashMap<>();

    public Cliente(int id, int volumenProduccion) {
        this.id = id;
        this.volumenProduccion = volumenProduccion;
    }
}
