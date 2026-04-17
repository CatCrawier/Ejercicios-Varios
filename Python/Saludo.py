def saludo():
    nombre = input("¿Cuál es tu nombre? ")
    return f"¡Hola, {nombre}! Bienvenido a Python."

Mayuscula = saludo().upper()
print(Mayuscula)