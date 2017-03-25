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
    
    private int getCommand(int address){
        synchronized(ram){
            return ram.get(address);
        }
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
    
    private void PUSH(){
        sp++;
        sendValue(sp, 1, 0);
    }
    
    private void POP(){
        getValue(sp, 1, 0);
        waitForData();
        sp--;
    }
    
    private void waitForData(){
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
    
    /**
     *@param adress - used if requesting value from memory
     *@param from:
     * 0 - input/output
     * 1 - RAM
     * 2 - memory
     * 3 - Processor
     * 
     *@return 
     */
    private void getValue(int adress, int from, int track){
        cdevice.setCDevice(3, track, from, 3, adress);
        while(ch != 0)
            waitForData();
        ch = 1;//requested
        System.out.println("Asking for resources");
    }
    
    /**
     *@param value - value to send
     *@param adress - used if requesting value from memory
     *@param to:
     * 0 - input/output
     * 1 - RAM
     * 2 - memory
     * 3 - Processor
     * 
     *@return 
     */
    private void sendValue(int adress, int to, int track){
        cdevice.setCDevice(track, 3, 3, to, adress);
        System.out.println("Waiting for cdevice");
        if(ch != 0)
            waitForData();
        System.out.println("Sending data");
        ch = 1;//requested
    }
    
    private boolean supervizorsInstructions(int command){
        int instruction = command / 10000;//nepamenu koki darom absoliutu adresa :(
        switch(instruction){
        //safe
            case 1://SPT
                sendValue(command % 10000, 1, 2);//sends ptr
                return true;
            case 2://SSP
                sendValue(command % 10000, 1, 5);//sends sp
                return true;
            case 3://SSF
                sendValue(command % 10000, 1, 3);//sends sf
                return true;
            case 4://SIF
                sendValue(command % 10000, 1, 4);//sends ifr
                return true;
        //load
            case 5://LPT
                getValue(command % 10000, 1, 2);//sets ptr
                waitForData();
                return true;
            case 6://LSP
                getValue(command % 10000, 1, 5);//sets sp
                waitForData();
                return true;
            case 7://UIF - unset IFR registra, reiks prirasyt dar viena instrukcija, nes kitaip neisivaizduoju kaip OS gali nuimt blokavima
                ifr = 0;
                return true;
            case 8://LSF
                getValue(command % 10000, 1, 3);//sets sf
                waitForData();
                return true;
            case 9://RUN
                getValue(command % 10000, 1, 1);//sets IP
                waitForData();
                mode = false;
                return true;
            case 10://RTI
                ti = (short)(command % 10000);
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
            command = getCommand(ip);
            ip++;
            
            instruction = command / adressParser;
            
            if(mode){
                if(ifr != 0)
                    break;
                if(instruction < 21){
                    command = command * 100 + getCommand(ip);
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
            if(command / 10000000 == 32){//SET
                command = command * 100 + getCommand(ip);
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
                getValue(command % adressParser, 1, 0);//sets rw
                waitForData();
            }
            switch(instruction){
            //arithmetics
                case 11://ADD xy
                    rw = rw + operand;
                    break;
                case 12://SUB xy
                    rw = rw - operand;
                    break;
                case 13://MUL xy
                    rw = rw * operand;
                    break;
                case 14://DIV xy
                    rw = rw / operand;
                    break;
            //logical
                case 15://AND xy
                    rw = rw & operand;
                    break;
                case 16://OR xy
                    rw = rw | operand;
                    break;
                case 17://NOT
                    rw = ~rw;
                    break;
            //compare
                case 18://CMP xy
                    CMP(operand);
                    break;
            //data stream
                case 19://LOW xy
                    getValue(command % adressParser, 1, 0);//sets rw
                    break;
                case 20://SAW xy
                    sendValue(command % adressParser, 1, 0);//sends rw
                    break;
            //stack
                case 21://PUSH
                    PUSH();
                    break;
                case 22://POP
                    POP();
                    break;
            //jumps
                case 23://JMP
                    ip = rw;
                    break;
                case 24://JE
                    ip = sf == 0? rw : ip;
                    break;
                case 25://JL
                    ip = sf == 1? rw : ip;
                    break;
                case 26://JG
                    ip = sf == 2? rw : ip;
                    break;
                case 27://JLE
                    ip = (sf == 0 || sf == 1)? rw : ip;
                    break;
                case 28://JGE
                    ip = (sf == 0 || sf == 2)? rw : ip;
                    break;
            //in/out and stop process
                case 29://IN
                    getValue(0, 0, 0);
                    break;
                case 30://OUT
                    sendValue(0, 0, 0);
                    break;
                case 31://HLT
                    return;
                default:
                    return;
            }
        }
    }
}

    
    

