import java.util.Scanner;

public class Conversor_Grados {
    
    public static double fahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32;
    }
    
    public static double kelvin(double celsius) {
        return celsius + 273.15;
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Ingrese los grados Celsius: ");
        try {
            double celsius = scanner.nextDouble();
            
            double fah = fahrenheit(celsius);
            double kel = kelvin(celsius);
            
            System.out.printf("Fahrenheit: %.2f °F%n", fah);
            System.out.printf("Kelvin: %.2f K%n", kel);
        } catch (Exception e) {
            System.out.println("Error: Ingrese un número válido.");
        } finally {
            scanner.close();
        }
    }
}