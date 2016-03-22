package game.actions;

public class RestAction implements Action {

   @Override
   public int getDuration() {
      return 1;
   }
   
   @Override
   public String toString() {
      return "Pass";
   }
}
