package renderer.client;

import java.net.Socket;

public class StartScreen {
   private Socket socket;
   
   public static void main(String args[]) throws Exception {
      new StartScreen();
   }
   
   public StartScreen() throws Exception {
      socket = new Socket("10.0.0.2", 1400);
      new PlayerTerminal(socket);
   }
}
