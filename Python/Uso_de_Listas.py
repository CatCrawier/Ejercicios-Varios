def mostrar_menu():
    print("\n--- MENÚ DE OPCIONES ---")
    print("1. Añadir número a la lista")
    print("2. Añadir número en una posición")
    print("3. Longitud de la lista")
    print("4. Eliminar el último número")
    print("5. Eliminar un número por posición")
    print("6. Contar apariciones de un número")
    print("7. Posiciones de un número")
    print("8. Mostrar números de la lista")
    print("9. Salir")

def añadir_numero(lista):
    try:
        numero = float(input("Introduce un número: "))
        lista.append(numero)
        print(f"Número {numero} añadido al final de la lista.")
    except ValueError:
        print("Entrada inválida. Debes introducir un número.")

def añadir_en_posicion(lista):
    try:
        numero = float(input("Introduce un número: "))
        posicion = int(input("Introduce la posición (empezando desde 1): "))
        if 1 <= posicion <= len(lista) + 1:
            lista.insert(posicion - 1, numero)
            print(f"Número {numero} añadido en la posición {posicion}.")
        else:
            print("La posición no existe.")
    except ValueError:
        print("Entrada inválida. Debes introducir valores numéricos.")

def longitud_lista(lista):
    print(f"La lista tiene {len(lista)} elementos.")

def eliminar_ultimo(lista):
    if lista:
        eliminado = lista.pop()
        print(f"Se ha eliminado el último número: {eliminado}")
    else:
        print("La lista está vacía.")

def eliminar_por_posicion(lista):
    try:
        posicion = int(input("Introduce la posición a eliminar (empezando desde 1): "))
        if 1 <= posicion <= len(lista):
            eliminado = lista.pop(posicion - 1)
            print(f"Elemento {eliminado} eliminado de la posición {posicion}.")
        else:
            print("La posición no existe.")
    except ValueError:
        print("Entrada inválida. Debes introducir un número entero.")

def contar_numero(lista):
    try:
        numero = float(input("Introduce el número a contar: "))
        apariciones = lista.count(numero)
        print(f"El número {numero} aparece {apariciones} veces en la lista.")
    except ValueError:
        print("Entrada inválida. Debes introducir un número.")

def posiciones_de_numero(lista):
    try:
        numero = float(input("Introduce el número: "))
        posiciones = [i + 1 for i, x in enumerate(lista) if x == numero]
        if posiciones:
            print(f"El número {numero} se encuentra en las posiciones: {posiciones}")
        else:
            print("El número no está en la lista.")
    except ValueError:
        print("Entrada inválida. Debes introducir un número.")

def mostrar_lista(lista):
    if lista:
        print("Lista actual:", lista)
    else:
        print("La lista está vacía.")

def main():
    lista = []
    while True:
        mostrar_menu()
        opcion = input("Selecciona una opción: ")

        if opcion == "1":
            añadir_numero(lista)
        elif opcion == "2":
            añadir_en_posicion(lista)
        elif opcion == "3":
            longitud_lista(lista)
        elif opcion == "4":
            eliminar_ultimo(lista)
        elif opcion == "5":
            eliminar_por_posicion(lista)
        elif opcion == "6":
            contar_numero(lista)
        elif opcion == "7":
            posiciones_de_numero(lista)
        elif opcion == "8":
            mostrar_lista(lista)
        elif opcion == "9":
            print("Saliendo del programa. ¡Hasta luego!")
            break
        else:
            print("Opción no válida. Inténtalo de nuevo.")

if __name__ == "__main__":
    main()