const formulario   = document.getElementById("formulario");
const entrada      = document.getElementById("inputTarea");
const lista        = document.getElementById("contenedorLista");
const estadistica  = document.getElementById("estadistica");

let pendientes  = 0;
let completadas = 0;
let contadorId  = 1;

function actualizarEstadistica() {
    estadistica.textContent =
        "Pendientes: " + pendientes + " | Completadas: " + completadas;
}

formulario.addEventListener("submit", function(evento) {
    evento.preventDefault();
    const texto = entrada.value.trim();
    if (texto === "") {
        alert("Debe escribir una tarea");
        return;
    }
    crearTarea(texto);
    entrada.value = "";
});

function crearTarea(texto) {
    contadorId++;

    const articulo = document.createElement("article");
    articulo.classList.add("tarjeta-tarea");
    articulo.setAttribute("id", contadorId);

    const divContenido = document.createElement("div");
    const titulo       = document.createElement("h3");
    titulo.textContent = texto;
    divContenido.appendChild(titulo);

    const divAcciones = document.createElement("div");
    divAcciones.classList.add("acciones-tarea");

    const casilla = document.createElement("input");
    casilla.type  = "checkbox";
    casilla.classList.add("btn-verificar");

    const icono = document.createElement("i");
    icono.classList.add("fa-solid", "fa-trash", "btn-eliminar");

    divAcciones.appendChild(casilla);
    divAcciones.appendChild(icono);
    articulo.appendChild(divContenido);
    articulo.appendChild(divAcciones);
    lista.appendChild(articulo);

    pendientes++;
    actualizarEstadistica();

    casilla.addEventListener("change", function() {
        if (casilla.checked) {
            completadas++;
            pendientes--;
            articulo.classList.add("esta-completada");
        } else {
            completadas--;
            pendientes++;
            articulo.classList.remove("esta-completada");
        }
        actualizarEstadistica();
    });

    icono.addEventListener("click", function() {
        if (casilla.checked) {
            completadas--;
        } else {
            pendientes--;
        }
        lista.removeChild(articulo);
        actualizarEstadistica();
    });
}

actualizarEstadistica();
