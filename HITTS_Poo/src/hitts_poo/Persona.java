package hitts_poo;

public class Persona {

    private static final int EDAD_MINIMA = 0;
    private static final int EDAD_MAXIMA = 130;
    private static final String SEXOS_VALIDOS = "MF";

    private String nombre;
    private String apellido;
    private int edad;
    private char sexo;

    public Persona(String nombre, String apellido, int edad, char sexo) {
        setNombre(nombre);
        setApellido(apellido);
        setEdad(edad);
        setSexo(sexo);
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getEdad() {
        return edad;
    }

    public char getSexo() {
        return sexo;
    }

    public final void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "nombre");
    }

    public final void setApellido(String apellido) {
        this.apellido = validarTexto(apellido, "apellido");
    }

    public final void setEdad(int edad) {
        this.edad = validarEntero(edad, "edad", EDAD_MINIMA, EDAD_MAXIMA);
    }

    public final void setSexo(char sexo) {
        this.sexo = validarSexo(sexo);
    }

    public void mostrarDatos() {
        System.out.println("Nombre: " + nombre);
        System.out.println("Apellido: " + apellido);
        System.out.println("Edad: " + edad);
        System.out.println("Sexo: " + sexo);
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El " + campo + " no puede ser nulo.");
        }

        String valorLimpio = valor.trim();
        if (valorLimpio.isEmpty()) {
            throw new IllegalArgumentException("El " + campo + " no puede estar vacio.");
        }

        return valorLimpio;
    }

    private static int validarEntero(int valor, String campo, int minimo, int maximo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("El " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }
    
    private static char validarSexo(char valor) {
        char valorNormalizado = Character.toUpperCase(valor);
        if (SEXOS_VALIDOS.indexOf(valorNormalizado) == -1) {
            throw new IllegalArgumentException("El sexo debe ser M o F.");
        }
        return valorNormalizado;
    }
}
