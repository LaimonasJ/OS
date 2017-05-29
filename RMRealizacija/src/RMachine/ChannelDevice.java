/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

/**
 * 
 * @author aleksas
 */
public class ChannelDevice extends Thread{
    private int sb, db, st, dt, inputRequester, inputRequesterAddress;
    private Procesor procesor;
    private IRAM ram;
    private Keyboard input;
    private IOutput output;
    private IExt memory;
    
    public String dataForOS = "";

    public ChannelDevice() {
        sb = 0;
        db = 0;
        st = 0;
        dt = 0;
    }
    
    public void setProcesor(Procesor procesor){
        this.procesor = procesor;
    }
    
    public void setRAM(IRAM ram){
        this.ram = ram;
    }
    
    public void setInput(Keyboard input){
        this.input = input;
    }
    
    public void setOutput(IOutput output){
        this.output = output;
    }
    
    public void setHDD(IExt memory){
        this.memory = memory;
    }
    
    public void setCDevice(int sb, int db, int st, int dt){
        this.sb = sb;
        this.db = db;
        this.st = st;
        this.dt = dt;
    }
    
    public void finalizeInput(){
        switch(inputRequester){
            case 0://output
                output.printInt((int)input.data.charAt(0));
                break;
            case 1://memory
                memory.save(inputRequesterAddress, (int)input.data.charAt(0));
                break;
            case 2://procesor
                synchronized(procesor){
                    procesor.inputChannel = 3;//delivered
                }
                while(true){
                    synchronized(procesor){
                        if(procesor.inputChannel == 0)
                            break;
                    }
                }
                procesor.rw = (int)input.data.charAt(0);
                break;
            case 3://RAM
                synchronized(ram){
                    ram.save(inputRequesterAddress, (int)input.data.charAt(0));
                }
                break;
            case 4:
                dataForOS = input.data;
                break;
        }
        synchronized(procesor){
            procesor.inputChannel = 0;//ready
        }
        synchronized(input){
            input.status = 0;
        }
    }
    
    @Override
    public void run() {
        int ch = 0;
        while(true){
//            synchronized(procesor){
//                System.out.println("ch=" + procesor.ch);
//                System.out.println("inputChannel=" + procesor.inputChannel);
//                System.out.println("st=" + st);
//                System.out.println("dt=" + dt);
//                System.out.println("sb=" + sb);
//                System.out.println("db=" + db);
//                System.out.println("inputRequester=" + inputRequester);
//                System.out.println("inputRequesterAddress=" + inputRequesterAddress);
//                System.out.println("---------------------------------------------------");
//            }
            while(ch == 0){
                if(input.status == 2){
                    synchronized(procesor){
                        finalizeInput();
                    }
                }
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
                    synchronized(input){
                        input.status = 1;
                    }
                    inputRequester = dt;
                    inputRequesterAddress = db;
                    ch = 0;
                    synchronized(procesor){
                        procesor.inputChannel = 2;
                        procesor.ch = 0;
                    }
                    continue;
                case 1://memory
                    stream = memory.get(sb);
                    break;
                case 2://processor
                    stream = procesor.rw;
                    break;
                case 3://RAM
                    synchronized(ram){
                        stream = ram.get(sb);
                    }
            }
            //destination
            switch(dt){
                case 0://Input/Output
                    output.printInt(stream);
                    synchronized(procesor){
                        procesor.ch = 0;//ready
                    }
                    break;
                case 1://memory
                    memory.save(db, stream);
                    synchronized(procesor){
                        procesor.ch = 0;//ready
                    }
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
                    //synchronized(this){
                    //    notify();
                    //}
                    break;
                case 3://RAM
                    synchronized(ram){
                        ram.save(db, stream);
                    }
//                    synchronized(procesor){
//                        procesor.ch = 0;//ready
//                    }
            }
            synchronized(procesor){
                if(procesor.ch != -1)
                    procesor.ch = 0;//free
            }
            ch = 0;
            //comment once with OS
//            synchronized(this){
//                notify();
//            }
        }
        synchronized(input){
            input.status = -1;
        }
        System.out.println("channel device is out");
    }
}
