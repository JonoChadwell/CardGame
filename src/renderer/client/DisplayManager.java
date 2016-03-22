package renderer.client;

import game.Game;

public class DisplayManager {
   private Game game;
   private GridDisplay gd;

   public DisplayManager(Game game) {
      this.game = game;
      gd = new GridDisplay(game);
   }
   
   public boolean isAlive() {
      return gd.isAlive();
   }
}
