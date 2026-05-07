let numero, unidades, decenas, centenas, resto;

numero = prompt("Ingrese un numero de 3 cifras");

centenas = Math.trunc(numero / 100);
resto = numero % 100;
decenas = Math.trunc(resto / 10);
unidades = resto % 10;

document.writeln("Unidades: " + unidades);
document.writeln("<br>");
document.writeln("Decenas: " + decenas);
document.writeln("<br>");
document.writeln("Centenas: " + centenas);