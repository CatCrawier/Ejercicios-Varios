import java.util.Scanner;

void main() {
    try (Scanner scanner = new Scanner(System.in)) {
        boolean acceso = false;

        String contraseña = "Hola";
        
        int intentos = 3;
        
        while(intentos > 0 && !acceso){
            System.out.print("Introduce la contraseña: ");
            String entrada = scanner.nextLine();
            
            if (entrada.equals(contraseña)) {
                acceso = true;
                System.out.println("Acceso concedido.");
            } else {
                intentos--;
                System.out.println("Contraseña incorrecta. Intentos restantes: " + intentos);
            }
        }

        if (!acceso) {
            System.out.println("Acceso bloqueado.");
        }
    }
}