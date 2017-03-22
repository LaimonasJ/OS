/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author vytau
 */
public class RealMachine {

    private boolean run = true;

    int rw; //darbinis 4B 
    int ip; //instrukcijos 4B
    int ptr; //puslapiavimo 4B
    int ift; //pertraukimų lentelės 4B
    int sf; //status 1B                     mum cia reiks int, nes mano komandose parasyta, kad CMP gali uzsetinti 0,1 arba 2
    boolean mode; //režimo 1B
    short ifr; //pertraukimų 2B
    short ti; //timer 2B
    short sp; //steko 2B
    short ch; //kanalų 2B
    public int[][]memory; //[nr]
    
    
    int max_blocks; //kiek blokų galima sutalpinti į op atmintį?
    int block_size; // numatyta 100 žodžių, žodis 4B int java 4B
    int max_mem_limit; //didžiausias galimas absoliutus adresas;

    private ChannelDevice cdevice;

    public RealMachine(int max_blocks) {
        
        this.max_blocks=max_blocks; 
        block_size=100;
        max_mem_limit=this.max_blocks*block_size;
        
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
        
        cdevice = new ChannelDevice(this);
        cdevice.run();
        
        //Mes turime daryti atminti, kaip atskira klase ir naudosime hashmap'a
        memory = new int[max_blocks][block_size];//išvaloma atmintis
        for (int block = 0; block < memory.length; block++) {
            for (int word = 0; word < memory[block].length; word++) { {
                    memory[block][word] = 0;
                }
            }
        }

        //printAllMem();


    }
    
    private ArrayList<Integer> mockRAM = new ArrayList<>();
    public void prepareMockRAM(){
        mockRAM.add(1101);
        mockRAM.add(23);
        mockRAM.add(3000);
        mockRAM.add(3100);
    }
    
    private void CMP(int value){
        if(rw == value){
            sf = 0;
            return;
        }
        if(rw < value){
            sf = 1;
            return;
        }
        if(rw > value){
            sf = 2;
        }
    }
    
    private void PUSH(){
        sp++;
        sendValue(rw, sp, 1);
    }
    
    private void POP(){
        rw = getValue(sp, 1);
        sp--;
    }
    
    /**
     *@param adress - used if requesting value from memory
     *@param from:
     * 0 - input/output
     * 1 - RAM
     * 2 - memory
     * 
     *@return 
     */
    private int getValue(int adress, int from){
        //Kai bus kanalu irenginys ir Vartotojo/supervizorine atmintis reiks padaryti, kad puslapiu lenteleje surastu absoliutu adresa
        return adress;//Kai bus kanalu irenginys ir Vartotojo/supervizorine atmintis reiks padaryti, kad value paimtu is atminties
        
    }
    
    /**
     *@param value - value to send
     *@param adress - used if requesting value from memory
     *@param to:
     * 0 - input/output
     * 1 - RAM
     * 2 - memory
     * 
     *@return 
     */
    private void sendValue(int value, int adress, int to){
        //sends value, vel gi reik minetu irenginiu (jei kyla klausimu kokia value tai zinokit, kad siunciama is rw)
        System.out.println(value);
    }
    
    /**
     *if IP is invalid sets ift to CODE_SEGMENTATION_FAULT
     */
    private void checkIPValid(){
        //checks for code segmentation error
    }
    
    private boolean supervizorsInstructions(int command){
        int instruction = command / 10000;//nepamenu koki darom absoliutu adresa :(
        switch(instruction){
        //safe
            case 1://SPT
                sendValue(ptr, command % 10000, 1);
                return true;
            case 2://SSP
                sendValue(sp, command % 10000, 1);
                return true;
            case 3://SSF
                sendValue(sf, command % 10000, 1);
                return true;
            case 4://SIF
                sendValue(ift, command % 10000, 1);
                return true;
        //load
            case 5://LPT
                ptr = getValue(command % 10000, 1);
                return true;
            case 6://LSP
                sp = (short)getValue(command % 10000, 1);
                return true;
            case 7://UIF - unset IF registra, reiks prirasyt dar viena instrukcija, nes kitaip neisivaizduoju kaip OS gali nuimt blokavima
                ptr = getValue(command % 10000, 1);
                return true;
            case 8://LSF
                sf = getValue(command % 10000, 1);
                return true;
            case 9://RUN
                ip = getValue(command % 10000, 1);
                mode = false;
                return true;
            case 10://RTI
                ti = (short)(command % 10000);
                return true;
        }
        return false;
    }
    
    public void runtime(){
        int adressParser;
        int command;
        int instruction;
        
        while(true){
            checkIPValid();
            adressParser = 100;
            command = mockRAM.get(ip);//Kai bus kanalu irenginys ir Vartotojo/supervizorine atmintis reiks padaryti, kad koda paimtu is atminties
            ip++;
            
            instruction = command / adressParser;
            
            if(mode){
                if(ift != 0)
                    break;
                if(instruction < 21){
                    command = command * 100 + mockRAM.get(ip);//Kai bus kanalu irenginys ir Vartotojo/supervizorine atmintis reiks padaryti, kad koda paimtu is atminties
                    ip++;
                }
                if(supervizorsInstructions(command))//Jei randam, kad tai supervizoriaus instrukcija, tolimesniu instrukciju paieska nebera svarbi
                    continue;
                adressParser = 10000;
            }
            else{
                if(ti == 0)
                    ift = 2;//cia tarkim yra time out bitas, bet galesim keisti
                ti--;
                if(ift != 0){
                    break;//po kokas break kol nesugalvojom kaip apdorosim interuptus
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
                command = command * 100 + 34;//Kai bus kanalu irenginys ir Vartotojo/supervizorine atmintis reiks padaryti, kad koda paimtu is atminties
                ip ++;//reiks 2 zodziu
                if(command / 1000000 % 10 == 1){
                    rw = -1 * command % 1000000;
                }
                else{
                    rw = command % 1000000;
                }
                continue;
            }
            
            switch(instruction){
            //arithmetics
                case 11://ADD xy
                    rw += getValue(command % adressParser, 1);
                    break;
                case 12://SUB xy
                    rw -= getValue(command % adressParser, 1);
                    break;
                case 13://MUL xy
                    rw *= getValue(command % adressParser, 1);
                    break;
                case 14://DIV xy
                    rw /= getValue(command % adressParser, 1);
                    break;
            //logical
                case 15://AND xy
                    rw &= getValue(command % adressParser, 1);
                    break;
                case 16://OR xy
                    rw |= getValue(command % adressParser, 1);
                    break;
                case 17://NOT
                    rw = ~rw;
                    break;
            //compare
                case 18://CMP xy
                    CMP(getValue(command % adressParser, 1));
                    break;
            //data stream
                case 19://LOW xy
                    rw = getValue(command % adressParser, 1);
                    break;
                case 20://SAW xy
                    sendValue(rw, command % adressParser, 1);
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
                    rw = getValue(0, 0);
                    break;
                case 30://OUT
                    sendValue(rw, 0, 0);
                    break;
                case 31://HLT
                    return;
                default:
                    return;
            }
        }
    }
    
//    public void runtime()  {
//    String[] cmd;
//    //currentVMIndex = 0;
//
//        //this.printAllMem();
//        //Scanner sc = new Scanner(System.in);
//        //sc.next();
//    }

    public int getBlockNr(int absolute)
    {
        return absolute/block_size;
    }
     public int getwordNr(int absolute)
    {
        return absolute%block_size;
    }
     public int toAbsolute(int block, int word)
     {
         return block*block_size+word;
     }

    public void printAllMem() {
    System.out.println("printing all memory");
        for (int i = 0; i < max_blocks; i++) {
            System.out.println("memory block #" + i);
            for (int n = 0; n < block_size; n++) {

                    System.out.print(String.valueOf(memory[i][n]));
                    System.out.print(" ");
            }
            System.out.println("");
        }
    }
         
         
}

    
    

