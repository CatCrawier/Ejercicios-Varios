try:
    torneo = input("¿Esta en el torneo? (si/no): ").lower()
    if torneo == "si":
        edad = int(input("Ingrese su edad: "))
        if edad >= 18 and edad <= 50:
            puntaje = int(input("Ingrese su puntaje: "))
            if puntaje >= 100:
                print("Bienvenido al torneo de videojuegos, ¡buena suerte!")
            elif puntaje < 100:
                print("Lo siento, necesitas un puntaje de al menos 100 puntos para participar.")
        else:
            print("Lo siento, debes tener entre 18 y 50 años para participar.")
    elif torneo == "no":
        print("¡Gracias por tu interés! Esperamos verte en futuros torneos.")
    else:
        print("Respuesta no válida. Por favor, responde con 'si' o 'no'.")
except ValueError:
    print("Entrada no válida. Por favor, ingresa un número válido para la edad y el puntaje.")
except Exception as e:
    print("Ocurrió un error:", e)