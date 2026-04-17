def fibonacci(n):
    a, b = 0, 1
    for _ in range(n):
        print(a, end=' ')
        a, b = b, a + b
    print()

try:
    n = int(input("¿Hasta que numero de la serie quiere llegar?: "))
    if n <= 0:
        print("El numero debe ser mayor a 0")
    else:
        print(f"Serie de Fibonacci hasta el numero {n}:")
        fibonacci(n)
except ValueError:
    print("Por favor, ingrese un numero entero valido.")