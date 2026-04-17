import java.util.Scanner;

public class Calculadora_Basica_Funciones {
    
    public static String operaciones(String opcion, double num1, double num2) {
        switch (opcion) {
            case "1" -> {
                double resultado = num1 + num2;
                return num1 + " + " + num2 + " es: " + resultado;
            }
            case "2" -> {
                double resultado = num1 - num2;
                return num1 + " - " + num2 + " es: " + resultado;
            }
            case "3" -> {
                double resultado = num1 * num2;
                return num1 + " * " + num2 + " es: " + resultado;
            }
            case "4" -> {
                if (num2 != 0) {
                    double resultado = num1 / num2;
                    return num1 + " / " + num2 + " es: " + resultado;
                } else {
                    return "Error: División por cero no permitida.";
                }
            }
            default -> {
            }
        }
        return "Error: Opción no válida.";
    }
    
    public static String menu(Scanner scanner) {
        System.out.println("\nSeleccione una operación:");
        System.out.println("\n1. Sumar");
        System.out.println("2. Restar");
        System.out.println("3. Multiplicar");
        System.out.println("4. Dividir");
        System.out.println("5. Salir");
        System.out.print("\nIngrese el número de la operación que desea realizar: ");
        return scanner.nextLine();
    }
    
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Bienvenido a la Calculadora Básica");
            OUTER:
            while (true) {
                String opcion = menu(scanner);
                switch (opcion) {
                    case "5" -> {
                        System.out.println("\nGracias por usar la calculadora. ¡Hasta luego!");
                        break OUTER;
                    }
                    case "1", "2", "3", "4" -> {
                        try {
                            System.out.print("\nIngrese el primer número: ");
                            double num1 = Double.parseDouble(scanner.nextLine());
                            System.out.print("\nIngrese el segundo número: ");
                            double num2 = Double.parseDouble(scanner.nextLine());
                            String resultadoStr = operaciones(opcion, num1, num2);
                            System.out.println("\nEl resultado de " + resultadoStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Por favor ingrese un número válido.");
                        }
                    }
                    default -> System.out.println("\nOpción no válida. Por favor seleccione una opción del 1 al 5.");
                }
            }
        }
    }
}