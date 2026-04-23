from tkinter import *
def ventana1():
    print("Hola, has presionado el botón #1")
def ventana2():
    print("Hola, has presionado el botón #2")
def ventana3():
    print("Hola, has presionado el botón #3")
def ventana4():
    print("Hola, has presionado el botón #4")
ventana = Tk()
ventana.title("Ventana de Ejemplo")
ventana.geometry("400x300")
label = Label(ventana, text="Esto es una etiqueta", font=("Arial", 14))
label.pack()
boton1 = Button(ventana, text="Presionar", command=ventana1, bg="black", fg="white", font=("Arial", 14))
boton1.place(x=10, y=50, width=100, height=30)
boton2 = Button(ventana, text="Presionar", command=ventana2, bg="black", fg="white", font=("Arial", 14))
boton2.place(x=115, y=50, width=100, height=30)
boton3 = Button(ventana, text="Presionar", command=ventana3, bg="black", fg="white", font=("Arial", 14))
boton3.place(x=220, y=50, width=100, height=30)
boton4 = Button(ventana, text="Presionar", command=ventana4, bg="black", fg="white", font=("Arial", 14))
boton4.place(x=325, y=50, width=100, height=30)
ventana.mainloop()