try:
    velocidad = float(input("Ingrese la velocidad registrada (km/h): "))

    if velocidad < 0:
        raise ValueError("La velocidad no puede ser negativa.")

    limite = float(input("Ingrese el límite de velocidad (km/h): "))

    if limite <= 0:
        raise ValueError("El límite de velocidad debe ser mayor que 0.")

    tipo_vehiculo = input("Tipo de vehículo (Carro, Moto, Camión): ").strip().lower()

    if tipo_vehiculo not in {"carro", "moto", "camión", "camion"}:
        raise ValueError("El tipo de vehículo debe ser Carro, Moto o Camión.")

    alcoholemia = float(input("Prueba de alcoholemia (0-3): "))

    if not 0 <= alcoholemia <= 3:
        raise ValueError("La prueba de alcoholemia debe estar entre 0 y 3.")

    multa = 0
    mensaje = ""

    if alcoholemia > 0:
        multa = 1000000
        mensaje = "Multa por alcoholemia + Suspensión de licencia"
    elif alcoholemia == 0:
        if velocidad <= limite:
            mensaje = "¡Felicitaciones! Conduce dentro del límite."
        elif velocidad > limite and velocidad < limite + 20:
            multa = 300000
            mensaje = "Multa por exceso de velocidad leve."
        else:
            multa = 600000
            mensaje = "Multa por exceso de velocidad grave."

    if tipo_vehiculo in {"camión", "camion"} and multa > 0:
        multa *= 2
        mensaje += " (Duplicada por ser camión)"

    print("\n--- RESULTADO ---")
    print(mensaje)
    if multa > 0:
        print(f"Monto de la multa: ${multa:,.0f}")
except ValueError as error:
    print(f"\nError de entrada: {error}")
except Exception as error:
    print(f"\nOcurrió un error inesperado: {error}")