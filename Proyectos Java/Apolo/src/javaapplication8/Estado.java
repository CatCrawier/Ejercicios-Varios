package javaapplication8;

public enum Estado {

    ABIERTO("Abierto"),
    EN_PROCESO("En proceso"),
    CERRADO("Cerrado");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Estado desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }

        String textoNormalizado = texto.trim().toUpperCase().replace(' ', '_');
        for (Estado estado : values()) {
            if (estado.name().equals(textoNormalizado)) {
                return estado;
            }
        }

        throw new IllegalArgumentException("El estado debe ser Abierto, En proceso o Cerrado.");
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
