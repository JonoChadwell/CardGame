package game.actions;

public class PlaceAction implements Action {
   private final int index;
   private final int x;
   private final int y;

   public PlaceAction(int index, int x, int y) {
      this.index = index;
      this.x = x;
      this.y = y;
   }

   public int getIndex() {
      return index;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   @Override
   public int getDuration() {
      return 6;
   }

   @Override
   public String toString() {
      return "Place Action: index " + index + " at (" + x + ", " + y + ")";
   }
}
