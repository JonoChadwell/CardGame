package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class EasyPopupMenu extends JPopupMenu {
   public void addButton(String text, Runnable action) {
      JMenuItem item = new JMenuItem(text);
      item.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            action.run();
         }
      });
      this.add(item);
   }
}
