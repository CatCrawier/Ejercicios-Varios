let numeros = [], num // Declaración de un arreglo vacío para almacenar los números ingresados por el usuario y una variable para almacenar el número ingresado.

num = parseInt(prompt("Digite un número y para terminar digite 0: ")) // El usuario ingresa un número y se convierte a entero. El ciclo se ejecutará hasta que el usuario ingrese 0

// El ciclo se ejecutará hasta que el usuario ingrese 0

while (num != 0) {
    numeros.push(num);
    num = parseInt(prompt("Digite un número y para terminar digite 0: "))
}

console.log("Orden ascendente: " + numeros.sort((a, b) => a - b)); // Imprimir el arreglo ordenado de menor a mayor

console.log("Orden descendente: " + numeros.sort((a, b) => b - a)); // Imprimir el arreglo ordenado de mayor a menor

// Suma de todos los números
const suma = numeros.reduce((acc, num) => acc + num, 0);
console.log("Suma total de los números: " + suma);

// Cada número elevado al cuadrado
numeros.forEach(num => {
    const cuadrado = num * num;
    console.log(num + "^2 = " + cuadrado);
});