package game.actions;

public class MoveAction implements Action {
   private final int x;
   private final int y;
   
   public MoveAction(int x, int y) {
      this.x = x;
      this.y = y;
   }

   @Override
   public int getDuration() {
      return 3;
   }
   
   public int getX() {
      return x;
   }
   
   public int getY() {
      return y;
   }
   
   @Override
   public String toString() {
      return "Move Action <" + x + "," + y + ">";
   }
}
