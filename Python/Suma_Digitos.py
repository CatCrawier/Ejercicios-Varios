def suma_digitos(n):
    if n == 0:
        return 0
    else:
        return (n % 10) + suma_digitos(n // 10)

n = int(input("Ingrese un número entero positivo: "))
print("La suma de los dígitos es:", suma_digitos(n))