package game;

import game.entities.BreakableWall;
import game.entities.Player;
import game.entities.Wall;
import game.grid.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
   private Grid grid;
   private List<Player> players;
   private Timer gameClock;
   
   private static final int GRID_SIZE = 12;
   private static final double WALL_DENSITY = 0.2;

   public Game(Player a, Player b) {
      grid = new Grid(GRID_SIZE);
      this.players = new ArrayList<>();
      players.add(a);
      players.add(b);
      grid.place(3, GRID_SIZE - 4, a);
      grid.place(GRID_SIZE - 4, 3, b);
      for (int row = 0; row < GRID_SIZE; row++) {
         for (int col = 0; col < GRID_SIZE; col++) {
            if (row == 0 || col == 0 || row == GRID_SIZE - 1 || col == GRID_SIZE - 1) {
               grid.place(row, col, new Wall());
            } else if (Math.random() < WALL_DENSITY) {
               try {
                  grid.place(row, col, new BreakableWall());
               } catch (GameException ex) {
                  // ignore failure to place walls on occupied squares
               }
            }
         }
      }
   }

   public void startGame() {
      gameClock = new Timer();
      gameClock.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            performGameTick();
         }
      }, 1000, 1000);
   }
   
   private void performGameTick() {
      grid.performGameTick();
   }
   
   public List<Player> getPlayers() {
      return players;
   }

   public Grid getGrid() {
      return grid;
   }
}
