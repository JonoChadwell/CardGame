package game.grid;

import game.GameException;
import game.actions.Action;
import game.actions.AttackAction;
import game.actions.DrawAction;
import game.actions.MoveAction;
import game.actions.PlaceAction;
import game.cards.Card;
import game.entities.Attacker;
import game.entities.Entity;
import game.entities.FactionMember;
import game.entities.Player;
import game.entities.Summoner;

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

import common.Location;
import common.Vector;

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
   
   private static class AttackData {
      public Attacker source;

      public AttackData(Attacker source) {
         this.source = source;
      }
   }

   public void performGameTick() {
      List<MovementData> movements = new ArrayList<>();
      List<SummonData> summons = new ArrayList<>();
      List<AttackData> attacks = new ArrayList<>();

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
               if (ent instanceof Attacker) {
                  attacks.add(new AttackData((Attacker) ent));
                  ent.actionSuceeded(a);
               } else {
                  ent.actionFailed(a, "This unit cannot attack");
               }
            } else if (a instanceof DrawAction) {
               if (ent instanceof Player) {
                  ((Player) ent).drawCard();
               } else {
                  ent.actionFailed(a, "This unit cannot draw");
               }
            } else if (a instanceof PlaceAction) {
               PlaceAction pa = (PlaceAction) a;
               try {
                  Entity toPlace = ((Summoner) ent).getSummon(pa.getIndex());
                  Location target = new Location(loc.getRow() - pa.getY(), loc.getCol() + pa.getX());
                  if (toPlace instanceof Card && target.distance(loc) > ((Card) toPlace).getCastRange()) {
                     throw new GameException("Target too far away");
                  }
                  checkValid(target);
                  ent.actionSuceeded(pa);
                  ((Summoner) ent).summonSucceeded(pa.getIndex());
                  summons.add(new SummonData(target, toPlace));
               } catch (GameException ex) {
                  ent.actionFailed(pa, ex.getMessage());
               }
            }
         } else {
            cooldowns.put(ent, cooldown - 1);
         }
      });
      //TODO: fix collisions
      for (SummonData sd : summons) {
         objects.put(sd.entity, sd.to);
         cooldowns.put(sd.entity, sd.entity.getSummoningDuration());
      }
      //TODO: fix collisions
      for (MovementData md : movements) {
         objects.put(md.entity, md.to);
      }
      for (AttackData ad : attacks) {
         FactionMember af;
         if (ad.source instanceof FactionMember) {
            af = (FactionMember) ad.source;
         } else {
            af = null;
         }
         getEntitiesInArea((Entity) ad.source, ad.source.getAttackRange()).forEach(entity -> {
            if (ad.source != entity && (af == null || !af.hasSameFaction(entity))) {
               entity.takeDamage(ad.source.getAttackDamage());
            }
         });
      }
      removeDeadObjects();
      updatePlayerData();
      if (tickEndCallback != null) {
         tickEndCallback.run();
      }
   }
   
   private void removeDeadObjects() {
      objects.entrySet().forEach(entry -> {
         if (entry.getKey().isDead() && entry.getKey() instanceof FactionMember) {
            FactionMember fm = (FactionMember) entry.getKey();
            fm.getFactionOwner().getSlainAllies().add(fm);
         }
      });
      objects.entrySet().removeIf(entry -> entry.getKey().isDead());
   }
   
   private void updatePlayerData() {
      Map<Player, Map<Vector, Entity>> visMap = new HashMap<>();
      objects.forEach((ent, loc) -> {
         if (ent instanceof FactionMember) {
            Player faction = ((FactionMember) ent).getFactionOwner();
            Set<FactionMember> members = faction.getAllies();
            if (!members.contains(ent)) {
               members.add((FactionMember) ent);
            }
            if (objects.containsKey(faction)) {
               Location playerLoc = objects.get(faction);
               Map<Vector, Entity> entVis = translate(getVisibleEntities(ent), playerLoc);
               Map<Vector, Entity> playersMap = visMap.putIfAbsent(faction, entVis);
               if (playersMap != null) {
                  playersMap.putAll(entVis);
               }
            }
         }
      });
      visMap.forEach((player, map) -> player.setVision(map));
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
      return objects.entrySet().stream().filter((entry) -> entry.getKey() != center && objects.get(center).distance(entry.getValue()) <= radius)
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
}
