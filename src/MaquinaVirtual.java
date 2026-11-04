import java.util.Random;

public class MaquinaVirtual extends Thread {
    public static final int T_MIN_MS = 4000;
    public static final int T_MAX_MS = 40000;


    private static final double ESCALA =
        Double.parseDouble(System.getProperty("caso3.escala", "1"));

    private final Solicitud solicitud;
    private final ControladorProcesadores cpu;
    private final ControladorMemoria memoria;
    private final ControladorAlmacenamiento disco;
    private final ZonaEspera zona;
    private final Random random = new Random();
    private int intentos = 0;

    public MaquinaVirtual(Solicitud solicitud, ControladorProcesadores cpu, ControladorMemoria memoria,
                          ControladorAlmacenamiento disco, ZonaEspera zona) {
        super("MV-" + solicitud.getId());
        this.solicitud = solicitud;
        this.cpu = cpu;
        this.memoria = memoria;
        this.disco = disco;
        this.zona = zona;
    }

    public String getIdMV() {
        return getName();
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public int getIntentos() {
        return intentos;
    }

    @Override
    public void run() {
        try {
            while (true) {
                long gen = zona.generacionActual();
                intentos++;
                if (intentarReservar()) {
                    break;
                }
                zona.esperar(this, gen);
            }
            zona.salir(this);

            long duracion = (long) ((T_MIN_MS + random.nextInt(T_MAX_MS - T_MIN_MS + 1)) * ESCALA);
            Log.info("configurada tras " + intentos + " intento(s), se ejecuta " + duracion + " ms");
            Thread.sleep(duracion);

            liberarTodo();
            Log.info("termina y libera sus recursos");
            zona.despertar();
        } catch (InterruptedException e) {
            liberarTodo();
            zona.despertar();
            Thread.currentThread().interrupt();
        }
    }


    private boolean intentarReservar() {
        String id = getIdMV();
        if (!cpu.reservar(solicitud.getProcesadores(), id)) {
            Log.info("no hay " + solicitud.getProcesadores() + " procesador(es) libres");
            return false;
        }
        if (!memoria.reservar(solicitud.getMemoriaGB(), id)) {
            cpu.liberar(id);
            Log.info("no hay " + solicitud.getMemoriaGB() + " GiB de memoria, devuelve procesadores");
            zona.despertar();
            return false;
        }
        if (!disco.reservar(solicitud.getAlmacenamientoGB(), id)) {
            memoria.liberar(id);
            cpu.liberar(id);
            Log.info("no hay " + solicitud.getAlmacenamientoGB()
                + " GiB de disco, devuelve memoria y procesadores");
            zona.despertar();
            return false;
        }
        return true;
    }

    private void liberarTodo() {
        String id = getIdMV();
        disco.liberar(id);
        memoria.liberar(id);
        cpu.liberar(id);
    }
}