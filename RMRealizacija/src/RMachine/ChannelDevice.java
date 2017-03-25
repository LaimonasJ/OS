/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *sb and db:
 * 0 - rw
 * 1 - ip
   2 - ptr
   3 - sf
   4 - ifr 
   5 - sp
 * 
 * @author vytau
 */
public class ChannelDevice extends Thread{
    private int sb, db, st, dt, adr;
    private Procesor procesor;
    private IRAM ram;
    private IInput input;
    private IOutput output;

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
    
    public void setCDevice(int sb, int db, int st, int dt, int adr){
        this.sb = sb;
        this.db = db;
        this.st = st;
        this.dt = dt;
        this.adr = adr;
    }
    
    private int getFromProcesor(){
        synchronized(procesor){
            switch(sb){
                case 0://rw
                    return procesor.rw;
                case 1://ip
                    return procesor.ip;
                case 2://ptr
                    return procesor.ptr;
                case 3://sf
                    return procesor.sf;
                case 4://ifr
                    return procesor.ifr;
                case 5://sp
                    return procesor.sp;
            }
        }
        synchronized(procesor){
            procesor.ch = 3;//error
        }
        return -1;
    }
    
    private void sendToProcesor(int stream){
        switch(db){
            case 0://rw
                procesor.rw = stream;
                break;
            case 1://ip
                procesor.ip = stream;
                break;
            case 2://ptr
                procesor.ptr = stream;
                break;
            case 3://sf
                procesor.sf = stream;
                break;
            case 4://ifr
                procesor.ifr = (short)stream;
                break;
            case 5://sp
                procesor.sp = (short)stream;
                break;
            default:
                synchronized(procesor){
                    procesor.ch = 3;//error
                }
        }
        synchronized(this){
            notify();
        }
    }
    
    @Override
    public void run() {
        int ch = 0;
        while(ch != -1){
            while(ch == 0){
                synchronized(procesor){
                    ch = procesor.ch;
                }
            }
            if(ch == -1)
                break;
            int stream = 0;
            synchronized(procesor){
                procesor.ch = 2;//busy
            }
            //source
            switch(st){
                case 0://Input/Output
                    stream = input.getInt();
                    break;
                case 1://RAM
                    stream = ram.get(adr);
                    break;
                case 2://memory
                    break;
                case 3://processor
                    stream = getFromProcesor();
                    break;
            }
            //destination
            switch(dt){
                case 0://Input/Output
                    output.printInt(stream);
                    break;
                case 1://RAM
                    ram.save(adr, stream);
                    break;
                case 2://memory
                    break;
                case 3://processor
                    sendToProcesor(stream);
                    break;
            }
            synchronized(procesor){
                if(procesor.ch != -1)
                    procesor.ch = 0;//free
            }
            ch = 0;
        }
    }
}
