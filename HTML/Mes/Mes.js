let mes;

mes = parseInt(prompt("Digite un numero del 1 al 12: "));

switch (mes) {
    case 2:
        document.write("El mes tiene 28 dias");
        break;
    case 4:
    case 6:
    case 9:
    case 11:
        document.write("El mes tiene 30 dias");
        break;
    default:
        document.write("El mes tiene 31 dias");
        break;
}