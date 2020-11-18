package multichat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/*Console Multichatting server program*/
public class MultiServer {
   HashMap<String, HashMap<String, ServerRecThread>> globalMap;
   HashMap<String, String> joinuser;
   HashMap<String, Integer> loginuser;
   HashMap<String, String> history;
   HashMap<String, String> cl; 

   ServerSocket serverSocket = null;
   Socket socket = null;
   static int connUserCount = 0;

   // Constructer
   public MultiServer() {
      globalMap = new HashMap<String, HashMap<String, ServerRecThread>>();
      joinuser = new HashMap<String, String>();
      loginuser = new HashMap<String, Integer>();
      history = new HashMap<String, String>();
      cl=new HashMap<String,String>();

      Collections.synchronizedMap(globalMap); // synchronized HashMap
      HashMap<String, ServerRecThread> homemenu = new HashMap<String, ServerRecThread>();
      Collections.synchronizedMap(homemenu);

      globalMap.put("home", homemenu);

   }

   public void init() {
      try {
         serverSocket = new ServerSocket(9999); // 9999port - serverSocket object
         System.out.println("##Server!");
         System.out.println("WELCOME SundiSodi");

         while (true) {
            socket = serverSocket.accept(); // waiting for client
            System.out.println(socket.getInetAddress() + ":" + socket.getPort()); // make socket object
            Thread msr = new ServerRecThread(socket); 
            msr.start(); 
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /** Send msg for all login users*/
   public void sendAllMsg(String msg) {
      Iterator global_it = globalMap.keySet().iterator();
      while (global_it.hasNext()) {
         try {
            HashMap<String, ServerRecThread> it_hash = globalMap.get(global_it.next());
            Iterator it = it_hash.keySet().iterator();
            while (it.hasNext()) {
               ServerRecThread st = it_hash.get(it.next());
               st.out.writeUTF(msg);
            }
         } catch (Exception e) {
            System.out.println("예외:" + e);
         }
      }
   }// sendAllMsg()-----------

   /* send msg to group members */
   public void sendGroupMsg(String loc, String msg) {
      HashMap<String, ServerRecThread> gMap = globalMap.get(loc);
      Iterator<String> group_it = globalMap.get(loc).keySet().iterator();
      while (group_it.hasNext()) {
         try {
            ServerRecThread st = gMap.get(group_it.next());
            st.out.writeUTF(msg);
         } catch (Exception e) {
            System.out.println("예외:" + e);
         }
      }
   }

   /* p2p chatting */
   public void sendp2pMsg(String loc, String fromName, String toName, String msg) {
      try {
         globalMap.get(loc).get(toName).out.writeUTF(msg);
         globalMap.get(loc).get(fromName).out.writeUTF(msg);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /* whispering */
   public void sendToMsg(String loc, String fromName, String toName, String msg) {

      try {

         globalMap.get(loc).get(toName).out.writeUTF("whisper|" + fromName + "|" + msg);
         globalMap.get(loc).get(fromName).out.writeUTF("whisper|" + fromName + "|" + msg);

      } catch (Exception e) {
         System.out.println("예외:" + e);
      }

   }

   public String getEachMapSize() {
      return getEachMapSize(null);
   }

   /* chatting room list */
   public String getEachMapSize(String loc) {
      Iterator<String> global_it = globalMap.keySet().iterator();
      StringBuffer sb = new StringBuffer();
      sb.append("===Room List=== \n");
      while (global_it.hasNext()) {
         try {
            String key = (String) global_it.next();
            HashMap<String, ServerRecThread> it_hash = globalMap.get(key);
            int size = it_hash.size();
            if (key.equals("home"))
               continue;
            if (!key.startsWith("~~~"))
               sb.append(key + ": (" + size + " people)\n");
         } catch (Exception e) {
            System.out.println("Exception:" + e);
         }
      }
      sb.append("⊙Users:" + connUserCount + " people \r\n");
      return sb.toString();
   }

   /* sorting login user name */
   public String getLoginUser(String name) {
      Iterator<String> login_it = loginuser.keySet().iterator();
      StringBuffer sb = new StringBuffer();
      sb.append("===Login List===\n");
      while (login_it.hasNext()) {
         String key = (String) login_it.next();
         sb.append(key + ", ");
      }
      sb.append("\n");
      sb.append(name+" 's warning : "+ loginuser.get(name)+"\n");
      return sb.toString();
   }

   /* check whether user joined */
   public boolean isThereUser(String name) {
      boolean result = false;
      Iterator<String> user_it = joinuser.keySet().iterator();
      while (user_it.hasNext()) {
         try {
            String key = user_it.next();
            if (key.equals(name)) { // joinuser.containsKey(name)
               result = true;
               break;
            }
         } catch (Exception e) {
            System.out.println("isThereUser() Exception" + e);
         }
      }
      return result;
   }

   /* check user in login user list*/
   public boolean IsLogin(String name) {
      boolean result = false;
      Iterator<String> user_it = loginuser.keySet().iterator();
      while (user_it.hasNext()) {
         try {
            String key = user_it.next();
            if (key.equals(name)) { // loginuser.containsKey(name)
               result = true;
               break;
            }
         } catch (Exception e) {
            System.out.println("isThereUser() Exception : " + e);
         }
      }
      return result;
   }

   /* check password correct */
   public boolean isPWRight(String name, String pw) {
      boolean result = false;
      // Iterator<String> user_it = user.keySet().iterator();

      try {
         String dbPW = (String) joinuser.get(name);
         if (dbPW.equals(pw)) {
            result = true;
         }
      } catch (Exception e) {
         System.out.println("isPWRight()Exception : " + e);
      }

      return result;
   }

   /* String null value and "" can be inserted as a replacement string */
   public String nVL(String str, String replace) {
      String output = "";
      if (str == null || str.trim().equals("")) {
         output = replace;
      } else {
         output = str;
      }
      return output;
   }

   // main method
   public static void main(String[] args) {
      MultiServer ms = new MultiServer(); // make server object
      ms.init();//execute
   }// main()------

   ////////////////////////////////////////////////////////////////////////
   // ----// inside class //--------//

   // method - sending msg
   class ServerRecThread extends Thread {

      int counting = 0;
      Socket socket;
      DataInputStream in;
      DataOutputStream out;
      String name = ""; // save name
      String pw = "";// save password
      String loc = ""; // save room
      String toNameTmp = "";// p2p chatting partner
      String fileServerIP; // save file server IP
      String filePath; // save file path
      String badname = "";
      boolean chatMode; // whether p2p chatting or not
      // int p2pRoomNum = 0;

      // constructor
      public ServerRecThread(Socket socket) {
         this.socket = socket;
         try {
            // get inputstream from socket
            in = new DataInputStream(socket.getInputStream());
            // get outputstream from socket
            out = new DataOutputStream(socket.getOutputStream());
         } catch (Exception e) {
            System.out.println("ServerRecThread Constructor Exception:" + e);
         }
      }

      /* login user convert to string*/
      public String showUserList() {

         StringBuilder output = new StringBuilder("==접속자목록==\r\n");
         Iterator<String> it = globalMap.get(loc).keySet().iterator();
         while (it.hasNext()) {
            try {
               String key = (String) it.next();
               if (key.equals(name)) { 
                  key += " (*) ";
               }

               output.append(key + "\r\n");
            } catch (Exception e) {
               System.out.println("예외:" + e);
            }
         } 
         output.append("==" + globalMap.get(loc).size() + " people are in <SundiSodi>==\r\n");
         System.out.println(output.toString());
         return output.toString();
      }

      /* msg parser */
      public String[] getMsgParse(String msg) {
         System.out.println("msgParse() : msg?   " + msg);
         String[] tmpArr = msg.split("[|]");
         return tmpArr;
      }

      @Override
      public void run() { // redefine run() mehod
         HashMap<String, ServerRecThread> clientMap = null;
         try {
            while (in != null) { 
               String msg = in.readUTF();
               String[] msgArr = getMsgParse(msg.substring(msg.indexOf("|") + 1));

               if (msg.startsWith("req_join")) { // try sign up
                  if (!(msgArr[0].trim().equals(""))) {
                     if (!isThereUser(msgArr[0])) { 
                        if (msgArr[1].equals(msgArr[2])) { 
                           joinuser.put(msgArr[0], msgArr[1]); 
                           out.writeUTF("join#yes"); 
                        } else {
                           out.writeUTF("join#err02");
                        }
                     } else {
                        out.writeUTF("join#err01");
                     }
                  }
               } else if (msg.startsWith("req_login")) {
                  if (!(msgArr[0].trim().equals("")) && IsLogin(msgArr[0])) { 
                     out.writeUTF("login#no|err02");
                  } else if (!(msgArr[0].trim().equals("")) && isThereUser(msgArr[0])
                        && isPWRight(msgArr[0], msgArr[1])) {
                     name = msgArr[0]; 
                     MultiServer.connUserCount++; 
                     loginuser.put(name, 0); 
                  cl.put(name, msgArr[2]);
                     clientMap = globalMap.get(msgArr[2]);
                     clientMap.put(name, this);
                     out.writeUTF("login#yes|" + getLoginUser(name));

                  } else { 
                     out.writeUTF("login#no|err01");
                  }

               } else if (msg.startsWith("req_home")) {
                  System.out.println(msgArr[0] + " has entered [HOME]");
                  clientMap = globalMap.get(msgArr[1]);
               clientMap.put(name, this);
               cl.put(name, msgArr[1]);
               out.writeUTF("goHome");
               } else if (msg.startsWith("req_cmdMsg")) { // send command
                  if (msgArr[1].trim().equals("#1")) {
                     out.writeUTF("roomlist|" + getEachMapSize(loc));

                  } else if (msgArr[1].trim().equals("#2")) {
                     System.out.println(msgArr[0] + " wants to have p2p chat.");
                     out.writeUTF("loginlist|" + getLoginUser(name) + "\n");
                  }
                  else if (msgArr[1].trim().startsWith("#p2p")) { // p2p
                     String[] msgSubArr = msgArr[1].split(" ", 2);

                     if (msgSubArr.length != 2) {
                        out.writeUTF("show|[##] Wrong input.\r\n " + "usage : [#p2p name]");
                        continue;
                     } else {

                        if (name.equals(msgSubArr[1])) {
                           out.writeUTF("show|[##] Wrong input.\r\n " + "You cannot chat with yourself. usage : [#p2p name]");
                           continue;
                        } else {
                           if (!chatMode) {
                              String toName = msgSubArr[1].trim();
                              out.writeUTF("show|[##] You have requested 1:1 chat with" + toName);
                              if (clientMap.containsKey(toName) && !clientMap.get(toName).chatMode) { // check user

                                 clientMap.get(toName).out.writeUTF("req_p2pchat|[##] [" + name
                                       + "] has requested 1:1 chat with you. \r\n Do you accept it?(y,n)|" + name);
                                 toNameTmp = toName;
                                 clientMap.get(toNameTmp).toNameTmp = name;
                                 
                              } else {
                                 out.writeUTF("show|[##] That user does not exist or cannot talk 1:1");
                              }

                           } else {
                              out.writeUTF("show|[##] You are in 1:1 chatting, so you cannot request 1:1 talk.");
                           }
                        }
                     }

                  } else if (msgArr[0].startsWith("#ENDp2p")) {

                     if (chatMode) {
                        chatMode = false; 
                        clientMap.get(toNameTmp).chatMode = false; 
                        clientMap.get(toNameTmp).out.writeUTF(
                              "get_ENDp2p|[##] " + name + "exit 1:1 talk. \nIf you enter [#HOME], go back 'HOME'.");
                        clientMap.get(toNameTmp).toNameTmp="";
                        clientMap.remove(name);
                        clientMap = globalMap.get(msgArr[1]);
                        clientMap.put(name, this);
                        out.writeUTF("goHome");


                     } else {
                        out.writeUTF("show|[##] Wrong input.");
                     }

                  } 
                  else if(msgArr[0].startsWith("#OUT")) {
                     toNameTmp="";
                     clientMap.remove(name);
                     clientMap = globalMap.get(msgArr[1]);
                     clientMap.put(name, this);
                     out.writeUTF("goHome");
                  }
                  
                
                  
                  else if (msgArr[1].trim().startsWith("#3")) { // make room
                     boolean check = true;
                     String[] msgSubArr = msg.split(" ");
                     if (msgSubArr.length == 2) {
                        String tmpLoc = msgSubArr[1]; 

                        Iterator<String> global_it = globalMap.keySet().iterator();

                        while (global_it.hasNext()) {
                           try {
                              String key = (String) global_it.next();

                              if (key.equals(tmpLoc)) {
                                 out.writeUTF("makeRoom#no|" + tmpLoc);
                                 check = false;
                              }
                           } catch (Exception e) {
                              System.out.println("예외:" + e);
                           }
                        }
                        if (check) {
                            HashMap<String, ServerRecThread> group = new HashMap<String, ServerRecThread>();
                            Collections.synchronizedMap(group);
                            globalMap.put(tmpLoc, group); 
                            String text = "\n\nHISTORY OF THIS CHATTING ROOM\n";
                            history.put(tmpLoc, text);
                            out.writeUTF("makeRoom#yes|"+tmpLoc);
                            out.writeUTF("show|" + getEachMapSize(loc));
                         }

                     } else {
                        out.writeUTF("show|[##] Wrong input.\n [##] usage : [#3 name].");
                     }
                  } else if (msgArr[1].trim().equals("#4")) { // send letter
                  out.writeUTF("tryLetter|"+ getLoginUser(name)+"\n");

                  } else if (msgArr[1].trim().equals("#5")) { // bad user
                  out.writeUTF("trybaduser|" + getLoginUser(name) + "\n");

                  } else if (msgArr[1].trim().equals("#6")) { // logout
                     loginuser.remove(msgArr[0]);
                     connUserCount--;
                     clientMap.remove(name);
                     out.writeUTF("logout");
                  cl.remove(name);


                  } else if (msgArr[1].trim().equals("#7")) { // exit program
                     loginuser.remove(msgArr[0]);
                     connUserCount--;
                     out.writeUTF("exitProgram");
                  cl.remove(name);

                     System.exit(0);
                  } else if (msgArr[1].trim().equals("#8")) {
                     connUserCount--;
                     joinuser.remove(msgArr[0]);
                     clientMap.remove(name);
                  cl.remove(name);

                     out.writeUTF("byeSundiSodi");
                  }

               }else if(msg.startsWith("req_bad")){
                  String[] msgSubArr = msgArr[1].split(" ");

               if (msgSubArr.length == 3) {
                  if (!msgSubArr[1].equals(name)) {
                     int reason = Integer.parseInt(msgSubArr[2]);

                     if (reason == 1) {
                        if (loginuser.containsKey(msgSubArr[1])) {
                           badname = msgSubArr[1];
                           loginuser.put(badname, loginuser.get(badname) + 1);
                                                                  
                           if (loginuser.get(badname) == 4) {
                              globalMap.get(cl.get(badname)).get(badname).out.writeUTF("byeSundiSodi");
                                  sendGroupMsg(cl.get(badname), "show|[##] [" + name + "] has been withdrawn.\n");

                              loginuser.remove(badname);
                              joinuser.remove(badname);
                              connUserCount--;
                              clientMap.remove(badname,this);
                              cl.remove(badname);
                              
                              out.writeUTF("badyeswithdraw|[##] [ " + badname
                                    + " ]is withdrawn.\n Enjoy clean Sundisodi\n");
                              badname=null;
                              
                           } else {
                              sendAllMsg("AllBad|[##] [" + badname + "] is reported. [to use abuses]\n");
                              out.writeUTF("badyes|[##] Your report made SundiSodi clean. ThankU.\n\n");
                           }
                        } else {
                           out.writeUTF("show|[##] There is no user named [" + msgSubArr[1]
                                 + "]. Please input again");
                        }
                     } else if (reason == 2) {
                        if (loginuser.containsKey(msgSubArr[1])) {
                           badname = msgSubArr[1];
                           loginuser.put(badname, loginuser.get(badname) + 1); 
                                                                  
                           if (loginuser.get(badname) == 4) {
                              globalMap.get(cl.get(badname)).get(badname).out.writeUTF("byeSundiSodi");
                                  sendGroupMsg(cl.get(badname), "show|[##] [" + name + "] has been withdrawn.\n");
                              loginuser.remove(badname);
                              joinuser.remove(badname);
                              connUserCount--;
                              clientMap.remove(badname);
                              cl.remove(badname);
                              
                              out.writeUTF("badyeswithdraw|[##] [ " + badname
                                    + " ]is withdrawn.\n Enjoy clean Sundisodi\n");
                              badname=null;
                              
                           } else {
                              sendAllMsg("AllBad|[##] [" + badname + "] is reported. [excessive advertising]\n");
                              out.writeUTF("badyes|[##] Your report made SundiSodi clean. ThankU.\n\n");
                           }
                        } else {
                           out.writeUTF("show|[##] There is no user named [" + msgSubArr[1]
                                 + "]. Please input again");
                        }
                        

                     } else if (reason == 3) {
                        if (loginuser.containsKey(msgSubArr[1])) {
                           badname = msgSubArr[1];
                           loginuser.put(badname, loginuser.get(badname) + 1);
                                                                  
                           if (loginuser.get(badname) == 4) {
                              globalMap.get(cl.get(badname)).get(badname).out.writeUTF("byeSundiSodi");
                                  sendGroupMsg(cl.get(badname), "show|[##] [" + name + "] has been withdrawn.\n");

                              loginuser.remove(badname);
                              joinuser.remove(badname);
                              connUserCount--;
                              clientMap.remove(badname,this);
                              cl.remove(badname);
                              
                              out.writeUTF("badyeswithdraw|[##] [ " + badname
                                    + " ]is withdrawn.\n Enjoy clean Sundisodi\n");
                              badname=null;
                              
                           } else {
                              sendAllMsg("AllBad|[##] [ " + badname + " ] is reported. [Distributing porn]\n");
                              out.writeUTF("badyes|[##] Your report made SundiSodi clean. ThankU.\n\n");
                           }
                        } else {
                           out.writeUTF("show|[##] There is no user named [" + msgSubArr[1]
                                 + "]. Please input again");
                        }
                     } else { 
                        out.writeUTF(
                              "show|[##] Plz choose one of the three.\nInput format - [#BAD name number]: ");
                     }
                  }else {
                     out.writeUTF(
                           "show|[##] You can't report yourself.\nInput format - [#BAD name number]: ");
                  }
               } else {
                  out.writeUTF(
                        "show|[##] That is wrong format. Please input again.\nInput format - [#BAD name number]: ");
               }
            
               }else if(msg.startsWith("sendLetter")){
                  String[] msgSubArr=msgArr[2].split(" ");
               int num=Integer.parseInt(msgArr[1]);
               if(num==msgSubArr.length){
                  for(int i=0;i<num;i++){
                     globalMap.get(cl.get(msgSubArr[i])).get(msgSubArr[i]).out.writeUTF("show|[##]["+msgArr[0]+"] sent a letter to you! : "+msgArr[3]);
                  }
                  out.writeUTF("sendyes|[##] Your letter is arrived well. \n");
                  
               }else{
                  out.writeUTF("sendno|[##] Plz write correctly.\n"+loginuser);
               }
               }
               else if (msg.startsWith("req_enterRoom")) {
                  String[] msgSubgArr = msgArr[1].split(" ");

                  loc = msgSubgArr[1];
                  if (globalMap.containsKey(loc)) {
                     sendGroupMsg(loc, "show|[##] [" + name + "] has entered Room["+loc+"].");
                     clientMap.remove(name);
                     clientMap = globalMap.get(loc);
                     clientMap.put(name, this); 
                  cl.put(name, loc);
                     System.out.println(getEachMapSize()); 
                     out.writeUTF("enterRoom#yes|"  + loc + "|" + (String)history.get(loc)); 
                  } else {
                     out.writeUTF("enterRoom#no|" + loc);
                  }

               } else if (msg.startsWith("req_exitRoom")) {
                  out.writeUTF("exitRoom#yes|" + loc);
                  clientMap.remove(name); 
                  sendGroupMsg(loc, "show|[##] [" + name + "] has left Room["+loc+"].");
                  System.out.println(name+" has left "+loc+".");
                  clientMap = globalMap.get(msgArr[1]); 
                  clientMap.put(name, this);
                  loc = null;
               cl.put(name, loc);
                  out.writeUTF("roomlist|" + getEachMapSize(loc));

               } else if (msg.startsWith("req_say")) { 
                   String texts = (String)history.get(loc);
                   texts += ("\n" + "[" + name + "] " + msgArr[1]);
                   history.put(loc, texts);
                  sendGroupMsg(loc, "say|" + name + "|" + msgArr[1]);
               
             } else if (msg.startsWith("req_whisper")) { // whisper
                  if (msgArr[1].trim().startsWith("#WHISPER")) {
                     String[] msgSubArr = msgArr[1].split(" ", 3);

                     if (msgSubArr == null || msgSubArr.length < 3) {
                        out.writeUTF("show|[##] Wrong input.\n usage : #WHISPER name message.");
                     } else {
                        String toName = msgSubArr[1];
                        String toMsg = msgSubArr[2];
                        if (clientMap.containsKey(toName)) { // check user
                           sendToMsg(loc, name, toName, toMsg);
                        } else {
                           out.writeUTF("show|[##] ["+toName+"] are not in <SundiSodi>");
                        }
                     } 
                  }

               } else if (msg.startsWith("p2pchat")) {
                  String[] msgSubArr = msgArr[0].split(" ", 2);
                  if (msgSubArr[0].startsWith("yes")) {
                     System.out.println(name);
                     System.out.println(toNameTmp);

                     name = msgSubArr[1];

                     chatMode = true;
                     System.out.println("##1:1 chatmode change");
                     loc = "~~~" + "abc" + counting;

                     clientMap.get(toNameTmp).out
                           .writeUTF("p2p#success|[##] [" + name + "] has accepted 1:1 chat. Do you want to start?");

                     HashMap<String, ServerRecThread> group = new HashMap<String, ServerRecThread>();
                     Collections.synchronizedMap(group);
                     globalMap.put(loc, group);

                     clientMap = globalMap.get(loc);
                     clientMap.put(name, this);

                     try {
                        out.writeUTF("p2p#yes|[##] send your message to  " + toNameTmp);

                     } catch (IOException e) {
                        e.printStackTrace();
                     }

                  } else {
                     out.writeUTF("p2p#no|[##] you have rejected 1:1 chat with" + toNameTmp);
                     clientMap.get(toNameTmp).out.writeUTF("p2p#reject|[##] [" + name + "] has rejected 1:1 chat.");
                  }

               } else if (msg.startsWith("2p2pchat")) {
                  String[] msgSubArr = msgArr[0].split(" ", 2);

                  if (msgSubArr[0].startsWith("yes")) {
                     System.out.println(name);
                     System.out.println(toNameTmp);

                     loc = "~~~" + "abc" + counting;

                     name = msgSubArr[1];
                     chatMode = true;

                     out.writeUTF("p2p#yes|");

                     clientMap = globalMap.get(loc); 
                     clientMap.put(name, this);

                     System.out.println("##1:1 chatmode change");
                     sendGroupMsg(loc, "show|[##] Let's start 1:1 chat.");

                  } else {

                     out.writeUTF("p2p#no|[##] You have rejected 1:1 chat with " + toNameTmp+".\n");

                  }

               }

               else if (msg.startsWith("req_file")) {
                  if (msgArr[1].trim().startsWith("#FILE")) {

                     if (!chatMode) {
                        out.writeUTF("show|[##] Wrong input. That is for 1:1 chat.");
                        continue;
                     }

                     String[] msgSubArr = msgArr[1].split(" ", 2);
                     if (msgSubArr.length != 2) {
                        out.writeUTF("show|[##] Wrong input\n usage : #FILE filepath");
                        continue;
                     }
                     filePath = msgSubArr[1];
                     File sendFile = new File(filePath);
                     String availExtList = "txt,java,jpeg,jpg,png,gif,bmp";

                     if (sendFile.isFile()) {
                        String fileExt = filePath.substring(filePath.lastIndexOf(".") + 1);
                        if (availExtList.contains(fileExt)) {
                           Socket s = globalMap.get(loc).get(toNameTmp).socket;
                           System.out.println("s.getInetAddress():FileServerIP=>" + s.getInetAddress());

                           fileServerIP = s.getInetAddress().getHostAddress();
                           clientMap.get(toNameTmp).out.writeUTF("req_fileSend|[##] [" + name + "] tries to send a file["
                                 + sendFile.getName() + "]. \nDo you accept it?(Y/N)");
                           out.writeUTF("show|[##] Try to send file["+sendFile.getAbsolutePath()+"] to [" + toNameTmp+"].\n");

                        } else {
                           out.writeUTF("show|[##] This file is impossible to send. \n You can send files in [ " + availExtList
                                 + " ]");
                        }
                     } else {
                        out.writeUTF("show|[##] This file does not exits.");
                     }
                  }

               }

               else if (msg.startsWith("fileSend")) { // send file
                  String result = msgArr[0];
                  if (result.equals("yes")) {
                     System.out.println("##FILESEND##YES");
                     try {
                        String tmpfileServerIP = clientMap.get(toNameTmp).fileServerIP;
                        String tmpfilePath = clientMap.get(toNameTmp).filePath;

                        clientMap.get(toNameTmp).out.writeUTF("fileSender|" + tmpfilePath);

                        String fileName = new File(tmpfilePath).getName();
                        out.writeUTF("fileReceiver|" + tmpfileServerIP + "|" + fileName);

                        /* reset */
                        clientMap.get(toNameTmp).filePath = "";
                        clientMap.get(toNameTmp).fileServerIP = "";

                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                  } else /* (result.equals("no")) */ {
                     clientMap.get(toNameTmp).out.writeUTF("show|[##] [" + name + "] has rejected it.");
                  }

               } else if (msg.startsWith("req_exit")) {

               }
            } 
         } catch (Exception e) {
            System.out.println("MultiServerRec:run():" + e.getMessage() + "----> ");
         } finally {

            if (clientMap != null) {
               clientMap.remove(name);
               sendGroupMsg(loc, "[##] [" + name + "] has left.");
               System.out.println("##"+(--MultiServer.connUserCount) + " people are in <SundiSodi>");
            }
         }
      }
   }
}
