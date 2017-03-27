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
//        memory.put(0, 1200);
//        memory.put(1, 68);
//        memory.put(2, 3100);
//        memory.put(3, 3000);
//        memory.put(4, 3200);
//        memory.put(68, 97);
        //full RM test
//        memory.put(0, 1300);
//        memory.put(1, 68);
//        memory.put(2, 1220);
//        memory.put(3, 1111);
//        memory.put(4, 1100);
//        memory.put(5, 3300);
//        memory.put(68, 97);
        memory.put(0, 1344);
        memory.put(1, 3200);
        memory.put(2, 3300);
        memory.put(20, 0);
        memory.put(34, 40);
        memory.put(44, 68);
        memory.put(100, 3300);
    }
    
    @Override
    public void save(int address, int value){
        memory.put(address, value);
    }
    @Override
    public int get(int address){
        return memory.get(address);
    }
}
