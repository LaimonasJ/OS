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
import java.nio.ByteBuffer;


public class HDD implements IExt{
    
    File file = null;
    RandomAccessFile rFile = null;

    
    public HDD(String name){
    file = new File(name);      
    }
    
    
    
    @Override
    public void save(int address, int value){
     
    try{  
    RandomAccessFile rFile = new RandomAccessFile(file, "rw");    
    rFile.seek(address*4);
    rFile.writeInt(value);  
    }   

     catch (Exception e)
    {
        System.out.println(e.getMessage());
    }  
    finally
    {
        try{
        rFile.close();    
        }
        catch (Exception e){
        
        }
        
    }

    }
    @Override
    public int get(int address){
    try{
    RandomAccessFile rFile = new RandomAccessFile(file, "r");
    rFile.seek(address*4);
    int ret = rFile.readInt();
    rFile.close();
    return ret;
    }    
    catch (Exception e)
    {
        System.out.println(e.getMessage());
    }
    
    return 0;
    }
}
