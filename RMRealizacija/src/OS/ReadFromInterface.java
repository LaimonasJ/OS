package OS;

import java.util.Map;

/**
 * @author Aleksas
 */
public class ReadFromInterface extends Thread{
    private final MainProc mainProc;
    private final GetPutData getPutData;
    private final GetInput input;
    private final Map<Integer, JobGovernor> jobGovernors; 
    
    public ReadFromInterface(MainProc mainProc, GetInput input, GetPutData getPutData, Map<Integer, JobGovernor> jobGovernors){
        this.jobGovernors = jobGovernors;
        this.mainProc = mainProc;
        this.getPutData = getPutData;
        this.input = input;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run(){
        boolean endOfList;
        String name, compareTo;
        char symbol;
        int digit;
        
        while(true){
            input.requests.add(0);
            while(!input.delivered.containsKey(0)){
                try {
                    synchronized(input){
                        input.wait();
                    }
                } catch (InterruptedException ex) {
                    System.out.println("Something is wrong with waiting for getInput in readfrominterface");
                }
            }
            name = (String) input.delivered.get(0);
            input.delivered.remove(0);
            
            System.out.println("read from interface got " + name);
            name += "$";
            
            if(name.equals("exit$"))
                break;
            if(name.equals("ls$")){
                System.out.println(jobGovernors.toString());
            }
            
            endOfList = false;
            int it = -1;
            
            while(!endOfList){
                symbol = '0';
                compareTo = "";
                
                while(symbol != '$'){
                    it++;
                    if(symbol == '/'){
                        endOfList = true;
                        break;
                    }
                    
                    getPutData.requests.put(0, new GetPutRequest(it, 1, true));
                    
                    while(!getPutData.delivered.containsKey(0)){
                        try {
                            synchronized(getPutData){
                                getPutData.wait();
                            }
                        } catch (InterruptedException ex) {
                            System.out.println("Something is wrong with waiting for getPutData in readfrominterface");
                        }
                    }

                    digit = getPutData.delivered.get(0);
                    getPutData.delivered.remove(0);
                    symbol = (char)digit;
                    compareTo += symbol;
                }
                it++;
                if(endOfList)
                    break;
                if(!name.equals(compareTo))
                    continue;
                
                getPutData.requests.put(0, new GetPutRequest(it, 1, true));
                    
                while(!getPutData.delivered.containsKey(0)){
                    try {
                        synchronized(getPutData){
                            getPutData.wait();
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Something is wrong with waiting for getPutData in readfrominterface");
                    }
                }
                mainProc.createNew.add(new NewProcess(name, getPutData.delivered.get(0)));
                getPutData.delivered.remove(0);
                //System.out.println("got address = " + address);
                break;
            }
        }
        mainProc.shutdown = true;
        input.shutDown = true;
        getPutData.shutDown = true;
        System.out.println("ReadFromInterface is shutting down...");
    }
}
