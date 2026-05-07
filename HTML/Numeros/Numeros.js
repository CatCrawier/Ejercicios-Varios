let num, par, impar, positivo, negativo, n, cero;

par = 0;
impar = 0;
positivo = 0;
negativo = 0;
cero = 0;

for (n = 1; n <= 10; n++) {
    num = parseInt(prompt("Introduce el número " + n + ":"));
    if (num % 2 == 0) {
        par++;
    } else {
        impar++;
    }
    if (num > 0) {
        positivo++;
    } else if (num < 0) {
        negativo++;
    } else {
        cero++;
    }
}

document.getElementById("contenedor").innerHTML +=
"Números pares: " + par + "<br>" +
"Números impares: " + impar + "<br>" +
"Números positivos: " + positivo + "<br>" +
"Números negativos: " + negativo + "<br>" +
"Números cero: " + cero;