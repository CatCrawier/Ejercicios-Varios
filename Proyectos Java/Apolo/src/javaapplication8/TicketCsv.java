package javaapplication8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class TicketCsv {

    public static final String ARCHIVO_PREDETERMINADO = "tickets.csv";

    private static final String SEPARADOR = ",";
    private static final String ENCABEZADO =
            "tipo,id,titulo,descripcion,solicitante,prioridad,estado,extra1,extra2,extra3";
    private static final int COLUMNAS_BASE = 7;

    private TicketCsv() {
    }

    public static boolean existe(String ruta) {
        return Files.isRegularFile(Paths.get(ruta));
    }

    public static void guardar(List<Ticket> tickets, String ruta) throws IOException {
        Path archivo = Paths.get(ruta);
        Path carpeta = archivo.getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }

        try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            escritor.write(ENCABEZADO);
            escritor.newLine();

            for (Ticket ticket : tickets) {
                escritor.write(construirLinea(ticket));
                escritor.newLine();
            }
        }
    }

    public static List<Ticket> cargar(String ruta) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        List<String> lineas = Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8);

        for (int i = 0; i < lineas.size(); i++) {
            String linea = eliminarMarcaBom(lineas.get(i)).trim();
            if (linea.isEmpty() || (i == 0 && linea.startsWith("tipo" + SEPARADOR))) {
                continue;
            }

            try {
                Ticket ticket = construirTicket(separarCampos(linea));
                if (contieneId(tickets, ticket.getId())) {
                    throw new IllegalArgumentException("el id " + ticket.getId() + " esta repetido");
                }

                tickets.add(ticket);
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: se omitio la linea " + (i + 1) + " del CSV (" + e.getMessage() + ").");
            }
        }

        return tickets;
    }

    private static String construirLinea(Ticket ticket) {
        StringBuilder linea = new StringBuilder();
        linea.append(escapar(ticket.getCodigoTipo())).append(SEPARADOR)
                .append(ticket.getId()).append(SEPARADOR)
                .append(escapar(ticket.getTitulo())).append(SEPARADOR)
                .append(escapar(ticket.getDescripcion())).append(SEPARADOR)
                .append(escapar(ticket.getSolicitante())).append(SEPARADOR)
                .append(ticket.getPrioridad().name()).append(SEPARADOR)
                .append(ticket.getEstado().name());

        for (String extra : ticket.getCamposExtra()) {
            linea.append(SEPARADOR).append(escapar(extra));
        }

        return linea.toString();
    }

    private static Ticket construirTicket(List<String> campos) {
        if (campos.size() < COLUMNAS_BASE) {
            throw new IllegalArgumentException("faltan columnas");
        }

        String tipo = campos.get(0).trim().toUpperCase(java.util.Locale.ROOT);
        int id = convertirEntero(campos.get(1), "id");
        String titulo = campos.get(2);
        String descripcion = campos.get(3);
        String solicitante = campos.get(4);
        Prioridad prioridad = Prioridad.valueOf(campos.get(5).trim().toUpperCase(java.util.Locale.ROOT));
        Estado estado = Estado.desdeTexto(campos.get(6));

        switch (tipo) {
            case "HARDWARE":
                validarExtras(campos, 3, tipo);
                return new TicketHardware(id, titulo, descripcion, solicitante, prioridad, estado,
                        campos.get(7), campos.get(8), convertirBooleano(campos.get(9), "requiere repuesto"));
            case "SOFTWARE":
                validarExtras(campos, 3, tipo);
                return new TicketSoftware(id, titulo, descripcion, solicitante, prioridad, estado,
                        campos.get(7), campos.get(8), campos.get(9));
            case "RED":
                validarExtras(campos, 3, tipo);
                return new TicketRed(id, titulo, descripcion, solicitante, prioridad, estado,
                        campos.get(7), campos.get(8), convertirEntero(campos.get(9), "numero de usuarios"));
            case "GENERAL":
                return new Ticket(id, titulo, descripcion, solicitante, prioridad, estado);
            default:
                throw new IllegalArgumentException("tipo desconocido: " + tipo);
        }
    }

    private static void validarExtras(List<String> campos, int cantidad, String tipo) {
        if (campos.size() < COLUMNAS_BASE + cantidad) {
            throw new IllegalArgumentException("faltan datos para el tipo " + tipo);
        }
    }

    private static int convertirEntero(String valor, String campo) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("el " + campo + " no es un numero valido");
        }
    }

    private static boolean convertirBooleano(String valor, String campo) {
        String valorNormalizado = valor.trim();
        if ("true".equalsIgnoreCase(valorNormalizado)) {
            return true;
        }
        if ("false".equalsIgnoreCase(valorNormalizado)) {
            return false;
        }

        throw new IllegalArgumentException("el " + campo + " debe ser true o false");
    }

    private static boolean contieneId(List<Ticket> tickets, int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private static String eliminarMarcaBom(String linea) {
        return linea.startsWith("\uFEFF") ? linea.substring(1) : linea;
    }

    private static String escapar(String valor) {
        if (valor.contains(SEPARADOR) || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }

        return valor;
    }

    private static List<String> separarCampos(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);

            if (entreComillas) {
                if (caracter == '"') {
                    boolean comillaEscapada = i + 1 < linea.length() && linea.charAt(i + 1) == '"';
                    if (comillaEscapada) {
                        campo.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    campo.append(caracter);
                }
                continue;
            }

            if (caracter == '"') {
                entreComillas = true;
            } else if (caracter == ',') {
                campos.add(campo.toString());
                campo.setLength(0);
            } else {
                campo.append(caracter);
            }
        }

        if (entreComillas) {
            throw new IllegalArgumentException("faltan comillas de cierre");
        }

        campos.add(campo.toString());
        return campos;
    }
}
