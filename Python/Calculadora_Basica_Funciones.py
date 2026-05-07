def operaciones(opcion, num1, num2):
    if opcion == '1':
        resultado = num1 + num2
        return f"{num1} + {num2} es: {resultado}"
    elif opcion == '2':
        resultado = num1 - num2
        return f"{num1} - {num2} es: {resultado}"
    elif opcion == '3':
        resultado = num1 * num2
        return f"{num1} * {num2} es: {resultado}"
    elif opcion == '4':
        if num2 != 0:
            resultado = num1 / num2
            return f"{num1} / {num2} es: {resultado}"
        else:
            return "Error: División por cero no permitida."
    return "Error: Opción no válida."

def menu():
    print("\nSeleccione una operación:")
    print("\n1. Sumar")
    print("2. Restar")
    print("3. Multiplicar")
    print("4. Dividir")
    print("5. Salir")
    return input("\nIngrese el número de la operación que desea realizar: ")

def main():
    print("Bienvenido a la Calculadora Básica")
    while True:
        opcion = menu()
        if opcion == '5':
            print("\nGracias por usar la calculadora. ¡Hasta luego!")
            break
        elif opcion in ['1', '2', '3', '4']:
            try:
                num1 = float(input("\nIngrese el primer número: "))
                num2 = float(input("\nIngrese el segundo número: "))
                resultado_str = operaciones(opcion, num1, num2)
                print(f"\nEl resultado de {resultado_str}")
            except ValueError:
                print("Error: Por favor ingrese un número válido.")
        else:
            print("\nOpción no válida. Por favor seleccione una opción del 1 al 5.")

main()