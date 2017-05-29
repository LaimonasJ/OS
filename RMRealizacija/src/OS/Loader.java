package OS;

import RMachine.IRAM;
import java.util.Map;

/**
 * @author Aleksas
 */
public class Loader{
    
    private final IRAM ram;
    private final Map<Integer, JobGovernor> jobGovernors;
    private final GetPutData getPutData;
    
    private final int pageTables = 100;
    private final int userMemory = 3100;
    
    public Loader(Map<Integer, JobGovernor> jobGovernors, GetPutData getPutData, IRAM ram){
        this.ram = ram;
        this.jobGovernors = jobGovernors;
        this.getPutData = getPutData;
    }
    
    public void loadProcess(int processNr){
        int newPageTable = pageTables;
        int counter = 0;
        while(counter < jobGovernors.size() - 1){
            counter = 0;
            newPageTable += 30;
            for(JobGovernor descriptor :jobGovernors.values()){
                if(descriptor.pageTablePointer == newPageTable && descriptor.id != processNr)
                    break;
                counter++;
            }
        }
        jobGovernors.get(processNr).pageTablePointer = newPageTable;
        int pageNr = (newPageTable - pageTables) / 30;
        int pageIt = pageNr * 300 + userMemory;
        for(int it = newPageTable; it != newPageTable + 30; it++){
            ram.save(it, pageIt);
            pageIt += 10;
        }
        pageIt = pageNr * 300 + userMemory;
        
        int program = jobGovernors.get(processNr).address;
        int data = 0, ite = program - 1;
        
        while((char)data != '/'){
            ite++;

            getPutData.requests.put(1, new GetPutRequest(ite, 1, true, 0));

            while(!getPutData.delivered.containsKey(1)){
                try {
                    synchronized(getPutData){
                        getPutData.wait();
                    }
                } catch (InterruptedException ex) {
                    System.out.println("Something is wrong with waiting for getPutData in readfrominterface");
                }
            }

            data = getPutData.delivered.get(1);
            getPutData.delivered.remove(1);
            ram.save(pageIt + ite - program, data);
        }
    }
}
