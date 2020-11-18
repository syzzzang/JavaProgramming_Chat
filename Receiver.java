package multichat;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import multichat.MultiClient;


class Receiver extends Thread {

   Socket socket;
   DataInputStream in;


   public Receiver(Socket socket) {
      this.socket = socket;

      try {
         in = new DataInputStream(this.socket.getInputStream());
      } catch (Exception e) {
         System.out.println("예외:" + e);
      }
   }

   /* msg parser */
   public String[] getMsgParse(String msg) {

      String[] tmpArr = msg.split("[|]");

      return tmpArr;
   }

   @Override
   public void run() { 

      while (in != null) { 
         try {

            String msg = in.readUTF(); 

            String[] msgArr = getMsgParse(msg.substring(msg.indexOf("|") + 1));

            if (msg.startsWith("join#yes")) { 
               System.out.println("[##]Congratulation! you are now our member!");
                    System.out.println("WELCOME SundiSodi\n");
                    System.out.println("Are you our member? [YES/NO]");
            
            } else if (msg.startsWith("join#err01")) {
               System.out.println("[##] ID is duplicated\n");
               System.out.println("Are you our member? [YES/NO]");
            
            } else if (msg.startsWith("join#err02")) {
               System.out.println("[##] PW is not correct");
               System.out.println("Are you our member? [YES/NO]");
               
            } else if (msg.startsWith("login#yes")) {
               System.out.println("Long time no see:)\n\n");
               System.out.println(msgArr[0]);
               MultiClient.chatState = 1; 

               System.out.println("HOME\n----------------------"
                     + "\nRule : Write like below.\n[#num]\n"
                     + "If you want to go HOME,\nWrite [#HOME].\n\n"
                     + "1\t\tROOM LIST\n"
                     + "2\t\t1:1 CHAT\n"
                     + "3\t\tMAKE NEW ROOM\n"
                     + "4\t\tSEND LETTER\n"
                     + "5\t\tBAD USER\n"
                     + "6\t\tLOGOUT\n"
                     + "7\t\tExit\n"
                     + "8\t\tWithdraw\n----------------------\n\n");
               System.out.println("▶ Input : ");

            } else if (msg.startsWith("login#no|err01")) { // 로그인 실패 (아이디가 없거나 비밀번호가 아닌경우)
               System.out.println("[##]ID or PW is not correct.\nPlease put ID again.");
               System.out.println("Are you our member? [YES/NO]");
               
            }else if(msg.startsWith("login#no|err02")){ 
               System.out.println("[##]ID is already in SundiSodi. Please put another ID.");
               System.out.println("Are you our member? [YES/NO]");
          
            } else if (msg.startsWith("goHome")) { 
               System.out.println("HOME\n----------------------"
                     + "\nRule : Write like below.\n[#num]\n"
                     + "If you want to go HOME,\nWrite [#HOME].\n\n"
                     + "1\t\tROOM LIST\n"
                     + "2\t\t1:1 CHAT\n"
                     + "3\t\tMAKE NEW ROOM\n"
                     + "4\t\tSEND LETTER\n"
                     + "5\t\tBAD USER\n"
                     + "6\t\tLOGOUT\n"
                     + "7\t\tExit\n"
                     + "8\t\tWithdraw\n----------------------\n\n");
              MultiClient.chatState = 1;
               System.out.println("▶ Input : ");

            } else if (msg.startsWith("enterRoom#yes")) { 

               System.out.println("[##] You has entered Room (" + msgArr[0] + ")" + msgArr[1] + "\nUpdate the history of conversation");
               MultiClient.chatState = 3;
               System.out.println("▶ Input : ");

               
            } else if (msg.startsWith("makeRoom#yes")) {
               
               System.out.println("[##] Room[" + msgArr[0] + "] has been made");
               System.out.println("▶ Input : ");

            } else if(msg.startsWith("makeRoom#no")){
               
               System.out.println("[##] Room["+msgArr[0]+"] already exists ");
               System.out.println("▶ Input : ");
               
            } else if (msg.startsWith("p2p#yes")) {
                
               System.out.println(msgArr[0]);
                MultiClient.chatState = 3;


             } else if(msg.startsWith("p2p#no")){
                
               System.out.println(msgArr[0]);
                MultiClient.chatState = 1;
                
             } else if(msg.startsWith("p2p#success")){
                 MultiClient.chatState = 6;
                System.out.println(msgArr[0]);
                 System.out.print("▶Input:");
                
             } else if (msg.startsWith("p2p#reject")) {
                 
                  System.out.println(msgArr[0]);
                  MultiClient.chatState = 1;

             } else if (msg.startsWith("enterRoom#no")) {

               System.out.println("[##] Room[" + msgArr[0] + "] doesn't exist.");
               System.out.println("▶Input:");

             } else if (msg.startsWith("show")) {

               System.out.println(msgArr[0]);
               System.out.println("▶ Input : ");


             } else if (msg.startsWith("ENDp2p")) {

                 System.out.println(msgArr[0]);
                 System.out.println("HOME\n----------------------"
                         + "\nRule : Write like below.\n[#num]\n"
                         + "If you want to go HOME,\nWrite [#HOME].\n\n"
                         + "1\t\tROOM LIST\n"
                         + "2\t\t1:1 CHAT\n"
                         + "3\t\tMAKE NEW ROOM\n"
                         + "4\t\tSEND LETTER\n"
                         + "5\t\tBAD USER\n"
                         + "6\t\tLOGOUT\n"
                         + "7\t\tExit\n"
                         + "8\t\tWithdraw\n----------------------\n\n");
                 MultiClient.chatState = 1;
                 System.out.println("▶ Input : ");


             } else if (msg.startsWith("get_ENDp2p")) {

                 System.out.println(msgArr[0]);
                 sleep(100);
                 System.out.println("▶ Input : ");
                 MultiClient.chatState = 7;

             }else if(msg.startsWith("trybaduser")){
                System.out.println("[##] Who is a bad user?\n");
                 System.out.println(msgArr[0]);
                 System.out.println("Why do you want to report that user?\n"
                       + "1. to use abuses\n"
                       + "2. excessive advertising\n"
                       + "3. Distributing porn");
                 MultiClient.chatState=10;
                 System.out.println("Input format - [#BAD name number]: ");
              
             }else if(msg.startsWith("tryLetter")){
                System.out.println("[##] Whom do you want to send a letter?\n");
                System.out.println(msgArr[0]);
                MultiClient.chatState=100;
                System.out.println("[##] how many users do you want to send letters? [format : 3]");
                
             }else if(msg.startsWith("sendno")){ 
                System.out.println(msgArr[0]);
                System.out.println("[##] how many users do you want to send letters? [format : 3]");

             }else if(msg.startsWith("sendyes")){ 
                System.out.println(msgArr[0]);
                System.out.println("HOME\n----------------------"
                         + "\nRule : Write like below.\n[#num]\n"
                         + "If you want to go HOME,\nWrite [#HOME].\n\n"
                         + "1\t\tROOM LIST\n"
                         + "2\t\t1:1 CHAT\n"
                         + "3\t\tMAKE NEW ROOM\n"
                         + "4\t\tSEND LETTER\n"
                         + "5\t\tBAD USER\n"
                         + "6\t\tLOGOUT\n"
                         + "7\t\tExit\n"
                         + "8\t\tWithdraw\n----------------------\n\n");
               MultiClient.chatState=1;
                 System.out.println("▶ Input : ");
                 
             }else if(msg.startsWith("badyeswithdraw")){
                System.out.println(msgArr[0]);
                System.out.println("HOME\n----------------------"
                         + "\nRule : Write like below.\n[#num]\n"
                         + "If you want to go HOME,\nWrite [#HOME].\n\n"
                         + "1\t\tROOM LIST\n"
                         + "2\t\t1:1 CHAT\n"
                         + "3\t\tMAKE NEW ROOM\n"
                         + "4\t\tSEND LETTER\n"
                         + "5\t\tBAD USER\n"
                         + "6\t\tLOGOUT\n"
                         + "7\t\tExit\n"
                         + "8\t\tWithdraw\n----------------------\n\n");
                MultiClient.chatState=1;
                 System.out.println("▶ Input : ");

             }else if(msg.startsWith("badyes")){ 
                System.out.println(msgArr[0]);
                System.out.println("HOME\n----------------------"
                         + "\nRule : Write like below.\n[#num]\n"
                         + "If you want to go HOME,\nWrite [#HOME].\n\n"
                         + "1\t\tROOM LIST\n"
                         + "2\t\t1:1 CHAT\n"
                         + "3\t\tMAKE NEW ROOM\n"
                         + "4\t\tSEND LETTER\n"
                         + "5\t\tBAD USER\n"
                         + "6\t\tLOGOUT\n"
                         + "7\t\tExit\n"
                         + "8\t\tWithdraw\n----------------------\n\n");
               MultiClient.chatState=1;
                System.out.println("▶ Input : ");
                
             }else if(msg.startsWith("AllBad")){ 
                System.out.println(msgArr[0]);
                
             }else if (msg.startsWith("roomlist")) {
               
               System.out.println(msgArr[0]);
               System.out.println("\nDo you want to enter the room?\n"
                     + "Then write like below.\n"
                     + "[#IN <room name>]\n"
                     + "or not, write [#HOME]]\n");
               MultiClient.chatState = 2;
               System.out.println("▶ Input : ");


            } else if(msg.startsWith("loginlist")){
               System.out.println(msgArr[0]);
               System.out.println("Whom do you want to chat with? format : [#p2p name] \n: ");
            } else if (msg.startsWith("logout")) { 

               System.out.println("Good bye~");
               MultiClient.chatState = 0;
                  System.out.println("\nWELCOME SundiSodi");
                  System.out.println("Are you our member? [YES/NO]");

            } else if(msg.startsWith("byeSundiSodi")){
               System.out.println("Good bye~");
                MultiClient.chatState = 0;
                   System.out.println("\nWELCOME SundiSodi");
                   System.out.println("Are you our member? [YES/NO]");
            } else if (msg.startsWith("say")) {
               System.out.println("[" + msgArr[0] + "] " + msgArr[1]);

            } else if (msg.startsWith("whisper")) {
               System.out.println("[whisper][" + msgArr[0] + "] : " + msgArr[1]);

            } else if (msg.startsWith("req_p2pchat")) {
               MultiClient.chatState = 4; 
               System.out.println(msgArr[0]);
               System.out.print("▶Input:");
            
            }else if (msg.startsWith("req_fileSend")) {

               MultiClient.chatState = 5; 
               System.out.println(msgArr[0]); 
               System.out.print("▶Input:");
               sleep(100);

            } else if (msg.startsWith("fileSender")) { 

               System.out.println("fileSender:" + InetAddress.getLocalHost().getHostAddress());
               System.out.println("fileSender:" + msgArr[0]);

               try {
                  new FileSender(msgArr[0]).start(); 
               } catch (Exception e) {
                  System.out.println("FileSender thread error :");
                  e.printStackTrace();
               }

            } else if (msg.startsWith("fileReceiver")) {

               System.out.println("fileReceiver:" + InetAddress.getLocalHost().getHostAddress());
               System.out.println("fileReceiver:" + msgArr[0] + "/" + msgArr[1]);

               String ip = msgArr[0]; 
               String fileName = msgArr[1]; 

               try {
                  new FileReceiver(ip, fileName).start(); 
               } catch (Exception e) {
                  System.out.println("FileSender thread error:");
                  e.printStackTrace();
               }

            }else if(msg.startsWith("exitRoom#yes")){
               System.out.println("[##] You has left Room[ "+msgArr[0]+" ]. ");
               MultiClient.chatState = 2;
            }else if(msg.startsWith("exitProgram")){
               System.out.println("[##] You has exited <SundiSodi>");
            }

         } catch (SocketException e) {
            System.out.println("Exception:" + e);
            System.out.println("[##]Disconnected with Server");
            return;

         } catch (Exception e) {
            System.out.println("Receiver:run() Exception:" + e);

         }
      } 
   }
}