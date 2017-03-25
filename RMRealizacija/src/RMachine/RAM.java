package RMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksas
 */
public class RAM implements IRAM{
    
    private final Map<Integer, Integer> memory = new HashMap<>();
    
    public RAM(){
        memory.put(0, 1100);
        memory.put(1, 68);
        memory.put(2, 3000);
        memory.put(3, 3100);
        memory.put(68, 97);
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
