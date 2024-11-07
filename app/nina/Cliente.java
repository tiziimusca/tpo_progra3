
public class Cliente {
    int id;
    double volumenProduccionAnual;

    public Cliente(int id, double volumenProduccionAnual) {
        this.id = id;
        this.volumenProduccionAnual = volumenProduccionAnual;
    }

    public int getId(){
        return this.id;
    }

}