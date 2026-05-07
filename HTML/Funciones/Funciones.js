const titulo = document.createElement("h1");
const subtitulo = document.createElement("h2");

titulo.textContent = "Funciones en JavaScript";
titulo.style.textAlign = "center";

subtitulo.textContent = "Calculadora";
subtitulo.style.textAlign = "center";

const container = document.getElementById("contenedor");

container.appendChild(titulo);
container.appendChild(subtitulo);

container.style.backgroundColor = "gray";

let num1, num2, resultado;

num1 = parseInt(prompt("Ingrese el primer número: "));
num2 = parseInt(prompt("Ingrese el segundo número: "));

function suma() {
    let resultado = num1 + num2;
    container.innerHTML += `<p>La suma de ${num1} + ${num2} es: ${resultado}</p>`;
}

function resta(x, y) {
    resultado = x - y;
    container.innerHTML += `<p>La resta de ${x} - ${y} es: ${resultado}</p>`;
}

function multiplicacion(x, y) {
    resultado = x * y;
    return resultado;
}

function division(x, y) {
    resultado = x / y;
    return resultado;
}

suma();
resta(num1, num2);
container.innerHTML += `<p>La multiplicación de ${num1} * ${num2} es: ${multiplicacion(num1, num2)}</p>`;
container.innerHTML += `<p>La división de ${num1} / ${num2} es: ${division(num1, num2)}</p>`;