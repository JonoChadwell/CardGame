package game.cards;

import game.entities.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck extends ArrayList<Card> {
   private static final int DECK_START_SIZE = 10;
   
   public Card draw() {
      if (isEmpty()) {
         return null;
      } else {
         return remove(size() - 1);
      }
   }

   public static Deck buildRandomDeck(Player owner) {
      Deck rtn = new Deck();
      Random rand = new Random();
      for (int i = 0; i < DECK_START_SIZE; i++) {
         if (rand.nextDouble() < 0.6) {
            rtn.add(new Trainee(owner));
         } else {
            rtn.add(new Scout(owner));
         }
      }
      return rtn;
   }
}
