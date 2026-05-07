void main() {
    try (Scanner scanner = new Scanner(System.in)) {
        IO.println("Ingrese su edad: ");
        int edad = scanner.nextInt();

        IO.println("\nIngrese su salario mensual: ");
        double ingresos = scanner.nextDouble();

        IO.println("\n¿Tiene un fiador? (1. Sí, 2. No): ");
        boolean fiador = scanner.nextInt() == 1;

        IO.println("\n¿Esta reportado? (1. Sí, 2. No): ");
        boolean reportado = scanner.nextInt() == 1;

        boolean edadValida = (edad >= 18 && edad <= 75);
        boolean ingresosValidos = (ingresos >= 2000000) || fiador;
        boolean historialLimpio = !reportado;
        boolean creditoAprobado = edadValida && ingresosValidos && historialLimpio;

        IO.println("\nResultado: ");
        IO.println("\nEdad válida: " + (edadValida ? "Sí" : "No"));
        IO.println("Ingresos válidos: " + (ingresosValidos ? "Sí" : "No"));
        IO.println("Historial limpio: " + (historialLimpio ? "Sí" : "No"));
        IO.println("Crédito aprobado: " + (creditoAprobado ? "Sí" : "No"));
    }
}