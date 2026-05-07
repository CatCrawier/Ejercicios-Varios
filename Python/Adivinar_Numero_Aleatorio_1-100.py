import random
x=random.randint(1,100)
intentos=0
print("Adivina el numero entre 1 y 100")
while True:
    intentos+=1
    n=int(input("Introduce un numero: "))
    if n<x:
        print("El numero es mayor")
    elif n>x:
        print("El numero es menor")
    else:
        print(f"Felicidades, has adivinado el numero {x} en {intentos} intentos")
        print("¿Quieres jugar de nuevo? (s/n)")
        if input().lower()=='s':
            x=random.randint(1,100)
            intentos=0
            print("Adivina el numero entre 1 y 100")
        else:
            print("Gracias por jugar")
            break