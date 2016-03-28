package main;

import client.PlayerTerminal;
import game.Game;
import game.entities.Player;
import server.DisplayManager;

public class Main {
   public static void main(String args[]) throws Exception {
      Player playerOne = new Player();
      Player playerTwo = new Player();
      playerOne.setName("Commie");
      playerTwo.setName("Americo");
      Game game = new Game(playerOne, playerTwo);
      game.startGame();

      new PlayerTerminal(playerOne);
      new PlayerTerminal(playerTwo);
   }
}