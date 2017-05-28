package OS;

/**
 * @author Aleksas
 */
public class JobGovernor {
    private final Loader loader;
    private final GetPutData getPutData;
    private final GetInput getInput;
    
    private final int address;
    public int status;
    public int id;
    
    public JobGovernor(Loader loader, GetPutData getPutData, GetInput getInput, int id, int address){
        this.loader = loader;
        this.getInput = getInput;
        this.getPutData = getPutData;
        this.address = address;
        this.id = id;
    }
    
    public void start(){
        
    }
    
    @Override
    public String toString(){
        return "{id:" + id + ", address:" + address + "}";
    }
}
