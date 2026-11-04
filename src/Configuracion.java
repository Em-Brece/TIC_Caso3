
/*
 * Archivo de configuración:
 * Este archivo no esta explicitamente mencionado o definido en el enunciado,
 * pero funciona como el lector de los archivos .txt que tienen la configuración
 * de prueba. Junto a este archivo, es posible encontrar un config.txt de ejemplo
 * el cual muestra la estructura y el formato de como se deben ver los archivos
 * de ese estilo.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuracion {
    private static final String[] CLAVES_REQUERIDAS = {
        "nu", "base", "ng", "tam1", "tam2",
        "procesadores", "memoria", "pagina", "almacenamiento", "bloque"
    };

    private final String rutaArchivo;
    private final Map<String, Integer> valores;

    public Configuracion(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo;
        this.valores = new LinkedHashMap<>();
    }

    private Configuracion(String rutaArchivo, Map<String, Integer> valores) {
        this.rutaArchivo = rutaArchivo;
        this.valores = new LinkedHashMap<>(valores);
    }

    public static Configuracion cargar(String rutaArchivo) throws IOException {
        Configuracion configuracion = new Configuracion(rutaArchivo);
        Map<String, Integer> valores = new LinkedHashMap<>();
        Path ruta = Path.of(rutaArchivo);

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                int comentario = linea.indexOf('#');
                if (comentario >= 0) {
                    linea = linea.substring(0, comentario);
                }
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                int separador = linea.indexOf('=');
                if (separador < 1) {
                    throw error(ruta, numeroLinea, "se esperaba el formato clave=valor");
                }

                String clave = linea.substring(0, separador).trim();
                String textoValor = linea.substring(separador + 1).trim();
                if (!esClaveRequerida(clave)) {
                    throw error(ruta, numeroLinea, "parámetro desconocido: " + clave);
                }
                if (valores.containsKey(clave)) {
                    throw error(ruta, numeroLinea, "parámetro duplicado: " + clave);
                }

                int valor;
                try {
                    valor = Integer.parseInt(textoValor);
                } catch (NumberFormatException excepcion) {
                    throw error(ruta, numeroLinea, "el valor de " + clave + " debe ser un entero");
                }
                if (valor <= 0) {
                    throw error(ruta, numeroLinea, "el valor de " + clave + " debe ser mayor que cero");
                }
                valores.put(clave, valor);
            }
        }

        for (String clave : CLAVES_REQUERIDAS) {
            if (!valores.containsKey(clave)) {
                throw new IOException("Falta el parámetro requerido '" + clave + "' en " + ruta + ".");
            }
        }

        return new Configuracion(configuracion.rutaArchivo, valores);
    }

    public String leerArchivo() throws IOException {
        StringBuilder contenido = new StringBuilder();
        try (BufferedReader lector = Files.newBufferedReader(
                Path.of(rutaArchivo), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                contenido.append(linea).append(System.lineSeparator());
            }
        }
        return contenido.toString();
    }

    public int getNu() {
        return getValor("nu");
    }

    public int getBase() {
        return getValor("base");
    }

    public int getNg() {
        return getValor("ng");
    }

    public int getTam1() {
        return getValor("tam1");
    }

    public int getTam2() {
        return getValor("tam2");
    }

    public int getProcesadores() {
        return getValor("procesadores");
    }

    public int getMemoria() {
        return getValor("memoria");
    }

    public int getPagina() {
        return getValor("pagina");
    }

    public int getAlmacenamiento() {
        return getValor("almacenamiento");
    }

    public int getBloque() {
        return getValor("bloque");
    }

    private int getValor(String clave) {
        Integer valor = valores.get(clave);
        if (valor == null) {
            throw new IllegalStateException(
                "La configuración no se ha cargado; use Configuracion.cargar(rutaArchivo).");
        }
        return valor;
    }

    private static boolean esClaveRequerida(String clave) {
        for (String requerida : CLAVES_REQUERIDAS) {
            if (requerida.equals(clave)) {
                return true;
            }
        }
        return false;
    }

    private static IOException error(Path ruta, int numeroLinea, String mensaje) {
        return new IOException(ruta + ":" + numeroLinea + ": " + mensaje + ".");
    }
}
