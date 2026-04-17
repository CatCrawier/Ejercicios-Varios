print("=== Calculadora Básica ===")

while True:
    print("\nOpciones:")
    print("\n1. Sumar")
    print("2. Restar")
    print("3. Multiplicar")
    print("4. Dividir")
    print("5. Salir")

    opcion = input("\nSelecciona una opción (1-5): ")

    if opcion == "5":
        print("\n¡Hasta luego!")
        break

    if opcion in ["1", "2", "3", "4"]:
        num1 = float(input("\nIngrese el primer número: "))
        num2 = float(input("Ingrese el segundo número: "))

        if opcion == "1":
            resultado = num1 + num2
            print(f"\nResultado: {resultado}")
        elif opcion == "2":
            resultado = num1 - num2
            print(f"\nResultado: {resultado}")
        elif opcion == "3":
            resultado = num1 * num2
            print(f"\nResultado: {resultado}")
        elif opcion == "4":
            if num2 == 0:
                print("Error: División por cero")
            else:
                resultado = num1 / num2
                print(f"\nResultado: {resultado}")
    else:
        print("Opción no válida")