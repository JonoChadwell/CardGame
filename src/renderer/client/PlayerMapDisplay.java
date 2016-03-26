package renderer.client;

import game.Game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import common.Vector;

public class PlayerMapDisplay {
   private JPanel myPanel;
   
   private static final int GRID_SIZE = 40;
   
   private Map<Vector, Entity> visible = new HashMap<>();
   private Map<Vector, Entity> pastVisible = new HashMap<>();
   private Map<Vector, Entity> next;
   private Map<Vector, Entity> nextPast;

   @SuppressWarnings("serial")
   public PlayerMapDisplay() {
      myPanel = new JPanel() {
         public void paint(Graphics g) {
            super.paint(g);
            drawPanel(g);
         }
         
         @Override
         public String getToolTipText(MouseEvent me) {
            int x = me.getX() / GRID_SIZE - myPanel.getWidth() / GRID_SIZE / 2;
            int y = -me.getY() / GRID_SIZE + myPanel.getHeight() / GRID_SIZE / 2;
            Vector v = new Vector(x,y);
            Entity ent = visible.get(v);
            if (ent == null) {
               return null;
            } else {
               return ent.getText();
            }
         }
      };
      myPanel.setToolTipText("");
      myPanel.setPreferredSize(new Dimension(0, 600));
   }
   
   public JPanel getPanel() {
      return myPanel;
   }
   
   public void begin() {
      next = new HashMap<>();
      nextPast = new HashMap<>();
   }
   
   public void end() {
      visible = next;
      pastVisible = nextPast;
   }
   
   public void put(Vector loc, Entity ent) {
      next.put(loc, ent);
   }
   
   public void putPast(Vector loc, Entity ent) {
      nextPast.put(loc, ent);
   }
   
   private void drawPanel(Graphics g) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, myPanel.getWidth(), myPanel.getHeight());
      g.setColor(new Color(64,64,64));
      for (int i = 0; i <= myPanel.getWidth() / GRID_SIZE; i++) {
         g.drawLine(i * GRID_SIZE, 0, i * GRID_SIZE, myPanel.getHeight());
      }
      for (int i = 0; i <=  myPanel.getHeight() / GRID_SIZE; i++) {
         g.drawLine(0, i * GRID_SIZE, myPanel.getWidth(), i * GRID_SIZE);
      }
      int x = myPanel.getWidth() / GRID_SIZE / 2;
      int y = myPanel.getHeight() / GRID_SIZE / 2;
      visible.forEach((loc, ent) -> {
         int xPos = (x + loc.getX()) * GRID_SIZE;
         int yPos = (y - loc.getY()) * GRID_SIZE;
         if (ent == null) {
            g.setColor(Color.WHITE);
            g.fillRect(xPos + 1, yPos + 1, GRID_SIZE - 1, GRID_SIZE - 1);
         } else {
            ent.draw(g, xPos, yPos, GRID_SIZE);
         }
      });
      pastVisible.forEach((loc, ent) -> {
         if (!visible.containsKey(loc)) {
            int xPos = (x + loc.getX()) * GRID_SIZE;
            int yPos = (y - loc.getY()) * GRID_SIZE;
            if (ent == null) {
               g.setColor(Color.WHITE);
               g.fillRect(xPos + 1, yPos + 1, GRID_SIZE - 1, GRID_SIZE - 1);
            } else {
               ent.draw(g, xPos, yPos, GRID_SIZE);
            }
            g.setColor(new Color(0,0,0,128));
            g.fillRect(xPos + 1, yPos + 1, GRID_SIZE - 1, GRID_SIZE - 1);
         }
      });
   }
}
