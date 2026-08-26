package hitts_poo;

import java.util.Scanner;

public class HITTS_Poo {

    private static final int OPCION_PERSONA = 1;
    private static final int OPCION_ESTUDIANTE = 2;
    private static final int OPCION_PROFESOR = 3;
    private static final int OPCION_EMPLEADO = 4;
    private static final int SEMESTRE_MINIMO = 1;
    private static final int SEMESTRE_MAXIMO = 12;
    private static final int HORAS_PROFESOR_MINIMAS = 1;
    private static final int HORAS_PROFESOR_MAXIMAS = 60;
    private static final double SALARIO_MINIMO = 0;
    private static final double SALARIO_MAXIMO = 100000000;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int cantidad = leerEntero(scanner, "Cuantas personas desea ingresar? ", 1, 100);

            Persona[] personas = new Persona[cantidad];

            for (int i = 0; i < cantidad; i++) {
                System.out.println();
                System.out.println("Ingrese los datos de la persona " + (i + 1) + ":");
                personas[i] = crearPersona(scanner);
            }

            System.out.println();
            System.out.println("Lista de personas:");
            for (Persona persona : personas) {
                persona.mostrarDatos();
                System.out.println("--------------------------------");
            }
        } catch (IllegalStateException e) {
            System.out.println("No fue posible leer datos desde la consola.");
        }
    }

    private static Persona crearPersona(Scanner scanner) {
        int tipo = leerTipoPersona(scanner);
        String nombre = leerTexto(scanner, "Ingrese el nombre: ");
        String apellido = leerTexto(scanner, "Ingrese el apellido: ");
        int edad = leerEntero(scanner, "Ingrese la edad: ", 0, 130);
        char sexo = leerSexo(scanner, "Ingrese el sexo (M/F): ");

        try {
            if (tipo == OPCION_ESTUDIANTE) {
                String programa = leerTexto(scanner, "Ingrese el programa academico: ");
                int semestre = leerEntero(scanner, "Ingrese el semestre: ", SEMESTRE_MINIMO, SEMESTRE_MAXIMO);

                return new Estudiante(nombre, apellido, edad, sexo, programa, semestre);
            }

            if (tipo == OPCION_PROFESOR) {
                String materia = leerTexto(scanner, "Ingrese la materia que dicta: ");
                int horasSemanales = leerEntero(scanner, "Ingrese las horas semanales: ",
                        HORAS_PROFESOR_MINIMAS, HORAS_PROFESOR_MAXIMAS);

                return new Profesor(nombre, apellido, edad, sexo, materia, horasSemanales);
            }

            if (tipo == OPCION_EMPLEADO) {
                String cargo = leerTexto(scanner, "Ingrese el cargo: ");
                double salario = leerDecimal(scanner, "Ingrese el salario: ", SALARIO_MINIMO, SALARIO_MAXIMO);

                return new Empleado(nombre, apellido, edad, sexo, cargo, salario);
            }

            return new Persona(nombre, apellido, edad, sexo);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Los datos ingresados no pudieron crear el registro.", e);
        }
    }

    private static int leerTipoPersona(Scanner scanner) {
        System.out.println("Seleccione el tipo de registro:");
        System.out.println("1. Persona normal");
        System.out.println("2. Estudiante");
        System.out.println("3. Profesor");
        System.out.println("4. Empleado");
        return leerEntero(scanner, "Opcion: ", OPCION_PERSONA, OPCION_EMPLEADO);
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Entrada finalizada inesperadamente.");
            }

            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }

            System.out.println("Error: el valor no puede estar vacio.");
        }
    }

    private static int leerEntero(Scanner scanner, String mensaje, int minimo, int maximo) {
        while (true) {
            String valor = leerTexto(scanner, mensaje);

            try {
                int numero = Integer.parseInt(valor);
                if (numero < minimo || numero > maximo) {
                    System.out.println("Error: el numero debe estar entre " + minimo + " y " + maximo + ".");
                    continue;
                }

                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Error: debe ingresar un numero entero valido.");
            }
        }
    }

    private static char leerCaracter(Scanner scanner, String mensaje) {
        while (true) {
            String valor = leerTexto(scanner, mensaje);
            if (valor.length() == 1) {
                return valor.charAt(0);
            }

            System.out.println("Error: debe ingresar un solo caracter.");
        }
    }

    private static double leerDecimal(Scanner scanner, String mensaje, double minimo, double maximo) {
        while (true) {
            String valor = leerTexto(scanner, mensaje);

            try {
                double numero = Double.parseDouble(valor);
                if (numero < minimo || numero > maximo) {
                    System.out.println("Error: el numero debe estar entre " + minimo + " y " + maximo + ".");
                    continue;
                }

                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Error: debe ingresar un numero decimal valido.");
            }
        }
    }

    private static char leerSexo(Scanner scanner, String mensaje) {
        while (true) {
            char sexo = Character.toUpperCase(leerCaracter(scanner, mensaje));
            if (sexo == 'M' || sexo == 'F') {
                return sexo;
            }

            System.out.println("Error: el sexo debe ser M o F.");
        }
    }
}
