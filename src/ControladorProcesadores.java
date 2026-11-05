
/*
 * ControladorProcesadores:
 * Esta clase es uno de los requisitos del caso y se encarga de revisar
 * que procesadores estan disponibles y cuales ya estan asignados.
 */
public class ControladorProcesadores {

    private final int numProcesadores;
    private final String[] propietarios;
    private int disponibles;

    public ControladorProcesadores(int totalProcesadores) {

        this.numProcesadores = totalProcesadores;
        this.propietarios = new String[totalProcesadores];
        this.disponibles = totalProcesadores;
    }

    public synchronized boolean reservar(int cantidad, String vmId) {
        if (cantidad > disponibles) {
            return false;
        }
        int asignados = 0;
        for (int i = 0; i < numProcesadores && asignados < cantidad; i++) {
            if (propietarios[i] == null) {
                propietarios[i] = vmId;
                asignados++;
            }
        }
        disponibles -= cantidad;
        return true;
    }

    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < numProcesadores; i++) {
            if (vmId.equals(propietarios[i])) {
                propietarios[i] = null;
                disponibles++;
            }
        }
    }

    public synchronized int disponibles() {
        return disponibles;
    }

    public synchronized int capacidadTotal() {
        return numProcesadores;
    }

    public synchronized int asignadosA(String vmId) {
        if (vmId == null) {
            return 0;
        }
        int cuenta = 0;
        for (int i = 0; i < numProcesadores; i++) {
            if (vmId.equals(propietarios[i])) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
