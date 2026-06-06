package hitts_poo;

import java.util.Scanner;

public class HITTS_Poo {

    public static void main(String[] args) {
        Persona[] personas;
        try (Scanner entrada = new Scanner(System.in)) {
            int cantidad = leerEnteroPositivo(entrada, "Cuantas personas desea ingresar? ");
            personas = new Persona[cantidad];
            for (int i = 0; i < personas.length; i++) {
                System.out.println("Ingrese los datos de la persona " + (i + 1));
                String nombre = leerTextoObligatorio(entrada, "Nombre: ");
                String apellido = leerTextoObligatorio(entrada, "Apellido: ");
                int edad = leerEnteroPositivo(entrada, "Edad: ");
                char sexo = leerSexo(entrada, "Sexo (masculino/femenino): ");
                personas[i] = new Persona(nombre, apellido, edad, sexo);
            }
        }

        personas[1].edad = 20;
        personas[0].setEdad(19);

        System.out.println("Datos de las personas: ");
        for (Persona persona : personas) {
            persona.mostrarDatos();
        }
    }

    private static int leerEnteroPositivo(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                int valor = Integer.parseInt(entrada.nextLine());

                if (valor > 0) {
                    return valor;
                }

                System.out.println("Error: ingrese un numero mayor que cero.");
            } catch (NumberFormatException ex) {
                System.out.println("Error: ingrese un numero entero valido.");
            }
        }
    }

    private static String leerTextoObligatorio(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();

            if (!texto.isEmpty()) {
                return texto;
            }

            System.out.println("Error: este campo no puede estar vacio.");
        }
    }

    private static char leerSexo(Scanner entrada, String mensaje) {
        while (true) {
            String sexo = leerTextoObligatorio(entrada, mensaje).toLowerCase();

            if (sexo.equals("masculino") || sexo.equals("m")) {
                return 'M';
            }

            if (sexo.equals("femenino") || sexo.equals("f")) {
                return 'F';
            }

            System.out.println("Error: ingrese solo masculino o femenino.");
        }
    }

}
