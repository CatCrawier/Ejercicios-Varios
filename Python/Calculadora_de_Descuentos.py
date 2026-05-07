precios = []

for i in range(1, 4):
    precio = float(input(f"Ingrese el precio {i}: "))
    precios.append(precio)

subtotal = sum(precios)

es_estudiante = input("¿Es estudiante? (si/no): ").lower() == "si"

if es_estudiante and subtotal > 50:
    porcentaje_descuento = 0.5/100
elif not es_estudiante and subtotal > 50:
    porcentaje_descuento = 0.1/100
else:
    porcentaje_descuento = 0

descuento = subtotal * porcentaje_descuento
total = subtotal - descuento

print(f"\n--- Resultados ---")
print(f"Subtotal: ${subtotal:.2f}")
print(f"Descuento: ${descuento:.2f}")
print(f"Total: ${total:.2f}")