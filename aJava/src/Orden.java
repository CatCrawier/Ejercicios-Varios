public class Orden {
    public static void main(String[] args) {
        int[] notas = {50, 20, 40, 10, 30};

        System.out.println("Arreglo Original: ");
        imprimirArreglo(notas);

        for (int i = 0; i < notas.length - 1; i++) {
            for (int j = 0; j < notas.length - 1; j++) {
                if (notas[j] > notas[j + 1]) {
                    int temp = notas[j];
                    notas[j] = notas[j + 1];
                    notas[j + 1] = temp;
                }
            }
        }
        System.out.println("\nArreglo Ordenado: ");
        imprimirArreglo(notas);
    }
    public static void imprimirArreglo(int[] arreglo) {
        for (int num : arreglo) {
            System.out.print(num + " ");
        }
    }
}