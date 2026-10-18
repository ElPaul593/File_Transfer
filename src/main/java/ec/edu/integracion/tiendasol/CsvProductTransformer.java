package ec.edu.integracion.tiendasol;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Transforma exclusivamente la columna "producto" a mayusculas.
 * Maneja valores CSV entrecomillados y comillas escapadas ("").
 * Mantiene cantidades, precios y orden de las columnas.
 */
public final class CsvProductTransformer {
    private CsvProductTransformer() { }

    public static String transform(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("El archivo CSV no puede estar vacio");
        }
        String separator = csv.contains("\r\n") ? "\r\n" : "\n";
        String[] lines = csv.split("\\r?\\n", -1);
        if (lines.length == 0) {
            throw new IllegalArgumentException("El CSV debe tener encabezado");
        }
        List<String> header = parseLine(lines[0]);
        int productColumn = -1;
        for (int i = 0; i < header.size(); i++) {
            if ("producto".equalsIgnoreCase(header.get(i).trim())) {
                productColumn = i;
                break;
            }
        }
        if (productColumn < 0) {
            throw new IllegalArgumentException("El encabezado CSV no contiene la columna 'producto'");
        }
        List<String> result = new ArrayList<>();
        // Conservamos el encabezado original.
        result.add(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                result.add(lines[i]);
                continue;
            }
            List<String> columns = parseLine(lines[i]);
            if (columns.size() != header.size()) {
                throw new IllegalArgumentException(
                    "Cantidad incorrecta de columnas en la linea " + (i + 1));
            }
            columns.set(productColumn, columns.get(productColumn).toUpperCase(Locale.ROOT));
            result.add(formatLine(columns));
        }
        return String.join(separator, result);
    }

    private static List<String> parseLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quotes = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quotes = !quotes;
                }
            } else if (current == ',' && !quotes) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        if (quotes) {
            throw new IllegalArgumentException("CSV con comillas sin cerrar");
        }
        columns.add(value.toString());
        return columns;
    }

    private static String formatLine(List<String> columns) {
        List<String> escaped = new ArrayList<>();
        for (String field : columns) {
            if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
                escaped.add("\"" + field.replace("\"", "\"\"") + "\"");
            } else {
                escaped.add(field);
            }
        }
        return String.join(",", escaped);
    }
}
