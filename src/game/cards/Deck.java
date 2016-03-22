package game.cards;

import game.entities.Player;

import java.util.ArrayList;
import java.util.List;

public class Deck extends ArrayList<Card> {
   private static final int DECK_START_SIZE = 30;
   
   public Card draw() {
      if (isEmpty()) {
         return null;
      } else {
         return remove(size() - 1);
      }
   }

   public static Deck buildRandomDeck(Player owner) {
      Deck rtn = new Deck();
      for (int i = 0; i < DECK_START_SIZE; i++) {
         rtn.add(new Trainee(owner));
      }
      return rtn;
   }
}
