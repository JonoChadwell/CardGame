package game.cards;

import game.entities.Player;

public class Heavy extends BasicSoldier {

   public Heavy(Player faction) {
      super(1.1, 1.1, 5, 6, 10, faction);
   }
   
   @Override
   public String toString() {
      return "Heavy: " + super.toString();
   }
}
