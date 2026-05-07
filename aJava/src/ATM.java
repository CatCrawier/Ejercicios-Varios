void main() {
    try (Scanner scanner = new Scanner(System.in)) {
        double saldo = 1000000;
        boolean ejecutando = true;
        
        System.out.println("Bienvenido al cajero automático");
        
        while (ejecutando) {
            System.out.println("--- MENU PRINCIPAL ---");
            System.out.println("Saldo actual: $" + saldo);
            System.out.println("1. Retirar dinero");
            System.out.println("2. Depositar dinero");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            
            int opcion = scanner.nextInt();
            
            switch (opcion) {
                case 1 -> {
                    System.out.print("Ingrese la cantidad a retirar: $");
                    double retiro = scanner.nextDouble();
                    
                    if (retiro > saldo) {
                        System.out.println("Saldo insuficiente.");
                    } else if (retiro <= 0){
                        System.out.println("La cantidad a retirar debe ser mayor a 0.");
                    } else {
                        saldo -= retiro;
                        System.out.println("Retiro exitoso. Saldo actual: $" + saldo);
                    }
                }
                    
                case 2 -> {
                    System.out.print("Ingrese la cantidad a depositar: $");
                    double deposito = scanner.nextDouble();

                    if (deposito > 0){
                        saldo += deposito;
                        System.out.println("Depósito exitoso. Saldo actual: $" + saldo);
                    } else {
                        System.out.println("La cantidad a depositar debe ser mayor a 0.");
                    }
                }
                    
                case 3 -> {
                    ejecutando = false;
                    System.out.println("Gracias por usar el cajero automático. ¡Hasta luego!");
                }
                    
                default -> System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
            }
        }
    }
}