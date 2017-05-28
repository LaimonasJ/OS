package OS;

import RMachine.Procesor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aleksas
 */
public class MainProc extends Thread{
    private final Procesor procesor;
    
    private final Loader loader;
    private final GetPutData getPutData;
    private final GetInput getInput;
    
    private final int MAX_PROCESSES = 100;
    private final Map<Integer, JobGovernor> jobGovernors = new HashMap<>();
    private int running = 0;
    
    public final List<Integer> createNew = new ArrayList<>();
    
    public boolean shutdown = false;
    
    public MainProc(Procesor procesor, Loader loader, GetPutData getPutData, GetInput getInput){
        this.procesor = procesor;
        this.loader = loader;
        this.getInput = getInput;
        this.getPutData = getPutData;
    }
    
    private void createProcess(){
        int inProgress = createNew.get(0);
        createNew.remove(0);
        int id = 0;
        while(jobGovernors.containsKey(id))
            id++;
        jobGovernors.put(id, new JobGovernor(loader, getPutData, getInput, id, inProgress));
        System.out.println("processes:" + jobGovernors.toString());
    }
    
    private void checkResources(int process){
        
    }
    
    private void startProcess(int process){
        
    }
    
    private void planner(){
        int it = running;
        JobGovernor governor;
        while(true){
            if(jobGovernors.isEmpty())
                break;
            while(!jobGovernors.containsKey(it))
                it = (it + 1) % MAX_PROCESSES;
            governor = jobGovernors.get(it);
            switch(governor.status){
                case 0:
                    startProcess(it);
                    return;
                case 1:
                    checkResources(it);
                    break;
                case 2:
                    jobGovernors.remove(it);
                    break;
            }
        }
    }
    
    @Override
    public void run(){
        while(!shutdown){
            if(!createNew.isEmpty())
                createProcess();
            if(!jobGovernors.isEmpty())
                planner();
        }
        System.out.println("MainProc is shutting down...");
    }
}
