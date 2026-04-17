void main() {
    Scanner scanner = new Scanner(System.in);

    IO.print("Ingrese su edad: ");

    try {
        int edad = scanner.nextInt();

        if (edad < 12) {
            IO.println("Eres un niño");
        } else if (edad < 18) {
            IO.println("Eres un adolescente");
        } else if (edad < 50) {
            IO.println("Eres un adulto");
        } else {
            IO.println("Eres un anciano");
        }
        scanner.close();
    } catch (InputMismatchException e) {
        IO.println("Por favor, ingrese un número válido para la edad.");
    }
}