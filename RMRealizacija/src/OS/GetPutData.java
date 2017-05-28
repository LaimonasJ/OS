package OS;

import RMachine.ChannelDevice;
import RMachine.IRAM;
import RMachine.Procesor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksas
 */
public class GetPutData extends Thread{
    
    private final Procesor procesor;
    private final ChannelDevice cdevice;
    private final IRAM ram;
    
    private final int processDataSegment = 200;
    private final int MAX_REQUESTS = 100;
    
    public Map<Integer, GetPutRequest> requests = new HashMap<>();
    public Map<Integer, Integer> delivered = new HashMap<>();
    public boolean shutDown = false;
    
    public GetPutData(ChannelDevice cdevice, IRAM ram, Procesor procesor){
        this.procesor = procesor;
        this.cdevice = cdevice;
        this.ram = ram;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run(){
        int it = 0;
        GetPutRequest request;
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
            while(!requests.containsKey(it))
                it = (it + 1) % MAX_REQUESTS;
            
            request = requests.get(it);
            
            if(request.get)
                cdevice.setCDevice(request.address, processDataSegment, request.toFrom, 3);
            else
                cdevice.setCDevice(processDataSegment, request.address, 3, request.toFrom);
                
            procesor.ch = 1;
            while(procesor.ch != 0);
            delivered.put(it, ram.get(processDataSegment));
            requests.remove(it);
            if(it == 0)
                synchronized(this){
                    notify();
                }
        }
        System.out.println("GetPutData is shutting down...");
    }
}
