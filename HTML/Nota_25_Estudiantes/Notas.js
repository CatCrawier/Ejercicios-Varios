// Notas de 25 estudiantes en Matemáticas, Estadística e Informática
// Autor: Adrian Vasquez Perez

function obtenerNotaValida(materia) {
    while (true) {
        let entrada = prompt(`Nota ${materia}:`);
        let nota = parseFloat(entrada);
        if (isNaN(nota)) {
            alert("Por favor, ingresa un número válido.");
            continue;
        }
        if (nota < 0 || nota > 5) {
            alert("La nota debe estar entre 0 y 5.");
            continue;
        }
        return nota;
    }
}

function notasEstudiantes() {
    let sumaInfo = 0;
    let aprobadosMate = 0;
    let mayorEst = -Infinity;
    let menorInfo = Infinity;

for (let i = 0; i < 25; i++) {
        let numeroEstudiante = i + 1;
        alert(`Estudiante #${numeroEstudiante}`);
        let mate = obtenerNotaValida("Matemáticas");
        let est = obtenerNotaValida("Estadística");
        let info = obtenerNotaValida("Informática");

        sumaInfo += info;

        if (mate >= 3) aprobadosMate++;
        if (est > mayorEst) mayorEst = est;
        if (info < menorInfo) menorInfo = info;
    }

    let promedioInfo = sumaInfo / 25;

    alert("Promedio Informática: " + promedioInfo.toFixed(2));
    alert("Aprobados Matemáticas: " + aprobadosMate);
    alert("Mayor nota Estadística: " + mayorEst);
    alert("Menor nota Informática: " + menorInfo);
}

notasEstudiantes();
