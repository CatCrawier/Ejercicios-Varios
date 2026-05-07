def suma (n):
    if n == 1:
        return 1
    else:
        return n + suma(n-1)

x = int(input("Ingrese un número entero positivo: "))
print(f"La suma de los primeros {x} números enteros es: {suma(x)}")