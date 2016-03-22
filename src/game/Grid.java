package game;

import game.actions.Action;
import game.actions.AttackAction;
import game.actions.MoveAction;
import game.actions.PlaceAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Grid {
   private final int width;
   private final int height;
   private BiMap<Entity, Location> objects;
   private Map<Entity, Integer> cooldowns;
   private Runnable tickEndCallback;

   public Grid(int size) {
      this(size, size);
   }

   public Grid(int width, int height) {
      this.width = width;
      this.height = height;
      objects = HashBiMap.create();
      cooldowns = new HashMap<>();
   }

   public boolean occupied(Location loc) {
      return objects.containsValue(loc);
   }

   public void place(int i, int j, Entity obj) {
      place(new Location(i, j), obj);
   }

   public void place(Location loc, Entity obj) {
      if (occupied(loc)) {
         throw new GameException("Location already occupied");
      }
      objects.put(obj, loc);
      cooldowns.put(obj, obj.getSummoningDuration());
   }

   public Map<Entity, Location> getObjects() {
      return new HashMap<>(objects);
   }

   private void checkValid(Location loc) {
      if (loc.getRow() < 0 || loc.getCol() < 0 || loc.getRow() >= width || loc.getCol() >= height) {
         throw new GameException("Invalid Location");
      } else if (occupied(loc)) {
         throw new GameException("Occupied Location");
      }
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   private static class MovementData {
      public Location to;
      public Entity entity;

      public MovementData(Location to, Entity entity) {
         this.to = to;
         this.entity = entity;
      }
   }

   private static class SummonData {
      public Location to;
      public Entity entity;

      public SummonData(Location to, Entity entity) {
         this.to = to;
         this.entity = entity;
      }
   }

   public void performGameTick() {
      List<MovementData> movements = new ArrayList<>();
      List<SummonData> summons = new ArrayList<>();

      objects.forEach((ent, loc) -> {
         int cooldown = cooldowns.get(ent);
         if (cooldown <= 1) {
            Action a = ent.getAction();
            cooldowns.put(ent, a.getDuration());
            if (a instanceof MoveAction) {
               MoveAction ma = (MoveAction) a;
               Location newLoc = new Location(loc.getRow() - ma.getY(), loc.getCol() + ma.getX());
               try {
                  checkValid(newLoc);
                  movements.add(new MovementData(newLoc, ent));
                  ent.actionSuceeded(a);
               } catch (GameException ex) {
                  ent.actionFailed(a);
               }
            } else if (a instanceof AttackAction) {

            } else if (a instanceof PlaceAction) {
               PlaceAction pa = (PlaceAction) a;
               try {
                  Entity toPlace = ((Summoner) ent).getSummon(pa.getIndex());
                  Location target = new Location(loc.getRow() - pa.getY(), loc.getCol() + pa.getX());
                  if (occupied(target)) {
                     ent.actionFailed(pa, "Location Occupied");
                  } else {
                     ent.actionSuceeded(pa);
                     ((Summoner) ent).summonSucceeded(pa.getIndex());
                     summons.add(new SummonData(target, toPlace));
                  }
               } catch (GameException ex) {
                  ent.actionFailed(pa, ex.getMessage());
               }
            }
         } else {
            cooldowns.put(ent, cooldown - 1);
         }
      });
      for (SummonData sd : summons) {
         objects.put(sd.entity, sd.to);
         cooldowns.put(sd.entity, sd.entity.getSummoningDuration());
      }
      for (MovementData md : movements) {
         objects.put(md.entity, md.to);
      }
      objects.forEach((ent, loc) -> {
         if (ent instanceof FactionMember) {
            Player faction = ((FactionMember) ent).getFactionOwner();
            if (objects.containsKey(faction)) {
               Location playerLoc = objects.get(faction);
               faction.addVision(translate(getVisibleEntities(ent), playerLoc));
            }
         }
      });
      if (tickEndCallback != null) {
         tickEndCallback.run();
      }
   }

   private Map<Vector, Entity> translate(Map<Location, Entity> map, Location center) {
      Map<Vector, Entity> rtn = new HashMap<>();
      map.forEach((loc, ent) -> {
         rtn.put(loc.difference(center), ent);
      });
      return rtn;
   }

   public void addTickEndCallback(Runnable tickEndCallback) {
      if (this.tickEndCallback == null) {
         this.tickEndCallback = tickEndCallback;
      } else {
         final Runnable oldCallback = this.tickEndCallback;
         this.tickEndCallback = () -> {
            oldCallback.run();
            tickEndCallback.run();
         };
      }
   }

   public Location getLocation(Entity entity) {
      return objects.get(entity);
   }

   public List<Entity> getEntitiesInArea(Entity center, double radius) {
      return objects.entrySet().stream().filter((entry) -> entry.getKey() != center && objects.get(center).distance(entry.getValue()) < radius)
            .map((entry) -> entry.getKey()).collect(Collectors.toList());
   }

   private Map<Location, Entity> getVisibleEntities(Entity viewer) {
      double dist = viewer.getViewDistance();
      int bound = (int) Math.ceil(dist);
      Map<Location, Entity> visible = new HashMap<>();
      Location source = objects.get(viewer);
      visible.put(source, viewer);
      double maxView = viewer.getViewDistance();
      for (int r = -bound; r <= bound; r++) {
         for (int c = -bound; c <= bound; c++) {
            castRay(source, r, c, visible, maxView);
         }
      }
      return visible;
   }

   private void castRay(Location source, int r, int c, Map<Location, Entity> visible, double maxView) {
      double dist = Math.sqrt(r * r + c * c);
      double rPart = r / dist;
      double cPart = c / dist;
      Location square = source;
      Set<Location> possible = new HashSet<>();
      double rLoc = source.getRow();
      double cLoc = source.getCol();
      while (square.distance(source) < maxView) {
         rLoc += rPart;
         cLoc += cPart;
         possible.clear();
         possible.add(square.shifted(0, 1));
         possible.add(square.shifted(1, 1));
         possible.add(square.shifted(-1, 1));
         possible.add(square.shifted(0, -1));
         possible.add(square.shifted(1, -1));
         possible.add(square.shifted(-1, -1));
         possible.add(square.shifted(1, 0));
         possible.add(square.shifted(-1, 0));
         final double rWorkaround = rLoc;
         final double cWorkaround = cLoc;
         square = possible.stream().min((a, b) -> Double.compare(a.distance(rWorkaround, cWorkaround), b.distance(rWorkaround, cWorkaround))).get();
         Entity contents = objects.inverse().get(square);
         if (!visible.containsKey(square)) {
            visible.put(square, contents);
         }
         if (contents != null && !contents.isTransparent()) {
            break;
         }
      }
   }

   // private Map<Location, Entity> getVisibleEntities(Entity viewer) {
   // Map<Location, Entity> visible = new HashMap<>();
   // int added = 1;
   // int dist = 0;
   // Location source = objects.get(viewer);
   // visible.put(source, viewer);
   // double maxView = viewer.getViewDistance();
   // while (added > 0) {
   // added = 0;
   // dist += 1;
   // int row, col;
   // col = dist;
   // for (row = -dist; row <= dist; row++) {
   // added += checkLocationVisibility(row, col, source, visible, maxView);
   // }
   // col = -dist;
   // for (row = -dist; row <= dist; row++) {
   // added += checkLocationVisibility(row, col, source, visible, maxView);
   // }
   // row = dist;
   // for (col = -dist + 1; col < dist; col++) {
   // added += checkLocationVisibility(row, col, source, visible, maxView);
   // }
   // row = -dist;
   // for (col = -dist + 1; col < dist; col++) {
   // added += checkLocationVisibility(row, col, source, visible, maxView);
   // }
   // }
   // return visible;
   // }
   //
   // private int checkLocationVisibility(int row, int col, Location source,
   // Map<Location, Entity> visible, double maxView) {
   // Location check = source.shifted(row, col);
   // if (source.distance(check) < maxView) {
   // for (Location loc : getVisibilitySquares(source, check)) {
   // if (visible.containsKey(loc) && (visible.get(loc) == null ||
   // visible.get(loc).isTransparent())) {
   // visible.put(check, objects.inverse().get(check));
   // return 1;
   // }
   // }
   // }
   // return 0;
   // }
   //
   // private Set<Location> getVisibilitySquares(Location cam, Location square)
   // {
   // Set<Location> possible = new HashSet<>();
   // possible.add(square.shifted(0,1));
   // possible.add(square.shifted(1,1));
   // possible.add(square.shifted(-1,1));
   // possible.add(square.shifted(0,-1));
   // possible.add(square.shifted(1,-1));
   // possible.add(square.shifted(-1,-1));
   // possible.add(square.shifted(1,0));
   // possible.add(square.shifted(-1,0));
   // possible.removeIf((loc) -> loc.distance(cam) >= square.distance(cam) -
   // 0.8);
   // return possible;
   // }
}
