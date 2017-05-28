package OS;

import RMachine.ChannelDevice;
import RMachine.IRAM;
import RMachine.Procesor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Aleksas
 */
public class GetInput extends Thread{
    
    private final Procesor procesor;
    private final ChannelDevice cdevice;
    private final IRAM ram;
    
    private final int processDataSegment = 400;
    private final int MAX_REQUESTS = 100;
    private final Scanner forSystemProcesses = new Scanner(System.in);
    
    public List<Integer> requests = new ArrayList<>();
    public Map<Integer, Object> delivered = new HashMap<>();
    public boolean shutDown = false;
    
    public GetInput(ChannelDevice cdevice, IRAM ram, Procesor procesor){
        this.procesor = procesor;
        this.cdevice = cdevice;
        this.ram = ram;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run(){
        int it = 0;
        while(!shutDown){
            if(requests.isEmpty()){
                try {
                    synchronized(this){
                        wait(50);
                    }
                    if(shutDown)
                        break;
                } catch (InterruptedException ex) {
                    System.out.println("GetInput could not wait properly");
                }
                continue;
            }
            while(!requests.contains(it))
                it = (it + 1) % MAX_REQUESTS;
            if(it == 0){
                delivered.put(it, forSystemProcesses.next());
                synchronized(this){
                    notify();
                }
            }
            else{
                cdevice.setCDevice(0, processDataSegment, 0, 3);
                procesor.ch = 1;
                while(procesor.inputChannel != 0);
                delivered.put(it, ram.get(processDataSegment));
            }
            requests.remove(it);
        }
        System.out.println("GetInput is shutting down...");
    }
}
