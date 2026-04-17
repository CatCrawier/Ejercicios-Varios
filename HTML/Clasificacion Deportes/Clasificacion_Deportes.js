// Hacer un programa que clasifique 10 personas segun el deporte que practican, los deportes son: Ajedrez, Futbol, Atletismo, Gimnasia y Natacion. Se debe mostrar la cantidad de personas que practican cada deporte.

let ajedrez, futbol, atletismo, gimnasia, natacion, n, deporte;

ajedrez = 0;
futbol = 0;
atletismo = 0;
gimnasia = 0;
natacion = 0;

document.getElementById("contenedor").innerHTML = "<h1>Clasificacion Deportes</h1>";

document.getElementById("contenedor").innerHTML +=
"1. Ajedrez <br>" +
"2. Futbol <br>" +
"3. Atletismo <br>" +
"4. Gimnasia <br>" +
"5. Natacion <br><br>" +
"Digite el numero del deporte que practica (1-5) <br><br>";

for (n = 1; n <= 10; n++) {
let input = prompt("Digite el numero del deporte que practica " + "persona " + n + " (1-5)");
    let deporte = parseInt(input);
    
    while (isNaN(deporte) || input === "" || deporte < 1 || deporte > 5) {
        if (isNaN(deporte) || input === "") {
            input = prompt("Por favor digite un numero entre 1 y 5");
        } else {
            input = prompt("Numero de deporte no valido. Digite el numero del deporte que practica (1-5)");
        }
        deporte = parseInt(input);
    }
    
    switch (deporte) {
        case 1:
            ajedrez++;
            break;
        case 2:
            futbol++;
            break;
        case 3:
            atletismo++;
            break;
        case 4:
            gimnasia++;
            break;
        case 5:
            natacion++;
            break;
        default:
            document.getElementById("contenedor").innerHTML += "Numero de deporte no valido <br>";
    }
}

document.getElementById("contenedor").innerHTML +=
"Cantidad de personas que practican Ajedrez: " + ajedrez + "<br>" +
"Cantidad de personas que practican Futbol: " + futbol + "<br>" +
"Cantidad de personas que practican Atletismo: " + atletismo + "<br>" +
"Cantidad de personas que practican Gimnasia: " + gimnasia + "<br>" +
"Cantidad de personas que practican Natacion: " + natacion + "<br>";