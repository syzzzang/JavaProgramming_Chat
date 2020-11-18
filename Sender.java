package multichat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

//class - sending msg to server
class Sender extends Thread {
   Socket socket;
   DataOutputStream out;
   String name; 
   String pw;
   String pw2;

   public Sender(Socket socket) { 
      this.socket = socket;
      try {
         out = new DataOutputStream(this.socket.getOutputStream());

      } catch (Exception e) {
         System.out.println("Exception:" + e);
      }
   }

   @Override
   public void run() { // redefine run() method

      Scanner s = new Scanner(System.in);

      while (out != null) {
         try {

            String msg = s.nextLine();
            if (msg == null || msg.trim().equals("")) {
               msg = " ";
            }

            if (MultiClient.chatState == 0) {    //0 : before login

               if (msg.equalsIgnoreCase("YES")) {

                  System.out.println("¢ºID : ");
                  name = s.nextLine();
                  System.out.println("¢ºPW : ");
                  pw = s.nextLine();
                  if (name == null || name.trim().equals("")) {
                     name = " ";
                  }
                  if (pw == null || pw.trim().equals("")) {
                     pw = " ";
                  }
                  if (!name.trim().equals("") && !pw.trim().equals("")) {
                     try {
                        out.writeUTF("req_login|" + name + "|" + pw + "|home");
                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                  } else {
                     System.out.println("[##] You cannot enter the space.\r");
                     System.out.println("Are you our member? [YES/NO]");
                  }

               } else if (msg.equalsIgnoreCase("NO")) {

                  System.out.println("[##]Please be a member of <SundiSodi>");

                  System.out.println("¢ºID : ");
                  name = s.nextLine(); 
                  System.out.println("¢ºPW : ");
                  pw = s.nextLine();
                  System.out.println("¢ºPW2 : ");
                  pw2 = s.nextLine();
                  if (name == null || name.trim().equals("")) {
                     name = " ";
                  }
                  if (pw == null || pw.trim().equals("")) {
                     pw = " ";
                  }
                  if (pw2 == null || pw2.trim().equals("")) {
                     pw2 = " ";
                  }
                  if (!name.trim().equals("") && !pw.trim().equals("") && !pw2.trim().equals("")) {
                     try {
                        out.writeUTF("req_join|" + name + "|" + pw + "|" + pw2);
                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                  }

               } else {
                  System.out.println("[##] Wrong input.");
                  System.out.println("Are you our member? [YES/NO]");
               }

            } else if (MultiClient.chatState == 1) {   //1 : after login(HOME)

               if (msg.equalsIgnoreCase("#1")) { // room list
                  System.out.println("[##] 1. ROOM LIST\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.startsWith("#2")) { // p2p chat
                  System.out.println("[##] Let's chat - 1:1");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.startsWith("#p2p")) { 
                  System.out.println("[##] Let's chat - 1:1");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);
              
               } else if (msg.startsWith("#3")) { // make new room
                  System.out.println("[##] 3. MAKE NEW ROOM\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.equalsIgnoreCase("#4")) { // send letter
                  System.out.println("[##] 4. SEND LETTER\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.equalsIgnoreCase("#5")) { // bad user
                  System.out.println("[##] 5. BAD USER\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.equalsIgnoreCase("#6")) { // logout
                  System.out.println("[##] 6. LOGOUT\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);

               } else if (msg.equalsIgnoreCase("#7")) { // exit
                  System.out.println("[##] 7. Exit");
                  System.exit(0);
               } else if (msg.equalsIgnoreCase("#8")) { // withdraw
                  System.out.println("[##] 8. BYE SUNDISODI\n");
                  out.writeUTF("req_cmdMsg|" + name + "|" + msg);
               
               } else if (msg.equalsIgnoreCase("#HOME")) { // home
                  System.out.println("[##] Go HOME");
                  out.writeUTF("req_home|" + name + "|home");

               } else {
                  System.out.println("[##] Wrong input.\n¢º Input:");
               }

            } else if (MultiClient.chatState == 2) {   //2 : room list

               if (msg.startsWith("#IN")) {
                  out.writeUTF("req_enterRoom|" + name + "|" + msg);
               } else if (msg.equalsIgnoreCase("#HOME")) {
                  System.out.println("[##] Go HOME.");
                  out.writeUTF("req_home|" + name + "|home");

               } else {
                  System.out.println("[##]Wrong input.");
                  System.out.println("¢º Input:");
               }

            } else if (MultiClient.chatState == 3) {   //3 : chatting
               if (msg.startsWith("#")) {
                  if (msg.startsWith("#WHISPER")) {
                     out.writeUTF("req_whisper|" + name + "|" + msg);
                  } else if (msg.startsWith("#FILE")) {
                     out.writeUTF("req_file|" + name + "|" + msg);
                  } else if (msg.startsWith("#ROOMOUT")) {
                     System.out.println("[##]Leave Room");
                     out.writeUTF("req_exitRoom|" + name + "|home");
                  } else if (msg.startsWith("#ENDp2p")) {
                     System.out.println("[##]END p2p chat");
                     out.writeUTF("req_cmdMsg|" + msg + "|home");

                  } else {
                     System.out.println("[##] Wrong input.If you want to go 'HOME', enter [#ROOMOUT]");
                  }
               } else {
                  out.writeUTF("req_say|" + name + "|" + msg);
               }
            } else if (MultiClient.chatState == 4) {    //4 : p2p
               msg = msg.trim();
               if (msg.equalsIgnoreCase("y")) {
                  out.writeUTF("p2pchat|yes" + " " + name);
               } else if (msg.equalsIgnoreCase("n")) {
                  out.writeUTF("p2pchat|no" + " " + name);
               } else {
                  System.out.println("[##] Wrong input.");

                  System.out.print("¢ºInput:");
               }

            } else if (MultiClient.chatState == 6) {   //6 : p2p checking

               msg = msg.trim(); 
               if (msg.equalsIgnoreCase("y")) {
                  out.writeUTF("2p2pchat|yes" + " " + name);
               } else if (msg.equalsIgnoreCase("n")) {
                  out.writeUTF("2p2pchat|no" + " " + name);
               } else {
                  System.out.println("[##] Wrong input.");
                  System.out.print("¢ºInput:");
               }

            } else if (MultiClient.chatState == 7) {   //7 : home
               if (msg.equalsIgnoreCase("#HOME")) {
                  System.out.println("[##] Go HOME.");
                  out.writeUTF("req_cmdMsg|" + "#OUT" + "|home");

               } else {
                  System.out.println("[##] Enter [#HOME]");
               }
               
            } else if (MultiClient.chatState == 5) {   //5 : send file
               if (msg.trim().equalsIgnoreCase("y")) {
                  out.writeUTF("fileSend|yes");
                  MultiClient.chatState = 3; 

               } else if (msg.trim().equalsIgnoreCase("n")) {
                  out.writeUTF("fileSend|no");
                  MultiClient.chatState = 3; 

               } else {
                  System.out.println("[##]Wrong input");
                        System.out.print("¢ºInput:");
               }

            }else if(MultiClient.chatState==10){   //10 : bad user
               if (msg.startsWith("#BAD")) {
               out.writeUTF("req_bad|" + name + "|" + msg + "|home");
            } else if (msg.equalsIgnoreCase("#HOME")) {
               System.out.println("[##] Go HOME.");
               out.writeUTF("req_home|" + name + "|home");
            } else {
               System.out.println("[##] That is wrong format. Please input again.\n"
                     + "Input format - [#BAD name number]: ");
            }
            }else if(MultiClient.chatState==100){    //100 : whisper
               if (msg.equalsIgnoreCase("#HOME")) {
               System.out.println("[##] Go HOME.");
               out.writeUTF("req_home|" + name + "|home");
            }else if (msg.trim().equals("")) {
               System.out.println("[##] Wrong input\n¢º Input:");
            } else {
               int num = Integer.parseInt(msg);
               System.out.println("[##] List the name of users [name name ...] : ");
               String lettername = s.nextLine();
               System.out.println("[##] What do you want to send? : ");
               String letter = s.nextLine();

               if (lettername == null || lettername.trim().equals("")) {
                  lettername = " ";
               }

               if (num != 0 && !lettername.trim().equals("")) {
                  try {
                     out.writeUTF("sendLetter|" + name + "|" + num + "|" + lettername + "|" + letter);
                  } catch (IOException e) {
                     e.printStackTrace();
                  }
               } else {
                  System.out.println("[##] Wrong input.\n¢º Input:");
               }
            }
            }

         } catch (SocketException e) {
            System.out.println("Sender:run() Exception:" + e);
            System.out.println("[##] disconnected with Server");
            return;
         } catch (IOException e) {
            System.out.println("¿¹¿Ü:" + e);
         }
      } 
   }
}