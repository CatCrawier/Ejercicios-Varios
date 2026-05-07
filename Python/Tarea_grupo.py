import csv

ARCHIVO_CSV = "asistentes_concierto.csv"

def cargar_asistentes():
    asistentes = []
    try:
        with open(ARCHIVO_CSV, mode='r', encoding='utf-8') as archivo:
            lector = csv.reader(archivo)
            next(lector, None)
            for fila in lector:
                if fila:
                    asistentes.append(fila[0])
    except FileNotFoundError:
        pass
    except Exception as e:
        print(f"Error al cargar datos: {e}")
    return asistentes

def guardar_asistentes(asistentes):
    try:
        with open(ARCHIVO_CSV, mode='w', encoding='utf-8', newline='') as archivo:
            escritor = csv.writer(archivo)
            escritor.writerow(["Nombre"])
            for nombre in asistentes:
                escritor.writerow([nombre])
    except Exception as e:
        print(f"Error al guardar datos: {e}")

def gestionar_eventos():
    asistentes = cargar_asistentes()
    CAPACIDAD_MAXIMA = 20
    
    while True:
        print("\n--- SISTEMA DE GESTIÓN CONCIERTOS VIP ---")
        print(f"Estado: {len(asistentes)}/{CAPACIDAD_MAXIMA} ocupados")
        print("1. Registrar asistente")
        print("2. Ver lista de asistentes")
        print("3. Corregir nombre (Editar)")
        print("4. Retirar asistente (Eliminar)")
        print("5. Ver estadísticas")
        print("6. Salir")
        
        opcion = input("Seleccione una opción: ")

        if opcion == "1":
            if len(asistentes) >= CAPACIDAD_MAXIMA:
                print("Cupo lleno. No se pueden registrar más personas.")
            else:
                nuevo_nombre = input("Nombre completo del asistente: ").strip().title()
                
                if nuevo_nombre in asistentes:
                    print(f"{nuevo_nombre} ya se encuentra en la lista.")
                elif nuevo_nombre == "":
                    print("El nombre no puede estar vacío.")
                else:
                    asistentes.append(nuevo_nombre)
                    guardar_asistentes(asistentes)
                    print(f"{nuevo_nombre} ha sido registrado con éxito.")

        elif opcion == "2":
            print("\n--- LISTA DE ASISTENTES ---")
            if not asistentes:
                print("La lista está vacía.")
            else:
                for i, nombre in enumerate(asistentes, 1):
                    print(f"{i}. {nombre}")

        elif opcion == "3":
            try:
                indice = int(input("Ingrese el número (ID) del asistente a corregir: ")) - 1
                nuevo_nombre = input("Ingrese el nombre correcto: ").strip().title()
                asistentes[indice] = nuevo_nombre
                guardar_asistentes(asistentes)
                print("Nombre actualizado correctamente.")
            except (ValueError, IndexError):
                print("ERROR: Número de asistente no válido.")

        elif opcion == "4":
            try:
                indice = int(input("Ingrese el número (ID) del asistente a retirar: ")) - 1
                eliminado = asistentes.pop(indice)
                guardar_asistentes(asistentes)
                print(f"🗑️ {eliminado} ha sido removido de la lista.")
            except (ValueError, IndexError):
                print("ERROR: No se encontró el registro para eliminar.")

        elif opcion == "5":
            ingresados = len(asistentes)
            disponibles = CAPACIDAD_MAXIMA - ingresados
            print(f"\n--- ESTADÍSTICAS ---")
            print(f"Total ingresados: {ingresados}")
            print(f"Cupos disponibles: {disponibles}")
            print(f"Porcentaje de ocupación: {(ingresados/CAPACIDAD_MAXIMA)*100}%")

        elif opcion == "6":
            print("Cerrando sistema... ¡Buen evento!")
            break
        
        else:
            print("Opción no válida, intente de nuevo.")

if __name__=="__main__":
    gestionar_eventos()