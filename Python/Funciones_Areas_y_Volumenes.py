import math
def area_esfera (r):
    return 4*math.pi*(r**2)
def volumen_esfera (r):
    return (4/3)*math.pi*(r**3)
def area_cono(r, l):
    return math.pi * r * (l + r)
def volumen_cono (r, h):
    return (1/3) * math.pi * (r**2) * h
def area_cubo (a):
    return 6 * (a**2)
def volumen_cubo (a):
    return a**3
def area_cilindro (r, h):
    return 2 * math.pi * r * (r + h)
def volumen_cilindro (r, h):
    return math.pi * (r**2) * h