import pandas as pd

datos = {
    "Vendedor": ["Juan", "Maria", "Pedro", "Ana", "Luis", "Juan"],
    "Ciudad": ["Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza", "Madrid"],
    "Ventas": [150, 200, 100, 120, 130, 300],
}

df = pd.DataFrame(datos)

ventas_vendedor = df.groupby("Vendedor")["Ventas"].sum()
print("Ventas por vendedor:\n", ventas_vendedor)

ventas_detalladas = df.groupby(["Vendedor", "Ciudad"])["Ventas"].sum()
print("Ventas detalladas por vendedor y ciudad:\n", ventas_detalladas)

resumen_ventas = df.groupby("Vendedor")["Ventas"].agg(["sum", "count", "mean"])
print("Resumen de ventas por vendedor:\n", resumen_ventas)

tabla_ventas = pd.pivot_table(
    df,
    values="Ventas",
    index="Ciudad",
    columns="Vendedor",
    aggfunc="sum",
    fill_value=0
)
print("Tabla de ventas pivotada por ciudad y vendedor:\n", tabla_ventas)

df_enero = pd.DataFrame({"ID": [1, 2], "Ventas": [100, 150]})
df_febrero = pd.DataFrame({"ID": [3, 4], "Ventas": [200, 50]})

df_total = pd.concat([df_enero, df_febrero], ignore_index=True)
print("Ventas totales por mes:\n", df_total)

transacciones = pd.DataFrame(
    {
        "ID_Cliente": [1, 2, 3],
        "Producto": ["Laptop", "Mouse", "Teclado"],
        "Precio": [2000, 25, 45]
    }
)

clientes = pd.DataFrame(
    {
        "ID_Cliente": [1, 2, 3],
        "Nombre": ["Juan", "Maria", "Pedro"],
        "Email": ["juan@example.com", "maria@example.com", "pedro@example.com"]
    }
)

df_combinado = pd.merge(transacciones, clientes, on="ID_Cliente")
print("Reporte de transacciones con datos de cliente:\n", df_combinado)