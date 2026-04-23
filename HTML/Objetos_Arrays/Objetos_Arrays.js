const datosEstudiante = document.getElementById("main");
const estudiantes = [
    {
        id: 1,
        nombre: "Adrian",
        apellido: "Vasquez",
        edad: 19,
    },
    {
        id: 2,
        nombre: "Juan",
        apellido: "Perez",
        edad: 20,
    },
    {
        id: 3,
        nombre: "Maria",
        apellido: "Gomez",
        edad: 21,
    },
    {
        id: 4,
        nombre: "Pedro",
        apellido: "Gomez",
        edad: 22,
    },
    {
        id: 5,
        nombre: "Maria",
        apellido: "Gomez",
        edad: 23,
    },
];

for (let estudiante of estudiantes) {
    datosEstudiante.innerHTML += `<h3>ID: ${estudiante.id}</h3>`;
    datosEstudiante.innerHTML += `<h3>Nombre: ${estudiante.nombre}</h3>`;
    datosEstudiante.innerHTML += `<h3>Apellido: ${estudiante.apellido}</h3>`;
    datosEstudiante.innerHTML += `<h3>Edad: ${estudiante.edad}</h3>`;
    datosEstudiante.innerHTML += `<br>`;
}