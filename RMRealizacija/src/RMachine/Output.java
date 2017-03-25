package RMachine;

/**
 * @author Aleksas
 */
public class Output implements IOutput{

    @Override
    public void printInt(int data){
        System.out.println((char)data);
    }
}
