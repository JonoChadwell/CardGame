package game;

import game.actions.Action;
import game.actions.RestAction;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Player implements Entity, Summoner, FactionMember {
   private Hand hand;
   private Deck deck;
   private LinkedList<Action> pendingActions = new LinkedList<>();
   private PrintWriter output;
   private String name;
   private Map<Vector, Entity> vision = new HashMap<>();
   
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
         //write("No action queued, passing turn");
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

   public void clearVision() {
      vision.clear();
   }
   
   public void addVision(Map<Vector, Entity> vis) {
      vision.putAll(vis);
   }
   
   public Map<Vector, Entity> getVision() {
      return vision;
   }

   public String getName() {
      return name;
   }
}
