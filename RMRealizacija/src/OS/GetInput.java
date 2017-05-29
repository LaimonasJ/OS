package OS;

import RMachine.ChannelDevice;
import RMachine.IRAM;
import RMachine.Procesor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aleksas
 */
public class GetInput extends Thread{
    
    private final Procesor procesor;
    private final ChannelDevice cdevice;
    private final IRAM ram;
    
    private final int processDataSegment = 11;
    private final int MAX_REQUESTS = 100;
    
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
            System.out.print("process " + it + "<");
            if(it == 0){
                cdevice.setCDevice(0, processDataSegment, 0, 4);
                procesor.inputChannel = 1;
                procesor.ch = 1;
                while(procesor.inputChannel != 0);
                delivered.put(it, cdevice.dataForOS);
                synchronized(this){
                    notify();
                }
            }
            else{
                cdevice.setCDevice(0, processDataSegment, 0, 3);
                procesor.inputChannel = 1;
                procesor.ch = 1;
                while(procesor.inputChannel != 0){
                    try {
                        synchronized(this){
                            wait(50);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("GetInput failed at waiting");
                    }
                }
                delivered.put(it, ram.get(processDataSegment));
            }
            requests.remove(requests.indexOf(it));
        }
        System.out.println("GetInput is shutting down...");
    }
}
