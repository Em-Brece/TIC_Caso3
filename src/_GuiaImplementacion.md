# Guía de archivos (layout plano)

Todos los `.java` van directamente en `src/`, sin paquetes.
Compilar:  `javac src/*.java -d bin`
Ejecutar:  `java -cp bin Main src/config.txt`

## Persona A — Infraestructura y Recursos
- `Configuracion.java` — lee el archivo de texto + `cargar(String ruta)`.
- `Solicitud.java` — id, procesadores, memoriaGB, almacenamientoGB (inmutable).
- Estructuras auxiliares (páginas/bloques/procesadores: slot libre o vmId dueño) + generador de IDs.
- `ControladorProcesadores.java`
- `ControladorMemoria.java`        (traduce GiB -> páginas; página en KiB)
- `ControladorAlmacenamiento.java` (traduce GiB -> bloques; bloque en KiB)

Interfaz acordada de los controladores (NO son threads, métodos synchronized):
    boolean reservar(int cantidad, String vmId);
    void liberar(String vmId);
    int disponibles();

## Persona B — Concurrencia y Flujo
- `Buzon.java` — capacidad limitada, productor-consumidor (wait/notifyAll).
      void agregar(Solicitud s); Solicitud obtener(); boolean todosUsuariosTerminaron();
- `ZonaEspera.java` — cola de MVs bloqueadas, contador de pendientes.
      void esperar(MaquinaVirtual mv); void despertar();
- `Usuario.java` — thread productor de solicitudes.
- `GestorSolicitudes.java` — procesa solicitudes, crea MVs, maneja el umbral.
- `MaquinaVirtual.java` — reserva en orden (procesador, memoria, disco), espera pasiva si falla, libera y despierta al terminar.

## Ambos
- `Main.java` — punto de entrada: carga Configuracion, crea controladores/buzón/zona, lanza threads, join().

## Interfaces a acordar desde el inicio
- Formato y nombres de claves del `config.txt`.
- Campos de `Solicitud`.
- Firmas de los controladores (arriba).
- Formato del id de solicitud (p. ej. "U1-3").
