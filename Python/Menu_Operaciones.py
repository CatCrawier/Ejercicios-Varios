import tkinter as tk
from tkinter import messagebox, simpledialog

ventana = tk.Tk()
ventana.title("Menú con Operaciones")
barra_menu = tk.Menu(ventana)
ventana.config(menu=barra_menu)

def salir():
    respuesta = messagebox.askquestion("Salir", "¿Estás seguro de que deseas salir?")
    if respuesta == 'yes':
        ventana.destroy()

def acerca_de():
    messagebox.showinfo("Acerca de", "Este es un menú de ejemplo con operaciones básicas.")

def restar():
    num1 = simpledialog.askfloat("Restar", "Ingrese el primer número:")
    num2 = simpledialog.askfloat("Restar", "Ingrese el segundo número:")
    resta = num1 - num2
    messagebox.showinfo("Resultado", f"La resta es: {resta}")

def ventana_suma():
    v1 = tk.Toplevel(ventana)
    v1.title("Suma")
    titulo = tk.Label(v1, text="Suma de dos números")
    v1.geometry("250x250")
    def sumar():
        num1 = float(txt1.get())
        num2 = float(txt2.get())
        suma = num1 + num2
        messagebox.showinfo("Resultado", f"La suma es: {suma}")
    txt1 = tk.Entry(v1)
    txt2 = tk.Entry(v1)
    boton1 = tk.Button(v1, text="Sumar", command=sumar)
    titulo.pack()
    txt1.pack()
    txt2.pack()
    boton1.pack()
    
menu_inicio = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Inicio", menu=menu_inicio)
menu_inicio.add_command(label="Salir", command=salir)
menu_operaciones = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Operaciones", menu=menu_operaciones)
menu_operaciones.add_command(label="Suma", command=ventana_suma)
menu_operaciones.add_command(label="Resta", command=restar)
menu_ayuda = tk.Menu(barra_menu, tearoff=0)
barra_menu.add_cascade(label="Ayuda", menu=menu_ayuda)
menu_ayuda.add_command(label="Acerca de", command=acerca_de)
ventana.mainloop()