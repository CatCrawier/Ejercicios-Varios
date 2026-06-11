package hitts_poo;

public class Estudiante extends Persona {
    private String programa;
    private int semestre;

    public Estudiante(String nombre, String apellido, int edad, char sexo, String programa, int semestre) {
        super(nombre, apellido, edad, sexo);
        this.programa = programa;
        this.semestre = semestre;
    }

    public void mostrarDatos() {
        System.out.println("Nombre: " + getNombre());
        System.out.println("Apellido: " + getApellido());
        System.out.println("Edad: " + getEdad());
        System.out.println("Sexo: " + getSexo());
        System.out.println("Programa: " + programa);
        System.out.println("Semestre: " + semestre);
    }
}
