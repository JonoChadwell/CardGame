package game.entities;

public class BreakableWall extends Wall {
   private boolean broken = false;
   
   @Override
   public String toString() {
      return "A cracked wall";
   }
   
   @Override
   public void takeDamage(int amount) {
      if (amount >= 5) {
         broken = true;
      }
   }
   
   @Override
   public boolean isDead() {
      return broken;
   }
}
