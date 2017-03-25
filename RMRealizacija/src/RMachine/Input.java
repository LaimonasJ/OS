package RMachine;

import java.util.Scanner;

/**
 * @author Aleksas
 */
public class Input implements IInput{
    
    Scanner input = new Scanner(System.in);
    
    @Override
    public int getInt(){
        return (int)input.nextByte();
    }
}
