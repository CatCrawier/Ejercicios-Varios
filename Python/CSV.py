import csv

datos = [{
    "Serie": "SN001",
    "Marca": "Lenovo",
    "Precio": 2000000
}, {
    "Serie": "SN002",
    "Marca": "Dell",
    "Precio": 3000000
}]

with open("datos.csv", mode="w", newline="") as f:
    escritor = csv.writer(f, delimiter=";", quotechar='"')

    escritor.writerow(["Serie", "Marca", "Precio"])

    for d in datos:
        escritor.writerow([d["Serie"], d["Marca"], d["Precio"]])

print("Archivo CSV creado exitosamente.")