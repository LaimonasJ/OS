package OS;

/**
 * @author Aleksas
 */
public class GetPutRequest {
    public int address;
    public int toFrom;
    public int value;
    public boolean get;
    
    public GetPutRequest(int address, int toFrom, boolean get, int value){
        this.address = address;
        this.toFrom = toFrom;
        this.get = get;
        this.value = value;
    }
}
