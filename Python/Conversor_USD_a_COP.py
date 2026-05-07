def convertir(monto, tasa):
    return monto * tasa

tasa = None
while tasa is None:
    try:
        tasa = float(input("Ingresa la tasa de cambio (COP por 1 USD): "))
        if tasa <= 0:
            print("La tasa debe ser un número positivo.")
            tasa = None
    except ValueError:
        print("Por favor ingresa un número válido.")

while True:
    try:
        entrada = input("\nIngresa el monto en USD (o 'salir' para terminar): ")
        if entrada.lower() == "salir":
            print("Gracias por usar el conversor de monedas.")
            break
        monto = float(entrada)
        if monto <= 0:
            print("El monto debe ser un número positivo.")
            continue
        resultado = convertir(monto, tasa)
        print(f"{monto} USD = ${resultado} COP")
    except ValueError:
        print("Monto inválido. Intenta de nuevo.")