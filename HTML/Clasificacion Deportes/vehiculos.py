
tipo_vehiculo = int("ingrese su tipo de vehiculo(carro/moto/camion)")
limite_velocidad = int("ingrese el limite de velocidad")
velocidad_registrada = int("ingrese la velocidad registrada")
prueba_alcoholemia = int("ingrese el nivel de alcoholemia")

multa_alta = int = 1000000 
multa_media = int = 600000
multa_baja = int = 300000

multa : True
suspencion_licencia : True
valor_multa = 0
prueba_alcoholemia = 0

int (valor_multa) = 0
bool (suspencion_licencia) = False
if velocidad_registrada < limite_velocidad:
        int("felicitaciones")
        if velocidad_registrada > limite_velocidad | velocidad_registrada < limite_velocidad + 20:
         multa : True
         valor_multa = multa_baja
        else:
         multa : True
         valor_multa = multa_media
         if prueba_alcoholemia > 0 | prueba_alcoholemia < 4:
          multa = True
          valor_multa = multa_alta
          suspencion_licencia = True
         else:
          int("valor de alcoholemia incorrecto")

         if tipo_vehiculo.equals("camion") | multa:
          valor_multa = multa_alta * 2
                 
         int("su multa es de: " + valor_multa + "y su estado de licencia: " + suspencion_licencia)
        