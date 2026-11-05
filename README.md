# TIC - Caso 3

## Compilación

Desde la raíz del proyecto:

```
javac -d bin src/*.java
```

## Ejecutar caso

El programa recibe como primer argumento la **ruta del archivo de configuración**.

Configuración por defecto (src/config.txt):
```
java -cp bin Main
```

Para tests:
```
java -cp bin Main tests/caso<n>.txt
```

## Archivo de configuración

```
- nu= Número de usuarios 
- base= Número base de solicitudes
- ng= Número de gestores 
- tam1= Capacidad del buzón de entrada 
- tam2= Umbral / capacidad de la zona de espera 
- procesadores= Número de procesadores del sistema 
- memoria= Memoria total en GiB
- pagina= Tamaño de página en KiB 
- almacenamiento= Almacenamiento total en GiB   
- bloque= Tamaño de bloque en KiB  
```
