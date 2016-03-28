package client;

import java.awt.Color;
import java.awt.Graphics;

public class Entity {
   private String text;

   public Entity(String text) {
      this.text = text;
   }
   
   public String getText() {
      return text;
   }
   
   public void draw(Graphics g, int x, int y, int grid_size) {
      g.setColor(Color.WHITE);
      g.fillRect(x + 1, y + 1, grid_size - 1, grid_size - 1);
      if (text.contains("wall")) {
         g.setColor(Color.BLACK);
         g.fillRect(x + 3, y + 3, grid_size - 5, grid_size - 5);
      } else if (text.startsWith("Player")) {
         g.setColor(Color.BLUE);
         g.fillOval(x + 3, y + 3, grid_size - 5, grid_size - 5);
      } else {
         g.setColor(Color.RED);
         g.fillOval(x + 3, y + 3, grid_size - 5, grid_size - 5);
      }
   }
}
