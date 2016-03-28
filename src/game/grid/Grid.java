package game.grid;

import game.GameException;
import game.actions.Action;
import game.actions.AttackAction;
import game.actions.DrawAction;
import game.actions.MoveAction;
import game.actions.PlaceAction;
import game.cards.Card;
import game.cards.Unit;
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
   private Map<Player, Map<Location, Entity>> playerMaps = new HashMap<>();

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

   private static class MotionData {
      public Location to;
      public Entity entity;
      public Action action;

      public MotionData(Location to, Entity entity, Action action) {
         this.to = to;
         this.entity = entity;
         this.action = action;
      }
   }

   private static class AttackData {
      public Attacker source;

      public AttackData(Attacker source) {
         this.source = source;
      }
   }

   public void performGameTick() {
      List<MotionData> movements = new ArrayList<>();
      List<MotionData> summons = new ArrayList<>();
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
                  movements.add(new MotionData(newLoc, ent, a));
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
                  if (toPlace instanceof Unit && target.distance(loc) > ((Unit) toPlace).getCastRange()) {
                     throw new GameException("Target too far away");
                  }
                  checkValid(target);
                  ((Summoner) ent).summonSucceeded(pa.getIndex());
                  summons.add(new MotionData(target, toPlace, a));
               } catch (GameException ex) {
                  ent.actionFailed(pa, ex.getMessage());
               }
            }
         } else {
            cooldowns.put(ent, cooldown - 1);
         }
      });
      Map<Location, MotionData> targets = new HashMap<>();
      Set<MotionData> remove = new HashSet<>();
      for (MotionData md : movements) {
         if (targets.containsKey(md.to)) {
            remove.add(targets.get(md.to));
            remove.add(md);
         } else {
            targets.put(md.to, md);
         }
      }
      for (MotionData sd : summons) {
         if (targets.containsKey(sd.to)) {
            remove.add(targets.get(sd.to));
            remove.add(sd);
         } else {
            targets.put(sd.to, sd);
         }
      }
      for (MotionData d : remove) {
         d.entity.actionFailed(d.action, "Action Bounced");
      }
      movements.removeAll(remove);
      summons.removeAll(remove);
      
      for (MotionData md : movements) {
         objects.put(md.entity, md.to);
         md.entity.actionSuceeded(md.action);
      }
      for (MotionData sd : summons) {
         objects.put(sd.entity, sd.to);
         cooldowns.put(sd.entity, sd.entity.getSummoningDuration());
         sd.entity.actionSuceeded(sd.action);
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
            if (!playerMaps.containsKey(faction)) {
               playerMaps.put(faction, new HashMap<>());
            }
            if (objects.containsKey(faction)) {
               Location playerLoc = objects.get(faction);
               Map<Location, Entity> entityVisionRaw = getVisibleEntities(ent);
               Map<Location, Entity> playerPersistantVision = playerMaps.get(faction);
               entityVisionRaw.forEach((loc2, ent2) -> {
                  if (ent2 == null || ent2.saveVision()) {
                     playerPersistantVision.put(loc2, ent2);
                  } else {
                     playerPersistantVision.put(loc2, null);
                  }
               });
               Map<Vector, Entity> entityVisionRelative = translate(entityVisionRaw, playerLoc);
               Map<Vector, Entity> playersMap = visMap.putIfAbsent(faction, entityVisionRelative);
               if (playersMap != null) {
                  playersMap.putAll(entityVisionRelative);
               }
            }
         }
      });
      visMap.forEach((player, map) -> player.setVision(map, translate(playerMaps.get(player), objects.get(player))));
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
