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

todas_las_notas = [nota for estudiante in estudiantes for nota in estudiante["Notas"]]
print(f"Promedio de las notas de los estudiantes: {calcular_promedio(todas_las_notas):.2f}")

def generar_reporte(estudiantes):
    """Genera un reporte de las notas de los estudiantes"""
    print("Reporte de notas de los estudiantes:")
    for estudiante in estudiantes:
        suma = sum(estudiante["Notas"])
        promedio = suma / len(estudiante["Notas"])

        estado = "Aprobado" if promedio >= 3 else "Reprobado"
        print(f"Estudiante: {estudiante['Nombre']} - Promedio: {promedio:.2f} - Estado: {estado}")

generar_reporte(estudiantes)