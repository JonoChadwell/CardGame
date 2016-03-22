package main;

import game.Card;
import game.Player;
import game.actions.Action;
import game.actions.MoveAction;
import game.actions.PlaceAction;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class PlayerStream {
   private Player player;
   private Scanner input;
   private volatile PrintStream output;
   
   public PlayerStream(Player player, InputStream input, OutputStream output) {
      this.player = player;
      this.input = new Scanner(input);
      this.output = new PrintStream(output);
      PrintWriter writer = new PrintWriter(this.output);
      player.setOutput(writer);
      new Thread(() -> run()).start();
   }
   
   private void run() {
      while(input.hasNextLine()) {
         String line = input.nextLine();
         try {
            Scanner lineScanner = new Scanner(line);
            String first = lineScanner.next();
            if (first.equalsIgnoreCase("move") || first.equalsIgnoreCase("m")) {
               String dir = lineScanner.next();
               MoveAction ma;
               if (dir.equalsIgnoreCase("north") || dir.equalsIgnoreCase("n")) {
                  write("move north queued");
                  ma = new MoveAction(0, 1);
               } else if (dir.equalsIgnoreCase("west") || dir.equalsIgnoreCase("w")) {
                  write("move west queued");
                  ma = new MoveAction(-1, 0);
               } else if (dir.equalsIgnoreCase("east") || dir.equalsIgnoreCase("e")) {
                  write("move east queued");
                  ma = new MoveAction(1, 0);
               } else if (dir.equalsIgnoreCase("south") || dir.equalsIgnoreCase("s")) {
                  write("move south queued");
                  ma = new MoveAction(0, -1);
               } else {
                  write("Move command invalid");
                  continue;
               }
               if (lineScanner.hasNextInt()) {
                  int count = lineScanner.nextInt();
                  for (;count > 0; count--) {
                     player.queueAction(ma);
                  }
               } else {
                  player.queueAction(ma);
               }
            } else if (first.equalsIgnoreCase("queue") || first.equalsIgnoreCase("q")) {
               if (player.getQueue().isEmpty()) {
                  write("No actions queued");
               } else {
                  StringBuilder queueText = new StringBuilder();
                  queueText.append("Queue:\n");
                  for (Action a : player.getQueue()) {
                     queueText.append("   " + a + "\n");
                  }
                  write(queueText.toString());
               }
            } else if (first.equalsIgnoreCase("status") || first.equalsIgnoreCase("s")) {
               write("Deck size: " + player.getDeckSize());
               if (player.getHand().isEmpty()) {
                  write("Empty Hand");
               } else {
                  StringBuilder handText = new StringBuilder();
                  handText.append("Hand:\n");
                  int i = 0;
                  for (Card c : player.getHand()) {
                     handText.append("   " + i++ + " " + c + "\n");
                  }
                  write(handText.toString());
               }
            } else if (first.equalsIgnoreCase("place") || first.equalsIgnoreCase("p")) {
               int card = lineScanner.nextInt();
               int x = lineScanner.nextInt();
               int y = lineScanner.nextInt();
               PlaceAction pa = new PlaceAction(card, x, y);
               player.queueAction(pa);
               write("queued: " + pa);
            } else if (first.equalsIgnoreCase("look") || first.equalsIgnoreCase("l")) {
               output.println("Visible Squares:");
               player.getVision().forEach((loc, ent) -> {
                  if (ent == null) {
                     output.println("   " + loc + " : empty");
                  } else {
                     output.println("   " + loc + " : " + ent);
                  }
               });
               output.flush();
            } else if (first.equalsIgnoreCase("help") || first.equalsIgnoreCase("h")) {
               write("<Game Description>"
                     + "Commands:\n"
                     + "   help (h): this dialog\n"
                     + "   status (s): show player status\n"
                     + "   queue (q): show current action queue\n"
                     + "   move (m) <north (n), south (s), west (w), east (e)>: move player\n"
                     + "   place (p) <card, x, y>: play card #<card> from your hand (<x>, <y>) units from your character");
                     
            } else {
               write("Command \"" + first + "\" not recognized");
            }
            lineScanner.close();
            continue;
         } catch (Exception ex) {}
         write("Input error on: " + line);
      }
      input.close();
   }
   
   private void write(String string) {
      output.println(string);
      output.flush();
   }
}
