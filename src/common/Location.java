package common;


public class Location {
   private final int row;
   private final int col;
   
   public Location(int row, int col) {
      this.row = row;
      this.col = col;
   }

   public int getRow() {
      return row;
   }
   
   public int getCol() {
      return col;
   }
   
   public double distance(Location other) {
      return Math.sqrt((row - other.row) * (row - other.row) + (col - other.col) * (col - other.col));
   }
   
   public double distance(double row, double col) {
      return Math.sqrt((this.row - row) * (this.row - row) + (this.col - col) * (this.col - col));
   }
   
   public Location shifted(int x, int y) {
      return new Location(row - y, col + x);
   }
   
   @Override
   public int hashCode() {
      return row + col << 16;
   }
   
   @Override
   public boolean equals(Object other) {
      return
            this.getClass() == other.getClass() &&
            this.row == ((Location) other).row &&
            this.col == ((Location) other).col;
   }
   
   @Override
   public String toString() {
      return "{Location : {row : " + row + ", col : " + col + "}}";
   }

   public Vector difference(Location center) {
      return new Vector(col - center.col,center.row - row);
   }
}
