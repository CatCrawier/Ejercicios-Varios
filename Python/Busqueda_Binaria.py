def busqueda_binaria(lista, objetivo):
    bajo = 0
    alto = len(lista) - 1

    while bajo <= alto:
        medio = (bajo + alto) // 2
        if lista[medio] == objetivo:
            return medio
        elif lista[medio] < objetivo:
            bajo = medio + 1
        else:
            alto = medio - 1

    return -1

numeros = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
posicion = busqueda_binaria(numeros, 7)
print(f"El elemento 7 esta en la posicion {posicion}")