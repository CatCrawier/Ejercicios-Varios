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

def generar_reporte(estudiantes):
    print("Reporte de notas de los estudiantes:")
    for estudiante in estudiantes:
        suma = sum(estudiante["Notas"])
        promedio = suma / len(estudiante["Notas"])

        estado = "Aprobado" if promedio >= 3 else "Reprobado"
        print(f"Estudiante: {estudiante['Nombre']} - Promedio: {promedio:.2f} - Estado: {estado}")