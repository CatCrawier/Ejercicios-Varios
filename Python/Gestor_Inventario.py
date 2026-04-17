inventario = {}

def agregar_producto():
    codigo = input("Código: ").strip()
    if not codigo:
        print("Error: Código vacío.")
        return
    
    if codigo in inventario:
        print("Error: Código ya existe.")
        return
    
    nombre = input("Nombre: ").strip()
    if not nombre:
        print("Error: Nombre vacío.")
        return
    
    while True:
        try:
            cantidad = int(input("Cantidad: "))
            if cantidad < 0:
                print("Cantidad debe ser positiva.")
                continue
            break
        except ValueError:
            print("Cantidad inválida.")
    
    while True:
        try:
            precio = float(input("Precio: "))
            if precio < 0:
                print("Precio debe ser positivo.")
                continue
            break
        except ValueError:
            print("Precio inválido.")
    
    inventario[codigo] = [nombre, cantidad, precio]
    print("✓ Producto agregado.")

def eliminar_producto():
    codigo = input("Código: ").strip()
    if not codigo:
        print("Error: Código vacío.")
        return
    
    if codigo not in inventario:
        print("Producto no encontrado.")
        return
    
    del inventario[codigo]
    print("✓ Producto eliminado.")

def actualizar_producto():
    codigo = input("Código: ").strip()
    if codigo not in inventario:
        print("Producto no encontrado.")
        return
    
    print("Presione Enter para no cambiar:")
    nuevo_nombre = input("Nuevo nombre: ").strip()
    if nuevo_nombre:
        inventario[codigo][0] = nuevo_nombre
    
    while True:
        nueva_cant = input("Nueva cantidad: ").strip()
        if nueva_cant:
            try:
                nueva_cant = int(nueva_cant)
                if nueva_cant >= 0:
                    inventario[codigo][1] = nueva_cant
                    break
                print("Cantidad >= 0")
            except ValueError:
                print("Inválido")
        else:
            break
    
    while True:
        nuevo_precio = input("Nuevo precio: ").strip()
        if nuevo_precio:
            try:
                nuevo_precio = float(nuevo_precio)
                if nuevo_precio >= 0:
                    inventario[codigo][2] = nuevo_precio
                    break
                print("Precio >= 0")
            except ValueError:
                print("Inválido")
        else:
            break
    
    print("✓ Actualizado.")

def buscar_producto():
    termino = input("Buscar: ").strip().lower()
    if not termino:
        print("Término vacío.")
        return
    
    encontrados = []
    for codigo in inventario:
        nombre = inventario[codigo][0].lower()
        if termino in codigo.lower() or termino in nombre:
            encontrados.append(codigo)
    
    if encontrados:
        print(f"\n{len(encontrados)} resultado(s):")
        for codigo in encontrados:
            datos = inventario[codigo]
            print(f"- {codigo}: {datos[0]} (cant: {datos[1]}, ${datos[2]:.2f})")
    else:
        print("No encontrado.")

def listar_inventario():
    if not inventario:
        print("Inventario vacío.")
        return
    
    print("\nINVENTARIO:")
    print("-" * 50)
    print("Código     Nombre              Cant  Precio")
    print("-" * 50)
    for codigo, datos in inventario.items():
        print(f"{codigo:<10} {datos[0][:20]:<20} {datos[1]:<5} ${datos[2]:<7.2f}")
    print("-" * 50)
    print(f"Total: {len(inventario)} productos")

def valor_total():
    total = 0.0
    for datos in inventario.values():
        total += datos[1] * datos[2]
    print(f"Valor total: ${total:.2f}")

def menu_principal():
    while True:
        print("MENU PRINCIPAL")
        print("\n" + "="*30)
        print("1. Agregar")
        print("2. Eliminar")
        print("3. Actualizar") 
        print("4. Buscar")
        print("5. Listar")
        print("6. Total")
        print("7. Salir")
        opcion = input("Opción: ").strip()
        
        if opcion == "1":
            agregar_producto()
        elif opcion == "2":
            eliminar_producto()
        elif opcion == "3":
            actualizar_producto()
        elif opcion == "4":
            buscar_producto()
        elif opcion == "5":
            listar_inventario()
        elif opcion == "6":
            valor_total()
        elif opcion == "7":
            print("¡Adiós!")
            break
        else:
            print("Opción inválida.")

if __name__ == "__main__":
    menu_principal()

