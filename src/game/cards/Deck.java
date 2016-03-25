package game.cards;

import game.entities.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck extends ArrayList<Card> {
   //private static final int DECK_START_SIZE = 10;
   
   public Card draw() {
      if (isEmpty()) {
         return null;
      } else {
         return remove(size() - 1);
      }
   }
   
   public void shuffle() {
      Collections.shuffle(this);
   }

   public static Deck buildRandomDeck(Player owner) {
      Deck rtn = new Deck();
      rtn.add(new Heavy(owner));
      rtn.add(new Scout(owner));
      rtn.add(new Scout(owner));
      rtn.add(new Trooper(owner));
      rtn.add(new Trooper(owner));
      rtn.add(new Trooper(owner));
      rtn.add(new Trainee(owner));
      rtn.add(new Trainee(owner));
      rtn.add(new Trainee(owner));
      rtn.add(new Trainee(owner));
      return rtn;
   }
}
