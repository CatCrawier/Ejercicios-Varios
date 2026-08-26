package javaapplication8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Apolo {

    private static final int OPCION_REGISTRAR = 1;
    private static final int OPCION_LISTAR = 2;
    private static final int OPCION_FILTRAR_ESTADO = 3;
    private static final int OPCION_FILTRAR_PRIORIDAD = 4;
    private static final int OPCION_ACTUALIZAR = 5;
    private static final int OPCION_CERRAR = 6;
    private static final int OPCION_RESUMEN = 7;
    private static final int OPCION_GUARDAR = 8;
    private static final int OPCION_CARGAR = 9;
    private static final int OPCION_SALIR = 10;

    private static final int CAMPO_TITULO = 1;
    private static final int CAMPO_DESCRIPCION = 2;
    private static final int CAMPO_SOLICITANTE = 3;
    private static final int CAMPO_PRIORIDAD = 4;
    private static final int CAMPO_ESTADO = 5;
    private static final int CAMPO_VOLVER = 6;

    private static final int TIPO_GENERAL = 1;
    private static final int TIPO_HARDWARE = 2;
    private static final int TIPO_SOFTWARE = 3;
    private static final int TIPO_RED = 4;

    private static final int ID_MINIMO = 1;
    private static final int ID_MAXIMO = 999999;
    private static final int USUARIOS_MINIMOS = 1;
    private static final int USUARIOS_MAXIMOS = 5000;

    private static final String SEPARADOR = "--------------------------------";

    public static void main(String[] args) {
        String archivo = args.length > 0 ? args[0] : TicketCsv.ARCHIVO_PREDETERMINADO;
        List<Ticket> tickets = cargarInicial(archivo);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean continuar = true;

            System.out.println("Gestor de tickets de soporte tecnico");
            System.out.println("Archivo de datos: " + archivo);

            while (continuar) {
                switch (leerOpcionMenu(scanner)) {
                    case OPCION_REGISTRAR -> registrarTicket(scanner, tickets);
                    case OPCION_LISTAR -> listarTickets(tickets);
                    case OPCION_FILTRAR_ESTADO -> filtrarPorEstado(scanner, tickets);
                    case OPCION_FILTRAR_PRIORIDAD -> filtrarPorPrioridad(scanner, tickets);
                    case OPCION_ACTUALIZAR -> actualizarTicket(scanner, tickets);
                    case OPCION_CERRAR -> cerrarTicket(scanner, tickets);
                    case OPCION_RESUMEN -> mostrarResumen(tickets);
                    case OPCION_GUARDAR -> guardarTickets(tickets, archivo);
                    case OPCION_CARGAR -> recargarTickets(scanner, tickets, archivo);
                    default -> {
                        continuar = false;
                        guardarTickets(tickets, archivo);
                        System.out.println("Fin del programa.");
                    }
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("No fue posible leer datos desde la consola.");
        }
    }

    private static List<Ticket> cargarInicial(String archivo) {
        if (!TicketCsv.existe(archivo)) {
            System.out.println("No se encontro el archivo " + archivo + ". Se iniciara con una lista vacia.");
            return new ArrayList<>();
        }

        try {
            List<Ticket> tickets = TicketCsv.cargar(archivo);
            System.out.println("Se cargaron " + tickets.size() + " tickets desde " + archivo + ".");
            return tickets;
        } catch (IOException e) {
            System.out.println("Error al leer el archivo " + archivo + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void guardarTickets(List<Ticket> tickets, String archivo) {
        try {
            TicketCsv.guardar(tickets, archivo);
            System.out.println("Se guardaron " + tickets.size() + " tickets en " + archivo + ".");
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo " + archivo + ": " + e.getMessage());
        }
    }

    private static void recargarTickets(Scanner scanner, List<Ticket> tickets, String archivo) {
        if (!TicketCsv.existe(archivo)) {
            System.out.println("No se encontro el archivo " + archivo + ".");
            return;
        }

        if (!tickets.isEmpty()
                && !leerConfirmacion(scanner, "Se reemplazaran los tickets en memoria. Continuar? (S/N): ")) {
            System.out.println("Carga cancelada.");
            return;
        }

        try {
            List<Ticket> cargados = TicketCsv.cargar(archivo);
            tickets.clear();
            tickets.addAll(cargados);
            System.out.println("Se cargaron " + tickets.size() + " tickets desde " + archivo + ".");
        } catch (IOException e) {
            System.out.println("Error al leer el archivo " + archivo + ": " + e.getMessage());
        }
    }

    private static void registrarTicket(Scanner scanner, List<Ticket> tickets) {
        try {
            int tipo = leerTipoTicket(scanner);
            int id = leerIdDisponible(scanner, tickets);
            String titulo = leerTexto(scanner, "Ingrese el titulo: ");
            String descripcion = leerTexto(scanner, "Ingrese la descripcion: ");
            String solicitante = leerTexto(scanner, "Ingrese el solicitante: ");
            Prioridad prioridad = leerPrioridad(scanner);
            Estado estado = leerEstado(scanner);

            Ticket ticket;
            switch (tipo) {
                case TIPO_HARDWARE ->  {
                    String equipo = leerTexto(scanner, "Ingrese el equipo afectado: ");
                    String numeroSerie = leerTexto(scanner, "Ingrese el numero de serie: ");
                    boolean requiereRepuesto = leerConfirmacion(scanner, "Requiere repuesto? (S/N): ");

                    ticket = new TicketHardware(id, titulo, descripcion, solicitante, prioridad, estado,
                            equipo, numeroSerie, requiereRepuesto);
                }
                case TIPO_SOFTWARE ->  {
                    String aplicacion = leerTexto(scanner, "Ingrese la aplicacion: ");
                    String version = leerTexto(scanner, "Ingrese la version: ");
                    String mensajeError = leerTexto(scanner, "Ingrese el mensaje de error: ");

                    ticket = new TicketSoftware(id, titulo, descripcion, solicitante, prioridad, estado,
                            aplicacion, version, mensajeError);
                }
                case TIPO_RED ->  {
                    String sede = leerTexto(scanner, "Ingrese la sede: ");
                    String tipoConexion = leerTexto(scanner, "Ingrese el tipo de conexion (cableada/wifi/vpn): ");
                    int usuariosAfectados = leerEntero(scanner, "Ingrese los usuarios afectados: ",
                            USUARIOS_MINIMOS, USUARIOS_MAXIMOS);

                    ticket = new TicketRed(id, titulo, descripcion, solicitante, prioridad, estado,
                            sede, tipoConexion, usuariosAfectados);
                }
                default -> ticket = new Ticket(id, titulo, descripcion, solicitante, prioridad, estado);
            }

            tickets.add(ticket);
            System.out.println("Ticket registrado correctamente con el id " + ticket.getId() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("El ticket no fue registrado.");
        }
    }

    private static void actualizarTicket(Scanner scanner, List<Ticket> tickets) {
        Ticket ticket = buscarTicketPorId(scanner, tickets);
        if (ticket == null) {
            return;
        }

        System.out.println();
        System.out.println("Ticket seleccionado:");
        ticket.mostrarDatos();
        System.out.println(SEPARADOR);

        boolean seguirEditando = true;
        while (seguirEditando) {
            int campo = leerCampoActualizable(scanner);

            try {
                switch (campo) {
                    case CAMPO_TITULO -> ticket.setTitulo(leerTexto(scanner, "Nuevo titulo: "));
                    case CAMPO_DESCRIPCION -> ticket.setDescripcion(leerTexto(scanner, "Nueva descripcion: "));
                    case CAMPO_SOLICITANTE -> ticket.setSolicitante(leerTexto(scanner, "Nuevo solicitante: "));
                    case CAMPO_PRIORIDAD -> ticket.setPrioridad(leerPrioridad(scanner));
                    case CAMPO_ESTADO -> ticket.setEstado(leerEstado(scanner));
                    default -> seguirEditando = false;
                }

                if (seguirEditando) {
                    System.out.println("Ticket " + ticket.getId() + " actualizado.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("Estado final del ticket:");
        ticket.mostrarDatos();
        System.out.println(SEPARADOR);
    }

    private static void cerrarTicket(Scanner scanner, List<Ticket> tickets) {
        Ticket ticket = buscarTicketPorId(scanner, tickets);
        if (ticket == null) {
            return;
        }

        try {
            ticket.cerrar();
            System.out.println("El ticket " + ticket.getId() + " fue cerrado.");
        } catch (IllegalStateException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }

    private static Ticket buscarTicketPorId(Scanner scanner, List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
            return null;
        }

        int id = leerEntero(scanner, "Ingrese el id del ticket: ", ID_MINIMO, ID_MAXIMO);
        Ticket ticket = buscarPorId(tickets, id);
        if (ticket == null) {
            System.out.println("No existe un ticket con el id " + id + ".");
        }

        return ticket;
    }

    private static void listarTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
            return;
        }

        System.out.println();
        System.out.println("Lista de tickets (" + tickets.size() + "):");
        for (Ticket ticket : tickets) {
            ticket.mostrarDatos();
            System.out.println(SEPARADOR);
        }
    }

    private static void filtrarPorEstado(Scanner scanner, List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
            return;
        }

        Estado estado = leerEstado(scanner);
        boolean encontrado = false;

        System.out.println();
        System.out.println("Tickets en estado " + estado.getEtiqueta() + ":");
        for (Ticket ticket : tickets) {
            if (ticket.getEstado() == estado) {
                ticket.mostrarDatos();
                System.out.println(SEPARADOR);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No hay tickets con ese estado.");
        }
    }

    private static void filtrarPorPrioridad(Scanner scanner, List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
            return;
        }

        Prioridad prioridad = leerPrioridad(scanner);
        boolean encontrado = false;

        System.out.println();
        System.out.println("Tickets con prioridad " + prioridad.getEtiqueta() + ":");
        for (Ticket ticket : tickets) {
            if (ticket.getPrioridad() == prioridad) {
                ticket.mostrarDatos();
                System.out.println(SEPARADOR);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No hay tickets con esa prioridad.");
        }
    }

    private static void mostrarResumen(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
            return;
        }

        System.out.println();
        System.out.println("Resumen por estado:");
        for (Estado estado : Estado.values()) {
            System.out.println("- " + estado.getEtiqueta() + ": " + contarPorEstado(tickets, estado));
        }

        System.out.println();
        System.out.println("Resumen por prioridad:");
        for (Prioridad prioridad : Prioridad.values()) {
            System.out.println("- " + prioridad.getEtiqueta() + ": " + contarPorPrioridad(tickets, prioridad));
        }

        System.out.println("Total de tickets: " + tickets.size());
    }

    private static int contarPorEstado(List<Ticket> tickets, Estado estado) {
        int cantidad = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getEstado() == estado) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private static int contarPorPrioridad(List<Ticket> tickets, Prioridad prioridad) {
        int cantidad = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getPrioridad() == prioridad) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private static int leerOpcionMenu(Scanner scanner) {
        System.out.println();
        System.out.println("Seleccione una opcion:");
        System.out.println("1. Registrar ticket");
        System.out.println("2. Listar tickets");
        System.out.println("3. Filtrar por estado");
        System.out.println("4. Filtrar por prioridad");
        System.out.println("5. Actualizar ticket por id");
        System.out.println("6. Cerrar ticket por id");
        System.out.println("7. Resumen");
        System.out.println("8. Guardar en CSV");
        System.out.println("9. Cargar desde CSV");
        System.out.println("10. Salir (guarda automaticamente)");
        return leerEntero(scanner, "Opcion: ", OPCION_REGISTRAR, OPCION_SALIR);
    }

    private static int leerCampoActualizable(Scanner scanner) {
        System.out.println();
        System.out.println("Que desea actualizar?");
        System.out.println("1. Titulo");
        System.out.println("2. Descripcion");
        System.out.println("3. Solicitante");
        System.out.println("4. Prioridad");
        System.out.println("5. Estado");
        System.out.println("6. Volver al menu principal");
        return leerEntero(scanner, "Opcion: ", CAMPO_TITULO, CAMPO_VOLVER);
    }

    private static int leerTipoTicket(Scanner scanner) {
        System.out.println("Seleccione el tipo de ticket:");
        System.out.println("1. General");
        System.out.println("2. Hardware");
        System.out.println("3. Software");
        System.out.println("4. Red");
        return leerEntero(scanner, "Opcion: ", TIPO_GENERAL, TIPO_RED);
    }

    private static int leerIdDisponible(Scanner scanner, List<Ticket> tickets) {
        while (true) {
            int id = leerEntero(scanner, "Ingrese el id del ticket: ", ID_MINIMO, ID_MAXIMO);
            if (buscarPorId(tickets, id) == null) {
                return id;
            }

            System.out.println("Error: ya existe un ticket con el id " + id + ".");
        }
    }

    private static Ticket buscarPorId(List<Ticket> tickets, int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }

        return null;
    }

    private static Estado leerEstado(Scanner scanner) {
        Estado[] estados = Estado.values();

        System.out.println("Seleccione el estado:");
        for (int i = 0; i < estados.length; i++) {
            System.out.println((i + 1) + ". " + estados[i].getEtiqueta());
        }

        int opcion = leerEntero(scanner, "Opcion: ", 1, estados.length);
        return estados[opcion - 1];
    }

    private static Prioridad leerPrioridad(Scanner scanner) {
        String mensaje = "Ingrese la prioridad (" + Prioridad.getCodigosValidos() + "): ";

        while (true) {
            try {
                return Prioridad.desdeCodigo(leerCaracter(scanner, mensaje));
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Entrada finalizada inesperadamente.");
            }

            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }

            System.out.println("Error: el valor no puede estar vacio.");
        }
    }

    private static int leerEntero(Scanner scanner, String mensaje, int minimo, int maximo) {
        while (true) {
            String valor = leerTexto(scanner, mensaje);

            try {
                int numero = Integer.parseInt(valor);
                if (numero < minimo || numero > maximo) {
                    System.out.println("Error: el numero debe estar entre " + minimo + " y " + maximo + ".");
                    continue;
                }

                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Error: debe ingresar un numero entero valido.");
            }
        }
    }

    private static char leerCaracter(Scanner scanner, String mensaje) {
        while (true) {
            String valor = leerTexto(scanner, mensaje);
            if (valor.length() == 1) {
                return valor.charAt(0);
            }

            System.out.println("Error: debe ingresar un solo caracter.");
        }
    }

    private static boolean leerConfirmacion(Scanner scanner, String mensaje) {
        while (true) {
            char valor = Character.toUpperCase(leerCaracter(scanner, mensaje));
            if (valor == 'S') {
                return true;
            }

            if (valor == 'N') {
                return false;
            }

            System.out.println("Error: debe responder S o N.");
        }
    }
}
