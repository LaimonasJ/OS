/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;
import java.io.*;

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
    byte sf; //status 1B
    byte mode; //režimo 1B
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
        mode=1; //1 super 0 user? gal keisti į boolean?
        ifr=0;
        ti=0;
        sp=0;
        ch=0;
        
        cdevice = new ChannelDevice();
        cdevice.run();
        
        
        memory = new int[max_blocks][block_size];//išvaloma atmintis
        for (int block = 0; block < memory.length; block++) {
            for (int word = 0; word < memory[block].length; word++) { {
                    memory[block][word] = 0;
                }
            }
        }

        printAllMem();


    }
    
        public void runtime()  {
        String[] cmd;
        //currentVMIndex = 0;

            //this.printAllMem();
            //Scanner sc = new Scanner(System.in);
            //sc.next();
        }
        
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

    
    

