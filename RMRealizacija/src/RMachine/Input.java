package RMachine;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * @author Aleksas
 */
public class Input implements IInput
{

   private Scanner input;
   private int in;
   private JTextField feedback;
   private JFrame frame;

   public Input()
   {
      in = -1;
      this.input = new Scanner(System.in);
      createFrame();
   }

   private void createFrame()
   {

     
      
      feedback = new JTextField(50);
      feedback.setEditable(false);
      updateFeedback();
      
      JTextField keyField = new JTextField(1);
      keyField.addKeyListener(new KeyAdapter()
      {
         public void keyTyped(KeyEvent e)
         {
            if (keyField.getText().length() >= 1) // limit textfield to 3 characters
            {
               e.consume();
            }
         }
      });
      

      JButton keyPress = new JButton("Enter the following key: ");
      keyPress.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (keyField.getText().length() > 0)
            {
               in = keyField.getText().charAt(0);
               keyField.setText("");
               updateFeedback();   
            }
            else
            {
               in = 10; //10 - new line
               updateFeedback();
            }
         }
      });
      
      
      JTextField intField = new JTextField(11);
      intField.addKeyListener(new KeyAdapter()
      {
         public void keyTyped(KeyEvent e)
         {
            if (intField.getText().length() > 11) // limit textfield to 3 characters
            {
               e.consume();
            }
         }
      });
      
      JButton intSend = new JButton("Enter the following decimal code: "); //2147483647
      intSend.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (intField.getText().length() > 0)
            {
               int temp;
               try{
               temp = Integer.parseInt(intField.getText());
               in = temp;
               intField.setText("");
               updateFeedback();   
               }
               catch(NumberFormatException ex)
               {
                  updateFeedback("Only integers from " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE + " allowed!");
               } 
            }
            else
            {
               in = 10; //10 - new line
               updateFeedback();
            }
         }
      });

      JButton enter = new JButton("Press : \"ENTER\"");
      enter.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            in = 10; //10 - new line
            updateFeedback();
         }
      });
      
      
      JPanel panel1 = new JPanel(new FlowLayout());
      panel1.add(keyPress);
      panel1.add(keyField);
      
      JPanel panel2 = new JPanel(new FlowLayout());
      panel2.add(intSend);
      panel2.add(intField);
      
      JPanel panel3 = new JPanel(new FlowLayout());
      panel2.add(enter);
      
      JPanel panel4 = new JPanel(new FlowLayout());
      panel4.add(feedback);
      
      
      frame = new JFrame("Input");
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.setLayout(new FlowLayout());

      frame.add(panel1);
      frame.add(panel2);
      frame.add(panel3);
      frame.add(panel4);
      
     
      frame.pack();
      frame.setVisible(true);
   }
   
   private void updateFeedback()
   {
      updateFeedback("");
   }
   
   private void updateFeedback(String message)
   {
      feedback.setText(message + " Current input: " + (in == -1 ? "null" : in));
   }

   @Override
   public int getInt()
   {
      in = -1;
      updateFeedback("Waiting for input!");
      while(in == -1)
      {
         try
         {
            Thread.sleep(500);
         } catch (InterruptedException ex)
         {}
      }
      int rez = in;
      in = -1;
      updateFeedback("Input sent!");
      return rez;
      //return (int) input.nextLine().charAt(0);
   }
   
   public void finalize()
   {
      frame.dispose();
   }

    @Override
    public boolean getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
