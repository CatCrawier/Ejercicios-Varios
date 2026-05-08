import pandas as pd

datos_productos = {
    'id_producto': [101, 102, 103, 104],
    'nombre': ['Laptop', 'Mouse', 'Monitor', 'Teclado'],
    'precio': [1200, 25, 300, 45],
    'categoria': ['Electrónica', 'Accesorios', 'Electrónica', 'Accesorios']
}
df_productos = pd.DataFrame(datos_productos)

datos_pedidos = {
    'id_pedido': [1, 2, 3, 4, 5, 6],
    'id_producto': [101, 102, 101, 103, 104, 102],
    'cantidad': [1, 5, 2, 1, 3, 2]
}
df_pedidos = pd.DataFrame(datos_pedidos)

df_completo = pd.merge(df_pedidos, df_productos, on='id_producto')

df_completo['Total_pedido'] = df_completo['cantidad'] * df_completo['precio']

resumen_dinero = df_completo.groupby('categoria')['Total_pedido'].sum().reset_index()

resultado_final = resumen_dinero.sort_values(by='Total_pedido', ascending=False)

print("Análisis de ingresos por categoría:")
print(resultado_final)

top_cat = resultado_final.iloc[0]['categoria']
print(f"\n>>> La categoría que más dinero genera es: {top_cat}")