package game.cards;

import game.entities.Player;

public class Trooper extends BasicSoldier {

   public Trooper(Player faction) {
      super(4, 2.3, 3, 4, 5, faction);
   }

   @Override
   public String toString() {
      return "Trooper: " + super.toString();
   }
}
