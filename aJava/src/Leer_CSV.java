import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class Leer_CSV {
    public static void main(String[] args) {
        System.out.println("Lectura de archivo CSV");
        
        File archivo = new File("datos.csv");

        try {
            try (Scanner lector = new Scanner(archivo)) {
                while (lector.hasNextLine()) {
                    String linea = lector.nextLine();
                    String[] campos = linea.split(";");
                    
                    System.out.println("Leyendo fila: " + campos[0] + " " + campos[1] + " " + campos[2]);
                }
            }
            System.out.println("Archivo leido exitosamente.");

        } catch (FileNotFoundException e) {
            System.out.println("Error al leer el archivo.");
        }
    }    
}