const estudiante = {
    id: 1,
    nombre: "Adrian",
    apellido: "Vasquez",
    edad: 19,
}

const datosEstudiante = document.getElementById("main");

datosEstudiante.innerHTML += `<p>ID: ${estudiante.id}</p>`;
datosEstudiante.innerHTML += `<p>Nombre: ${estudiante.nombre}</p>`;
datosEstudiante.innerHTML += `<p>Apellido: ${estudiante.apellido}</p>`;
datosEstudiante.innerHTML += `<p>Edad: ${estudiante.edad}</p>`;

datosEstudiante.innerHTML += `<br>`;

for (let i in estudiante) {
    datosEstudiante.innerHTML += `<h3>${i}: ${estudiante[i]}</h3>`;
}

datosEstudiante.innerHTML += `<br>`;

const curso = prompt("Modulo Actual")

for (let i in estudiante) {
    datosEstudiante.innerHTML += `<h3>${i}: ${estudiante[i]}</h3>`;
}