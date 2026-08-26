package javaapplication8;

public class TicketSoftware extends Ticket {

    private final String aplicacion;
    private final String version;
    private final String mensajeError;

    public TicketSoftware(int id, String titulo, String descripcion, String solicitante, Prioridad prioridad,
            Estado estado, String aplicacion, String version, String mensajeError) {
        super(id, titulo, descripcion, solicitante, prioridad, estado);
        this.aplicacion = validarTexto(aplicacion, "aplicacion");
        this.version = validarTexto(version, "version");
        this.mensajeError = validarTexto(mensajeError, "mensaje de error");
    }

    public String getAplicacion() {
        return aplicacion;
    }

    public String getVersion() {
        return version;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    @Override
    public String getTipo() {
        return "Software";
    }

    @Override
    public String getCodigoTipo() {
        return "SOFTWARE";
    }

    @Override
    public String[] getCamposExtra() {
        return new String[]{aplicacion, version, mensajeError};
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Aplicacion: " + aplicacion);
        System.out.println("Version: " + version);
        System.out.println("Mensaje de error: " + mensajeError);
    }
}
