package nina;

public class CentroDeDistribucion {
    int id;
    double costoTransportePuerto;
    double costoOperacionAnual;

    public CentroDeDistribucion(int id, double costoTransportePuerto, double costoOperacionAnual) {
        this.id = id;
        this.costoTransportePuerto = costoTransportePuerto;
        this.costoOperacionAnual = costoOperacionAnual;
    }

    public int getId() {
        return this.id;
    }
}