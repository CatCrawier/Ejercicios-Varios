import pandas as pd
import os

edades = pd.Series([20, 21, 22, 23, 24, 25])
print("Edades:\n", edades)

datos = {
    "nombre": ["Juan", "Maria", "Pedro", "Ana"],
    "edad": [20, 21, 22, 23],
    "ciudad": ["Madrid", "Barcelona", "Valencia", "Sevilla"],
}

df = pd.DataFrame(datos)
print("\nDataFrame de personas:\n", df)

directorio_script = os.path.dirname(os.path.abspath(__file__))
ruta_csv = os.path.join(directorio_script, "MOCK_DATA.csv")

df_csv = pd.read_csv(ruta_csv)
print("\nPrimeras filas del archivo CSV:\n", df_csv.head())
print("\nÚltimas filas del archivo CSV:\n", df_csv.tail())
print(df_csv.info())
print("\nInformacion Estadistica del archivo CSV:\n", df_csv.describe())
print(f"Forma del archivo CSV: {df_csv.shape}")
print(f"Columnas del archivo CSV: {df_csv.columns}")

nombres_productos = df_csv["Producto"]
print("\nNombres de los productos:\n", nombres_productos)

resumen = df_csv[["Producto", "Cantidad"]]
print("\nResumen de los productos y cantidades:\n", resumen)

df_csv["Total_Precio"] = df_csv["Precio"] * df_csv["Cantidad"]
print("\nTotal de precios:\n", df_csv["Total_Precio"])

promedio_ventas = df_csv["Total_Precio"].mean()
print(f"Promedio de ventas: {promedio_ventas:.2f}.")

suma_ventas = df_csv["Total_Precio"].sum()
print(f"Suma de ventas: {suma_ventas:.2f}.")