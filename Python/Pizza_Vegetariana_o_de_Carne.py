pizza=input("¿Qué tipo de pizza quieres? (vegetariana o de carne) (todas las pizzas llevan mozzarella y tomate): ").lower()
if pizza=="vegetariana":
    ingrediente1=input("Elige un ingrediente (pimiento o tofu): ").lower()
    if ingrediente1=="pimiento":
        print("Has elegido una pizza vegetariana con mozzarella, tomate y pimiento.")
    elif ingrediente1=="tofu":
        print("Has elegido una pizza vegetariana con mozzarella, tomate y tofu.")
    else:
        print("Ingrediente no válido.")
elif pizza=="de carne":
    ingrediente2=input("Elige un ingrediente (peperoni, jamón o salmón): ").lower()
    if ingrediente2=="peperoni":
        print("Has elegido una pizza de carne con mozzarella, tomate y peperoni.")
    elif ingrediente2=="jamón":
        print("Has elegido una pizza de carne con mozzarella, tomate y jamón.")
    elif ingrediente2=="salmón":
        print("Has elegido una pizza de carne con mozzarella, tomate y salmón.")
    else:
        print("Ingrediente no válido.")