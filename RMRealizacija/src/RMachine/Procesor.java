/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

import OS.StartStop;

/**
 *ch / inputChannel:
 * 0-ready
 * 1-requested
 * 2-busy
 * 3-delivered
 * 4-error
 * 
 * 
 * @author aleksas
 */
public class Procesor {

    public boolean shutDown = false;
    
    private final int ift; //pertraukimų lentelės 4B
    public boolean mode; //režimo 1B
    public short ti; //timer 2B
    public short ch; //kanalų 2B
    public short inputChannel; //inputo 2B
    public int rw; //darbinis 4B 
    public int ip; //instrukcijos 4B
    public int ptr; //puslapiavimo 4B
    public int sf; //status 1B                     mum cia reiks int, nes mano komandose parasyta, kad CMP gali uzsetinti 0,1 arba 2
    public short sp; //steko 2B
    public short ifr; //pertraukimų 2B
    
    private ChannelDevice cdevice;
    private IRAM ram;
    private StartStop os;

    public Procesor() {
        rw=0;
        ip=0;
        ptr=20;
        ift=50;
        sf=0;
        mode=true; //true:supervizor
        ifr=0;
        ti=33;
        sp=0;
        ch=0;
        inputChannel = 0;
    }
    
    public void setup(StartStop os, ChannelDevice device, IRAM ram){
        cdevice = device;
        cdevice.setProcesor(this);
        this.ram = ram;
        this.os = os;
    }
    
    @SuppressWarnings("empty-statement")
    public void start(){
        if(cdevice == null || ram == null)
            return;
        cdevice.start();
        runtime();
        
        System.out.println("rw=" + rw);
        System.out.println("ip=" + ip);
        System.out.println("ptr=" + ptr);
        System.out.println("ift=" + ift);
        System.out.println("sf=" + sf);
        System.out.println("mode=" + mode);
        System.out.println("ifr=" + ifr);
        System.out.println("ti=" + ti);
        System.out.println("ch=" + ch);
        System.out.println(ram.toString());
        
        System.out.println("PC is shutting down...");
        
//        while(true);
        ch = -1;
//        while(true);
//        try {
//            cdevice.join();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Procesor.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    private void CMP(int value){
        if(rw == value){
            sf = 0;
            return;
        }
        if(rw > value){
            sf = 1;
            return;
        }
        if(rw < value){
            sf = 2;
        }
    }
    
    private void GRE(int fromInput){
        int registerVal;
        if(fromInput > 0)
            registerVal = inputChannel;
        else
            registerVal = ch;
        if(registerVal == 3){
            if(fromInput > 0){
                inputChannel = 0;
                sf = 2;
            }
            else{
                ch = 0;
                sf = 1;
            }
        }
        else{
            sf = 0;
        }
        //waitForCDevice();
    }
    
    private void PUSH(){
        sp++;
        sendToRAM(sp, rw, 2);
    }
    
    private void POP(){
        rw = getFromRAM(sp, 2);
        sp--;
    }
    
    private void XCH(int command){
        //xxzy hhhh qqqq
        command = (command * 10000 + getFromRAM(ip, 0)) % 1000000;
        ip++;
        int from = command / 100000;
        int to = (command / 10000) % 10;
        int addressFrom = command % 10000;
        int addressTo = getFromRAM(ip, 0);
        ip++;
        if(ch != 0)
            sf = 0;
        else{
            if(from == 0){
                cdevice.setCDevice(addressFrom, addressTo, from, to);
                inputChannel = 1;
            }
            else{
            //System.out.println("Asking for resources");
                cdevice.setCDevice(addressFrom, addressTo, from, to);
            }
            ch = 1;//requested
//            try {
//                cdevice.wait();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Procesor.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    
    /*
    segment:
        0 - code
        1 - data
        2 - stack
    */
    private int absoluteAddress(int address, int segment){
        int firstBlockAddress = 0;
        int blockNr = address / 10;
        synchronized(ram){
            switch(segment){
                case 0:
                    firstBlockAddress = ram.get(ptr + blockNr);//code segment
                    break;
                case 1:
                    firstBlockAddress = ram.get(ptr + 10 + blockNr);//data segment
                    break;
                case 2:
                    firstBlockAddress = ram.get(ptr + 20 + blockNr);//stack segment
                    break;
            }
        }
        return firstBlockAddress + (address % 10);
    }
    
    private void waitForCDevice(){
        try {
            System.out.println("Waiting for resources");
            synchronized(cdevice){
                cdevice.wait();
            }
            System.out.println("Got " + rw);
        } catch (InterruptedException ex) {
            System.out.println("Something is wrong with waiting for cdevice");
        }
    }
    
    private int getFromRAM(int address, int segment){
        if(!mode)
            address = absoluteAddress(address, segment);
        synchronized(ram){
            return ram.get(address);
        }
    }
    
    private void sendToRAM(int address, int value, int segment){
        if(!mode)
            address = absoluteAddress(address, segment);
        synchronized(ram){
            ram.save(address, rw);
        }
    }
    
    /**
     *@param adress - used if requesting value from memory
     *@param from:
     * 0 - input/output
     * 1 - memory
     * 2 - Processor
     * 
     *@return 
     */
    private void getFromCDevice(int address, int from){
        while(ch != 0)
            waitForCDevice();
        cdevice.setCDevice(address, 2, from, 2);
        ch = 1;//requested
        System.out.println("Asking for resources");
        waitForCDevice();
    }
    
    /**
     *@param value - value to send
     *@param adress - used if requesting value from memory
     *@param to:
     * 0 - input/output
     * 1 - memory
     * 2 - Processor
     * 
     *@return 
     */
    private void sendToCDevice(int adress, int to){
        System.out.println("Waiting for cdevice");
        if(ch != 0)
            waitForCDevice();
        System.out.println("Sending data");
        cdevice.setCDevice(2, adress, 2, to);
        ch = 1;//requested
    }
    
    private boolean supervizorsInstructions(int command){
        
        int instruction = command / 100;
        
        switch(instruction){
            //save
            case 1://SPT
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sendToRAM(command % 10000, ptr, 1);//sends ptr
                return true;
            case 2://SSP
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sendToRAM(command % 10000, sp, 1);//sends sp
                return true;
            case 3://SSF
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sendToRAM(command % 10000, sf, 1);//sends sf
                return true;
            case 4://SIF
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sendToRAM(command % 10000, ifr, 1);//sends ifr
                return true;
            //load
            case 5://LPT
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                ptr = getFromRAM(command % 10000, 1);//sets ptr
                return true;
            case 6://LSP
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sp = (short) getFromRAM(command % 10000, 1);//sets sp
                return true;
            case 7://UIF - unset IFR registra, reiks prirasyt dar viena instrukcija, nes kitaip neisivaizduoju kaip OS gali nuimt blokavima
                ifr = 0;
                return true;
            case 8://LSF
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                sf = getFromRAM(command % 10000, 1);//sets sf
                return true;
            case 9://RUN
                command = command * 100 + getFromRAM(ip, 0);
                ip++;
                ip = getFromRAM(command % 10000, 1);//sets IP
                mode = false;
                return true;
            case 10://RTI
                ti = (short)(command % 100);
                return true;
            case 11://GRE - get resource(if ch = delivered(3), sets SF = 1 and CH = 0, else SF = 0)
                GRE(command % 100);
                return true;
            case 12:
                XCH(command);//XCH (XCH komanda nesimaisys su SPT, nes ji yra zymiai ilgesne)
                    //xyzhhhh:
                    //  xx - instruction code
                    //  y - from
                    //  z - to
                    //  address(if not needed can be filled with anything)
                return true;
        }
        return false;
    }
    
    private void runtime(){
        int adressParser;
        int command;
        int instruction;
        int operand = 0;
        boolean interuptInVM = false;
        
        //Cia turi paleisti StartStop<----------------------------------------------------------------------
        //StartStop os = new StartStop(this, cdevice, ram);
        os.start();
//        try {
//            os.join();
//        } catch (InterruptedException ex) {
//            System.out.println("RM failed to wait for OS");
//        }
            
        while(!shutDown){
//            System.out.println("rw=" + rw);
//            System.out.println("ip=" + ip);
//            System.out.println("ptr=" + ptr);
//            System.out.println("ift=" + ift);
//            System.out.println("sf=" + sf);
//            System.out.println("mode=" + mode);
//            System.out.println("ifr=" + ifr);
//            System.out.println("ti=" + ti);
//            System.out.println("ch=" + ch);
//            System.out.println(ram.toString());
//            System.out.println("-------------------------------------------------------");

            adressParser = 100;
            command = getFromRAM(ip, 0);
            ip++;
            
            instruction = command / adressParser;
            
            if(mode){
                //Cia bus dalis OS'o<------------------------------------------------------------------------------
                synchronized(this){
                    notify();
                }
                while(mode){
                    try {
                        synchronized(this){
                            wait(50);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("RM failed at waiting");
                    }
                }
                continue;
//                if(ifr != 0 && !interuptInVM)
//                    break;
//                if(supervizorsInstructions(command))//Jei randam, kad tai supervizoriaus instrukcija, tolimesniu instrukciju paieska nebera svarbi
//                    continue;
//                if(instruction < 21){
//                    command = command * 100 + getFromRAM(ip, 0);
//                    ip++;
//                }
//                adressParser = 10000;
            }
            else{
                //checks for code segment error
                if(ip < 0 || ip > 99)
                    ifr = 1;//cia tarkim yra CODE_SEG_ERROR bitas, bet galesim keisti
                if(ti == 0)
                    ifr = 2;//cia tarkim yra TIME_OUT bitas, bet galesim keisti
                else
                    ti--;
                if(ifr != 0){
                    interuptInVM = true;
                    ram.save(sp, ip);
                    sp++;
                    mode = true;
                    //ip = ift;
                    continue;
                }
            }
            //xxyyzz:
            //xx - instrukcijos kodas
            //yy - adresas, jei mode = 0
            //yyzz - adresas, jei mode = 1
            //instrukcijos, kuriom pakanka maziau nei 1 zodzio, like bitai uzpildomi 0
            //SET instrukcija - xxyzzzzzz:
            //xx - instrukcijos kodas
            //y - zenkas:
            //  0 = +
            //  1 = -
            //zzzzzz - sesiazenklis skaicius
            if(command / 100 == 35){//SET
                int symbol = command / 10 % 10;
                int digit = command % 10 * 10000 + getFromRAM(ip, 0);
                ip ++;//reiks 2 zodziu
                if(symbol > 0)
                    rw = -1 * digit;
                else
                    rw = digit;
                continue;
            }
            
            if(instruction < 19){
                operand = rw;
                if(command % adressParser > 99){
                    interuptInVM = true;
                    ifr = 4;//cia tegul DATA_SEGMENT_FAULT
                    mode = true;
                    ip = ift;
                    continue;
                }
                rw = getFromRAM(command % adressParser, 1);//sets rw
            }
            switch(instruction){
            //arithmetics
                case 13://ADD xy
                    rw = rw + operand;
                    break;
                case 14://SUB xy
                    rw = rw - operand;
                    break;
                case 15://MUL xy
                    rw = rw * operand;
                    break;
                case 16://DIV xy
                    if(operand == 0){
                        if(mode)
                            return;
                        ifr = 256;//DIVISION_BY_ZERO
                        interuptInVM = true;
                        mode = true;
                        ram.save(sp, ip);
                        sp++;
                        ip = ift;
                    }
                    rw = rw / operand;
                    break;
            //logical
                case 17://AND xy
                    rw = rw & operand;
                    break;
                case 18://OR xy
                    rw = rw | operand;
                    break;
                case 19://NOT
                    rw = ~rw;
                    break;
            //compare
                case 20://CMP xy
                    CMP(operand);
                    break;
            //data stream
                case 21://LOW xy
                    rw = getFromRAM(command % adressParser, 1);//sets rw
                    break;
                case 22://SAW xy
                    sendToRAM(command % adressParser, rw, 1);//sends rw
                    break;
            //stack
                case 23://PUSH
                    PUSH();
                    break;
                case 24://POP
                    POP();
                    break;
            //jumps
                case 25://JMP
                    ip = rw;
                    break;
                case 26://JE
                    ip = sf == 0? rw : ip;
                    break;
                case 27://JL
                    ip = sf == 1? rw : ip;
                    break;
                case 28://JG
                    ip = sf == 2? rw : ip;
                    break;
                case 29://JLE
                    ip = (sf == 0 || sf == 1)? rw : ip;
                    break;
                case 30://JGE
                    ip = (sf == 0 || sf == 2)? rw : ip;
                    break;
            //in/out and stop process
                case 31://IN
                    //getFromCDevice(0, 0);
                    //Uncomment once with OS
                    if(mode)
                        return;
                    interuptInVM = true;
                    ifr = 32;//cia tarkim yra GET_FROM_CDEVICE bitas
                    mode = true;
                    ram.save(sp, ip);
                    sp++;
                    //ip = ift;
                    break;
                case 32://OUT
                    //Uncomment once with OS
                    //sendToCDevice(0, 0);
                    if(mode)
                        return;
                    interuptInVM = true;
                    ifr = 64;//cia tarkim yra SEND_TO_CDEVICE bitas
                    mode = true;
                    ram.save(sp, ip);
                    sp++;
                    //ip = ift;
                    break;
                case 33://TOD xxxx -(to disk) saves value stored in rw to memory
                    if(mode)
                        return;
                    interuptInVM = true;
                    ifr = 128;//cia tarkim yra SEND_TO_DISK bitas
                    mode = true;
                    ram.save(sp, ip);
                    sp++;
                    //ip = ift;
                    break;
                case 34://HLT
                    if(mode)
                        return;
                    interuptInVM = true;
                    ifr = 8;//cia tarkim yra PROCESS_END bitas
                    mode = true;
                    ip = ift;
                    break;
                default:
                    if(mode)
                        return;
                    interuptInVM = true;
                    ifr = 16;//cia tarkim yra ILLEGAL_INSTRUCTION bitas
                    mode = true;
                    ram.save(sp, ip);
                    sp++;
                    ip = ift;
            }
        }
    }
}

    
    

