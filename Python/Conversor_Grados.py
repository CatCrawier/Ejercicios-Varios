def fahrenheit(celsius):
    return (celsius * 9.0 / 5.0) + 32

def kelvin(celsius):
    return celsius + 273.15

def main():
    try:
        celsius = float(input("Ingrese los grados Celsius: "))
        fah = fahrenheit(celsius)
        kel = kelvin(celsius)
        print(f"Fahrenheit: {fah:.2f} °F")
        print(f"Kelvin: {kel:.2f} K")
    except ValueError:
        print("Error: Ingrese un número válido.")

if __name__ == "__main__":
    main()