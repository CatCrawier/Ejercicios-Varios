import pandas as pd
import os

directorio_script = os.path.dirname(os.path.abspath(__file__))
ruta_csv = os.path.join(directorio_script, "Empleados.csv")

df = pd.read_csv(ruta_csv)
print(f"Datos nulos:\n {df.isna()}")
print(f"Datos nulos por columna:\n {df.isna().sum()}")
print(f"Datos Duplicados: {df.duplicated().sum()}")

df_clean = df.dropna()
print(f"Datos limpios:\n {df_clean.info()}")

df["Salario"] = df["Salario"].fillna(0)
print(f"Datos con salario rellenado:\n {df['Salario']}")

promedio_edad = df["Edad"].mean()
df["Edad"] = df["Edad"].fillna(promedio_edad)
print(f"Datos con edad rellenada:\n {df['Edad']}")

mayores_30 = df[df["Edad"] > 30].astype({"Edad": int})
print(f"Empleados mayores de 30 años:\n {mayores_30}")

filtro_ingresos_Xiaogang = df[(df["Ciudad"] == "Xiaogang") & (df["Salario"] > 1000000)]
print(f"Empleados de Xiaogang con ingresos superiores a 1000000:\n {filtro_ingresos_Xiaogang}")

df["Ciudad"] = df["Ciudad"].str.upper()
print(f"Datos con ciudad en mayusculas:\n {df['Ciudad']}")

df["Nombre"] = df["Nombre"].str.strip()
print(f"Datos con nombre sin espacios:\n {df['Nombre']}")
print(f"Datos con nombre sin espacios y cantidad de veces que aparece:\n {df['Nombre'].value_counts()}")