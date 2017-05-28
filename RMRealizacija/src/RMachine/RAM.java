package RMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksas
 */
public class RAM implements IRAM{
    
    private final Map<Integer, Integer> memory = new HashMap<>();
    
    public RAM(){
        //simple RM test
//        memory.put(0, 1300);
//        memory.put(1, 68);
//        memory.put(2, 3200);
//        memory.put(3, 3100);
//        memory.put(4, 3400);
//        memory.put(68, 97);
        //full RM test
//        memory.put(0, 1300);
//        memory.put(1, 68);
//        memory.put(2, 1220);
//        memory.put(3, 1111);
//        memory.put(4, 2222);
//        memory.put(5, 3400);
//        memory.put(68, 97);
        //VM test
//        memory.put(0, 1344);
//        memory.put(1, 3100);
//        memory.put(2, 3400);
//        memory.put(20, 0);
//        memory.put(34, 40);
//        memory.put(44, 68);
//        memory.put(100, 3400);
        //2 * 2
//        memory.put(0, 3500);
//        memory.put(1, 2);
//        memory.put(2, 1500);
//        memory.put(3, 68);
//        memory.put(4, 3400);
//        memory.put(68, 2);
        //XCH
//        memory.put(0, 3500);
//        memory.put(1, 66);
//        memory.put(2, 1221);
//        memory.put(3, 0000);
//        memory.put(4, 0001);
//        memory.put(5, 3500);
//        memory.put(6, 7);
//        memory.put(7, 2500);
//        memory.put(8, 3400);
        //Laimonui
//        memory.put(0, 3500);
//        memory.put(1, 65);
//        memory.put(2, 1202);
//        memory.put(3, 0000);
//        memory.put(4, 0001);
////        memory.put(5, 3500);
////        memory.put(6, 7);
////        memory.put(7, 2500);
//        memory.put(8, 3400);

        //VM inf loop
//        memory.put(0, 3500);
//        memory.put(1, 2);
//        memory.put(2, 2500);
//        memory.put(20, 0);
//        memory.put(34, 40);
//        memory.put(44, 68);
//        memory.put(100, 3400);
        //XCH with input and output
//        memory.put( 0, 1202);
//        memory.put( 1, 0);
//        memory.put( 2, 0);
//        memory.put( 3, 3500);
//        memory.put( 4, 49);
//        memory.put( 5, 1220);
//        memory.put( 6, 0);
//        memory.put( 7, 0);
//        memory.put( 8, 1101);
//        memory.put(9, 2600);
//        memory.put(10, 3400);
//        memory.put(49, 3500);
        memory.put(50, 8);
//        memory.put(51, 2500);
        
    }
    
    @Override
    public void save(int address, int value){
        memory.put(address, value);
    }
    @Override
    public int get(int address){
        try{
            return memory.get(address);
        }
        catch(Exception exp){
            return 0;
        }
    }
    @Override
    public String toString(){
        return memory.toString();
    }
}
