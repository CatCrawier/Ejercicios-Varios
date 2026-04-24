estudiantes = [
    {"Nombre": "Juan", "Notas": [3.5, 4.0, 3.8]},
    {"Nombre": "Maria", "Notas": [3.0, 3.5, 3.2]},
    {"Nombre": "Pedro", "Notas": [3.2, 3.8, 3.6]},
]

def calcular_promedio(notas):
    return sum(notas) / len(notas) if notas else 0

def validar_nota(nota):
    if 0.0 <= nota <= 5.0:
        return True
    print("Error: La nota debe estar entre 0.0 y 5.0.")
    return False

def generar_reporte(estudiantes):
    print("\n--- Reporte de notas (Resumen) ---")
    for e in estudiantes:
        promedio = calcular_promedio(e["Notas"])
        estado = "Aprobado" if promedio >= 3 else "Reprobado"
        print(f"Estudiante: {e['Nombre']} | Promedio: {promedio:.2f} | Estado: {estado}")

def listar_todos(estudiantes):
    print("\n--- Listado Detallado de Estudiantes ---")
    for e in estudiantes:
        print(f"Estudiante: {e['Nombre']} | Notas: {e['Notas']}")

def mostrar_menu():
    print("\n--- MENÚ DE GESTIÓN ---")
    print("1. Ver promedio general    | 2. Ver reporte resumido")
    print("3. Listar estudiantes (Detallado)")
    print("4. Agregar estudiante      | 5. Eliminar estudiante")
    print("6. Modificar nota          | 7. Agregar notas adicionales")
    print("8. Eliminar nota específica| 9. Salir")
    return input("Seleccione una opción: ")

def main():
    while True:
        opcion = mostrar_menu()
        
        if opcion == "1":
            todas = [n for e in estudiantes for n in e["Notas"]]
            print(f"\nPromedio general del curso: {calcular_promedio(todas):.2f}")
            
        elif opcion == "2":
            generar_reporte(estudiantes)
            
        elif opcion == "3":
            listar_todos(estudiantes)
            
        elif opcion == "4":
            nombre = input("Nombre del nuevo estudiante: ")
            try:
                entrada = input("Ingrese las notas (ej: 3.5 4.0 3.2): ").split()
                notas = [float(n) for n in entrada]
                if all(validar_nota(n) for n in notas):
                    estudiantes.append({"Nombre": nombre, "Notas": notas})
                    print("Estudiante agregado correctamente.")
                else:
                    print("Operación cancelada: alguna nota no es válida.")
            except ValueError:
                print("Error: Ingrese valores numéricos válidos.")
            
        elif opcion == "5":
            nombre = input("Nombre del estudiante a eliminar: ")
            antes = len(estudiantes)
            estudiantes[:] = [e for e in estudiantes if e["Nombre"] != nombre]
            if len(estudiantes) < antes:
                print(f"Estudiante '{nombre}' eliminado.")
            else:
                print("Estudiante no encontrado.")
            
        elif opcion == "6":
            nombre = input("Nombre del estudiante: ")
            est = next((e for e in estudiantes if e["Nombre"] == nombre), None)
            if est:
                print(f"Notas actuales: {est['Notas']}")
                try:
                    idx = int(input(f"Índice de nota a cambiar (0 a {len(est['Notas'])-1}): "))
                    if 0 <= idx < len(est['Notas']):
                        nueva = float(input("Nueva nota (0.0 - 5.0): "))
                        if validar_nota(nueva):
                            est['Notas'][idx] = nueva
                            print("Nota actualizada.")
                    else:
                        print("Índice fuera de rango.")
                except ValueError:
                    print("Error: Entrada no válida.")
            else:
                print("Estudiante no encontrado.")
                
        elif opcion == "7":
            nombre = input("Nombre del estudiante al que desea agregar notas: ")
            est = next((e for e in estudiantes if e["Nombre"] == nombre), None)
            if est:
                try:
                    nuevas = input("Ingrese las notas a añadir (separadas por espacio): ").split()
                    notas_temp = [float(n) for n in nuevas]
                    if all(validar_nota(n) for n in notas_temp):
                        est['Notas'].extend(notas_temp)
                        print("Notas agregadas correctamente.")
                    else:
                        print("Operación cancelada: alguna nota no es válida.")
                except ValueError:
                    print("Error: Entrada no válida.")
            else:
                print("Estudiante no encontrado.")

        elif opcion == "8":
            nombre = input("Nombre del estudiante: ")
            est = next((e for e in estudiantes if e["Nombre"] == nombre), None)
            if est:
                print(f"Notas actuales: {est['Notas']}")
                try:
                    idx = int(input(f"Índice de la nota a eliminar (0 a {len(est['Notas'])-1}): "))
                    if 0 <= idx < len(est['Notas']):
                        eliminada = est['Notas'].pop(idx)
                        print(f"Nota {eliminada} eliminada con éxito.")
                    else:
                        print("Índice fuera de rango.")
                except ValueError:
                    print("Error: Entrada no válida.")
            else:
                print("Estudiante no encontrado.")
            
        elif opcion == "9":
            print("Saliendo del programa...")
            break
        else:
            print("Opción no válida.")

if __name__ == "__main__":
    main()