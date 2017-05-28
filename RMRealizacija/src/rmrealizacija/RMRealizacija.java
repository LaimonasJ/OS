/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmrealizacija;
import RMachine.*;
/**
 *
 * @author vytau
 */
public class RMRealizacija {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChannelDevice cdevice = new ChannelDevice();
        RAM ram = new RAM();
        //Input input = new Input();
        Output output = new Output();
        HDD memory = new HDD("memory.txt");
        cdevice.setRAM(ram);
        //cdevice.setInput(input);
        cdevice.setOutput(output);
        cdevice.setHDD(memory);
        Procesor procesor = new Procesor();
        procesor.setup(cdevice, ram);
        procesor.start();
        
//        MockProcesor procesor = new MockProcesor();
//        procesor.init(cdevice);
//        procesor.test();
        
        //input.finalize();
    }
    
}
