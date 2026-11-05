
/*
 * Solicitud:
 * Esta clase hace parte de la implementacion de manera explicita para el
 * laboratorio, un usuario se encarga de enviar multiples solicitudes, las
 * cuales cuentan con un id unico y la informacion de los recursos solicitados
 * por el usuario.
 */
public final class Solicitud {

    private String id;
    private int procesadores;
    private int memoriaGiB;
    private int almacenamientoGiB;

    public Solicitud(String id, int procesadores, int memoriaGiB, int almacenamientoGiB) {

        this.id = id;
        this.procesadores = procesadores;
        this.memoriaGiB = memoriaGiB;
        this.almacenamientoGiB = almacenamientoGiB;
    }

    public String getId() {
        return id;
    }

    public int getProcesadores() {
        return procesadores;
    }

    public int getMemoriaGiB() {
        return memoriaGiB;
    }

    public int getAlmacenamientoGiB() {
        return almacenamientoGiB;
    }

    public String infoSolicitud() {
        return "Solicitud{" +
            "id='" + id + '\'' +
            ", procesadores=" + procesadores +
            ", memoriaGiB=" + memoriaGiB +
            ", almacenamientoGiB=" + almacenamientoGiB +
            '}';
    }
}
