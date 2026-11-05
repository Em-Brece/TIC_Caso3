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

    public synchronized void agregar(Solicitud s) throws InterruptedException {
        while (cola.size() >= capacidad) {
            Log.info("buzón lleno (" + cola.size() + "/" + capacidad + "), espera pasiva");
            wait();
        }
        cola.addLast(s);
        totalRecibidas++;
        Log.info("deposita " + s.getId() + " en el buzón (" + cola.size() + "/" + capacidad + ")");
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
        Log.info(usuario + " reporta su terminación en el buzón ("
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