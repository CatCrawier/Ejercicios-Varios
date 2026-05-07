import math

class esfera:
    
    def __init__(self, radio):
        self.radio = radio

    def area(self):
        return 4 * math.pi * self.radio ** 2

    def volumen(self):
        return (4/3) * math.pi * self.radio ** 3

class cubo:
    
    def __init__(self, lado):
        self.lado = lado

    def area(self):
        return 6 * self.lado ** 2

    def volumen(self):
        return self.lado ** 3

class cono:
    
    def __init__(self, radio, altura):
        self.radio = radio
        self.altura = altura

    def area(self):
        generatriz = math.sqrt(self.radio ** 2 + self.altura ** 2)
        return math.pi * self.radio * (self.radio + generatriz)

    def volumen(self):
        return (1/3) * math.pi * self.radio ** 2 * self.altura

class cilindro:
    
    def __init__(self, radio, altura):
        self.radio = radio
        self.altura = altura

    def area(self):
        return 2 * math.pi * self.radio * (self.radio + self.altura)

    def volumen(self):
        return math.pi * self.radio ** 2 * self.altura

def menu():
    
    while True:
        print("\nCalculadora de Áreas y Volúmenes")
        print("\nSeleccione una figura para calcular su área y volumen:")
        print("\n1. Esfera")
        print("2. Cubo")
        print("3. Cono")
        print("4. Cilindro")
        print("5. Salir")

        opcion = input("\nIngrese el número de la opción deseada: ")

        if opcion == '1':
            radio = float(input("\nIngrese el radio de la esfera: "))
            figura = esfera(radio)
        elif opcion == '2':
            lado = float(input("\nIngrese el lado del cubo: "))
            figura = cubo(lado)
        elif opcion == '3':
            radio = float(input("\nIngrese el radio del cono: "))
            altura = float(input("\nIngrese la altura del cono: "))
            figura = cono(radio, altura)
        elif opcion == '4':
            radio = float(input("\nIngrese el radio del cilindro: "))
            altura = float(input("\nIngrese la altura del cilindro: "))
            figura = cilindro(radio, altura)
        elif opcion == '5':
            print("\nSaliendo del programa.")
            break
        else:
            print("\nOpción no válida. Intente de nuevo.")
            continue

        print(f"\nÁrea: {figura.area():.2f}")
        print(f"\nVolumen: {figura.volumen():.2f}")

if __name__ == "__main__":
    menu()