def ATM():
    saldo = 1000000
    ejecutando = True

    print("Bienvenido al cajero automático")

    while ejecutando:
        print("--- MENU PRINCIPAL ---")
        print(f"Saldo actual: ${saldo}")
        print("1. Retirar dinero")
        print("2. Depositar dinero")
        print("3. Salir")
        print("Seleccione una opción: ", end="")

        try:
            opcion = int(input())
        except ValueError:
            print("Opción inválida. Por favor, seleccione una opción válida.")
            continue

        match opcion:
            case 1:
                print("Ingrese la cantidad a retirar: $", end="")
                try:
                    retiro = float(input())
                except ValueError:
                    print("La cantidad a retirar debe ser un número.")
                    continue

                if retiro > saldo:
                    print("Saldo insuficiente.")
                elif retiro <= 0:
                    print("La cantidad a retirar debe ser mayor a 0.")
                else:
                    saldo -= retiro
                    print(f"Retiro exitoso. Saldo actual: ${saldo}")

            case 2:
                print("Ingrese la cantidad a depositar: $", end="")
                try:
                    deposito = float(input())
                except ValueError:
                    print("La cantidad a depositar debe ser un número.")
                    continue

                if deposito > 0:
                    saldo += deposito
                    print(f"Depósito exitoso. Saldo actual: ${saldo}")
                else:
                    print("La cantidad a depositar debe ser mayor a 0.")

            case 3:
                ejecutando = False
                print("Gracias por usar el cajero automático. ¡Hasta luego!")

            case _:
                print("Opción inválida. Por favor, seleccione una opción válida.")


if __name__ == "__main__":
    ATM()