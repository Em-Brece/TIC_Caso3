import java.util.Random;


public class Usuario extends Thread {
    private final int idUsuario;
    private final int numSolicitudes;
    private final Buzon buzon;
    private final int maxProcesadores;
    private final int maxMemoria;
    private final int maxAlmacenamiento;
    private final Random random;

    public Usuario(int idUsuario, Configuracion config, Buzon buzon) {
        super("Usuario-" + idUsuario);
        this.idUsuario = idUsuario;
        this.numSolicitudes = config.getBase() * idUsuario;
        this.buzon = buzon;
        this.maxProcesadores = tresCuartos(config.getProcesadores());
        this.maxMemoria = tresCuartos(config.getMemoria());
        this.maxAlmacenamiento = tresCuartos(config.getAlmacenamiento());
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= numSolicitudes; i++) {
                String id = "U" + idUsuario + "-" + i;
                int procesadores = aleatorio(maxProcesadores);
                int memoria = potenciaDeDosInferior(aleatorio(maxMemoria));
                int almacenamiento = multiploDeDosInferior(aleatorio(maxAlmacenamiento), maxAlmacenamiento);
                Solicitud s = new Solicitud(id, procesadores, memoria, almacenamiento);
                System.out.println("Usuario-" + idUsuario + " genera la solicitud " + id
                    + " [procesadores=" + procesadores
                    + ", memoria=" + memoria + " GiB"
                    + ", almacenamiento=" + almacenamiento + " GiB]");
                buzon.agregar(s, getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            buzon.registrarFinUsuario(getName());
        }
    }

    public int getNumSolicitudes() {
        return numSolicitudes;
    }


    private int aleatorio(int max) {
        return 1 + random.nextInt(max);
    }

    static int tresCuartos(int capacidad) {
        return Math.max(1, Math.round(capacidad * 3 / 4.0f));
    }


    static int potenciaDeDosInferior(int v) {
        return Integer.highestOneBit(v);
    }

    static int multiploDeDosInferior(int v, int max) {
        int r = v - (v % 2);
        if (r == 0) {
            r = max >= 2 ? 2 : 1;
        }
        return r;
    }
}
