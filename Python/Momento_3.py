import mysql.connector
import csv
from mysql.connector import Error

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '', 
    'database': 'makana_pizza'
}

def conectar_db():
    
    try:
        conn = mysql.connector.connect(**db_config)
        if conn.is_connected():
            return conn
    except Error:
        print("\n[!] Error: No se pudo conectar a la base de datos. Verifique que XAMPP esté activo.")
        return None

def exportar_a_csv(nombre_archivo, cabeceras, datos):
    
    try:
        with open(nombre_archivo, mode='w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(cabeceras)
            writer.writerows(datos)
        print(f"\n[OK] Reporte generado exitosamente: {nombre_archivo}")
    except PermissionError:
        print(f"\n[!] Error: El archivo '{nombre_archivo}' está abierto en otro programa (Excel). Ciérrelo e intente de nuevo.")

def modulo_reportes():
    
    conn = conectar_db()
    if not conn: return
    cursor = conn.cursor()

    print("\n--- SECCIÓN DE REPORTES ---")
    print("1. Reporte General (Todos los Productos)")
    print("2. Reporte Filtrado (Productos por Categoría)")
    print("3. Reporte Estadístico (Total de Pedidos por Estado)")
    print("4. Volver al menú principal")

    try:
        opcion = int(input("Seleccione un reporte: "))
        
        if opcion == 1:
            
            cursor.execute("SELECT producto_id, nombre, precio FROM productos")
            datos = cursor.fetchall()
            exportar_a_csv("reporte_general_productos.csv", ["ID", "Nombre", "Precio"], datos)

        elif opcion == 2:
            
            cursor.execute("SELECT categoria_id, nombre FROM categorias")
            categorias = cursor.fetchall()
            print("\nCategorías disponibles:")
            for cat in categorias: print(f"{cat[0]}. {cat[1]}")
            
            id_cat = input("Ingrese el ID de la categoría a exportar: ")
            cursor.execute("SELECT nombre, precio FROM productos WHERE categoria_id = %s", (id_cat,))
            datos = cursor.fetchall()
            exportar_a_csv(f"productos_categoria_{id_cat}.csv", ["Producto", "Precio"], datos)

        elif opcion == 3:
            
            query = """
                SELECT estado, COUNT(*) as cantidad, SUM(total) as ingresos 
                FROM pedidos 
                GROUP BY estado
            """
            cursor.execute(query)
            datos = cursor.fetchall()
            exportar_a_csv("estadisticas_ventas.csv", ["Estado", "Cantidad Pedidos", "Total Ingresos"], datos)
            
    except ValueError:
        print("[!] Error: Ingrese un número válido para el reporte.")
    finally:
        cursor.close()
        conn.close()

def menu_principal():
    
    while True:
        print("\n===============================")
        print("   MAKANA PIZZA - SISTEMA ADMIN")
        print("===============================")
        print("1. Ver Clientes Registrados")
        print("2. Generar Reportes (Exportar CSV)")
        print("3. Salir")
        
        try:
            
            opcion = int(input("\nSeleccione una opción: "))

            if opcion == 1:
                conn = conectar_db()
                if conn:
                    cursor = conn.cursor()
                    cursor.execute("SELECT nombre, telefono FROM clientes")
                    for (nombre, telefono) in cursor:
                        print(f"Cliente: {nombre} | Tel: {telefono}")
                    cursor.close()
                    conn.close()
            
            elif opcion == 2:
                modulo_reportes()
                
            elif opcion == 3:
                print("Saliendo del sistema... ¡Buen día!")
                break
            else:
                print("[!] Opción no válida. Intente de nuevo.")
                
        except ValueError:
            print("[!] Error de entrada: Por favor, ingrese solo números.")

if __name__ == "__main__":
    menu_principal()