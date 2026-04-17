let w, n, moneda, suma;

suma = 0;

document.getElementById("contenedor").innerHTML = "<h1>Alcancía</h1>";

w = parseInt(prompt("¿Cuántas monedas quieres meter en la alcancía?"));

for (n = 1; n <= w; n++) {
    moneda = parseInt(prompt("Valor de la moneda " + n + " que quieres meter en la alcancía:"));
    suma += moneda;
    document.getElementById("contenedor").innerHTML += "<p>Moneda " + n + ": " + moneda + "</p>";
}

document.getElementById("contenedor").innerHTML += "<p>La suma total de las monedas que has metido en la alcancía es: " + suma + "</p>";