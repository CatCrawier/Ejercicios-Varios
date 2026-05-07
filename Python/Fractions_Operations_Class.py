class fraccion:
    
    def __init__(self, numerador, denominador):
        self.numerador = numerador
        self.denominador = denominador
    
    def print_fraccion(self):
        print(f"Fraccion: {self.numerador}/{self.denominador}")
        
    def suma(self, otra_fraccion):
        nuevo_numerador = (self.numerador * otra_fraccion.denominador) + (otra_fraccion.numerador * self.denominador)
        nuevo_denominador = self.denominador * otra_fraccion.denominador
        return fraccion(nuevo_numerador, nuevo_denominador)
    
    def resta(self, otra_fraccion):
        nuevo_numerador = (self.numerador * otra_fraccion.denominador) - (otra_fraccion.numerador * self.denominador)
        nuevo_denominador = self.denominador * otra_fraccion.denominador
        return fraccion(nuevo_numerador, nuevo_denominador)
    
    def multiplicacion(self, otra_fraccion):
        nuevo_numerador = self.numerador * otra_fraccion.numerador
        nuevo_denominador = self.denominador * otra_fraccion.denominador
        return fraccion(nuevo_numerador, nuevo_denominador)
    
    def division(self, otra_fraccion):
        nuevo_numerador = self.numerador * otra_fraccion.denominador
        nuevo_denominador = self.denominador * otra_fraccion.numerador
        return fraccion(nuevo_numerador, nuevo_denominador)

def main():
    
    fraccion1 = fraccion(1, 2)
    fraccion2 = fraccion(3, 5)
    
    fraccion1.print_fraccion()
    fraccion2.print_fraccion()
    
    resultado_suma = fraccion1.suma(fraccion2)
    resultado_suma.print_fraccion()
    
    resultado_resta = fraccion1.resta(fraccion2)
    resultado_resta.print_fraccion()

    resultado_multiplicacion = fraccion1.multiplicacion(fraccion2)
    resultado_multiplicacion.print_fraccion()

    resultado_division = fraccion1.division(fraccion2)
    resultado_division.print_fraccion()

if __name__ == "__main__":
    main()