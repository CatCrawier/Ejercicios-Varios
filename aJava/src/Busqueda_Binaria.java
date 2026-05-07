public class Busqueda_Binaria {

    static int busquedaBinaria(int[] lista, int objetivo) {
        int bajo = 0;
        int alto = lista.length - 1;

        while (bajo <= alto) {
            int medio = (bajo + alto) / 2;
            if (lista[medio] == objetivo) {
                return medio;
            } else if (lista[medio] < objetivo) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }

        return -1;
    }

    public static void main(String[] args) {
        int[] numeros = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int posicion = busquedaBinaria(numeros, 7);
        System.out.println("El elemento 7 esta en la posicion " + posicion);
    }
}