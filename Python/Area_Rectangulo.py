# def calcular_area_rectangulo(base, altura):
#     """
#     Calcular el área de un rectángulo.
#     
#     Args:
#         base: La base del rectángulo
#         altura: La altura del rectángulo
#     
#     Returns:
#         El área del rectángulo
#     """
#     return base * altura
#
# if __name__ == "__main__":
#     base = float(input("Ingrese la base del rectángulo: "))
#     altura = float(input("Ingrese la altura del rectángulo: "))
#     
#     area = calcular_area_rectangulo(base, altura)
#     print(f"El área del rectángulo es: {area}")

def calcular_area_rectangulo():
    base = float(input("Ingrese la base del rectángulo: "))
    altura = float(input("Ingrese la altura del rectángulo: "))
    area = base * altura
    print(f"El área del rectángulo es: {area}")

if __name__ == "__main__":
    calcular_area_rectangulo()