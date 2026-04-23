import math
import tkinter as tk
from tkinter import messagebox, simpledialog, PhotoImage

ventana = tk.Tk()
ventana.title("Menú de Áreas y Volúmenes")
barra_menu = tk.Menu(ventana)
ventana.config(menu=barra_menu, bg="black")
ventana.geometry("400x400")
icono = PhotoImage(file='ESCUDO_COLOR.png')
ventana.iconphoto(False, icono)

def salir():
    respuesta = messagebox.askquestion("Salir", "¿Estás seguro de que deseas salir?")
    if respuesta == 'yes':
        ventana.destroy()

def acerca_de():
    messagebox.showinfo("Acerca de", "Este es un menú de ejemplo con operaciones de áreas y volúmenes.")

def area_esfera():
    radio = simpledialog.askfloat("Área de la Esfera", "Ingrese el radio de la esfera:")
    area = 4 * math.pi * radio**2
    messagebox.showinfo("Resultado", f"El área de la esfera es: {area}")

def volumen_esfera():
    radio = simpledialog.askfloat("Volumen de la Esfera", "Ingrese el radio de la esfera:")
    volumen = (4/3) * math.pi * radio**3
    messagebox.showinfo("Resultado", f"El volumen de la esfera es: {volumen}")

def area_cubo():
    lado = simpledialog.askfloat("Área del Cubo", "Ingrese la longitud de un lado del cubo:")
    area = 6 * lado**2
    messagebox.showinfo("Resultado", f"El área del cubo es: {area}")

def volumen_cubo():
    lado = simpledialog.askfloat("Volumen del Cubo", "Ingrese la longitud de un lado del cubo:")
    volumen = lado**3
    messagebox.showinfo("Resultado", f"El volumen del cubo es: {volumen}")

def area_cilindro():
    radio = simpledialog.askfloat("Área del Cilindro", "Ingrese el radio del cilindro:")
    altura = simpledialog.askfloat("Área del Cilindro", "Ingrese la altura del cilindro:")
    area = 2 * math.pi * radio * (radio + altura)
    messagebox.showinfo("Resultado", f"El área del cilindro es: {area}")

def volumen_cilindro():
    radio = simpledialog.askfloat("Volumen del Cilindro", "Ingrese el radio del cilindro:")
    altura = simpledialog.askfloat("Volumen del Cilindro", "Ingrese la altura del cilindro:")
    volumen = math.pi * radio**2 * altura
    messagebox.showinfo("Resultado", f"El volumen del cilindro es: {volumen}")

def area_cono():
    radio = simpledialog.askfloat("Área del Cono", "Ingrese el radio del cono:")
    altura = simpledialog.askfloat("Área del Cono", "Ingrese la altura del cono:")
    generatriz = math.sqrt(radio**2 + altura**2)
    area = math.pi * radio * (radio + generatriz)
    messagebox.showinfo("Resultado", f"El área del cono es: {area}")

def volumen_cono():
    radio = simpledialog.askfloat("Volumen del Cono", "Ingrese el radio del cono:")
    altura = simpledialog.askfloat("Volumen del Cono", "Ingrese la altura del cono:")
    volumen = (1/3) * math.pi * radio**2 * altura
    messagebox.showinfo("Resultado", f"El volumen del cono es: {volumen}")

menu_inicio = tk.Menu(barra_menu, tearoff=0,)
barra_menu.add_cascade(label="Inicio", menu=menu_inicio)
menu_inicio.add_command(label="Salir", command=salir)
menu_areas = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Áreas", menu=menu_areas)
menu_areas.add_command(label="Área de la Esfera", command=area_esfera)
menu_areas.add_command(label="Área del Cubo", command=area_cubo)
menu_areas.add_command(label="Área del Cilindro", command=area_cilindro)
menu_areas.add_command(label="Área del Cono", command=area_cono)
menu_volumenes = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Volúmenes", menu=menu_volumenes)
menu_volumenes.add_command(label="Volumen de la Esfera", command=volumen_esfera)
menu_volumenes.add_command(label="Volumen del Cubo", command=volumen_cubo)
menu_volumenes.add_command(label="Volumen del Cilindro", command=volumen_cilindro)
menu_volumenes.add_command(label="Volumen del Cono", command=volumen_cono)
menu_ayuda = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Ayuda", menu=menu_ayuda)
menu_ayuda.add_command(label="Acerca de", command=acerca_de)
ventana.mainloop()