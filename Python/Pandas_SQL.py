import pandas as pd
import matplotlib.pyplot as plt
from sqlalchemy import create_engine

try:
    sql = create_engine("mysql+pymysql://root:@localhost/motos_colombia")
    print("Conexion Exitosa")
except Exception as e:
    print(f"Error de conexion: {e}")

query1 = "SELECT marca, categoria, uso, modelo FROM v_catalogo"
query2 = "SELECT id_marca, id_categoria FROM modelos"

df1 = pd.read_sql(query1, sql)
df2 = pd.read_sql(query2, sql)

print(df1.head(2))

marca_counts = df1.groupby("marca")["modelo"].count()

plt.figure(figsize=(10, 5))
plt.plot(marca_counts.index, marca_counts.values, marker='o', color='blue', linestyle='--')
plt.title('Cantidad de Modelos por Marca')
plt.xlabel('Marca')
plt.ylabel('Cantidad de Modelos')
plt.grid(True)
plt.show()

plt.figure(figsize=(8, 6))
colores = ["red", "green", "orange", "purple"]
plt.bar(marca_counts.index, marca_counts.values, color=colores)
plt.title('Distribución de Modelos por Marca')
plt.xlabel('Marca')
plt.ylabel('Cantidad')
plt.savefig("grafica.png", dpi=300)
plt.show()

plt.figure(figsize=(8, 6))
plt.scatter(df2["id_marca"], df2["id_categoria"], alpha=0.5, color="darkred")
plt.title('Relación entre ID de Marca e ID de Categoría')
plt.xlabel('ID Marca')
plt.ylabel('ID Categoría')
plt.show()