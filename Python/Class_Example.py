class animal:
    def __init__(self, especie, edad):
        self.especie = especie
        self.edad = edad
    def hablar(self):
        pass
    def moverse(self):
        pass
    def descripcion(self):
        print("Soy un animal del tipo", type(self).__name__)
class perro(animal):
    def hablar(self):
        print("Guau")
    def moverse(self):
        print("Caminando con 4 patas")
class vaca(animal):
    def hablar(self):
        print("Muuuu")
    def moverse(self):
        print("Caminando con 4 patas")
class abeja(animal):
    def hablar(self):
        print("Zzzzz")
    def moverse(self):
        print("Volando con 2 alas")
    def picar(self):
        print("Picando")

mi_perro = perro("Mamífero", 8)
print(mi_perro.especie)
print(mi_perro.edad)
mi_perro.descripcion()
mi_perro.hablar()
mi_perro.moverse()

mi_vaca = vaca("Mamífero", 20)
print(mi_vaca.especie)
print(mi_vaca.edad)
mi_vaca.descripcion()
mi_vaca.hablar()
mi_vaca.moverse()

mi_abeja = abeja("Insecto", 1)
print(mi_abeja.especie)
print(mi_abeja.edad)
print(mi_abeja.picar())
mi_abeja.descripcion()
mi_abeja.hablar()
mi_abeja.moverse()
mi_abeja.picar()