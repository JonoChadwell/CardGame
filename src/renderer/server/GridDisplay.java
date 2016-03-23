package renderer.server;

import game.Game;
import game.cards.BasicSoldier;
import game.entities.Entity;
import game.entities.Player;
import game.entities.Wall;
import game.grid.Grid;
import game.grid.Location;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GridDisplay {
   private final Game game;
   private final Grid grid;
   private final JFrame myFrame;
   private final JPanel myPanel;
   private boolean isAlive = true;
   
   private static final int GRID_SIZE = 40;

   @SuppressWarnings("serial")
   public GridDisplay(Game game) {
      this.game = game;
      this.grid = game.getGrid();
      myFrame = new JFrame();
      myPanel = new JPanel() {
         public void paint(Graphics g) {
            super.paint(g);
            drawPanel(g);
         }
      };
      myFrame.setContentPane(myPanel);
      myFrame.setSize(800, 800);
      myFrame.setVisible(true);
      myFrame.setTitle("Shitty Swing GUI");
      myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      myFrame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent arg0) {
            isAlive = false;
         }
      });
      grid.addTickEndCallback(() -> myFrame.repaint());
   }
   
   private void drawPanel(Graphics g) {
      g.setColor(Color.GRAY);
      g.clearRect(0, 0, myPanel.getWidth(), myPanel.getHeight());
      Map<Entity, Location> objs = grid.getObjects();
      g.setColor(Color.BLACK);
      for (int i = 0; i <= grid.getWidth(); i++) {
         g.drawLine(i * GRID_SIZE, 0, i * GRID_SIZE, myPanel.getHeight());
      }
      for (int i = 0; i <= grid.getHeight(); i++) {
         g.drawLine(0, i * GRID_SIZE, myPanel.getWidth(), i * GRID_SIZE);
      }
      for (Map.Entry<Entity, Location> entry : objs.entrySet()) {
         drawEntity(g, entry.getValue().getCol() * GRID_SIZE, entry.getValue().getRow() * GRID_SIZE, entry.getKey());
      }
      game.getPlayers().forEach((player) -> {
         Location center = grid.getLocation(player);
         player.getVision().forEach((vec, ent) -> {
            Color color;
            if (player.getName().equals("Americo")) {
               color = new Color(0, 0, 255, 20);
            } else {
               color = new Color(255, 0, 0, 20);
            }
            Location toDraw = vec.apply(center);
            g.setColor(color);
            g.fillRect(toDraw.getCol() * GRID_SIZE + 1, toDraw.getRow() * GRID_SIZE + 1, GRID_SIZE - 1, GRID_SIZE - 1);
         });
      });
      g.setColor(Color.GRAY);
      g.fillRect(GRID_SIZE * grid.getWidth() + 1, 0, myPanel.getWidth(), myPanel.getHeight());
      g.fillRect(0, GRID_SIZE * grid.getHeight() + 1, myPanel.getWidth(), myPanel.getHeight());
   }
   
   private void drawEntity(Graphics g, int x, int y, Entity e) {
      if (e instanceof Wall) {
         g.setColor(Color.BLACK);
         g.fillRect(x + 3, y + 3, GRID_SIZE - 5, GRID_SIZE - 5);
      } else if (e instanceof Player) {
         g.setColor(Color.BLUE);
         g.fillOval(x + 3, y + 3, GRID_SIZE - 5, GRID_SIZE - 5);
      } else if (e instanceof BasicSoldier) {
         g.setColor(Color.RED);
         g.fillOval(x + 3, y + 3, GRID_SIZE - 5, GRID_SIZE - 5);
      }
   }

   public boolean isAlive() {
      return isAlive;
   }
}
