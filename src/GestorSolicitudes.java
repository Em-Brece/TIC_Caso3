import java.util.ArrayList;
import java.util.List;


public class GestorSolicitudes extends Thread {
    private final Buzon buzon;
    private final ZonaEspera zona;
    private final ControladorProcesadores cpu;
    private final ControladorMemoria memoria;
    private final ControladorAlmacenamiento disco;
    private final List<MaquinaVirtual> maquinas = new ArrayList<>();

    public GestorSolicitudes(int idGestor, Buzon buzon, ZonaEspera zona, ControladorProcesadores cpu,
                             ControladorMemoria memoria, ControladorAlmacenamiento disco) {
        super("Gestor-" + idGestor);
        this.buzon = buzon;
        this.zona = zona;
        this.cpu = cpu;
        this.memoria = memoria;
        this.disco = disco;
    }

    @Override
    public void run() {
        try {
            while (true) {
                zona.esperarAdmision();

                Solicitud s = buzon.obtener();
                if (s == null) {
                    if (buzon.terminado()) {
                        break;
                    }
                    Thread.yield();
                    continue;
                }

                MaquinaVirtual mv = new MaquinaVirtual(s, cpu, memoria, disco, zona);
                maquinas.add(mv);
                Log.info("procesa " + s.getId() + " y crea " + mv.getIdMV());
                mv.start();
            }
            Log.info("termina (usuarios terminados y buzón vacío), creó " + maquinas.size() + " MV(s)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public List<MaquinaVirtual> getMaquinas() {
        return maquinas;
    }
}