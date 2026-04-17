num1 = int(input("Ingrese el número para mostrar su tabla de multiplicar: "))
num2 = int(input("Ingrese hasta qué número desea mostrar la tabla de multiplicar: "))

print(f"Tabla de multiplicar del {num1} hasta el {num2}:")
for i in range(1, num2 + 1):
    resultado = num1 * i
    print(f"{num1} x {i} = {resultado}")