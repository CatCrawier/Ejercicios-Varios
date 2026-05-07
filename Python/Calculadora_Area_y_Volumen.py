from Funciones_Areas_y_Volumenes import (
    area_esfera, volumen_esfera,
    area_cono, volumen_cono,
    area_cubo, volumen_cubo,
    area_cilindro, volumen_cilindro
)

def menu():
    while True:
        print ("\n--- CALCULADORA DE AREAS Y VOLUMENES ---")
        print ("1. Esfera")
        print ("2. Cono")
        print ("3. Cubo")
        print ("4. Cilindro")
        print ("5. Salir")

        opcion = input("Elige una Figura (1-5): ")

        if opcion == "1":
            r = float(input("Ingresa el Radio de la Esfera: "))
            print("Area:", area_esfera(r))
            print("Volumen:", volumen_esfera(r))
        
        elif opcion == "2":
            r = float(input("Ingresa el Radio del Cono: "))
            h = float(input("Ingresa la Altura del Cono: "))
            l = float(input("Ingresa la Generatriz del Cono: "))
            print("Area:", area_cono(r, l))
            print("Volumen:", volumen_cono(r, h))

        elif opcion == "3":
            a = float(input("Ingresa la Longitud del Lado del Cubo: "))
            print("Area:", area_cubo(a))
            print("Volumen:", volumen_cubo(a))

        elif opcion == "4":
            r = float(input("Ingresa el Radio del Cilindro: "))
            h = float(input("Ingresa la Altura del Cilindro: "))
            print("Area:", area_cilindro(r, h))
            print("Volumen:", volumen_cilindro(r, h))

        elif opcion == "5":
            print("Hasta Luego")
            break

        else:
            print("\nOpcion Invalida, Intenta de Nuevo")

if __name__ == "__main__":
    menu()