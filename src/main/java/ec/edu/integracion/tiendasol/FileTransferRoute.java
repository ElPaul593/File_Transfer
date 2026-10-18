package ec.edu.integracion.tiendasol;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

/**
 * Implementacion del patron File Transfer para TiendaSol.
 * Ejecutar desde la raiz del proyecto para resolver input/, output/ y archived/.
 */
public class FileTransferRoute extends RouteBuilder {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new FileTransferRoute());
        main.run(args);
    }

    @Override
    public void configure() {
        onException(Exception.class)
            .log(LoggingLevel.ERROR,
                 "ERROR procesando ${header.CamelFileName}: ${exception.message}")
            .handled(false); // No elimina el archivo de entrada si falla la ruta.

        // Ruta 1: lee SOLO CSV, transforma los nombres y entrega al inventario.
        // delete=true elimina el CSV de input SOLO al finalizar la ruta con exito.
        from("file:input?include=.*\\.csv&readLock=changed&readLockMinAge=1000&delete=true&delay=1000")
            .routeId("tiendasol-ventas-a-inventario")
            .filter(header(Exchange.FILE_NAME).endsWith(".csv"))
                .log("INICIO | ${date:now:yyyy-MM-dd HH:mm:ss} | Archivo: ${header.CamelFileName}")
                .convertBodyTo(String.class, "UTF-8")
                .process(exchange -> {
                    String original = exchange.getMessage().getBody(String.class);
                    String transformado = CsvProductTransformer.transform(original);
                    exchange.getMessage().setBody(transformado);
                })
                .to("file:output?charset=UTF-8")
                .log("SALIDA | ${date:now:yyyy-MM-dd HH:mm:ss} | output/${header.CamelFileName}")
                .to("direct:archivar")
            .end();

        // Ruta 2: archiva el CSV ya transformado.
        // Como la primera ruta usa delete=true, se conserva la version procesada
        // en output y archived, y el original desaparece de input cuando todo termina.
        from("direct:archivar")
            .routeId("tiendasol-archivado")
            .to("file:archived?charset=UTF-8")
            .log("ARCHIVADO | ${date:now:yyyy-MM-dd HH:mm:ss} | archived/${header.CamelFileName}");
    }
}
