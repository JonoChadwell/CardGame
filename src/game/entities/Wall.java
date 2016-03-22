package game.entities;

import game.actions.Action;
import game.actions.RestAction;

public class Wall implements Entity {

   @Override
   public Action getAction() {
      return new RestAction();
   }
   
   @Override
   public boolean isTransparent() {
      return false;
   }
   
   public String toString() {
      return "A wall";
   }
}
