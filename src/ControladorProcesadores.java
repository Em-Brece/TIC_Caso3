
/*
 * ControladorProcesadores:
 * Esta clase es uno de los requisitos del caso y se encarga de revisar
 * que procesadores estan disponibles y cuales ya estan asignados.
 */

public class ControladorProcesadores {

    private final int numProcesadores;
    // duenos[i] == null  -> procesador i libre
    // duenos[i] == vmId   -> procesador i asignado a esa MV
    private final String[] duenos;
    private int disponibles;

    public ControladorProcesadores(int totalProcesadores) {

        this.numProcesadores = totalProcesadores;
        this.duenos = new String[totalProcesadores];
        this.disponibles = totalProcesadores;
    }

    /*
     * Intenta reservar 'cantidad' procesadores para vmId.
     * Solo se asignan si en este momento hay suficientes disponibles (todo o nada).
     * Devuelve true si logro reservarlos, false en caso contrario.
     */
    public synchronized boolean reservar(int cantidad, String vmId) {
        validarEntrada(cantidad, vmId);
        if (cantidad > disponibles) {
            return false;
        }
        int asignados = 0;
        for (int i = 0; i < numProcesadores && asignados < cantidad; i++) {
            if (duenos[i] == null) {
                duenos[i] = vmId;
                asignados++;
            }
        }
        disponibles -= cantidad;
        return true;
    }

    /*
     * Libera todos los procesadores que tenga asignados vmId.
     */
    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < numProcesadores; i++) {
            if (vmId.equals(duenos[i])) {
                duenos[i] = null;
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

    /*
     * Cuantos procesadores tiene asignados una MV concreta (util para pruebas).
     */
    public synchronized int asignadosA(String vmId) {
        if (vmId == null) {
            return 0;
        }
        int cuenta = 0;
        for (int i = 0; i < numProcesadores; i++) {
            if (vmId.equals(duenos[i])) {
                cuenta++;
            }
        }
        return cuenta;
    }

    private void validarEntrada(int cantidad, String vmId) {
        if (vmId == null || vmId.trim().isEmpty()) {
            throw new IllegalArgumentException("El vmId no puede estar vacio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor que cero.");
        }
    }
}
