let suma, numeros;

numeros = 0

document.getElementById("contenedor").innerHTML = "Ver Elementos";

for (let num of numeros) {
    document.getElementById("contenedor").innerHTML += num + "<br>";
}

let i = 0;

do {
    document.getElementById("contenedor").innerHTML += numeros[i] + "<br>";
    i++;
} while (i < numeros.length);