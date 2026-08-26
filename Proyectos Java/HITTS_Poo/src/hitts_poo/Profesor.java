package hitts_poo;

public class Profesor extends Persona {

    private static final int HORAS_MINIMAS = 1;
    private static final int HORAS_MAXIMAS = 60;

    private final String materia;
    private final int horasSemanales;

    public Profesor(String nombre, String apellido, int edad, char sexo, String materia, int horasSemanales) {
        super(nombre, apellido, edad, sexo);
        this.materia = validarTexto(materia, "materia");
        this.horasSemanales = validarHorasSemanales(horasSemanales);
    }

    public String getMateria() {
        return materia;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Materia: " + materia);
        System.out.println("Horas semanales: " + horasSemanales);
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("La " + campo + " no puede ser nula.");
        }

        String valorLimpio = valor.trim();
        if (valorLimpio.isEmpty()) {
            throw new IllegalArgumentException("La " + campo + " no puede estar vacia.");
        }

        return valorLimpio;
    }

    private static int validarHorasSemanales(int horasSemanales) {
        if (horasSemanales < HORAS_MINIMAS || horasSemanales > HORAS_MAXIMAS) {
            throw new IllegalArgumentException("Las horas semanales deben estar entre "
                    + HORAS_MINIMAS + " y " + HORAS_MAXIMAS + ".");
        }

        return horasSemanales;
    }
}
