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
public class Process {

 
    public int id;
    public int status;
    public int priority = 4;
    public Process father = null;
    public Map<Integer, Resource> createdResources = new HashMap<>();
    public Map<Integer, Resource> ownedResources = new HashMap<>();
    public Map<Integer, Resource> requestedResources = new HashMap<>();
    
       public Process(int id, int status) {
        this.id = id;
        this.status = status;
    }
    
    
    public void createProcess(int id, int status)
    {
        Process newProcess = new Process (id, status);
        newProcess.setFather(this);
    }
    
    public void removeProcess(Process target)
    {
        if (target.getFather()==this)
        {
            for (Resource res : target.ownedResources.values())
            {
                res.getRequests().remove(target.getId());
                res.setAssigned(false);
            }
            target = null;
        }
        else
        {
            System.out.println("Attempted to delete non-child process by process id: " + id);
        }
    }
    
    public void stopProcess(Process target)
    {
        if (target == this || target.getFather()==this)
        {
            target.setStatus(3);
        }
        
    }
    
    
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Process getFather() {
        return father;
    }

    public void setFather(Process father) {
        this.father = father;
    }

    public Map<Integer, Resource> getCreatedResources() {
        return createdResources;
    }

    public void setCreatedResources(Map<Integer, Resource> createdResources) {
        this.createdResources = createdResources;
    }

    public Map<Integer, Resource> getOwnedResources() {
        return ownedResources;
    }

    public void setOwnedResources(Map<Integer, Resource> ownedResources) {
        this.ownedResources = ownedResources;
    }

    public Map<Integer, Resource> getRequestedResources() {
        return requestedResources;
    }

    public void setRequestedResources(Map<Integer, Resource> requestedResources) {
        this.requestedResources = requestedResources;
    }

  
    
    
    
    
}
