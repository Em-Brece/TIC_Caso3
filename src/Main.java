import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
        String ruta = args.length > 0 ? args[0] : "src/config.txt";
        Configuracion config = Configuracion.cargar(ruta);

        ControladorProcesadores cpu = new ControladorProcesadores(config.getProcesadores());
        ControladorMemoria memoria = new ControladorMemoria(config.getMemoria(), config.getPagina());
        ControladorAlmacenamiento disco =
            new ControladorAlmacenamiento(config.getAlmacenamiento(), config.getBloque());

        int cpuInicial = cpu.disponibles();
        int memInicial = memoria.disponibles();
        int discoInicial = disco.disponibles();

        Buzon buzon = new Buzon(config.getTam1(), config.getNu());
        ZonaEspera zona = new ZonaEspera(config.getTam2());

        System.out.println("inicio: nu=" + config.getNu() + " base=" + config.getBase()
            + " ng=" + config.getNg()
            + " tam1=" + config.getTam1() + " tam2=" + config.getTam2());

        List<Usuario> usuarios = new ArrayList<>();
        int esperadas = 0;
        for (int i = 1; i <= config.getNu(); i++) {
            Usuario u = new Usuario(i, config, buzon);
            esperadas += u.getNumSolicitudes();
            usuarios.add(u);
        }

        List<GestorSolicitudes> gestores = new ArrayList<>();
        for (int i = 1; i <= config.getNg(); i++) {
            gestores.add(new GestorSolicitudes(i, buzon, zona, cpu, memoria, disco));
        }

        for (Usuario u : usuarios) {
            u.start();
        }
        for (GestorSolicitudes g : gestores) {
            g.start();
        }

        for (Usuario u : usuarios) {
            u.join();
        }
        System.out.println("todos los usuarios terminaron");

        List<MaquinaVirtual> maquinas = new ArrayList<>();
        for (GestorSolicitudes g : gestores) {
            g.join();
            maquinas.addAll(g.getMaquinas());
        }
        System.out.println("todos los gestores terminaron");

        for (MaquinaVirtual mv : maquinas) {
            mv.join();
        }
        System.out.println("todas las maquinas virtuales terminaron");

        boolean ok = true;
        ok &= verificar("solicitudes generadas = recibidas en el buzón",
            esperadas == buzon.getTotalRecibidas(), esperadas + " vs " + buzon.getTotalRecibidas());
        ok &= verificar("solicitudes recibidas = MVs creadas",
            buzon.getTotalRecibidas() == maquinas.size(),
            buzon.getTotalRecibidas() + " vs " + maquinas.size());
        ok &= verificar("buzón vacío", buzon.tamano() == 0, "tamaño=" + buzon.tamano());
        ok &= verificar("zona de espera vacía", zona.pendientes() == 0 && zona.dormidas() == 0,
            "pendientes=" + zona.pendientes() + " dormidas=" + zona.dormidas());
        ok &= verificar("procesadores en estado inicial", cpu.disponibles() == cpuInicial,
            cpu.disponibles() + "/" + cpuInicial);
        ok &= verificar("memoria en estado inicial", memoria.disponibles() == memInicial,
            memoria.disponibles() + "/" + memInicial);
        ok &= verificar("almacenamiento en estado inicial", disco.disponibles() == discoInicial,
            disco.disponibles() + "/" + discoInicial);

        System.out.println("maximo de MVs pendientes en la zona: " + zona.getMaxPendientes()
            + ", veces que se suspendio la admision: " + zona.getVecesDrenado());
        System.out.println(ok ? "VERIFICACION FINAL CORRECTA" : "VERIFICACION FINAL CON ERRORES");
        if (!ok) {
            System.exit(1);
        }
    }

    private static boolean verificar(String aspecto, boolean condicion, String detalle) {
        System.out.println((condicion ? "[OK]    " : "[FALLA] ") + aspecto + " (" + detalle + ")");
        return condicion;
    }
}