/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmrealizacija;
import RMachine.RealMachine;

/**
 *
 * @author vytau
 */
public class RMRealizacija {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RealMachine rm = new RealMachine(2);
        rm.runtime();
    }
    
}
