package game.cards;

import game.actions.Action;
import game.actions.AttackAction;
import game.actions.MoveAction;
import game.entities.Player;

public class Scout extends BasicSoldier {

   public Scout(Player faction) {
      super(4, 1.5, 1, 2, 1, faction);
   }
   
   @Override
   public Action getAction() {
      double val = Math.random();
      if (val > 0.8) {
         return new AttackAction();
      } else if (val > 0.6) {
         return new MoveAction(1, 0);
      } else if (val > 0.4) {
         return new MoveAction(0, -1);
      } else if (val > 0.2) {
         return new MoveAction(0, 1);
      } else {
         return new MoveAction(-1, 0);
      }
   }
   
   @Override
   public double getViewDistance() {
      return 7;
   }
   
   @Override
   public String toString() {
      return "Scout: " + super.toString();
   }
}
