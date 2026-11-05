
/*
 * ControladorAlmacenamiento (objeto de control, NO es un thread).
 *
 * Registra cuales bloques de disco estan asignados y cuales estan libres, y
 * guarda la maquina virtual (vmId) duena de cada bloque ocupado.
 *
 * Traduccion de unidades: la capacidad total y las solicitudes llegan en GiB,
 * pero el recurso se administra en bloques cuyo tamano se expresa en KiB.
 *   1 GiB = 1024 * 1024 KiB.
 *   total de bloques      = (almacenamientoGiB en KiB) / bloqueKiB
 *   bloques de una pedido = GiB -> KiB redondeado al multiplo mas cercano del
 *                           tamano de bloque, dividido por bloqueKiB.
 *
 * Todos los metodos que tocan el estado compartido son synchronized.
 */

public class ControladorAlmacenamiento {

    private static final long KIB_POR_GIB = 1024L * 1024L;

    private final int bloqueKiB;
    private final int totalBloques;
    // duenos[i] == null -> bloque i libre ; si no, vmId de la MV que lo tiene.
    private final String[] duenos;
    private int libresBloques;

    public ControladorAlmacenamiento(int almacenamientoGiB, int bloqueKiB) {
        if (almacenamientoGiB <= 0) {
            throw new IllegalArgumentException("El almacenamiento total debe ser mayor que cero.");
        }
        if (bloqueKiB <= 0) {
            throw new IllegalArgumentException("El tamano de bloque debe ser mayor que cero.");
        }
        long capacidadKiB = (long) almacenamientoGiB * KIB_POR_GIB;
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
        this.duenos = new String[this.totalBloques];
        this.libresBloques = this.totalBloques;
    }

    /*
     * Traduce una cantidad de disco en GiB al numero de bloques equivalente,
     * redondeando los KiB al multiplo mas cercano del tamano de bloque.
     */
    public int gibABloques(int almacenamientoGiB) {
        if (almacenamientoGiB <= 0) {
            throw new IllegalArgumentException("El almacenamiento solicitado debe ser mayor que cero.");
        }
        long kib = (long) almacenamientoGiB * KIB_POR_GIB;
        long bloques = (kib + (bloqueKiB / 2L)) / bloqueKiB;
        if (bloques <= 0) {
            bloques = 1;
        }
        return (int) bloques;
    }

    /*
     * Intenta reservar el disco equivalente a 'cantidadGiB' GiB para vmId.
     * La reserva es atomica (todo o nada) sobre los bloques libres.
     */
    public synchronized boolean reservar(int cantidadGiB, String vmId) {
        validarEntrada(cantidadGiB, vmId);
        int bloquesPedidos = gibABloques(cantidadGiB);
        if (bloquesPedidos > libresBloques) {
            return false;
        }
        int asignados = 0;
        for (int i = 0; i < totalBloques && asignados < bloquesPedidos; i++) {
            if (duenos[i] == null) {
                duenos[i] = vmId;
                asignados++;
            }
        }
        libresBloques -= bloquesPedidos;
        return true;
    }

    /*
     * Libera todos los bloques asignados a vmId.
     */
    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < totalBloques; i++) {
            if (vmId.equals(duenos[i])) {
                duenos[i] = null;
                libresBloques++;
            }
        }
    }

    /*
     * Numero de bloques actualmente libres.
     */
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
            if (vmId.equals(duenos[i])) {
                cuenta++;
            }
        }
        return cuenta;
    }

    private void validarEntrada(int cantidadGiB, String vmId) {
        if (vmId == null || vmId.trim().isEmpty()) {
            throw new IllegalArgumentException("El vmId no puede estar vacio.");
        }
        if (cantidadGiB <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor que cero.");
        }
    }
}
