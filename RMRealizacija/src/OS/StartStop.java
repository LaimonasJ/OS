package OS;

import RMachine.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksas
 */
public class StartStop extends Thread{
    private final Procesor procesor;
    
    private final GetPutData getPutData;
    private final GetInput getInput;
    
    private final MainProc mainProc;
    private final Loader loader;
    private final ReadFromInterface readFromInterface;
    private final Map<Integer, JobGovernor> jobGovernors = new HashMap<>(); 
    
    public StartStop(Procesor procesor, ChannelDevice cdevice, IRAM ram){
        this.procesor = procesor;
        
        getPutData = new GetPutData(cdevice, ram, procesor);
        getInput = new GetInput(cdevice, ram, procesor);
        loader = new Loader(jobGovernors, getPutData, ram);
        mainProc = new MainProc(procesor, loader, getPutData, getInput, jobGovernors);
        readFromInterface = new ReadFromInterface(mainProc, getInput, getPutData, jobGovernors);
    }
    
    @SuppressWarnings("empty-statement")
    @Override
    public void run(){
        getInput.start();
        getPutData.start();
        mainProc.start();
        readFromInterface.start();
        
        try {
            readFromInterface.join();
        } catch (InterruptedException ex) {
            System.out.println("Somethin is wrong with shutting down readFromInterface");
        }

        try {
            mainProc.join();
            getInput.join();
            getPutData.join();
        } catch (InterruptedException ex) {
            System.out.println("Could not join one of the system processes");
        }
        System.out.println("OS is shutting down...");
        procesor.shutDown = true;
        procesor.mode = false;
    }
}
