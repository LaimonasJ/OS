/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMachine;

/**
 *
 * @author vytau
 */
public class ChannelDevice {
    
public int sb, db, st, dt;
RealMachine rm;

public ChannelDevice(RealMachine aThis) {
        sb = 1;
        db = 9;
        st = 1;
        dt = 1;
        rm = aThis;
    }

public void run() {
    
}
}
