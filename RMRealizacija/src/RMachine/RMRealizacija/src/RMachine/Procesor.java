/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *ch:
 * 0-ready
 * 1-requested
 * 2-busy
 * 3-error
 * 
 * 
 * @author vytau
 */
public class Procesor {

    private final int ift; //pertraukimų lentelės 4B
    private boolean mode; //režimo 1B
    private short ti; //timer 2B
    public short ch; //kanalų 2B
    public int rw; //darbinis 4B 
    public int ip; //instrukcijos 4B
    public int ptr; //puslapiavimo 4B
    public int sf; //status 1B                     mum cia reiks int, nes mano komandose parasyta, kad CMP gali uzsetinti 0,1 arba 2
    public short sp; //steko 2B
    public short ifr; //pertraukimų 2B
    
    private ChannelDevice cdevice;
    private IRAM ram;

    public Procesor() {
        rw=0;
        ip=0;
        ptr=0;
        ift=0;
        sf=0;
        mode=true; //1 super 0 user? gal keisti į boolean? jo geriau boolean :) true:super
        ifr=0;
        ti=0;
        sp=0;
        ch=0;
    }
    
    public void setup(ChannelDevice device, IRAM ram){
        cdevice = device;
        cdevice.setProcesor(this);
        this.ram = ram;
    }
    
    public boolean start(){
        if(cdevice == null || ram == null)
            return false;
        cdevice.start();
        runtime();
        ch = -1;
        try {
            cdevice.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Procesor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
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
    
    private void GRE(){
        if(ch == 3){
            ch = 0;
            sf = 1;
        }
        else{
            sf = 0;
        }
        waitForCDevice();
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
        int from = command / 100000;
        int to = (command / 10000) % 10;
        int address = command % 10000;
        if(ch != 0){
            sf = 0;
        }
        else{
            System.out.println("Asking for resources");
            cdevice.setCDevice(from, to, from, to, address);
            ch = 1;//requested
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
                    firstBlockAddress = ram.get(ptr + blockNr);
                    break;
                case 1:
                    firstBlockAddress = ram.get(ptr + 10 + blockNr);
                    break;
                case 2:
                    firstBlockAddress = ram.get(ptr + 20 + blockNr);
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
            Logger.getLogger(Procesor.class.getName()).log(Level.SEVERE, null, ex);
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
    private void getFromCDevice(int adress, int from){
        while(ch != 0)
            waitForCDevice();
        cdevice.setCDevice(2, 2, from, 2, adress);
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
        cdevice.setCDevice(2, 2, 2, to, adress);
        ch = 1;//requested
    }
    
    private boolean supervizorsInstructions(int command){
        
        if(command / 1000000 == 1){//XCH (XCH komanda nesimaisys su SPT, nes ji yra zymiai ilgesne)
            //xyzhhhh:
            //  xx - instruction code
            //  y - from
            //  z - to
            //  address(if not needed can be filled with anything)
            XCH(command);
            return true;
        }
        
        int instruction = command / 10000;
        
        switch(instruction){
        //safe
            case 1://SPT
                sendToRAM(command % 10000, ptr, 1);//sends ptr
                return true;
            case 2://SSP
                sendToRAM(command % 10000, sp, 1);//sends sp
                return true;
            case 3://SSF
                sendToRAM(command % 10000, sf, 1);//sends sf
                return true;
            case 4://SIF
                sendToRAM(command % 10000, ifr, 1);//sends ifr
                return true;
        //load
            case 5://LPT
                ptr = getFromRAM(command % 10000, 1);//sets ptr
                return true;
            case 6://LSP
                sp = (short) getFromRAM(command % 10000, 1);//sets sp
                return true;
            case 7://UIF - unset IFR registra, reiks prirasyt dar viena instrukcija, nes kitaip neisivaizduoju kaip OS gali nuimt blokavima
                ifr = 0;
                return true;
            case 8://LSF
                sf = getFromRAM(command % 10000, 1);//sets sf
                return true;
            case 9://RUN
                ip = getFromRAM(command % 10000, 1);//sets IP
                mode = false;
                return true;
            case 10://RTI
                ti = (short)(command % 10000);
                return true;
            case 11://GRE - get resource(if ch = delivered(3), sets SF = 1 and CH = 0, else SF = 0)
                GRE();
                return true;
        }
        return false;
    }
    
    private void runtime(){
        int adressParser;
        int command;
        int instruction;
        int operand = 0;
        
        while(true){
            adressParser = 100;
            command = getFromRAM(ip, 0);
            ip++;
            
            instruction = command / adressParser;
            
            if(mode){
                if(ifr != 0)
                    break;
                if(instruction < 21){
                    command = command * 100 + getFromRAM(ip, 0);
                    ip++;
                }
                if(supervizorsInstructions(command))//Jei randam, kad tai supervizoriaus instrukcija, tolimesniu instrukciju paieska nebera svarbi
                    continue;
                adressParser = 10000;
            }
            else{
                //checks for code segment error
                if(ip < 0 || ip > 99)
                    ifr = 1;//cia tarkim yra CODE_SEG_ERROR bitas, bet galesim keisti
                if(ti == 0)
                    ifr = 2;//cia tarkim yra TIME_OUT bitas, bet galesim keisti
                ti--;
                if(ifr != 0){
                    mode = true;
                    ip = ift;
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
            if(command / 10000000 == 33){//SET
                command = command * 100 + getFromRAM(ip, 0);
                ip ++;//reiks 2 zodziu
                if(command / 1000000 % 10 == 1){
                    rw = -1 * command % 1000000;
                }
                else{
                    rw = command % 1000000;
                }
                continue;
            }
            
            if(instruction < 19){
                operand = rw;
                if(command % adressParser > 99){
                    ifr = 4;//cia tegul DATA_SEGMENT_FAULT
                    mode = true;
                    ip = ift;
                    continue;
                }
                rw = getFromRAM(command % adressParser, 1);//sets rw
            }
            switch(instruction){
            //arithmetics
                case 12://ADD xy
                    rw = rw + operand;
                    break;
                case 13://SUB xy
                    rw = rw - operand;
                    break;
                case 14://MUL xy
                    rw = rw * operand;
                    break;
                case 15://DIV xy
                    rw = rw / operand;
                    break;
            //logical
                case 16://AND xy
                    rw = rw & operand;
                    break;
                case 17://OR xy
                    rw = rw | operand;
                    break;
                case 18://NOT
                    rw = ~rw;
                    break;
            //compare
                case 19://CMP xy
                    CMP(operand);
                    break;
            //data stream
                case 20://LOW xy
                    rw = getFromRAM(command % adressParser, 1);//sets rw
                    break;
                case 21://SAW xy
                    sendToRAM(command % adressParser, rw, 1);//sends rw
                    break;
            //stack
                case 22://PUSH
                    PUSH();
                    break;
                case 23://POP
                    POP();
                    break;
            //jumps
                case 24://JMP
                    ip = rw;
                    break;
                case 25://JE
                    ip = sf == 0? rw : ip;
                    break;
                case 26://JL
                    ip = sf == 1? rw : ip;
                    break;
                case 27://JG
                    ip = sf == 2? rw : ip;
                    break;
                case 28://JLE
                    ip = (sf == 0 || sf == 1)? rw : ip;
                    break;
                case 29://JGE
                    ip = (sf == 0 || sf == 2)? rw : ip;
                    break;
            //in/out and stop process
                case 30://IN
                    //getFromCDevice(0, 0);
                    //Uncomment once with OS
                    if(mode)
                        return;
                    ifr = 32;//cia tarkim yra GET_FROM_CDEVICE bitas
                    mode = true;
                    ip = ift;
                    break;
                case 31://OUT
                    //Uncomment once with OS
                    //sendToCDevice(0, 0);
                    if(mode)
                        return;
                    ifr = 64;//cia tarkim yra SEND_TO_CDEVICE bitas
                    mode = true;
                    ip = ift;
                    break;
                case 32://HLT
                    if(mode)
                        return;
                    ifr = 8;//cia tarkim yra PROCESS_END bitas
                    mode = true;
                    ip = ift;
                default:
                    if(mode)
                        return;
                    ifr = 16;//cia tarkim yra ILLEGAL_INSTRUCTION bitas
                    mode = true;
                    ip = ift;
            }
        }
    }
}

    
    

