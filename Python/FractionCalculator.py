import flet
from flet import Column, Row, Text, TextField, ElevatedButton, Container, alignment, MainAxisAlignment, CrossAxisAlignment, Card
from Fractions_Operations_Class import fraccion
import math

def main(page):
    page.title = "Calculadora de Fracciones"
    page.vertical_alignment = MainAxisAlignment.CENTER
    page.horizontal_alignment = CrossAxisAlignment.CENTER
    page.padding = 30
    page.bgcolor = "#000000"
    def simplificar_fraccion(fracc):
        if fracc.numerador == 0:
            return fraccion(0, 1)

        mcd = math.gcd(abs(fracc.numerador), abs(fracc.denominador))
        numerador_simplificado = fracc.numerador // mcd
        denominador_simplificado = fracc.denominador // mcd
        if denominador_simplificado < 0:
            numerador_simplificado = -numerador_simplificado
            denominador_simplificado = -denominador_simplificado

        return fraccion(numerador_simplificado, denominador_simplificado)
    def fraccion_a_texto(fracc):
        if fracc.denominador == 1:
            return str(fracc.numerador)
        elif abs(fracc.numerador) > abs(fracc.denominador):
            parte_entera = fracc.numerador // fracc.denominador
            residuo = abs(fracc.numerador) % abs(fracc.denominador)
            if residuo == 0:
                return str(parte_entera)
            return f"{parte_entera} {residuo}/{abs(fracc.denominador)}"
        else:
            return f"{fracc.numerador}/{fracc.denominador}"
    def calcular(e):
        try:
            num1 = int(numerador1.value)
            den1 = int(denominador1.value)
            num2 = int(numerador2.value)
            den2 = int(denominador2.value)

            if den1 == 0 or den2 == 0:
                resultado.value = "Error: El denominador no puede ser cero"
                page.update()
                return

            f1 = fraccion(num1, den1)
            f2 = fraccion(num2, den2)

            operacion = operacion_seleccionada.value

            if operacion == "Suma":
                resultado_fraccion = f1.suma(f2)
            elif operacion == "Resta":
                resultado_fraccion = f1.resta(f2)
            elif operacion == "Multiplicación":
                resultado_fraccion = f1.multiplicacion(f2)
            elif operacion == "División":
                resultado_fraccion = f1.division(f2)
            else:
                resultado.value = "Seleccione una operación"
                page.update()
                return
            resultado_simplificado = simplificar_fraccion(resultado_fraccion)
            resultado.value = f"Resultado: {fraccion_a_texto(resultado_simplificado)}"
            resultado_detalle.value = f"Fracción sin simplificar: {resultado_fraccion.numerador}/{resultado_fraccion.denominador}"
            page.update()

        except ValueError:
            resultado.value = "Error: Ingrese números válidos"
            resultado_detalle.value = ""
            page.update()
    titulo = Text("Calculadora de Fracciones", size=30, weight="bold")
    texto1 = Text("Primera Fracción", size=18, weight="bold")
    numerador1 = TextField(label="Numerador", width=150, text_align="center")
    denominador1 = TextField(label="Denominador", width=150, text_align="center")
    texto2 = Text("Segunda Fracción", size=18, weight="bold")
    numerador2 = TextField(label="Numerador", width=150, text_align="center")
    denominador2 = TextField(label="Denominador", width=150, text_align="center")
    texto_operacion = Text("Operación", size=18, weight="bold")
    operacion_seleccionada = TextField(
        label="Operación (Suma, Resta, Multiplicación, División)",
        width=300,
        value="Suma"
    )

    boton_calcular = ElevatedButton(
        text="Calcular",
        on_click=calcular,
        width=200,
        height=50
    )

    resultado = Text("", size=20, weight="bold")
    resultado_detalle = Text("", size=14)

    page.add(
        Column([
            titulo,
            Container(height=20),
            texto1,
            Row([
                numerador1,
                Text("/", size=24),
                denominador1
            ], alignment=MainAxisAlignment.CENTER),
            Container(height=20),
            texto2,
            Row([
                numerador2,
                Text("/", size=24),
                denominador2
            ], alignment=MainAxisAlignment.CENTER),
            Container(height=20),
            texto_operacion,
            operacion_seleccionada,
            Container(height=20),
            boton_calcular,
            Container(height=20),
            resultado,
            resultado_detalle
        ], alignment=MainAxisAlignment.CENTER, horizontal_alignment=CrossAxisAlignment.CENTER)
    )

if __name__ == "__main__":
    flet.app(target=main)