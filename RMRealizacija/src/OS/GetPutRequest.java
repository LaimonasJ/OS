package OS;

/**
 * @author Aleksas
 */
public class GetPutRequest {
    public int address;
    public int toFrom;
    public boolean get;
    
    public GetPutRequest(int address, int toFrom, boolean get){
        this.address = address;
        this.toFrom = toFrom;
        this.get = get;
    }
}
