import random

def crearmatriz(n):
    matriz = []
    for i in range(n):
        fila = []
        for j in range(n):
            x = random.randint(1, 10)
            fila.append(x)
        matriz.append(fila)
    return matriz

def sumamatrices(x, y):
    resultado = []
    for i in range(len(x)):
        for j in range(len(x)):
            resultado = x[i][j] + y[i][j]
    return resultado

def restamatrices(x, y):
    resultado = []
    for i in range(len(x)):
        for j in range(len(x)):
            resultado = x[i][j] - y[i][j]
    return resultado

def productomatrices(x, y):
    resultado = []
    for i in range(len(x)):
        fila = []
        for j in range(len(x)):
            suma = 0
            for k in range(len(x)):
                suma += x[i][k] * y[k][j]
            fila.append(suma)
        resultado.append(fila)
    return resultado

A = crearmatriz(4)
B = crearmatriz(4)
C = sumamatrices(A, B)
D = productomatrices(A, B)
E = restamatrices(A, B)

print("Matriz A:", A)
print("Matriz B:", B)
print("Resultado de la suma:", C)
print("Resultado de la resta:", E)
print("Resultado de la multiplicación:", D)