package hitts_poo;

public class Empleado extends Persona {

    private static final double SALARIO_MINIMO = 0;
    private static final double SALARIO_MAXIMO = 100000000;

    private final String cargo;
    private final double salario;

    public Empleado(String nombre, String apellido, int edad, char sexo, String cargo, double salario) {
        super(nombre, apellido, edad, sexo);
        this.cargo = validarCargo(cargo);
        this.salario = validarSalario(salario);
    }

    public String getCargo() {
        return cargo;
    }

    public double getSalario() {
        return salario;
    }

    @Override
    public void mostrarDatos() {
        super.mostrarDatos();
        System.out.println("Cargo: " + cargo);
        System.out.println("Salario: " + salario);
    }

    private static String validarCargo(String cargo) {
        if (cargo == null) {
            throw new IllegalArgumentException("El cargo no puede ser nulo.");
        }

        String cargoLimpio = cargo.trim();
        if (cargoLimpio.isEmpty()) {
            throw new IllegalArgumentException("El cargo no puede estar vacio.");
        }

        return cargoLimpio;
    }

    private static double validarSalario(double salario) {
        if (salario < SALARIO_MINIMO || salario > SALARIO_MAXIMO) {
            throw new IllegalArgumentException("El salario debe estar entre "
                    + SALARIO_MINIMO + " y " + SALARIO_MAXIMO + ".");
        }

        return salario;
    }
}
