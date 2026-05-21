def invertir_numero(n):
    invertido = 0
    while n > 0:
        digito = n % 10
        invertido = invertido * 10 + digito
        n = n // 10
    return invertido

n = int(input("Ingrese un número entero positivo: "))
print("El número invertido es:", invertir_numero(n))