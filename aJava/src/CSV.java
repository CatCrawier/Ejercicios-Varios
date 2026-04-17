import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSV {
    public static void main(String[] args) {
        System.out.println("CSV Writer");

        String[][] datos = {
            {"SN001", "Lenovo", "2000000"},
            {"SN002", "Dell", "3000000"}
        };

        String rutaArchivo = "datos.csv";

        try {
            FileWriter archivo = new FileWriter(rutaArchivo);
            try (PrintWriter escritor = new PrintWriter(archivo)) {
                escritor.println("Serie, Marca, Precio");
                
                for (String[] dato : datos) {
                    escritor.println(dato[0] + "," + dato[1] + "," + dato[2]);
                }
            }
            System.out.println("Archivo creado exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo.");
        }
    }
}