salario_junior = 2500000
salario_senior = 5000000
bonus_ingles = 500000

print("Oferta de trabajo: Desarrollador Python o Java")

try:
    edad = int(input("¿Cuál es tu edad? "))

    if edad >= 18:
        lenguajes = input("¿Qué lenguajes de programación conoces? (Python/Java) ").lower()
        if lenguajes in ["python", "java"]:
            experiencia = int(input("¿Cuántos años de experiencia tienes? "))
            if experiencia >= 2:
                salario_ofrecido = salario_senior
            else:
                salario_ofrecido = salario_junior

            ingles = input("¿Tienes conocimientos de inglés? (Sí/No) ").lower()
            if ingles == "sí":
                salario_ofrecido += bonus_ingles

            expectativa_salarial = int(input("¿Cuál es tu expectativa salarial? "))
            if expectativa_salarial <= salario_ofrecido:
                print(f"¡Felicidades! Has sido seleccionado para la oferta de trabajo. Salario ofrecido: {salario_ofrecido} COP")
            else:
                print("Lo sentimos, tu expectativa salarial es demasiado alta para esta oferta.")
        else:
            print("Lo sentimos, esta oferta es solo para desarrolladores Python o Java.")
    else:
        print("Lo sentimos, debes ser mayor de edad para aplicar a esta oferta.")
except ValueError:
    print("Por favor, ingresa un valor válido para edad, experiencia y expectativa salarial.")

except Exception as e:
    print(f"Ocurrió un error: {e}")

finally:
    print("Gracias por aplicar a la oferta de trabajo.")