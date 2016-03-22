package game.actions;

public class DrawAction implements Action {

   @Override
   public int getDuration() {
      return 6;
   }

   @Override
   public String toString() {
      return "Draw Action";
   }
}