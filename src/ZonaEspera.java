import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZonaEspera {
    private final int umbral;
    private final List<MaquinaVirtual> dormidas = new ArrayList<>();
    private final Set<String> pendientes = new HashSet<>();
    private long generacion = 0;
    private boolean drenando = false;
    private int maxPendientes = 0;
    private int vecesDrenado = 0;

    public ZonaEspera(int umbral) {
        if (umbral <= 0) {
            throw new IllegalArgumentException("El umbral de la zona de espera debe ser positivo.");
        }
        this.umbral = umbral;
    }

    public synchronized long generacionActual() {
        return generacion;
    }


    public synchronized void esperar(MaquinaVirtual mv, long genVista) throws InterruptedException {
        if (pendientes.add(mv.getIdMV())) {
            maxPendientes = Math.max(maxPendientes, pendientes.size());
            System.out.println(mv.getIdMV() + " entra a la zona de espera (pendientes="
                + pendientes.size() + "/" + umbral + ")");
        }
        if (generacion != genVista) {

            return;
        }
        dormidas.add(mv);
        try {
            while (generacion == genVista) {
                wait();
            }
        } finally {
            dormidas.remove(mv);
        }
    }


    public synchronized void despertar() {
        generacion++;
        notifyAll();
    }


    public synchronized void salir(MaquinaVirtual mv) {
        if (pendientes.remove(mv.getIdMV())) {
            System.out.println(mv.getIdMV() + " sale de la zona de espera (pendientes="
                + pendientes.size() + ")");
            if (pendientes.isEmpty() && drenando) {
                drenando = false;
                System.out.println("Zona de espera vacia, se reabre la admision de solicitudes");
                notifyAll();
            }
        }
    }


    public synchronized void esperarAdmision() throws InterruptedException {
        if (!drenando && pendientes.size() >= umbral) {
            drenando = true;
            vecesDrenado++;
            System.out.println(Thread.currentThread().getName() + " umbral alcanzado (pendientes="
                + pendientes.size() + "), se suspende la admision hasta vaciar la zona");
        }
        while (drenando) {
            wait();
        }
    }

    public synchronized int pendientes() {
        return pendientes.size();
    }

    public synchronized int dormidas() {
        return dormidas.size();
    }

    public synchronized int getMaxPendientes() {
        return maxPendientes;
    }

    public synchronized int getVecesDrenado() {
        return vecesDrenado;
    }

    public int getUmbral() {
        return umbral;
    }
}