# Taller 1 - File Transfer con Apache Camel | TiendaSol

**Integrantes:** Victor Gomez Suquilanda Fogacho, Roberto Guaña y Paul Larrea.

## Objetivo
Automatizar el envio de ventas entre dos sistemas heredados por medio del patron **File Transfer**, leyendo `ventas.csv`, transformando **solo los nombres de productos** a mayusculas, entregando una copia a `output/`, archivando el archivo transformado en `archived/` y registrando fecha/hora en consola y `logs/tiendasol.log`.

## Versiones necesarias
- **JDK 17 o superior** (ideal JDK 17 o 21).
- **Apache Maven 3.9+**, `git` opcional para publicar.
- Conexion a Internet para descargar las dependencias la **primera vez** que se ejecuta Maven.
- Apache Camel **4.10.0** fijado desde `pom.xml`. **No** es necesario instalar Camel como ZIP ni construir el classpath manualmente.

> Aunque la guia original menciona Java 11+, el proyecto usa Camel 4.10.0, cuya version minima es Java 17. Se implementa mediante Maven para evitar los problemas de dependencias del procedimiento con `javac -cp "lib/*"`.

## Estructura

```text
Taller_Apache_Camel_TiendaSol/
├── pom.xml
├── README.md
├── input/ventas.csv             # entrada inicial (desaparece luego del exito)
├── output/                     # copia transformada para inventario
├── archived/                   # copia transformada archivada
├── logs/                       # archivo tiendasol.log al ejecutarse
├── ejemplos/
│   ├── ventas-original.csv     # respaldo para pruebas repetidas
│   └── ventas-transformadas-esperado.csv
├── src/main/java/ec/edu/integracion/tiendasol/
│   ├── FileTransferRoute.java
│   └── CsvProductTransformer.java
├── src/main/resources/logback.xml
├── src/test/java/ec/edu/integracion/tiendasol/
│   └── CsvProductTransformerTest.java
├── scripts/
│   ├── verificar_resultados.py
│   └── publicar_github.md
├── evidencias/
└── Informe_Taller_Apache_Camel_TiendaSol.pdf
```

## 1. Verificacion de herramientas

```bash
java -version
mvn -version
```

## 2. Compilar y ejecutar las 5 pruebas unitarias

Ubicate en la **raiz de esta carpeta** (donde se encuentra `pom.xml`) y ejecuta:

```bash
mvn clean test
```

## 3. Iniciar el flujo (Windows PowerShell, Linux o macOS)

```bash
mvn exec:java
```

La aplicacion se queda observando la carpeta `input/`. Luego de unos segundos deberia procesar `ventas.csv`. Debera aparecer algo similar a:

```text
INICIO | [fecha y hora] | Archivo: ventas.csv
SALIDA | [fecha y hora] | output/ventas.csv
ARCHIVADO | [fecha y hora] | archived/ventas.csv
```

**Los anteriores mensajes son ejemplos del formato esperado, no evidencias de ejecucion.** Para detener la aplicacion utiliza `Ctrl + C`.

## 4. Inspeccionar resultados

En `output/ventas.csv` y `archived/ventas.csv` debe aparecer:

```csv
id,producto,cantidad,precio
1,MONITOR,2,150
2,TECLADO,5,25
3,MOUSE,3,15
```

El archivo de entrada **se elimina solo despues de completar correctamente las dos rutas** (`delete=true` del consumidor File); la version inicial siempre se conserva en `ejemplos/ventas-original.csv`. Asi evitamos reprocesar cada dia el mismo archivo. `input/` deberia quedar vacia tras el exito.

Puedes comprobar con:

```bash
python scripts/verificar_resultados.py
```

En Windows, si `python` no funciona, intenta `py scripts/verificar_resultados.py`.

## 5. Repetir la prueba

En Windows PowerShell:

```powershell
Copy-Item ejemplos/ventas-original.csv input/ventas.csv
mvn exec:java
```

En Linux/macOS:

```bash
cp ejemplos/ventas-original.csv input/ventas.csv
mvn exec:java
```

Si el proceso de Camel ya esta ejecutandose, solo copia nuevamente el CSV; no hace falta ejecutar `mvn exec:java` una segunda vez.

## 6. Prueba de filtro

Coloca un archivo `notas.txt` dentro de `input/` mientras Camel se ejecuta: debera quedar intacto, y no debera aparecer como `output/notas.txt`, gracias al `include=.*\\.csv` del consumidor y al filtro de ruta.

## 7. Guardar evidencias solicitadas en la tarea

**Las siguientes capturas solo pueden tomarse tras ejecutar Apache Camel en tu computadora con Java y Maven:**
1. Captura de la terminal con los tres mensajes **INICIO, SALIDA y ARCHIVADO** de `ventas.csv` y la fecha/hora.
2. Captura del explorador de archivos mostrando `output/ventas.csv`.
3. Captura con el contenido transformado de `output/ventas.csv`.
4. Opcional: `archived/ventas.csv`, `logs/tiendasol.log`, y resultado de `python scripts/verificar_resultados.py`.

Guarda las capturas en `evidencias/` y anadeles al informe DOCX incluido. El informe ya contiene el resto de la redaccion, pero **las capturas reales no estan incluidas** porque no es correcto inventar una ejecucion.

## 8. Publicar en GitHub

Lee `scripts/publicar_github.md`. El enlace definitivo de repositorio debe obtenerse despues de subirlo con tu cuenta. Este paquete es el proyecto listo para subir.

## Comportamiento detallado

- **Ruta 1 (tiendasol-ventas-a-inventario):** selecciona `.csv`, aplica doble filtro, lee el archivo cuando deja de cambiar, valida encabezado/filas, transforma solo `producto` a mayusculas, escribe en `output`, envia el mensaje al segundo flujo.
- **Ruta 2 (tiendasol-archivado):** recibe por `direct:archivar` el contenido transformado y lo escribe en `archived`.
- **Al terminar ambas rutas:** Camel elimina el archivo original de `input`, evitando duplicados por reprocesamiento.
- **Fallas:** un archivo invalido lanza una excepcion, registra un ERROR y no se elimina automaticamente el archivo de entrada. Corrige el archivo antes de volver a procesarlo.
- **Limitacion:** el lector CSV implementado soporta campos citados dentro de una misma linea y comillas dobles escapadas; no admite registros con saltos de linea dentro de un campo citado, lo cual no se requiere en este taller.

## Fuentes
- Guia del docente: `Taller File Transfer Apache Camel.pdf` (suministrada en la actividad).
- Documentacion Apache Camel: https://camel.apache.org/camel-core/getting-started/index.html
- Componente File: https://camel.apache.org/components/4.10.x/file-component.html
- Notas de version Camel 4.10.0: https://camel.apache.org/releases/release-4.10.0/

> **Antes de publicar:** si ejecutaste el flujo y `input/ventas.csv` fue eliminado, vuelve a copiar el ejemplo a `input/` para que el repositorio conserve una entrada de prueba.
