let n, w, dw;

document.getElementById("contenedor").innerHTML = "<h2>For</h2>";

for (n = 1; n <= 10; n++) {
    document.getElementById("contenedor").innerHTML += n + ". Hola<br>";
}

w = 1;

document.getElementById("contenedor").innerHTML += "<h2>While</h2>";

while (w <= 10) {
    document.getElementById("contenedor").innerHTML += w + ". Programando...<br>";
    w++;
}

dw = 1;

document.getElementById("contenedor").innerHTML += "<h2>Do While</h2>";

do {
    document.getElementById("contenedor").innerHTML += dw + ". Do While...<br>";
    dw++;
} while (dw <= 10);