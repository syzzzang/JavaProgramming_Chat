package multichat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

public class MultiClient {

   static boolean chatmode = false;
   static int chatState = 0;
   
   // 0 : before login
   // 1 : after login(HOME)
   // 2 : room list
   // 3 : chatting
   // 4 : p2p
   // 5 : send file
   // 6 : p2p checking
   // 7 : home
   // 10 : bad user
   // 100 : whisper

   public static void main(String[] args) throws UnknownHostException, IOException {

      try {
         String ServerIP = "localhost";
         Socket socket = new Socket(ServerIP, 9999);
         System.out.println("[##] Connect with Server......\n\n");
         
         System.out.println("\nWELCOME SundiSodi\n");
         System.out.println("Are you our member? [YES/NO]");

         Thread sender = new Sender(socket);
         Thread receiver = new Receiver(socket);

         sender.start();
         receiver.start();

      } catch (Exception e) {
         System.out.println("¿¹¿Ü[MultiClient class]:" + e);
      }

   }
}