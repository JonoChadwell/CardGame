package game.cards;

import game.entities.Player;


public class Trainee extends BasicSoldier {

   public Trainee(Player faction) {
      super(3, 2, 1, 3, 1, faction);
   }
   
   @Override
   public String toString() {
      return "Trainee: " + super.toString();
   }
}
