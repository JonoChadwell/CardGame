package game.cards;

import game.Attacker;
import game.Card;
import game.Entity;
import game.FactionMember;
import game.Player;
import game.actions.Action;
import game.actions.AttackAction;

public class BasicCard implements Card, Entity, Attacker, FactionMember {
   private final double castRange;
   private final double attackRange;
   private final int attackDamage;
   private final int health;
   private final int cost;
   private int damage = 0;
   private Player faction;
   
   public BasicCard(double castRange, double attackRange, int attackDamage, int health, int cost, Player faction) {
      this.castRange = castRange;
      this.attackRange = attackRange;
      this.attackDamage = attackDamage;
      this.health = health;
      this.cost = cost;
      this.faction = faction;
   }

   @Override
   public double getCastRange() {
      return castRange;
   }

   @Override
   public double getAttackRange() {
      return attackRange;
   }

   @Override
   public int getAttackDamage() {
      return attackDamage;
   }

   @Override
   public int getCost() {
      return cost;
   }

   @Override
   public int getHealth() {
      return health;
   }

   @Override
   public Action getAction() {
      return new AttackAction();
   }
   
   public String toString() {
      return "A basic unit"
            + ": Cast Range: " + castRange
            + ", Attack Range: " + attackRange
            + ", Max Health: " + health
            + ", Current Health: " + (health - damage)
            + ", Cost: " + cost;
   }

   @Override
   public Player getFactionOwner() {
      return faction;
   }
}
