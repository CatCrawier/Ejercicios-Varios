let numeros = [], num

num = parseInt(prompt("Digite un número y para terminar digite 0: "))

while (num != 0) {
    numeros.push(num);
    num = parseInt(prompt("Digite un número y para terminar digite 0: "))
}

console.log("Orden ascendente: " + numeros.sort((a, b) => a - b));

console.log("Orden descendente: " + numeros.sort((a, b) => b - a));

const suma = numeros.reduce((acc, num) => acc + num, 0);
console.log("Suma total de los números: " + suma);

numeros.forEach(num => {
    const cuadrado = num * num;
    console.log(num + "^2 = " + cuadrado);
});