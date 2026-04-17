edad_cliente = int(input("Ingrese la edad del cliente: "))
ingresos = float(input("\nIngrese los ingresos mensuales del cliente: "))
fiador = input("\n¿El cliente tiene un fiador? (sí/no): ").lower()
reportado = input("\n¿El cliente ha sido reportado por incumplimiento de pagos? (sí/no): ").lower() == "sí"

if fiador == "sí":
    fiador = True

elif fiador == "no":
    fiador = False

else:
    print("Entrada no válida para el fiador. Por favor, ingrese 'sí' o 'no'.")

edad = (edad_cliente >= 18 and edad_cliente <= 75)
ingresos_suficientes = (ingresos >= 2000000 or fiador)
no_reportado = not reportado
credito_aprobado = edad and ingresos_suficientes and no_reportado

mensaje = "Crédito aprobado" if credito_aprobado else "Crédito denegado"

if credito_aprobado:
    print("\n¡Crédito aprobado!")
else:
    print("\nCrédito denegado.")

print("\n¿Edad del cliente es válida?", edad)
print("\n¿Ingresos del cliente son suficientes?", ingresos_suficientes)
print("\n¿El cliente no ha sido reportado?", no_reportado)
print("\n¿Crédito aprobado?", credito_aprobado, mensaje)