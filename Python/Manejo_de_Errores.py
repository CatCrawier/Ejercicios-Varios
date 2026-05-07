for i in range(1, 4):
    try:
        edad = int(input("Ingrese su edad: "))

        if edad < 12:
            print("Eres un niño.")

        elif edad < 18:
            print("Eres un adolescente.")

        elif edad < 50:
            print("Eres un adulto.")

        elif edad > 110:
            raise ValueError("Edad no válida. Por favor, ingresa una edad realista.")

        else:
            print("Eres un adulto mayor.")
    except ValueError as e:
        print("Error:", e)