/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OS;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vyt-pc
 */
public class Resource {
    boolean assigned;
    public int id;
    public String name;
    public Process creator;
    public Map<Integer, Process> requests = new HashMap<>();
    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Process getCreator() {
        return creator;
    }

    public void setCreator(Process creator) {
        this.creator = creator;
    }

    public Map<Integer, Process> getRequests() {
        return requests;
    }

    public void setRequests(Map<Integer, Process> requests) {
        this.requests = requests;
    }
   
}
