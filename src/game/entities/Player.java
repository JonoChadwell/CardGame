package game.entities;

import game.GameException;
import game.actions.Action;
import game.actions.RestAction;
import game.cards.Card;
import game.cards.Deck;
import game.cards.Hand;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Vector;

public class Player implements Entity, Summoner, FactionMember {
   private Hand hand;
   private Deck deck;
   private PrintWriter output;
   private String name;
   private Map<Vector, Entity> vision = new HashMap<>();
   private LinkedList<Card> burnt = new LinkedList<>();
   private LinkedList<Card> drawn = new LinkedList<>();
   private LinkedList<Action> pendingActions = new LinkedList<>();
   private LinkedList<FactionMember> slainAllies = new LinkedList<>();
   private Set<FactionMember> allies = new HashSet<>();
   private boolean living = true;

   public Player(String name) {
      this.name = name;
      deck = Deck.buildRandomDeck(this);
      hand = new Hand();
      for (int i = 0; i < Hand.HAND_STARTING_SIZE; i++) {
         hand.add(deck.draw());
      }
   }

   public void queueAction(Action a) {
      pendingActions.add(a);
   }

   @Override
   public Action getAction() {
      if (!pendingActions.isEmpty()) {
         Action action = pendingActions.removeFirst();
         write("Performing Action: " + action);
         return action;
      } else {
         // write("No action queued, passing turn");
         return new RestAction();
      }
   }

   public void setOutput(PrintWriter output) {
      this.output = output;
   }

   private void write(String message) {
      if (output != null) {
         output.println(message);
         output.flush();
      }
   }

   public int getDeckSize() {
      return deck.size();
   }

   public Hand getHand() {
      return hand;
   }

   @Override
   public void actionSuceeded(Action a) {
      write("Action Sucessful: " + a);
   }

   @Override
   public void actionFailed(Action a) {
      write("Action Failed: " + a);
   }

   @Override
   public void actionFailed(Action a, String reason) {
      write("Action Failed: " + a + " reason: " + reason);
   }

   @Override
   public String toString() {
      return "Player " + name;
   }

   public List<Action> getQueue() {
      return new ArrayList<>(pendingActions);
   }
   
   public void clearQueue() {
      pendingActions = new LinkedList<>();
   }

   @Override
   public Entity getSummon(int index) {
      try {
         return (Entity) hand.get(index);
      } catch (RuntimeException ex) {
         throw new GameException("Unable to summon card " + index);
      }
   }

   @Override
   public void summonSucceeded(int index) {
      hand.remove(index);
   }

   @Override
   public Player getFactionOwner() {
      return this;
   }

   public void setVision(Map<Vector, Entity> vis) {
      vision = vis;
   }

   public Map<Vector, Entity> getVision() {
      return vision;
   }

   public String getName() {
      return name;
   }

   @Override
   public boolean isDead() {
      return !living;
   }

   @Override
   public void takeDamage(int amount) {
      while (amount-- > 0) {
         if (deck.size() > 0) {
            burnt.add(takeTopCard());
         } else {
            living = false;
            break;
         }
      }
   }

   public Set<Card> clearBurntCards() {
      Set<Card> rtn = new HashSet<>();
      while(!burnt.isEmpty()) {
         rtn.add(burnt.removeFirst());
      }
      return rtn;
   }
   
   public Set<Card> clearDrawnCards() {
      Set<Card> rtn = new HashSet<>();
      while(!drawn.isEmpty()) {
         rtn.add(drawn.removeFirst());
      }
      return rtn;
   }

   public List<FactionMember> getSlainAllies() {
      return slainAllies;
   }

   public Set<FactionMember> clearSlainAllies() {
      Set<FactionMember> rtn = new HashSet<>();
      while(!slainAllies.isEmpty()) {
         rtn.add(slainAllies.removeFirst());
      }
      return rtn;
   }
   
   public Set<FactionMember> getAllies() {
      allies.removeIf(ally -> ally.isDead());
      return allies;
   }
   
   public void drawCard() {
      Card toDraw = takeTopCard();
      hand.add(toDraw);
      drawn.add(toDraw);
   }
   
   private Card takeTopCard() {
      return deck.remove(deck.size() - 1);
   }
}
