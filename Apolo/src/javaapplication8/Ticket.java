package javaapplication8;

public class Ticket {

    private static final int ID_MINIMO = 1;
    private static final int ID_MAXIMO = 999999;

    private int id;
    private String titulo;
    private String descripcion;
    private String solicitante;
    private Prioridad prioridad;
    private Estado estado;

    public Ticket(int id, String titulo, String descripcion, String solicitante, Prioridad prioridad, Estado estado) {
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setSolicitante(solicitante);
        setPrioridad(prioridad);
        setEstado(estado);
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    public final void setId(int id) {
        this.id = validarEntero(id, "id", ID_MINIMO, ID_MAXIMO);
    }

    public final void setTitulo(String titulo) {
        this.titulo = validarTexto(titulo, "titulo");
    }

    public final void setDescripcion(String descripcion) {
        this.descripcion = validarTexto(descripcion, "descripcion");
    }

    public final void setSolicitante(String solicitante) {
        this.solicitante = validarTexto(solicitante, "solicitante");
    }

    public final void setPrioridad(Prioridad prioridad) {
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad no puede ser nula.");
        }

        this.prioridad = prioridad;
    }

    public final void setEstado(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }

        this.estado = estado;
    }

    public boolean isCerrado() {
        return estado == Estado.CERRADO;
    }

    public void cerrar() {
        if (isCerrado()) {
            throw new IllegalStateException("El ticket " + id + " ya se encuentra cerrado.");
        }

        this.estado = Estado.CERRADO;
    }

    public String getTipo() {
        return "Ticket general";
    }

    public String getCodigoTipo() {
        return "GENERAL";
    }

    public String[] getCamposExtra() {
        return new String[0];
    }

    public void mostrarDatos() {
        System.out.println("Tipo: " + getTipo());
        System.out.println("Id: " + id);
        System.out.println("Titulo: " + titulo);
        System.out.println("Descripcion: " + descripcion);
        System.out.println("Solicitante: " + solicitante);
        System.out.println("Prioridad: " + prioridad.getEtiqueta());
        System.out.println("Estado: " + estado.getEtiqueta());
    }

    protected static String validarTexto(String valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El " + campo + " no puede ser nulo.");
        }

        String valorLimpio = valor.trim();
        if (valorLimpio.isEmpty()) {
            throw new IllegalArgumentException("El " + campo + " no puede estar vacio.");
        }

        return valorLimpio;
    }

    protected static int validarEntero(int valor, String campo, int minimo, int maximo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("El " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }

        return valor;
    }
}
