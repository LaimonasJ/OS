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
    
    FileInputStream in = null;
    FileOutputStream out = null;
    File file = null;
    
    public HDD(String name){
    file = new File(name);       
            
    }
    
    
    
    @Override
    public void save(int address, int value){
     
    try{
    FileOutputStream fos = new FileOutputStream(file);    
    fos.write(ByteBuffer.allocate(4).putInt(value).array(),address,4);  
    fos.close();
    }   
    
    
     catch (Exception e)
    {
        System.out.println(e.getMessage());
    }  
    
    
    
    }
    @Override
    public int get(int address){
    try{
    FileInputStream fis = new FileInputStream(file);  
    byte [] arr = new byte[4];
    fis.read(arr, address, 4);
    ByteBuffer bb = ByteBuffer.wrap(arr);
    return bb.getInt();
    }    
    catch (Exception e)
    {
        System.out.println(e.getMessage());
    }
    
    return 0;
    }
}
