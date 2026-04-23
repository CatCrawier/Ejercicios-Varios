const cliente = {
    nombre: "",
    saldo: 0,

    ingresar: function (cantidad) {
        this.saldo += cantidad;
        alert(`Se ingresaron ${cantidad}. Nuevo saldo: ${this.saldo}`);
    },

    retirar: function (cantidad) {
        if (cantidad > this.saldo) {
            alert("No puedes retirar más dinero del que tienes.");
        } else {
            this.saldo -= cantidad;
            alert(`Se retiraron ${cantidad}. Nuevo saldo: ${this.saldo}`);
        }
    }
};

cliente.nombre = prompt("Ingresa el nombre del titular:");
cliente.saldo = parseFloat(prompt("Ingresa el saldo inicial:"));

let opcion;

do {
    opcion = prompt(
        `Cliente: ${cliente.nombre}\nSaldo actual: ${cliente.saldo}\n\n` +
        "¿Qué deseas hacer?\n" +
        "1. Ingresar dinero\n" +
        "2. Retirar dinero\n" +
        "3. Salir"
    );

    switch (opcion) {
        case "1":
            let ingreso = parseFloat(prompt("¿Cuánto deseas ingresar?"));
            if (!isNaN(ingreso) && ingreso > 0) {
                cliente.ingresar(ingreso);
            } else {
                alert("Cantidad inválida.");
            }
            break;

        case "2":
            let retiro = parseFloat(prompt("¿Cuánto deseas retirar?"));
            if (!isNaN(retiro) && retiro > 0) {
                cliente.retirar(retiro);
            } else {
                alert("Cantidad inválida.");
            }
            break;

        case "3":
            alert("Gracias por usar el sistema.");
            break;

        default:
            alert("Opción no válida.");
    }

} while (opcion !== "3");