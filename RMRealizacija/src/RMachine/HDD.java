/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

/**
 *
 * @author Vytautas
 */
import java.io.*;

public class HDD implements IExt{
    
    File file = null;
    RandomAccessFile rFile = null;

    
    public HDD(String name){
        file = new File(name);
        save(0, (int)'c');
        save(1, (int)'a');
        save(2, (int)'$');
        save(3, 20);
        save(4, (int)'b');
        save(5, (int)'a');
        save(6, (int)'$');
        save(7, 420);
        save(8, (int)'/');
    }
    
    
    
    @Override
    public void save(int address, int value){
        try{  
            rFile = new RandomAccessFile(file, "rw");    
            rFile.seek(address*4);
            rFile.writeInt(value);  
            rFile.close();  
        }   
         catch (IOException e)
        {
            System.out.println(e.getMessage());
        }  
    }
    
    @Override
    public int get(int address){
        try{
            rFile = new RandomAccessFile(file, "r");
            rFile.seek(address*4);
            int ret = rFile.readInt();
            rFile.close();
            return ret;
        }    
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
    }
}
