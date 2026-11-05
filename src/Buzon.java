
import java.util.LinkedList;


public class Buzon {
    private final LinkedList<Solicitud> cola = new LinkedList<>();
    private final int capacidad;
    private final int totalUsuarios;
    private int usuariosTerminados = 0;
    private int totalRecibidas = 0;
    private int totalEntregadas = 0;

    public Buzon(int capacidad, int totalUsuarios) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad del buzón debe ser positiva.");
        }
        this.capacidad = capacidad;
        this.totalUsuarios = totalUsuarios;
    }

    public synchronized void agregar(Solicitud s, String emisor) throws InterruptedException {
        while (cola.size() >= capacidad) {
            System.out.println("Buzon lleno (" + cola.size() + "/" + capacidad + "), "
                + emisor + " espera (espera pasiva)");
            wait();
        }
        cola.addLast(s);
        totalRecibidas++;
        System.out.println(emisor + " deposita " + s.getId()
            + " en el buzon (" + cola.size() + "/" + capacidad + ")");
        notifyAll();
    }

    public synchronized Solicitud obtener() {
        if (cola.isEmpty()) {
            return null;
        }
        Solicitud s = cola.removeFirst();
        totalEntregadas++;
        notifyAll();
        return s;
    }

    public synchronized void registrarFinUsuario(String usuario) {
        usuariosTerminados++;
        System.out.println(usuario + " reporta su terminacion en el buzon ("
            + usuariosTerminados + "/" + totalUsuarios + ")");
        notifyAll();
    }

    public synchronized boolean todosUsuariosTerminaron() {
        return usuariosTerminados >= totalUsuarios;
    }

    public synchronized int usuariosActivos() {
        return totalUsuarios - usuariosTerminados;
    }


    public synchronized boolean terminado() {
        return usuariosTerminados >= totalUsuarios && cola.isEmpty();
    }

    public synchronized int tamano() {
        return cola.size();
    }

    public synchronized int getTotalRecibidas() {
        return totalRecibidas;
    }

    public synchronized int getTotalEntregadas() {
        return totalEntregadas;
    }
}
