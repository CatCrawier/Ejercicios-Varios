@SuppressWarnings("ConvertToStringSwitch")
void main() {
    try (Scanner scanner = new Scanner(System.in)) {
        IO.println("¿Esta en el torneo? (si/no): ");
        String torneo = scanner.next().toLowerCase();

        if (torneo.equals("si")) {
            IO.println("Ingrese su edad: ");
            int edad = scanner.nextInt();

            if (edad >= 18 && edad <= 50) {
                IO.println("Ingrese su puntaje: ");
                int puntaje = scanner.nextInt();

                if (puntaje >= 100) {
                    IO.println("Bienvenido al torneo de videojuegos, ¡buena suerte!");
                } else {
                    IO.println("Lo siento, necesitas un puntaje de al menos 100 puntos para participar.");
                }
            } else {
                IO.println("Lo siento, debes tener entre 18 y 50 años para participar.");
            }
        } else if (torneo.equals("no")) {
            IO.println("¡Gracias por tu interés! Esperamos verte en futuros torneos.");
        } else {
            IO.println("Respuesta no válida. Por favor, responde con 'si' o 'no'.");
        }
    } catch (Exception e) {
        IO.println("Entrada no válida. Por favor, ingresa un número válido para la edad y el puntaje.");
    }
}