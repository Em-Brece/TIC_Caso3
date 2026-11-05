
/*
 * ControladorMemoria (objeto de control, NO es un thread).
 *
 * Registra cuales paginas de memoria estan asignadas y cuales estan libres, y
 * guarda la maquina virtual (vmId) duena de cada pagina ocupada.
 *
 * Traduccion de unidades: la capacidad total y las solicitudes llegan en GiB,
 * pero el recurso se administra en paginas cuyo tamano se expresa en KiB.
 *   1 GiB = 1024 * 1024 KiB.
 *   total de paginas      = (memoriaGiB en KiB) / paginaKiB
 *   paginas de una pedido = GiB -> KiB redondeado al multiplo mas cercano del
 *                           tamano de pagina, dividido por paginaKiB.
 *
 * Todos los metodos que tocan el estado compartido son synchronized.
 */
public class ControladorMemoria {

    private static final long KIB_POR_GIB = 1024L * 1024L;

    private final int paginaKiB;
    private final int totalPaginas;
    // duenos[i] == null -> pagina i libre ; si no, vmId de la MV que la tiene.
    private final String[] duenos;
    private int libresPaginas;

    public ControladorMemoria(int memoriaGiB, int paginaKiB) {
        if (memoriaGiB <= 0) {
            throw new IllegalArgumentException("La memoria total debe ser mayor que cero.");
        }
        if (paginaKiB <= 0) {
            throw new IllegalArgumentException("El tamano de pagina debe ser mayor que cero.");
        }
        long capacidadKiB = (long) memoriaGiB * KIB_POR_GIB;
        if (capacidadKiB % paginaKiB != 0) {
            throw new IllegalArgumentException(
                "La memoria total en KiB no es multiplo del tamano de pagina.");
        }
        long paginas = capacidadKiB / paginaKiB;
        if (paginas > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Demasiadas paginas para representar.");
        }
        this.paginaKiB = paginaKiB;
        this.totalPaginas = (int) paginas;
        this.duenos = new String[this.totalPaginas];
        this.libresPaginas = this.totalPaginas;
    }

    /*
     * Traduce una cantidad de memoria en GiB al numero de paginas equivalente,
     * redondeando los KiB al multiplo mas cercano del tamano de pagina.
     */
    public int gibAPaginas(int memoriaGiB) {
        if (memoriaGiB <= 0) {
            throw new IllegalArgumentException("La memoria solicitada debe ser mayor que cero.");
        }
        long kib = (long) memoriaGiB * KIB_POR_GIB;
        // Multiplo mas cercano del tamano de pagina (redondeo a la mitad hacia arriba).
        long paginas = (kib + (paginaKiB / 2L)) / paginaKiB;
        if (paginas <= 0) {
            paginas = 1; // al menos una pagina para una solicitud positiva.
        }
        return (int) paginas;
    }

    /*
     * Intenta reservar la memoria equivalente a 'cantidadGiB' GiB para vmId.
     * La reserva es atomica (todo o nada) sobre las paginas libres.
     */
    public synchronized boolean reservar(int cantidadGiB, String vmId) {
        validarEntrada(cantidadGiB, vmId);
        int paginasPedidas = gibAPaginas(cantidadGiB);
        if (paginasPedidas > libresPaginas) {
            return false;
        }
        int asignadas = 0;
        for (int i = 0; i < totalPaginas && asignadas < paginasPedidas; i++) {
            if (duenos[i] == null) {
                duenos[i] = vmId;
                asignadas++;
            }
        }
        libresPaginas -= paginasPedidas;
        return true;
    }

    /*
     * Libera todas las paginas asignadas a vmId.
     */
    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < totalPaginas; i++) {
            if (vmId.equals(duenos[i])) {
                duenos[i] = null;
                libresPaginas++;
            }
        }
    }

    /*
     * Numero de paginas actualmente libres.
     */
    public synchronized int disponibles() {
        return libresPaginas;
    }

    public synchronized int capacidadTotal() {
        return totalPaginas;
    }

    public int getPaginaKiB() {
        return paginaKiB;
    }

    public synchronized int asignadosA(String vmId) {
        if (vmId == null) {
            return 0;
        }
        int cuenta = 0;
        for (int i = 0; i < totalPaginas; i++) {
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
