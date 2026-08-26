package javaapplication8;

public enum Prioridad {

    ALTA('A', "Alta"),
    MEDIA('M', "Media"),
    BAJA('B', "Baja");

    private final char codigo;
    private final String etiqueta;

    Prioridad(char codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public char getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Prioridad desdeCodigo(char codigo) {
        char codigoNormalizado = Character.toUpperCase(codigo);
        for (Prioridad prioridad : values()) {
            if (prioridad.codigo == codigoNormalizado) {
                return prioridad;
            }
        }

        throw new IllegalArgumentException("La prioridad debe ser A, M o B.");
    }

    public static String getCodigosValidos() {
        StringBuilder codigos = new StringBuilder();
        for (Prioridad prioridad : values()) {
            if (codigos.length() > 0) {
                codigos.append('/');
            }
            codigos.append(prioridad.codigo);
        }

        return codigos.toString();
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
