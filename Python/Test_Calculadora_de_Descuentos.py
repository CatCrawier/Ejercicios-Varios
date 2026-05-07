import sys
import subprocess
import random

print("=" * 60)
print("PRUEBAS CON VALORES ALEATORIOS")
print("=" * 60)

# Primera prueba aleatoria
print("=" * 60)
print("PRIMERA PRUEBA CON VALORES ALEATORIOS")
print("=" * 60)
precio1 = round(random.uniform(5, 100), 2)
precio2 = round(random.uniform(5, 100), 2)
precio3 = round(random.uniform(5, 100), 2)
es_estudiante = random.choice(["si", "no"])

print(f"\nPrecios generados: {precio1}, {precio2}, {precio3}")
print(f"Es estudiante: {es_estudiante}")
print("-" * 60)

resultado = subprocess.run(
    [sys.executable, "Calculadora_de_Descuentos.py"],
    input=f"{precio1}\n{precio2}\n{precio3}\n{es_estudiante}\n",
    capture_output=True,
    text=True,
    cwd="Python"
)
print(resultado.stdout)

# Segunda prueba aleatoria
print("=" * 60)
print("SEGUNDA PRUEBA CON VALORES ALEATORIOS")
print("=" * 60)

precio1 = round(random.uniform(5, 100), 2)
precio2 = round(random.uniform(5, 100), 2)
precio3 = round(random.uniform(5, 100), 2)
es_estudiante = random.choice(["si", "no"])

print(f"\nPrecios generados: {precio1}, {precio2}, {precio3}")
print(f"Es estudiante: {es_estudiante}")
print("-" * 60)

resultado = subprocess.run(
    [sys.executable, "Calculadora_de_Descuentos.py"],
    input=f"{precio1}\n{precio2}\n{precio3}\n{es_estudiante}\n",
    capture_output=True,
    text=True,
    cwd="Python"
)
print(resultado.stdout)

# Tercera prueba aleatoria
print("=" * 60)
print("TERCERA PRUEBA CON VALORES ALEATORIOS")
print("=" * 60)

precio1 = round(random.uniform(5, 100), 2)
precio2 = round(random.uniform(5, 100), 2)
precio3 = round(random.uniform(5, 100), 2)
es_estudiante = random.choice(["si", "no"])

print(f"\nPrecios generados: {precio1}, {precio2}, {precio3}")
print(f"Es estudiante: {es_estudiante}")
print("-" * 60)

resultado = subprocess.run(
    [sys.executable, "Calculadora_de_Descuentos.py"],
    input=f"{precio1}\n{precio2}\n{precio3}\n{es_estudiante}\n",
    capture_output=True,
    text=True,
    cwd="Python"
)
print(resultado.stdout)

print("=" * 60)