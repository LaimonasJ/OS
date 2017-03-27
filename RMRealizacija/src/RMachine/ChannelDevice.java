/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author vytau
 */
public class ChannelDevice extends Thread{
    private int sb, db, st, dt, adr;
    private Procesor procesor;
    private IRAM ram;
    private IInput input;
    private IOutput output;
    private IExt memory;

    public ChannelDevice() {
        sb = 0;
        db = 0;
        st = 0;
        dt = 0;
        adr = 0;
    }
    
    public void setProcesor(Procesor procesor){
        this.procesor = procesor;
    }
    
    public void setRAM(IRAM ram){
        this.ram = ram;
    }
    
    public void setInput(IInput input){
        this.input = input;
    }
    
    public void setOutput(IOutput output){
        this.output = output;
    }
    
    public void setHDD(IExt memory){
        this.memory = memory;
    }
    
    public void setCDevice(int sb, int db, int st, int dt, int adr){
        this.sb = sb;
        this.db = db;
        this.st = st;
        this.dt = dt;
        this.adr = adr;
    }
    
    @Override
    public void run() {
        int ch = 0;
        while(true){
            while(ch == 0){
                synchronized(procesor){
                    ch = procesor.ch;
                }
            }
            int stream = 0;
            synchronized(procesor){
                if(procesor.ch == -1)
                    break;
                procesor.ch = 2;//busy
            }
            //source
            switch(st){
                case 0://Input/Output
                    stream = input.getInt();
                    break;
                case 1://memory
                    stream = memory.get(adr);
                    break;
                case 2://processor
                    stream = procesor.rw;
                    break;
                case 3://RAM
                    synchronized(ram){
                        stream = ram.get(adr);
                    }
            }
            //destination
            switch(dt){
                case 0://Input/Output
                    output.printInt(stream);
                    break;
                case 1://memory
                    memory.save(adr, stream);
                    break;
                case 2://processor
                    synchronized(procesor){
                        procesor.ch = 3;//delivered
                    }
                    //Uncomment once with OS
                    while(true){
                        synchronized(procesor){
                            if(procesor.ch == 0)
                                break;
                        }
                    }
                    procesor.rw = stream;
                    synchronized(this){
                        notify();
                    }
                    break;
                case 3://RAM
                    synchronized(ram){
                        ram.save(adr, stream);
                    }
            }
            synchronized(procesor){
                if(procesor.ch != -1)
                    procesor.ch = 0;//free
            }
            ch = 0;
            //comment once with OS
            synchronized(this){
                notify();
            }
        }
    }
}
