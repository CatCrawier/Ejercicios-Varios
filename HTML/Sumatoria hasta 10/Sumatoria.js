let n, suma;

suma = 0;

for (n = 1; n <= 10; n++) {
    suma += n;
    document.getElementById("contenedor").innerHTML += "Valor Parcial: " + suma + "<br>";
}

document.getElementById("contenedor").innerHTML += "<br>La sumatoria de los números del 1 al 10 es: " + suma;