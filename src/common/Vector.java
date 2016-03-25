package common;


public class Vector {
   private final int x;
   private final int y;
   
   public Vector(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public int getX() {
      return x;
   }
   
   public int getY() {
      return y;
   }
   
   public double distance(Vector other) {
      return Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y));
   }
   
   @Override
   public int hashCode() {
      return x + y << 16;
   }
   
   @Override
   public boolean equals(Object other) {
      return
            this.getClass() == other.getClass() &&
            this.x == ((Vector) other).x &&
            this.y == ((Vector) other).y;
   }
   
   public Location apply(Location start) {
      return new Location(start.getRow() - y, start.getCol() + x);
   }
   
   @Override
   public String toString() {
      return "vector " + x + " " + y;
   }
   
   public static Vector fromString(String vec) {
      if (vec.matches("vector -*\\d* -*\\d*")) {
         String parts[] = vec.split(" ");
         return new Vector(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
      } else {
         throw new NumberFormatException("Expected vector, got \"" + vec + "\"");
         
      }
   }
}
