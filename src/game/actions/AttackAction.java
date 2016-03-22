package game.actions;

public class AttackAction implements Action {

   @Override
   public int getDuration() {
      return 6;
   }
   
   @Override
   public String toString() {
      return "Attack Action";
   }
}
