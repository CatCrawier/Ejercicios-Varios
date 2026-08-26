package hitts_poo;

public class Estudiante extends Persona {

    private static final int SEMESTRE_MINIMO = 1;
    private static final int SEMESTRE_MAXIMO = 12;

    private final String programa;
    private final int semestre;

    public Estudiante(String nombre, String apellido, int edad, char sexo, String programa, int semestre) {
        super(nombre, apellido, edad, sexo);
        this.programa = validarPrograma(programa);
        this.semestre = validarSemestre(semestre);
    }

    public String getPrograma() {
        return programa;
    }

    public int getSemestre() {
        return semestre;
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Programa academico: " + programa);
        System.out.println("Semestre: " + semestre);
    }

    private static String validarPrograma(String programa) {
        if (programa == null) {
            throw new IllegalArgumentException("El programa no puede ser nulo.");
        }

        String programaLimpio = programa.trim();
        if (programaLimpio.isEmpty()) {
            throw new IllegalArgumentException("El programa no puede estar vacio.");
        }

        return programaLimpio;
    }

    private static int validarSemestre(int semestre) {
        if (semestre < SEMESTRE_MINIMO || semestre > SEMESTRE_MAXIMO) {
            throw new IllegalArgumentException("El semestre debe estar entre "
                    + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO + ".");
        }

        return semestre;
    }
}
