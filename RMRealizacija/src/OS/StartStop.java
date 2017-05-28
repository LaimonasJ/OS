package OS;

import RMachine.*;

/**
 * @author Aleksas
 */
public class StartStop {
    private final Procesor procesor;
    private final ChannelDevice cdevice;
    private final IRAM ram;
    
    private final Interupt interupt = new Interupt();
    
    private final GetPutData getPutData;
    private final GetInput getInput;
    
    private final MainProc mainProc;
    private final Loader loader;
    private final ReadFromInterface readFromInterface;
    
    public StartStop(Procesor procesor, ChannelDevice cdevice, IRAM ram){
        this.procesor = procesor;
        this.cdevice = cdevice;
        this.ram = ram;
        
        loader = new Loader();
        getPutData = new GetPutData(cdevice, ram, procesor);
        getInput = new GetInput(cdevice, ram, procesor);
        mainProc = new MainProc(procesor, loader, getPutData, getInput);
        readFromInterface = new ReadFromInterface(mainProc, getInput, getPutData);
    }
    
    @SuppressWarnings("empty-statement")
    public void init(){
        getInput.start();
        getPutData.start();
        loader.start();
        mainProc.start();
        readFromInterface.start();
        
//        GetPutData input = (GetPutData)getPutData;
//        while(input.status != 0);
//        System.out.println("StartStop got " + input.get(1, 0));

        try {
            synchronized(readFromInterface){
                readFromInterface.wait();
            }
        } catch (InterruptedException ex) {
            System.out.println("Something is wrong with waiting for readfrom interface in startstop");
        }
        
        try {
            readFromInterface.join();
        } catch (InterruptedException ex) {
            System.out.println("Somethin is wrong with shutting down readFromInterface");
        }

        try {
            mainProc.join();
            loader.join();
        } catch (InterruptedException ex) {
            System.out.println("Could not join one of the system processes");
        }
        System.out.println("OS is shutting down...");
    }
}
