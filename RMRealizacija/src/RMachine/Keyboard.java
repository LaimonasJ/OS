package RMachine;

import java.util.Scanner;

/**
 * @author Aleksas
 */
public class Keyboard  extends Thread{

    
    private final Scanner scannner = new Scanner(System.in);
    public int status = 0;
    //0 - ready
    //1 - requested
    //2 - busy
    //3 - delivered
    public String data;
    @Override
    @SuppressWarnings("empty-statement")
    public void run(){
        while(status != -1){
            while(status == 0);
            if(status == -1)
                break;
            data = scannner.next();
            if(status == -1)
                break;
            status = 2;
            while(status == 2);
        }
        System.out.println("keyboard is out");
    }
}