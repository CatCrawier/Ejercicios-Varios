const titulo = document.createElement("h1");
const subtitulo = document.createElement("h2");

titulo.textContent = "Funciones en JavaScript";
titulo.style.textAlign = "center";

subtitulo.textContent = "Adrian Vasquez Perez";
subtitulo.style.textAlign = "center";

const container = document.getElementById("contenedor");

container.appendChild(titulo);
container.appendChild(subtitulo);

function menu() {
    let opcion = "";

    while (opcion !== "0") {
        opcion = prompt(
            "Seleccione una opción:\n" +
            "1. Opción 1\n" +
            "2. Opción 2\n" +
            "3. Opción 3\n" +
            "4. Opción 4\n" +
            "0. Salir"
        );

        switch (opcion) {
            case "1":
                alert("Has seleccionado la Opción 1");
                break;
            case "2":
                alert("Has seleccionado la Opción 2");
                break;
            case "3":
                alert("Has seleccionado la Opción 3");
                break;
            case "4":
                alert("Has seleccionado la Opción 4");
                break;
            case "0":
                alert("Saliendo del menú");
                break;
            default:
                alert("Opción no válida, por favor intente de nuevo.");
                break;
        }
        container.innerHTML += `<p>Opción seleccionada: ${opcion}</p>`;
    }
}

menu();