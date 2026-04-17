import csv

inventario = []

try:
    with open("datos.csv", mode="r") as f:
        lector = csv.reader(f, delimiter=";", quotechar='"')

        for fila in lector:
            print(f"Leyendo fila {len(inventario)+1}: {fila[0]} {fila[1]} {fila[2]}")
            inventario.append(fila)
    print(f"Archivo leido exitosamente. \n{len(inventario)} filas leidas.")
except:
    print("Error al leer el archivo.")