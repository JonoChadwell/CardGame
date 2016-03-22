package main;

import game.Game;
import game.Player;
import renderer.client.DisplayManager;
import renderer.server.PlayerTerminal;

public class Main {
   public static void main(String args[]) throws Exception {
      Player playerOne = new Player("Americo");
      Player playerTwo = new Player("Commie");
      Game game = new Game(playerOne, playerTwo);
      new DisplayManager(game);
      game.startGame();
      
      new PlayerTerminal(playerOne);
      new PlayerTerminal(playerTwo);
   }
}
