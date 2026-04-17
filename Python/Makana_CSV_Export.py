import csv
from datetime import datetime

import mysql.connector

DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "makana_pizza",
}

TABLAS = [
    "categorias",
    "clientes",
    "productos",
    "promociones",
    "pedidos",
    "detalle_pedidos",
]


def obtener_datos_tabla(cursor_bd, tabla):
    cursor_bd.execute(f"SELECT * FROM `{tabla}`")
    filas = cursor_bd.fetchall()
    columnas = [desc[0] for desc in cursor_bd.description]
    return filas, columnas


def exportar_csv(archivo_csv, datos):
    with open(archivo_csv, "w", newline="", encoding="utf-8-sig") as archivo:
        writer = csv.writer(archivo, quoting=csv.QUOTE_MINIMAL)
        for tabla, filas, columnas in datos:
            writer.writerow([f"### TABLA: {tabla.upper()} ###"])
            writer.writerow(columnas)
            writer.writerows(filas)
            writer.writerow([])


def principal():
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    archivo_csv = f"makana_pizza_{timestamp}.csv"

    conn = None
    cursor = None

    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("Conexion exitosa a MySQL\n")

        datos_para_csv = []

        print("-" * 40)
        print(f"{'Tabla':<25} {'Filas':>6}  {'CSV':^6}")
        print("-" * 40)

        for tabla in TABLAS:
            try:
                filas, columnas = obtener_datos_tabla(cursor, tabla)
                datos_para_csv.append((tabla, filas, columnas))
                print(f"  {tabla:<23} {len(filas):>6}    OK")
            except mysql.connector.Error as error:
                print(f"  {tabla:<23} ERROR: {error}")

        print("-" * 40)

        exportar_csv(archivo_csv, datos_para_csv)

        print(f"\nCSV generado: {archivo_csv}")
        print("Exportacion completada correctamente.\n")

    except mysql.connector.Error as error:
        print(f"Error de conexion: {error}")

    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None and conn.is_connected():
            conn.close()
            print("Conexion cerrada.")


if __name__ == "__main__":
    principal()
