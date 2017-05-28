package RMachine;

/**
 * @author Aleksas
 */
public class MockProcesor {
    public short ch; //kanalų 2B
    public short inputChannel; //inputo 2B
    public int rw; //darbinis 4B 
    
    private ChannelDevice cdevice;
    
    public MockProcesor(){
        ch = 0;
        inputChannel = 0;
        rw = 0;
    }
    
    public void init(ChannelDevice cdevice){
        this.cdevice = cdevice;
        //this.cdevice.setProcesor(this);
        cdevice.start();
    }
    
    @SuppressWarnings("empty-statement")
    public void test(){
        cdevice.setCDevice(0, 0, 0, 2);
        inputChannel = 1;
        ch = 1;
        while(inputChannel != 3);
        inputChannel = 0;
        System.out.println("from procesor rw = " + rw);
        ch = -1;
        System.out.println("procesor is out");
    }
}
