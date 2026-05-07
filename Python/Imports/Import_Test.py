def mostrar(nombre: str, edad: int) -> tuple[str, int]:

    '''
    Esta funcion sirve para imprimir el nombre de este archivo y retornar el nombre y la edad que se le pasen como parametros.
    
    Ejemplo de uso:
    
    Nombre: str
    Edad: int
    
    mostrar(Nombre, Edad)
    '''
    print("Estoy en el archivo Import_Test.py")

    return nombre, edad

if __name__ == "__main__":
    mostrar("Adrian", 19)