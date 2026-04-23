def hanoi(n, origen, auxiliar, destino):
    if n == 1:
        print(f"Mover disco 1 de {origen} a {destino}")
    else:
        hanoi(n-1, origen, destino, auxiliar)
        print(f"Mover disco {n} de {origen} a {destino}")
        hanoi(n-1, auxiliar, origen, destino)

n = int(input("Ingrese el número de discos: "))
hanoi(n, 'A', 'B', 'C')