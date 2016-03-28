package client;

import game.entities.Player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Vector;
import main.DataStream;

public class PlayerTerminal {
   private JFrame myFrame;
   private JPanel myCenter;
   private JTextArea in;
   private JTextArea out;
   private JTextArea playerStatus;
   private Runnable scrollToBottom;
   private PlayerMapDisplay map;
   private PrintWriter writer;
   private List<Card> hand = new ArrayList<>();
   
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
      myCenter = new JPanel();
      myCenter.setLayout(new BorderLayout());
      myCenter.add(sp, BorderLayout.CENTER);
      myCenter.add(in, BorderLayout.SOUTH);
      myCenter.add(map.getPanel(), BorderLayout.NORTH);
      myFrame.add(myCenter, BorderLayout.CENTER);
      playerStatus = new JTextArea();
      playerStatus.setPreferredSize(new Dimension(300, 0));
      playerStatus.setEditable(false);
      playerStatus.setLineWrap(true);
      playerStatus.setWrapStyleWord(true);
      myFrame.add(playerStatus, BorderLayout.EAST);
      myFrame.setSize(1076, 800);
      myFrame.setTitle("Game!");
      myFrame.setVisible(true);
      myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      Timer t = new Timer();
      t.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            writer.println("updategui");
            writer.flush();
         }
      }, 20, 20);
      JPanel panel = map.getPanel();
      panel.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent me) {
            if (writer != null) {
               if (me.getButton() == MouseEvent.BUTTON3) {
                  int x = me.getX() / PlayerMapDisplay.GRID_SIZE - panel.getWidth() / PlayerMapDisplay.GRID_SIZE / 2;
                  int y = -me.getY() / PlayerMapDisplay.GRID_SIZE + panel.getHeight() / PlayerMapDisplay.GRID_SIZE / 2;
                  EasyPopupMenu menu = new EasyPopupMenu();
                  if (x == 0 && y == 1) {
                     menu.addButton("Move North", () -> {
                        writer.println("m n");
                        writer.flush();
                     });
                  }
                  if (x == 0 && y == -1) {
                     menu.addButton("Move South", () -> {
                        writer.println("m s");
                        writer.flush();
                     });
                  }
                  if (x == 1 && y == 0) {
                     menu.addButton("Move East", () -> {
                        writer.println("m e");
                        writer.flush();
                     });
                  }
                  if (x == -1 && y == 0) {
                     menu.addButton("Move West", () -> {
                        writer.println("m w");
                        writer.flush();
                     });
                  }
                  for (int i = 0; i < hand.size(); i++) {
                     final int loc = i;
                     menu.addButton("Play " + hand.get(i).getName(), () -> {
                        writer.println("place " + loc + " " + x + " " + y);
                        writer.flush();
                     });
                  }
                  menu.addButton("Draw Card", () -> {
                     writer.println("draw");
                     writer.flush();
                  });
                  menu.show(me.getComponent(), me.getX(), me.getY());
               }
            }
         }
      });
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
         List<Card> newHand = new ArrayList<>();
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
               } else if (line.startsWith("HAND:")) {
                  if (line.equals("HAND:BEGIN")) {
                     newHand = new ArrayList<>();
                  } else if (line.equals("HAND:END")) {
                     hand = newHand;
                     StringBuilder statusText = new StringBuilder();
                     for (int i = 0; i < hand.size(); i++) {
                        statusText.append("   Card " + i + ": " + hand.get(i).getText() + "\n");
                     }
                     playerStatus.setText(statusText.toString());
                  } else {
                     newHand.add(new Card(line.substring(5)));
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
         scanner.close();
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
               String msg = in.getText();
               if (msg.equals("clear\n")) {
                  out.setText("");
                  in.setText("");
               } else {
                  writer.print(msg);
                  writer.flush();
                  in.setText("");
               }
            }
            if (arg0.getKeyChar() == '') {
               out.setText("");
            }
            if (arg0.getKeyChar() == '') {
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
