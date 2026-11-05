
/*
 * ControladorMemoria:
 * Esta clase se encarga de revisar que paginas de memoria estan disponibles y
 * cuales ya estan asignadas, traduciendo cada solicitud en GiB al multiplo mas
 * cercano del tamano de pagina.
 */
public class ControladorMemoria {

    private static final long KIB_POR_GIB = 1024L * 1024L;

    private final int paginaKiB;
    private final int totalPaginas;
    private final String[] propietarios;
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
        this.propietarios = new String[this.totalPaginas];
        this.libresPaginas = this.totalPaginas;
    }

    public int gibAPaginas(int memoriaGiB) {
        long kib = (long) memoriaGiB * KIB_POR_GIB;
        long paginas = (kib + (paginaKiB / 2L)) / paginaKiB;
        return (int) paginas;
    }

    public synchronized boolean reservar(int cantidadGiB, String vmId) {
        int paginasPedidas = gibAPaginas(cantidadGiB);
        if (paginasPedidas > libresPaginas) {
            return false;
        }
        int asignadas = 0;
        for (int i = 0; i < totalPaginas && asignadas < paginasPedidas; i++) {
            if (propietarios[i] == null) {
                propietarios[i] = vmId;
                asignadas++;
            }
        }
        libresPaginas -= paginasPedidas;
        return true;
    }

    public synchronized void liberar(String vmId) {
        if (vmId == null) {
            return;
        }
        for (int i = 0; i < totalPaginas; i++) {
            if (vmId.equals(propietarios[i])) {
                propietarios[i] = null;
                libresPaginas++;
            }
        }
    }

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
            if (vmId.equals(propietarios[i])) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
