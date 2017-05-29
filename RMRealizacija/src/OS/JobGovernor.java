package OS;

import RMachine.Procesor;

/**
 * @author Aleksas
 */
public class JobGovernor {
    private final Procesor procesor;
    
    private final Loader loader;
    private final GetPutData getPutData;
    private final GetInput getInput;
    
    //Descriptor
    public int id;
    public String name;
    public int status;
    public final int address;
    //0- nothing
    //1- input
    //2-output
    //3-to disk
    public int requires = 0;
    
    //state
    public int rw;
    private int ip;
    private int sf;
    private int sp;
    public int pageTablePointer;
    //End of state
    //End of descriptor
    
    public JobGovernor(Procesor procesor, Loader loader, GetPutData getPutData, GetInput getInput, int id, String name, int address){
        this.procesor = procesor;
        this.loader = loader;
        this.getInput = getInput;
        this.getPutData = getPutData;
        this.address = address;
        this.id = id;
        this.name = name;
    }
    
    private void interupt(){
        int addressForCDevice;
        switch(procesor.ifr){
            //CODE_SEGMENT_ERROR
            case 1:
                System.out.println("Process " + id + ":" + "CODE_SEGMENT_ERROR");
                status = 3;
                break;
            //TIME_OUT
            case 2:
                status = 0;
                ip--;
                break;
            //DATA_SEGMENT_FAULT
            case 4:
                System.out.println("Process " + id + ":" + "DATA_SEGMENT_FAULT");
                status = 3;
                break;
            //PROCESS_END
            case 8:
                status = 3;
                break;
            //ILLEGAL_INSTRUCTION
            case 16:
                System.out.println("Process " + id + ":" + "ILLEGAL_INSTRUCTION");
                status = 3;
                break;
            //GET_FROM_CDEVICE
            case 32:
                synchronized(getInput){
                    getInput.requests.add(id);
                }
                requires = 1;
                status = 2;
                break;
            //SEND_TO_CDEVICE
            case 64:
                addressForCDevice = pageTablePointer + 10 + rw / 10 + (rw % 10);
                getPutData.requests.put(id, new GetPutRequest(addressForCDevice, 0, false, rw));
                requires = 2;
                status = 2;
                break;
            //SEND_TO_DISK
            case 128:
                addressForCDevice = pageTablePointer + 10 + rw / 10 + (rw % 10);
                getPutData.requests.put(id, new GetPutRequest(addressForCDevice, 1, false, rw));
                requires = 3;
                break;
            //DIVISION_BY_ZERO
            case 256:
                System.out.println("Process " + id + ":" + "DIVISION_BY_ZERO");
                status = 3;
                break;
        }
    }
    
    public void allocate(){
        loader.loadProcess(id);
        status = 1;
        ip = 0;
        rw = 0;
        sf = 0;
        sp = 0;
    }
    
    public void run(){
        status = 0;
        procesor.ip = ip;
        procesor.rw = rw;
        procesor.sf = sf;
        procesor.ti = 10;
        procesor.sp = (short) sp;
        procesor.ifr = 0;
        procesor.ptr = pageTablePointer;
        procesor.mode = false;
        try {
            synchronized(procesor){
                procesor.wait();
            }
        } catch (InterruptedException ex) {
            System.out.println("JobGovernor failed while waiting for procesor");
        }
        ip = procesor.ip - 1;
        rw = procesor.rw;
        sf = procesor.sf;
        sp = procesor.sp - 1;
        interupt();
    }
    
    @Override
    public String toString(){
        return "{id:" + id + ", name:" + name + ", status:" + status + " requires:" + requires + "}";
    }
}
