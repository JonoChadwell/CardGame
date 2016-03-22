package renderer.server;

import game.entities.Player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import main.PlayerStream;

public class PlayerTerminal {
   private JFrame myFrame;
   private JTextArea in;
   private JTextArea out;
   private Runnable scrollToBottom;
   Player player;
   
   public PlayerTerminal(Player player) throws IOException {
      in = new JTextArea();
      out = new JTextArea();
      this.player = player;
      JScrollPane sp = new JScrollPane(out);
      sp.setAutoscrolls(true);
      scrollToBottom = () -> sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      
      new PlayerStream(player, setupInput(), setupOutput());
      
      myFrame = new JFrame();
      myFrame.setLayout(new BorderLayout());
      myFrame.add(sp, BorderLayout.CENTER);
      myFrame.add(in, BorderLayout.SOUTH);
      myFrame.setSize(600, 400);
      myFrame.setTitle(player.toString());
      myFrame.setVisible(true);
   }
   
   private OutputStream setupOutput() throws IOException {
      out.setBackground(Color.DARK_GRAY);
      out.setForeground(Color.white);
      out.setEditable(false);
      
      PipedOutputStream fromPlayer = new PipedOutputStream();
      PipedInputStream toTerminal = new PipedInputStream(fromPlayer);
      
      new Thread(() -> {
         Scanner scanner = new Scanner(toTerminal);
         while (scanner.hasNextLine()) {
            out.append(scanner.nextLine() + "\n");
            try {
               Thread.sleep(5);
            } catch (InterruptedException ex) {
               throw new RuntimeException(ex);
            }
            scrollToBottom.run();
         }
      }).start();
      
      return fromPlayer;
   }
   
   private InputStream setupInput() throws IOException {
      in.setBackground(Color.GRAY);
      
      PipedOutputStream fromTerminal = new PipedOutputStream();
      PipedInputStream toPlayer = new PipedInputStream(fromTerminal);
      PrintWriter writer = new PrintWriter(fromTerminal);
      
      in.addKeyListener(new KeyAdapter() {
         @Override
         public void keyTyped(KeyEvent arg0) {
            if (arg0.getKeyChar() == '\n') {
               writer.print(in.getText());
               writer.flush();
               in.setText("");
            }
         }
      });
      
      return toPlayer;
   }
}
