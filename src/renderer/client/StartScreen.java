package renderer.client;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartScreen {
   private Socket socket;
   public JFrame myFrame;
   public JPanel myPanel;
   
   
   public static void main(String args[]) throws Exception {
      new StartScreen();
   }
   
   public StartScreen() throws Exception {
      myFrame = new JFrame();
      myFrame.setSize(200, 150);
      myFrame.setLayout(new FlowLayout());
      JTextField name = new JTextField();
      name.setPreferredSize(new Dimension(100, 16));
      JTextField server = new JTextField();
      server.setPreferredSize(new Dimension(100, 16));
      JButton connect = new JButton("Connect");
      connect.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               socket = new Socket(server.getText(), 1400);
               new PlayerTerminal(socket).setName(name.getText());
               myFrame.setVisible(false);
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         }
      });
      
      myFrame.add(new JLabel("Name:"));
      myFrame.add(name);
      myFrame.add(new JLabel("Server:"));
      myFrame.add(server);
      myFrame.add(connect);
      myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      myFrame.setVisible(true);
   }
}
