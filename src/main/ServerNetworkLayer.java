package main;

import game.Game;
import game.entities.Player;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import server.DisplayManager;

public class ServerNetworkLayer {
   private ServerSocket server;
   private LinkedList<Socket> pending = new LinkedList<>();
   private LinkedList<Game> ongoing = new LinkedList<>();

   public static void main(String args[]) throws Exception {
      new ServerNetworkLayer();
   }

   public ServerNetworkLayer() throws Exception {
      server = new ServerSocket(1400);
      new Thread(() -> checkForClients()).start();
      System.out.println("Listening for clients");
   }

   public void checkForClients() {
      while (true) {
         try {
            Socket client = server.accept();
            System.out.println("Accepted client");
            pending.add(client);
            if (pending.size() >= 2) {
               Socket a = pending.removeFirst();
               Socket b = pending.removeFirst();

               Player playerA = new Player();
               Player playerB = new Player();
               
               Game game = new Game(playerA, playerB);
               game.startGame();
               ongoing.add(game);

               new DataStream(playerA, a.getInputStream(), a.getOutputStream());
               new DataStream(playerB, b.getInputStream(), b.getOutputStream());
            }

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
