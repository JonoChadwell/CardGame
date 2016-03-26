package main;

import game.actions.Action;
import game.actions.DrawAction;
import game.actions.MoveAction;
import game.actions.PlaceAction;
import game.cards.Card;
import game.entities.Entity;
import game.entities.FactionMember;
import game.entities.Player;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import common.Vector;

public class DataStream {
   private Player player;
   private Scanner input;
   private volatile PrintStream output;
   private final Map<String, Consumer<String[]>> handlers;

   private Map<String, Consumer<String[]>> buildHandlers() {
      // Move
      Consumer<String[]> move = arg -> {
         if (arg.length <= 1 || arg.length > 3) {
            write("format: move <dir> [count]");
         } else {
            int count = 1;
            MoveAction ma;
            String dir = arg[1];
            switch (dir) {
            case "n":
            case "north":
               write("move north queued");
               ma = new MoveAction(0, 1);
               break;
            case "w":
            case "west":
               write("move west queued");
               ma = new MoveAction(-1, 0);
               break;
            case "s":
            case "south":
               write("move south queued");
               ma = new MoveAction(0, -1);
               break;
            case "e":
            case "east":
               write("move east queued");
               ma = new MoveAction(1, 0);
               break;
            default:
               write("format: move <dir> [count]");
               return;
            }
            if (arg.length == 3) {
               try {
                  count = Integer.parseInt(arg[2]);
               } catch (NumberFormatException ex) {
                  write("format: move <dir> [count]");
                  return;
               }
            }
            for (; count > 0; count--) {
               player.queueAction(ma);
            }
         }
      };

      // Help
      Consumer<String[]> help = arg -> {
         write("<Game Description>\n" + "Commands:\n" 
               + "   help (h): this dialog\n" 
               + "   status (s): show player status\n"
               + "   queue (q): show current action queue\n" 
               + "   move (m) <north (n), south (s), west (w), east (e)>: move player\n"
               + "   place (p) <card, x, y>: play card #<card> from your hand (<x>, <y>) units from your character\n"
               + "   look (l) display character vision\n" 
               + "   draw (d) draw a card from your deck");
      };

      // Queue
      Consumer<String[]> queue = arg -> {
         if (arg.length == 1) {
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
         } else if (arg.length == 2 && (arg[1].equalsIgnoreCase("clear") || arg[1].equalsIgnoreCase("c"))) {
            player.clearQueue();
            write("Queue cleared");
         } else {
            write("format: queue (clear/c)");
         }
      };

      // Status
      Consumer<String[]> status = arg -> {
         output.println("Deck size: " + player.getDeckSize());
         if (player.getHand().isEmpty()) {
            output.println("Empty Hand");
         } else {
            output.println("Hand:");
            int i = 0;
            for (Card c : player.getHand()) {
               output.println("   " + i++ + " " + c);
            }
         }
         for (Card card : player.clearBurntCards()) {
            output.println("Card Destroyed: " + card);
         }
         for (Card card : player.clearDrawnCards()) {
            output.println("Card Drawn: " + card);
         }
         for (FactionMember ally : player.clearSlainAllies()) {
            output.println("Ally Destroyed: " + ally);
         }
         output.flush();
      };

      // Place
      Consumer<String[]> place = arg -> {
         if (arg.length == 4) {
            try {
               int card = Integer.parseInt(arg[1]);
               int x = Integer.parseInt(arg[2]);
               int y = Integer.parseInt(arg[3]);
               PlaceAction pa = new PlaceAction(card, x, y);
               player.queueAction(pa);
               write("queued: " + pa);
            } catch (NumberFormatException ex) {
               write("format: place <card> <x> <y>");
            }
         } else {
            write("format: place <card> <x> <y>");
         }
      };

      // Look
      Consumer<String[]> look = arg -> {
         output.println("MAP:BEGIN");
         player.getVision().forEach((loc, ent) -> {
            if (ent == null) {
               output.println("MAP:" + loc + ":EMPTY");
            } else {
               output.println("MAP:" + loc + ":" + ent);
            }
         });
         player.getPastVision().forEach((loc, ent) -> {
            if (ent == null) {
               output.println("MAP:PAST:" + loc + ":EMPTY");
            } else {
               output.println("MAP:PAST:" + loc + ":" + ent);
            }
         });
         
         output.println("MAP:END");
         output.flush();
      };

      // Draw
      Consumer<String[]> draw = arg -> {
         write("Drawing Card");
         player.queueAction(new DrawAction());
      };

      // Allies
      Consumer<String[]> allies = arg -> {
         output.println("Allies:");
         player.getAllies().forEach(ally -> {
            output.println("   " + ally);
         });
         output.flush();
      };
      
      // SetName
      Consumer<String[]> setname = arg -> {
         if (arg.length == 2) {
            player.setName(arg[1]);
         } else {
            write("setname <name>");
         }
      };

      Map<String, Consumer<String[]>> handlers = new HashMap<>();
      handlers.put("move", move);
      handlers.put("m", move);
      handlers.put("help", help);
      handlers.put("h", help);
      handlers.put("queue", queue);
      handlers.put("q", queue);
      handlers.put("status", status);
      handlers.put("s", status);
      handlers.put("place", place);
      handlers.put("p", place);
      handlers.put("look", look);
      handlers.put("l", look);
      handlers.put("draw", draw);
      handlers.put("d", draw);
      handlers.put("allies", allies);
      handlers.put("a", allies);
      handlers.put("setname", setname);

      return handlers;
   }

   public DataStream(Player player, InputStream input, OutputStream output) {
      handlers = buildHandlers();
      this.player = player;
      this.input = new Scanner(input);
      this.output = new PrintStream(output);
      PrintWriter writer = new PrintWriter(this.output);
      player.setOutput(writer);
      new Thread(() -> run()).start();
   }

   private void run() {
      while (input.hasNextLine()) {
         String line = input.nextLine();
         String arg[] = line.split(" ");
         if (handlers.containsKey(arg[0].toLowerCase())) {
            handlers.get(arg[0].toLowerCase()).accept(arg);
         } else {
            write("Command \"" + arg[0] + "\" not recognized");
         }
      }
      input.close();
   }

   private void write(String string) {
      output.println(string);
      output.flush();
   }
}
