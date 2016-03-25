package main;

import game.Game;
import game.entities.Player;
import renderer.client.PlayerTerminal;
import renderer.server.DisplayManager;

public class Main {
   public static void main(String args[]) throws Exception {
      Player playerOne = new Player("Americo");
      Player playerTwo = new Player("Commie");
      Game game = new Game(playerOne, playerTwo);
      game.startGame();
      
      new PlayerTerminal(playerOne);
      new PlayerTerminal(playerTwo);
   }
}
