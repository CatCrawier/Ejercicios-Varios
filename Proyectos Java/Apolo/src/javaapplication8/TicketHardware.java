package javaapplication8;

public class TicketHardware extends Ticket {

    private final String equipo;
    private final String numeroSerie;
    private final boolean requiereRepuesto;

    public TicketHardware(int id, String titulo, String descripcion, String solicitante, Prioridad prioridad,
            Estado estado, String equipo, String numeroSerie, boolean requiereRepuesto) {
        super(id, titulo, descripcion, solicitante, prioridad, estado);
        this.equipo = validarTexto(equipo, "equipo");
        this.numeroSerie = validarTexto(numeroSerie, "numero de serie");
        this.requiereRepuesto = requiereRepuesto;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public boolean isRequiereRepuesto() {
        return requiereRepuesto;
    }

    @Override
    public String getTipo() {
        return "Hardware";
    }

    @Override
    public String getCodigoTipo() {
        return "HARDWARE";
    }

    @Override
    public String[] getCamposExtra() {
        return new String[]{equipo, numeroSerie, String.valueOf(requiereRepuesto)};
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Equipo: " + equipo);
        System.out.println("Numero de serie: " + numeroSerie);
        System.out.println("Requiere repuesto: " + (requiereRepuesto ? "Si" : "No"));
    }
}
