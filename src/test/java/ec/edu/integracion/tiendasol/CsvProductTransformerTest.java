package ec.edu.integracion.tiendasol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CsvProductTransformerTest {
    @Test
    void transformaSoloLaColumnaProducto() {
        String csv = "id,producto,cantidad,precio\n1,Monitor,2,150\n2,Teclado,5,25\n3,Mouse,3,15\n";
        String expected = "id,producto,cantidad,precio\n1,MONITOR,2,150\n2,TECLADO,5,25\n3,MOUSE,3,15\n";
        assertEquals(expected, CsvProductTransformer.transform(csv));
    }

    @Test
    void respetaComasEntreComillas() {
        String csv = "id,producto,cantidad,precio\n1,\"Monitor, 24 pulgadas\",2,150\n";
        assertEquals("id,producto,cantidad,precio\n1,\"MONITOR, 24 PULGADAS\",2,150\n",
                     CsvProductTransformer.transform(csv));
    }

    @Test
    void conservaSaltosDeLineaWindows() {
        String csv = "id,producto,cantidad,precio\r\n1,Mouse,3,15\r\n";
        assertEquals("id,producto,cantidad,precio\r\n1,MOUSE,3,15\r\n",
                     CsvProductTransformer.transform(csv));
    }

    @Test
    void detectaEncabezadoInvalido() {
        assertThrows(IllegalArgumentException.class,
                     () -> CsvProductTransformer.transform("id,articulo,cantidad\n1,Mouse,3"));
    }

    @Test
    void detectaFilasIncompletas() {
        assertThrows(IllegalArgumentException.class,
                     () -> CsvProductTransformer.transform("id,producto,cantidad,precio\n1,Mouse,3"));
    }
}
