// Calculadora de tarifas de gas
// Autor: Adrian Vasquez Perez

function calcularGas() {
let cuenta;
while (true) {
    cuenta = prompt("Ingrese el numero de cuenta (6 digitos):");
    if (cuenta !== null && /^\d{6}$/.test(cuenta)) {
        break;
    }
    if (cuenta !== null) {
        alert("El número de cuenta debe ser de exactamente 6 dígitos numéricos.");
    } else {
        return;
    }
}
let estrato;
while (true) {
    let input = prompt("Ingrese el estrato (1-6):");
    if (input === null) return;
    estrato = parseInt(input);
    if (!isNaN(estrato) && estrato >= 1 && estrato <= 6) {
        break;
    }
    alert("El estrato debe ser un número entero entre 1 y 6.");
}
let tipo;
while (true) {
    let input = prompt("Ingrese el tipo de servicio (1 para residencial, 2 para comercial, 3 para industrial):");
    if (input === null) return;
    tipo = parseInt(input);
    if (!isNaN(tipo) && (tipo === 1 || tipo === 2 || tipo === 3)) {
        break;
    }
    alert("El tipo debe ser 1 (residencial), 2 (comercial) o 3 (industrial).");
}
let consumo;
while (true) {
    let input = prompt("Ingrese el consumo en metros cúbicos:");
    if (input === null) return;
    consumo = parseFloat(input);
    if (!isNaN(consumo) && consumo >= 0) {
        break;
    }
    alert("El consumo debe ser un número mayor o igual a 0.");
}



    let tarifa;

    if (consumo < 35) tarifa = 545;
    else if (consumo <= 75) tarifa = 600;
    else if (consumo <= 144) tarifa = 750;
    else tarifa = 803;

    let valorConsumo = consumo * tarifa;

    let cargoFijo = (estrato <= 2) ? 15700 : (estrato <= 4) ? 20200 : 34075;

    let subtotal = valorConsumo + cargoFijo;

    let recargo = tipo === 1 ? 0.40 : tipo === 2 ? 0.55 : 0.70;
    let total = subtotal + (subtotal * recargo);

    if (tipo === 1 && consumo <= 30) total *= 0.75;
    else if (tipo === 2 && consumo >= 30 && consumo <= 75) total *= 0.83;
    else if (tipo === 3) total *= 0.93;

    alert("Número de cuenta: " + cuenta + "\nValor a pagar: $" + total.toFixed(2));
}

calcularGas();