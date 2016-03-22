package game.entities;

public interface FactionMember extends Entity {
   public Player getFactionOwner();
   public default boolean hasSameFaction(Entity other) {
      if (other instanceof FactionMember) {
         return this.getFactionOwner() == ((FactionMember) other).getFactionOwner();
      } else {
         return false;
      }
   }
}
