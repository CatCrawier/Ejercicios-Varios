nombre = input("Ingrese su nombre: ")

while True:
    try:
        edad = int(input("Ingrese su edad: "))
        if edad > 0:
            break
        else:
            print("La edad debe ser un número positivo.")
    except ValueError:
        print("Error: La edad debe ser un número entero. Intente nuevamente.")

try:
    if edad < 18:
        print(f"Lo siento {nombre}, no tienes acceso al club.")
        raise ValueError("Acceso denegado: Menor de edad")
    
    vip = input("¿Es miembro VIP? (s/n): ").lower()

    if edad >= 18 and vip == 's':
        print(f"Bienvenido {nombre}, puedes acceder al club como miembro VIP.")
    elif edad >= 18 and vip == 'n':
        print(f"Bienvenido {nombre}, puedes acceder al club.")
    else:
        print(f"Lo siento {nombre}, no tienes acceso al club.")
        raise ValueError("Acceso denegado: Condición no reconocida")
except ValueError as e:
    print(f"Error: {e}")
except Exception as e:
    print(f"Ocurrió un error inesperado: {e}")