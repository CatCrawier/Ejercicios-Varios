/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hitts_poo;

/**
 *
 * @author Cesde
 */
public class HITTS_Poo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Persona persona1 = new Persona("Adrian", "Vasquez", 19, 'M');
        Persona persona2 = new Persona("Edwin", "Boada", 19, 'M');
        persona1.mostrarDatos();
        persona2.mostrarDatos();
    }
    
}