package game;

import game.actions.Action;

public interface Entity {
   public Action getAction();
   public default int getSummoningDuration() {
      return 6;
   }
   
   public default boolean isTransparent() {
      return true;
   }
   
   public default void actionSuceeded(Action a) {}
   public default void actionFailed(Action a) {}
   public default void actionFailed(Action a, String reason) {
      actionFailed(a);
   }
   public default double getViewDistance() {return 10;}
}
