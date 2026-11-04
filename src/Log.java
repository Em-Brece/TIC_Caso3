
public final class Log {
    private static final long INICIO = System.currentTimeMillis();
    private static volatile boolean activo = true;

    private Log() {
    }

    public static void setActivo(boolean valor) {
        activo = valor;
    }

    public static synchronized void info(String mensaje) {
        if (!activo) {
            return;
        }
        long t = System.currentTimeMillis() - INICIO;
        System.out.printf("[%7d ms] %-12s %s%n", t, Thread.currentThread().getName(), mensaje);
    }
}