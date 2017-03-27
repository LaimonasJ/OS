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
      frame = new JFrame("Input");
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

     
      
      feedback = new JTextField(50);
      feedback.setEditable(false);
      updateFeedback();
      
      JTextField txtGuess = new JTextField(1);
      txtGuess.addKeyListener(new KeyAdapter()
      {
         public void keyTyped(KeyEvent e)
         {
            if (txtGuess.getText().length() >= 1) // limit textfield to 3 characters
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
            if (txtGuess.getText().length() > 0)
            {
               in = txtGuess.getText().charAt(0);
               txtGuess.setText("");
               updateFeedback();   
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
      
      
      JPanel panel = new JPanel(new FlowLayout());
      panel.add(keyPress);
      panel.add(txtGuess);
      panel.add(enter);
      panel.add(feedback);
      
      
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
   }
   
   private void updateFeedback()
   {
      feedback.setText("Latest input: " + (in == -1 ? "null" : in));
   }

   @Override
   public int getInt()
   {
      int rez = in;
      in = -1;
      updateFeedback();
      return rez;
      //return (int) input.nextLine().charAt(0);
   }
   
   public void finalize()
   {
      frame.dispose();
   }
}
