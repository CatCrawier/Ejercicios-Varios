import sqlite3

DB = "tienda.db"

ESQUEMA = """
CREATE TABLE IF NOT EXISTS categorias (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS productos (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT NOT NULL,
    precio       REAL NOT NULL CHECK (precio >= 0),
    stock        INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    categoria_id INTEGER NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS clientes (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email  TEXT NOT NULL UNIQUE,
    ciudad TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS pedidos (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id  INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad    INTEGER NOT NULL CHECK (cantidad > 0),
    fecha       TEXT NOT NULL,
    FOREIGN KEY (cliente_id)  REFERENCES clientes(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);
"""


def conectar():
    con = sqlite3.connect(DB)
    con.execute("PRAGMA foreign_keys = ON")  # SQLite las trae apagadas por defecto
    con.row_factory = sqlite3.Row
    return con

def cargar_datos(con):
    if con.execute("SELECT COUNT(*) FROM categorias").fetchone()[0] > 0:
        return  # ya hay datos

    categorias = [
        ("Electrónica", "Dispositivos y gadgets"), ("Hogar", "Artículos para la casa"),
        ("Ropa", "Prendas de vestir"), ("Deportes", "Equipo deportivo"),
        ("Juguetes", "Juegos para niños"), ("Libros", "Libros y revistas"),
        ("Cocina", "Utensilios de cocina"), ("Oficina", "Papelería y mobiliario"),
        ("Belleza", "Cuidado personal"), ("Mascotas", "Productos para mascotas"),
        ("Jardín", "Herramientas de jardinería"), ("Automotriz", "Accesorios para autos"),
        ("Música", "Instrumentos y accesorios"), ("Videojuegos", "Consolas y juegos"),
        ("Calzado", "Zapatos y zapatillas"), ("Joyería", "Accesorios y joyas"),
        ("Salud", "Bienestar y salud"), ("Herramientas", "Herramientas manuales"),
        ("Bebés", "Artículos para bebés"), ("Viajes", "Maletas y accesorios"),
    ]
    con.executemany("INSERT INTO categorias(nombre, descripcion) VALUES (?,?)", categorias)

    productos = [
        ("Audífonos Bluetooth", 59.90, 40, 1), ("Smartphone X10", 499.00, 15, 1),
        ("Lámpara LED", 24.50, 60, 2), ("Juego de sábanas", 39.99, 30, 2),
        ("Camiseta algodón", 15.00, 100, 3), ("Chaqueta impermeable", 79.90, 25, 3),
        ("Balón de fútbol", 29.90, 50, 4), ("Pesas 10kg", 45.00, 20, 4),
        ("Rompecabezas 500pz", 18.00, 35, 5), ("Muñeca articulada", 22.00, 45, 5),
        ("Novela de misterio", 14.50, 70, 6), ("Atlas mundial", 32.00, 12, 6),
        ("Sartén antiadherente", 27.90, 38, 7), ("Juego de cuchillos", 54.00, 18, 7),
        ("Silla ergonómica", 189.00, 10, 8), ("Resma de papel", 6.50, 200, 8),
        ("Crema hidratante", 12.90, 80, 9), ("Alimento para perro 10kg", 35.00, 42, 10),
        ("Manguera 20m", 21.00, 28, 11), ("Cargador para auto", 11.50, 90, 12),
        ("Guitarra acústica", 150.00, 8, 13), ("Consola GameBox", 399.00, 14, 14),
        ("Zapatillas running", 85.00, 33, 15), ("Collar de plata", 65.00, 16, 16),
        ("Vitaminas C", 9.90, 120, 17), ("Taladro eléctrico", 72.00, 19, 18),
        ("Coche de bebé", 210.00, 7, 19), ("Maleta de viaje", 95.00, 22, 20),
    ]
    con.executemany(
        "INSERT INTO productos(nombre, precio, stock, categoria_id) VALUES (?,?,?,?)", productos)

    clientes = [
        ("Ana Torres", "ana@mail.com", "Bogotá"), ("Luis Pérez", "luis@mail.com", "Medellín"),
        ("María Gómez", "maria@mail.com", "Cali"), ("Carlos Ruiz", "carlos@mail.com", "Bogotá"),
        ("Laura Díaz", "laura@mail.com", "Barranquilla"), ("Jorge Silva", "jorge@mail.com", "Cali"),
        ("Sofía Vargas", "sofia@mail.com", "Bogotá"), ("Andrés Mora", "andres@mail.com", "Medellín"),
        ("Valentina Rojas", "vale@mail.com", "Cartagena"), ("Diego Castro", "diego@mail.com", "Bogotá"),
        ("Camila Ortiz", "camila@mail.com", "Cali"), ("Felipe Núñez", "felipe@mail.com", "Bucaramanga"),
        ("Paula Herrera", "paula@mail.com", "Medellín"), ("Juan Molina", "juan@mail.com", "Bogotá"),
        ("Daniela Ríos", "daniela@mail.com", "Pereira"), ("Sergio Peña", "sergio@mail.com", "Cali"),
        ("Natalia Cruz", "natalia@mail.com", "Bogotá"), ("Mateo Salazar", "mateo@mail.com", "Medellín"),
        ("Isabela León", "isabela@mail.com", "Barranquilla"), ("Tomás Acosta", "tomas@mail.com", "Bogotá"),
    ]
    con.executemany("INSERT INTO clientes(nombre, email, ciudad) VALUES (?,?,?)", clientes)

    pedidos = [
        (1, 1, 2, "2026-01-05"), (2, 5, 3, "2026-01-12"), (3, 2, 1, "2026-01-20"),
        (4, 7, 2, "2026-02-02"), (5, 11, 4, "2026-02-14"), (6, 15, 1, "2026-02-18"),
        (7, 3, 2, "2026-03-01"), (8, 22, 1, "2026-03-09"), (9, 17, 5, "2026-03-15"),
        (10, 9, 2, "2026-03-22"), (11, 23, 1, "2026-04-03"), (12, 13, 1, "2026-04-11"),
        (13, 18, 2, "2026-04-19"), (14, 21, 1, "2026-05-01"), (15, 25, 6, "2026-05-10"),
        (16, 26, 1, "2026-05-17"), (17, 4, 2, "2026-06-02"), (18, 2, 1, "2026-06-12"),
        (19, 10, 3, "2026-06-25"), (19, 6, 1, "2026-07-04"), (1, 12, 1, "2026-07-13"),
        (4, 1, 1, "2026-07-21"), (7, 14, 2, "2026-08-01"), (10, 2, 1, "2026-08-09"),
        (3, 8, 2, "2026-08-16"), (12, 16, 10, "2026-08-24"), (15, 20, 3, "2026-09-02"),
        (1, 24, 1, "2026-09-10"), (5, 27, 1, "2026-09-18"), (9, 19, 2, "2026-09-25"),
    ]
    con.executemany(
        "INSERT INTO pedidos(cliente_id, producto_id, cantidad, fecha) VALUES (?,?,?,?)", pedidos)
    con.commit()


TABLAS = {
    "categorias": ["nombre", "descripcion"],
    "productos":  ["nombre", "precio", "stock", "categoria_id"],
    "clientes":   ["nombre", "email", "ciudad"],
    "pedidos":    ["cliente_id", "producto_id", "cantidad", "fecha"],
}


def crear(con, tabla, valores):
    """CREATE: inserta un registro. valores = dict {columna: valor}"""
    cols = TABLAS[tabla]
    sql = f"INSERT INTO {tabla}({', '.join(cols)}) VALUES ({', '.join('?' * len(cols))})"
    cur = con.execute(sql, [valores[c] for c in cols])
    con.commit()
    return cur.lastrowid


def leer(con, tabla, id_=None):
    """READ: devuelve todos los registros o uno solo si se da el id."""
    if id_ is None:
        return con.execute(f"SELECT * FROM {tabla}").fetchall()
    return con.execute(f"SELECT * FROM {tabla} WHERE id = ?", (id_,)).fetchall()


def actualizar(con, tabla, id_, valores):
    """UPDATE: modifica solo las columnas incluidas en valores."""
    cols = [c for c in valores if c in TABLAS[tabla]]
    if not cols:
        return 0
    sql = f"UPDATE {tabla} SET {', '.join(c + ' = ?' for c in cols)} WHERE id = ?"
    cur = con.execute(sql, [valores[c] for c in cols] + [id_])
    con.commit()
    return cur.rowcount


def eliminar(con, tabla, id_):
    """DELETE: borra un registro por id."""
    cur = con.execute(f"DELETE FROM {tabla} WHERE id = ?", (id_,))
    con.commit()
    return cur.rowcount


CONSULTAS = [
    # ---------- SIN JOIN ----------
    ("Todos los productos ordenados por nombre",
     "SELECT * FROM productos ORDER BY nombre"),
    ("Productos con precio mayor a 50",
     "SELECT nombre, precio FROM productos WHERE precio > 50 ORDER BY precio DESC"),
    ("Productos con stock bajo (menos de 20)",
     "SELECT nombre, stock FROM productos WHERE stock < 20 ORDER BY stock"),
    ("Clientes de Bogotá",
     "SELECT nombre, email FROM clientes WHERE ciudad = 'Bogotá'"),
    ("Cantidad de clientes por ciudad",
     "SELECT ciudad, COUNT(*) AS total FROM clientes GROUP BY ciudad ORDER BY total DESC"),
    ("Precio promedio, mínimo y máximo de los productos",
     "SELECT ROUND(AVG(precio),2) AS promedio, MIN(precio) AS minimo, MAX(precio) AS maximo FROM productos"),
    ("Valor total del inventario",
     "SELECT ROUND(SUM(precio * stock),2) AS valor_inventario FROM productos"),
    ("Pedidos realizados en el mes de marzo de 2026",
     "SELECT * FROM pedidos WHERE fecha BETWEEN '2026-03-01' AND '2026-03-31'"),
    ("Productos cuyo nombre contiene 'a' y cuestan menos de 20",
     "SELECT nombre, precio FROM productos WHERE nombre LIKE '%a%' AND precio < 20"),
    ("Los 5 productos más caros",
     "SELECT nombre, precio FROM productos ORDER BY precio DESC LIMIT 5"),

    # ---------- CON JOIN ----------
    ("Productos con el nombre de su categoría",
     """SELECT p.nombre AS producto, c.nombre AS categoria, p.precio
        FROM productos p
        INNER JOIN categorias c ON p.categoria_id = c.id
        ORDER BY c.nombre"""),
    ("Pedidos con nombre del cliente y del producto",
     """SELECT pe.id, cl.nombre AS cliente, pr.nombre AS producto, pe.cantidad, pe.fecha
        FROM pedidos pe
        JOIN clientes cl ON pe.cliente_id = cl.id
        JOIN productos pr ON pe.producto_id = pr.id
        ORDER BY pe.fecha"""),
    ("Total de cada pedido (cantidad x precio)",
     """SELECT pe.id, cl.nombre AS cliente, ROUND(pe.cantidad * pr.precio, 2) AS total
        FROM pedidos pe
        JOIN clientes cl ON pe.cliente_id = cl.id
        JOIN productos pr ON pe.producto_id = pr.id
        ORDER BY total DESC"""),
    ("Total gastado por cada cliente",
     """SELECT cl.nombre, ROUND(SUM(pe.cantidad * pr.precio), 2) AS total_gastado
        FROM clientes cl
        JOIN pedidos pe ON pe.cliente_id = cl.id
        JOIN productos pr ON pe.producto_id = pr.id
        GROUP BY cl.id
        ORDER BY total_gastado DESC"""),
    ("Clientes que NO han hecho ningún pedido (LEFT JOIN)",
     """SELECT cl.nombre, cl.email
        FROM clientes cl
        LEFT JOIN pedidos pe ON pe.cliente_id = cl.id
        WHERE pe.id IS NULL"""),
    ("Productos que nunca se han pedido (LEFT JOIN)",
     """SELECT pr.nombre, pr.stock
        FROM productos pr
        LEFT JOIN pedidos pe ON pe.producto_id = pr.id
        WHERE pe.id IS NULL"""),
    ("Cantidad de productos por categoría (incluye categorías vacías)",
     """SELECT c.nombre AS categoria, COUNT(p.id) AS num_productos
        FROM categorias c
        LEFT JOIN productos p ON p.categoria_id = c.id
        GROUP BY c.id
        ORDER BY num_productos DESC, c.nombre"""),
    ("Ventas totales por categoría",
     """SELECT c.nombre AS categoria, SUM(pe.cantidad) AS unidades_vendidas,
               ROUND(SUM(pe.cantidad * pr.precio), 2) AS ingresos
        FROM pedidos pe
        JOIN productos pr ON pe.producto_id = pr.id
        JOIN categorias c ON pr.categoria_id = c.id
        GROUP BY c.id
        ORDER BY ingresos DESC"""),
    ("Ciudades con más ingresos",
     """SELECT cl.ciudad, ROUND(SUM(pe.cantidad * pr.precio), 2) AS ingresos
        FROM pedidos pe
        JOIN clientes cl ON pe.cliente_id = cl.id
        JOIN productos pr ON pe.producto_id = pr.id
        GROUP BY cl.ciudad
        ORDER BY ingresos DESC"""),
    ("Clientes que han comprado productos de la categoría Electrónica",
     """SELECT DISTINCT cl.nombre, pr.nombre AS producto
        FROM pedidos pe
        JOIN clientes cl ON pe.cliente_id = cl.id
        JOIN productos pr ON pe.producto_id = pr.id
        JOIN categorias c ON pr.categoria_id = c.id
        WHERE c.nombre = 'Electrónica'"""),
]


def ejecutar_consulta(con, numero):
    desc, sql = CONSULTAS[numero - 1]
    filas = con.execute(sql).fetchall()
    print(f"\n[{numero}] {desc}")
    print("-" * 60)
    if not filas:
        print("(sin resultados)")
        return
    print(" | ".join(filas[0].keys()))
    for f in filas:
        print(" | ".join(str(v) for v in tuple(f)))
    print(f"({len(filas)} filas)")


def elegir_tabla():
    nombres = list(TABLAS)
    for i, t in enumerate(nombres, 1):
        print(f"  {i}. {t}")
    try:
        return nombres[int(input("Tabla: ")) - 1]
    except (ValueError, IndexError):
        print("Opción no válida.")
        return None


def pedir_valores(tabla, actuales=None):
    valores = {}
    for col in TABLAS[tabla]:
        pista = f" [{actuales[col]}]" if actuales else ""
        txt = input(f"  {col}{pista}: ").strip()
        if txt == "" and actuales:
            continue  # al actualizar, Enter deja el valor igual
        valores[col] = txt
    return valores


def mostrar(filas):
    if not filas:
        print("(sin registros)")
        return
    print(" | ".join(filas[0].keys()))
    for f in filas:
        print(" | ".join(str(v) for v in tuple(f)))


def menu():
    con = conectar()
    con.executescript(ESQUEMA)
    cargar_datos(con)

    while True:
        print("""
===== CRUD TIENDA =====
1. Crear registro
2. Listar registros
3. Actualizar registro
4. Eliminar registro
5. Ejecutar una consulta (1-20)
6. Ejecutar TODAS las consultas
0. Salir""")
        op = input("Opción: ").strip()
        try:
            if op == "1":
                t = elegir_tabla()
                if t:
                    nuevo = crear(con, t, pedir_valores(t))
                    print(f"Registro creado con id {nuevo}")
            elif op == "2":
                t = elegir_tabla()
                if t:
                    mostrar(leer(con, t))
            elif op == "3":
                t = elegir_tabla()
                if t:
                    id_ = int(input("ID a actualizar: "))
                    fila = leer(con, t, id_)
                    if not fila:
                        print("No existe ese id.")
                        continue
                    print("(Enter = dejar el valor actual)")
                    n = actualizar(con, t, id_, pedir_valores(t, fila[0]))
                    print("Actualizado." if n else "Sin cambios.")
            elif op == "4":
                t = elegir_tabla()
                if t:
                    id_ = int(input("ID a eliminar: "))
                    print("Eliminado." if eliminar(con, t, id_) else "No existe ese id.")
            elif op == "5":
                n = int(input("Número de consulta (1-20): "))
                if 1 <= n <= len(CONSULTAS):
                    ejecutar_consulta(con, n)
                else:
                    print("Número fuera de rango.")
            elif op == "6":
                for n in range(1, len(CONSULTAS) + 1):
                    ejecutar_consulta(con, n)
            elif op == "0":
                break
            else:
                print("Opción no válida.")
        except ValueError:
            print("Entrada no válida: se esperaba un número.")
        except sqlite3.IntegrityError as e:
            print(f"Error de integridad (dato duplicado, llave foránea o regla CHECK): {e}")
        except sqlite3.Error as e:
            print(f"Error de base de datos: {e}")

    con.close()
    print("¡Hasta luego!")


if __name__ == "__main__":
    menu()
