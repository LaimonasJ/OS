package OS;

import RMachine.Procesor;
import java.util.ArrayList;
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
    private final Map<Integer, JobGovernor> jobGovernors;
    private int running = 0;
    
    public final List<NewProcess> createNew = new ArrayList<>();
    
    public boolean shutdown = false;
    
    public MainProc(Procesor procesor, Loader loader, GetPutData getPutData, GetInput getInput, Map<Integer, JobGovernor> jobGovernors){
        this.jobGovernors = jobGovernors;
        this.procesor = procesor;
        this.loader = loader;
        this.getInput = getInput;
        this.getPutData = getPutData;
    }
    
    private void createProcess(){
        NewProcess inProgress = createNew.get(0);
        createNew.remove(0);
        int id = 2;
        while(jobGovernors.containsKey(id) || id < 2)
            id++;
        jobGovernors.put(id, new JobGovernor(procesor, loader, getPutData, getInput, id, inProgress.name, inProgress.address));
        jobGovernors.get(id).allocate();
    }
    
    private void checkResources(int process){
        switch(jobGovernors.get(process).requires){
            case 0:
                jobGovernors.get(process).status = 1;
            case 1:
                if(getInput.delivered.containsKey(process)){
                    jobGovernors.get(process).rw = (int) getInput.delivered.get(process);
                    synchronized(getInput){
                        getInput.delivered.remove(process);
                    }
                    jobGovernors.get(process).status = 1;
                }
                break;
            case 2:
            case 3:
                if(getPutData.delivered.containsKey(process)){
                    getPutData.delivered.remove(process);
                    jobGovernors.get(process).status = 1;
                }
                break;
        }
    }
    
    private void startProcess(int process){
        running = process;
        jobGovernors.get(running).run();
    }
    
    private void planner(){
        int it = running;
        JobGovernor governor;
        while(true){
            if(jobGovernors.isEmpty() || shutdown)
                break;
            while(!jobGovernors.containsKey(it))
                it = (it + 1) % MAX_PROCESSES;
            governor = jobGovernors.get(it);
            switch(governor.status){
                //running
                case 0:
                //ready
                case 1:
                    startProcess(it);
                    if(it == 4)
                        jobGovernors.remove(it);
                    return;
                //blocked
                case 2:
                    checkResources(it);
                    break;
                //stopped
                case 3:
                    jobGovernors.remove(it);
                    break;
            }
            if(!createNew.isEmpty())
                createProcess();
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
