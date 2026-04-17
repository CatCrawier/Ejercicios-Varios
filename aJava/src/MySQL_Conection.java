import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL_Conection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/makana_pizza";
        String user = "root";
        String password = "";
        String nombreArchivo = "export.csv";
        int contador = 0;

        try (
            Connection conexion = DriverManager.getConnection(url, user, password);
            FileWriter fileWriter = new FileWriter(nombreArchivo);
            PrintWriter escritor = new PrintWriter(fileWriter);
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery("SELECT * FROM productos")
        ) {
            escritor.println("producto_id,categoria_id,nombre,descripcion,precio,precio_combo,precio_grande,disponible");

            System.out.println("Conectado a la base de datos MySQL");

            while (resultado.next()) {
                int producto_id = resultado.getInt("producto_id");
                int categoria_id = resultado.getInt("categoria_id");
                String nombre = resultado.getString("nombre");
                String descripcion = resultado.getString("descripcion");
                double precio = resultado.getDouble("precio");
                double precio_combo = resultado.getDouble("precio_combo");
                double precio_grande = resultado.getDouble("precio_grande");
                boolean disponible = resultado.getBoolean("disponible");

                escritor.println(
                    producto_id + "," +
                    categoria_id + "," +
                    nombre + "," +
                    descripcion + "," +
                    precio + "," +
                    precio_combo + "," +
                    precio_grande + "," +
                    disponible
                );

                contador++;
            }

            System.out.println("Datos exportados correctamente a " + nombreArchivo);
            System.out.println("Se exportaron " + contador + " registros.");
        } catch (SQLException e) {
            System.err.println("Error al conectar o consultar la base de datos: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo: " + e.getMessage());
        }
    }
}