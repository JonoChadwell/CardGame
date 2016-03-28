package client;

public class Card {
   private final String text;
   private final String name;
   
   public Card(String text) {
      this.text = text;
      this.name = text.substring(0, text.indexOf(':'));
   }

   public String getText() {
      return text;
   }

   public String getName() {
      return name;
   }
}
