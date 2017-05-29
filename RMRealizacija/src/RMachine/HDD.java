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
        save(0, (int)'i');
        save(1, (int)'n');
        save(2, (int)'p');
        save(3, (int)'u');
        save(4, (int)'t');
        save(5, (int)'$');
        save(6, 20);
        save(7, (int)'l');
        save(8, (int)'o');
        save(9, (int)'o');
        save(10, (int)'p');
        save(11, (int)'$');
        save(12, 420);
        save(13, (int)'/');
        save(20, 3100);
        save(21, 3200);
        save(22, 3400);
        save(23, (int)'/');
        save(420, 3500);
        save(421, 2);
        save(422, 2500);
        save(423, 3400);
        save(424, (int)'/');
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
