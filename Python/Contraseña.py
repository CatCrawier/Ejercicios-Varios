acceso = False
clave = "Hola"
intentos = 3

while(intentos > 0 and not acceso):
    clave = input("Ingrese la contraseña: ")

    if clave == "Hola":
        acceso = True
        print("Acceso concedido")
    else:
        intentos -= 1
        print("Contraseña incorrecta. Intentos restantes:", intentos)

if not acceso:
    print("Acceso denegado. Has agotado todos los intentos.")