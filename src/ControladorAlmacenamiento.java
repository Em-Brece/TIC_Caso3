
/*
 * ControladorAlmacenamiento:
 * Esta clase se encarga de revisar que bloques de disco estan disponibles y
 * cuales ya estan asignados, traduciendo cada solicitud en GiB al multiplo mas
 * cercano del tamano de bloque.
 */
public class ControladorAlmacenamiento {

    private final int bloqueKiB;
    private final int totalBloques;
    private final String[] propietarios;
    private int libresBloques;

    public ControladorAlmacenamiento(int almacenamientoGiB, int bloqueKiB) {
        if (almacenamientoGiB <= 0) {
            throw new IllegalArgumentException("El almacenamiento total debe ser mayor que cero.");
        }
        if (bloqueKiB <= 0) {
            throw new IllegalArgumentException("El tamano de bloque debe ser mayor que cero.");
        }
        long capacidadKiB = (long) almacenamientoGiB * 1024L * 1024L;
        if (capacidadKiB % bloqueKiB != 0) {
            throw new IllegalArgumentException(
                "El almacenamiento total en KiB no es multiplo del tamano de bloque.");
        }
        long bloques = capacidadKiB / bloqueKiB;
        if (bloques > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Demasiados bloques para representar.");
        }
        this.bloqueKiB = bloqueKiB;
        this.totalBloques = (int) bloques;
        this.propietarios = new String[this.totalBloques];
        this.libresBloques = this.totalBloques;
    }

    public int gibABloques(int almacenamientoGiB) {
        long kib = (long) almacenamientoGiB * 1024L * 1024L;
        long bloques = (kib + (bloqueKiB / 2L)) / bloqueKiB;
        return (int) bloques;
    }

    public synchronized boolean reservar(int cantidadGiB, String vmId) {
        int bloquesPedidos = gibABloques(cantidadGiB);
        if (bloquesPedidos > libresBloques) {
            return false;
        }
        int asignados = 0;
        for (int i = 0; i < totalBloques && asignados < bloquesPedidos; i++) {
            if (propietarios[i] == null) {
                propietarios[i] = vmId;
                asignados++;
            }
        }
        libresBloques -= bloquesPedidos;
        return true;
    }

    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < totalBloques; i++) {
            if (vmId.equals(propietarios[i])) {
                propietarios[i] = null;
                libresBloques++;
            }
        }
    }

    public synchronized int disponibles() {
        return libresBloques;
    }

    public synchronized int capacidadTotal() {
        return totalBloques;
    }

    public int getBloqueKiB() {
        return bloqueKiB;
    }

    public synchronized int asignadosA(String vmId) {
        if (vmId == null) {
            return 0;
        }
        int cuenta = 0;
        for (int i = 0; i < totalBloques; i++) {
            if (vmId.equals(propietarios[i])) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
