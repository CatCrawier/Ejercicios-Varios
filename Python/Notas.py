estudiantes = [
    {
        "Nombre": "Juan",
        "Notas": [3.5, 4.0, 3.8]
    },
    {
        "Nombre": "Maria",
        "Notas": [3.0, 3.5, 3.2]
    },
    {
        "Nombre": "Pedro",
        "Notas": [3.2, 3.8, 3.6]
    },
]

print(f"Notas de los estudiantes: {len(estudiantes)}")

def calcular_promedio(notas):
    """Calcula el promedio de las notas de un estudiante"""
    if len(notas) == 0:
        return 0
    suma = 0
    for nota in notas:
        suma += nota
    return suma / len(notas)

print(f"Promedio de las notas de los estudiantes: {calcular_promedio(estudiantes)}")