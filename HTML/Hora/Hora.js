let tiempo, horas, minutos, segundos, partes;

tiempo = prompt("Digite la hora en formato HH:MM:SS");
partes = tiempo.split(":");

horas = parseInt(partes[0], 10);
minutos = parseInt(partes[1], 10);
segundos = parseInt(partes[2], 10);

if (horas === 23 && minutos === 59 && segundos === 59) {
    horas = 0;
    minutos = 0;
    segundos = 0;
} else if (minutos === 59 && segundos === 59) {
    horas += 1;
    minutos = 0;
    segundos = 0;
} else if (segundos === 59) {
    minutos += 1;
    segundos = 0;
} else {
    segundos += 1;
}

document.writeln("La hora un segundo despues es: " + horas + ":" + minutos + ":" + segundos);