try:
    cantidad_productos = int(input("¿Cuántos productos desea registrar? "))
    if cantidad_productos <= 0:
        raise ValueError("La cantidad de productos debe ser mayor a 0.")
except ValueError as e:
    print(f"Error: {e}")
    exit()

productos = []
cantidades = []

for i in range(cantidad_productos):
    try:
        nombre = input(f"\nIngrese el nombre del producto {i + 1}: ").strip()
        if not nombre:
            raise ValueError("El nombre del producto no puede estar vacío.")
        stock = int(input(f"Ingrese la cantidad de stock de '{nombre}': "))
        if stock < 0:
            raise ValueError("El stock no puede ser negativo.")
        productos.append(nombre)
        cantidades.append(stock)
    except ValueError as e:
        print(f"Error: {e}")
        exit()

print("\n===== REPORTE DE STOCK =====")
for i in range(len(productos)):
    nombre = productos[i]
    stock = cantidades[i]

    if stock == 0:
        print(f"🔴 Crítico: {nombre} - Solicitar pedido")
    elif stock >= 5:
        if stock > 20:
            print(f"🟢 {nombre} - Stock excedente")
        else:
            print(f"🟡 {nombre} - Stock saludable")
    else:
        print(f"🟠 {nombre} - Stock bajo (entre 1 y 4 unidades)")

total_productos = len(productos)
suma_cantidades = sum(cantidades)
promedio_stock = suma_cantidades / total_productos if total_productos > 0 else 0

print("\n===== RESUMEN FINAL =====")
print(f"Total de productos: {total_productos}")
print(f"Promedio de stock: {promedio_stock:.2f}")