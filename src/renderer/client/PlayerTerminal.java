package renderer.client;

import game.entities.Player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Vector;
import main.DataStream;

public class PlayerTerminal {
   private JFrame myFrame;
   private JTextArea in;
   private JTextArea out;
   private Runnable scrollToBottom;
   private PlayerMapDisplay map;
   private PrintWriter writer;
   
   public PlayerTerminal(Player player) throws IOException {
      setupFrame();
      new DataStream(player, setupInput(), setupOutput());
      
   }
   
   public PlayerTerminal(Socket sock) throws IOException {
      setupFrame();
      setupOutputDisplay(sock.getInputStream());
      setupInputBar(sock.getOutputStream());
      
   }
   
   private void setupFrame() throws IOException {
      map = new PlayerMapDisplay();
      in = new JTextArea();
      out = new JTextArea();
      JScrollPane sp = new JScrollPane(out);
      sp.setAutoscrolls(true);
      scrollToBottom = () -> sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      
      myFrame = new JFrame();
      myFrame.setLayout(new BorderLayout());
      myFrame.add(sp, BorderLayout.CENTER);
      myFrame.add(in, BorderLayout.SOUTH);
      myFrame.add(map.getPanel(), BorderLayout.NORTH);
      myFrame.setSize(776, 800);
      myFrame.setTitle("Game!");
      myFrame.setVisible(true);
      myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      Timer t = new Timer();
      t.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            writer.println("look");
            writer.flush();
         }
      }, 20, 20);
   }
   
   private OutputStream setupOutput() throws IOException {
      PipedOutputStream fromPlayer = new PipedOutputStream();
      PipedInputStream toTerminal = new PipedInputStream(fromPlayer);
      
      setupOutputDisplay(toTerminal);
      
      return fromPlayer;
   }
   
   private void setupOutputDisplay(InputStream stream) throws IOException {
      out.setBackground(Color.DARK_GRAY);
      out.setForeground(Color.white);
      out.setEditable(false);
      
      new Thread(() -> {
         Scanner scanner = new Scanner(stream);
         while (scanner.hasNextLine()) {
            try {
               String line = scanner.nextLine();
               if (line.startsWith("MAP:")) {
                  if (line.equals("MAP:BEGIN")) {
                     map.begin();
                  } else if (line.equals("MAP:END")) {
                     map.end();
                     myFrame.repaint();
                  } else {
                     if (line.startsWith("MAP:PAST:")) {
                        String vec = line.substring(9, line.indexOf(":", 10));
                        String rest = line.substring(line.indexOf(":", 10) + 1);
                        if (rest.equals("EMPTY")) {
                           map.putPast(Vector.fromString(vec), null);
                        } else {
                           map.putPast(Vector.fromString(vec), new Entity(rest));
                        }
                     } else {
                        String vec = line.substring(4, line.indexOf(":", 5));
                        String rest = line.substring(line.indexOf(":", 5) + 1);
                        if (rest.equals("EMPTY")) {
                           map.put(Vector.fromString(vec), null);
                        } else {
                           map.put(Vector.fromString(vec), new Entity(rest));
                        }
                     }
                  }
               } else {
                  out.append(line + "\n");
                  Thread.sleep(5);
                  scrollToBottom.run();
                  myFrame.repaint();
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }).start();
   }
   
   private InputStream setupInput() throws IOException {
      PipedOutputStream fromTerminal = new PipedOutputStream();
      PipedInputStream toPlayer = new PipedInputStream(fromTerminal);
      
      setupInputBar(fromTerminal);
      
      return toPlayer;
   }
   
   private void setupInputBar(OutputStream stream) throws IOException {
      in.setBackground(Color.GRAY);
      in.setPreferredSize(new Dimension(10000, 16));
      
      writer = new PrintWriter(stream);
      
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
   }

   public void setName(String name) {
      writer.write("setname " + name);
      writer.flush();
   }
}
