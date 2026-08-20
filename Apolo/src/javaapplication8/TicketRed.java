package javaapplication8;

public class TicketRed extends Ticket {

    private static final int USUARIOS_MINIMOS = 1;
    private static final int USUARIOS_MAXIMOS = 5000;

    private final String sede;
    private final String tipoConexion;
    private final int usuariosAfectados;

    public TicketRed(int id, String titulo, String descripcion, String solicitante, Prioridad prioridad,
            Estado estado, String sede, String tipoConexion, int usuariosAfectados) {
        super(id, titulo, descripcion, solicitante, prioridad, estado);
        this.sede = validarTexto(sede, "sede");
        this.tipoConexion = validarTexto(tipoConexion, "tipo de conexion");
        this.usuariosAfectados = validarEntero(usuariosAfectados, "numero de usuarios afectados",
                USUARIOS_MINIMOS, USUARIOS_MAXIMOS);
    }

    public String getSede() {
        return sede;
    }

    public String getTipoConexion() {
        return tipoConexion;
    }

    public int getUsuariosAfectados() {
        return usuariosAfectados;
    }

    @Override
    public String getTipo() {
        return "Red";
    }

    @Override
    public String getCodigoTipo() {
        return "RED";
    }

    @Override
    public String[] getCamposExtra() {
        return new String[]{sede, tipoConexion, String.valueOf(usuariosAfectados)};
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Sede: " + sede);
        System.out.println("Tipo de conexion: " + tipoConexion);
        System.out.println("Usuarios afectados: " + usuariosAfectados);
    }
}
