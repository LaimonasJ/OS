/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

/**
 *
 * @author Aleksas
 */
public interface IRAM {
    public void save(int address, int value);
    public int get(int address);
}
