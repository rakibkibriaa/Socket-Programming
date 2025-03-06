package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ServerQueryThread implements Runnable
{
    private User user;
    private Server server;

    private boolean exit;
    private Socket serverSocket;




    public ServerQueryThread(User user, Server server, Socket serverSocketFileStrem) {
        this.user = user;
        this.server = server;
        this.serverSocket = serverSocketFileStrem;
    }

    @Override
    public void run() {
        try
        {
           user.getQueryOutputStream().writeUTF("Please Enter Your username");
           user.getQueryOutputStream().flush();

        }
        catch (SocketException e)
        {
            System.out.println("user: "+this.user.getUsername() + " logged out");
            this.user.setActive(false);
            this.user.closeConnection();

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        try
        {
            String clientUsername = user.getQueryInputStream().readUTF();

            if(this.server.isLoggedIn(clientUsername))
            {
                user.getQueryOutputStream().writeUTF("ERROR You are already logged in");
                user.getQueryOutputStream().flush();

                Thread.currentThread().interrupt();

                return;

            }
            else
            {

               User temp_user = this.server.isInUserList(clientUsername);

               if(temp_user == null)
               {
                   this.user.setUsername(clientUsername);
                   this.server.addUser(this.user);

                   new File("Files/" + this.user.getUsername()+"/Public").mkdirs();
                   new File("Files/" + this.user.getUsername()+"/Private").mkdirs();

                   this.user.getQueryOutputStream().writeUTF("Welcome "+ clientUsername);
                   this.user.getQueryOutputStream().flush();
               }
               else
               {

                   this.user.setMessageList(temp_user.getMessageList());

                   this.user.setUsername(temp_user.getUsername());

                   for (HashMap.Entry<Integer, User> entry : this.server.getReqhashMap().entrySet())
                   {
                       if(entry.getValue().getUsername().equals(this.user.getUsername()))
                       {
                           this.server.getReqhashMap().put(entry.getKey(),user);
                       }
                   }
                   for (HashMap.Entry<Integer, User> entry : this.server.getUserFileMap().entrySet())
                   {
                       if(entry.getValue().getUsername().equals(this.user.getUsername()))
                       {
                           this.server.getUserFileMap().put(entry.getKey(),user);
                       }
                   }

                   for(int i=0;i<this.server.getUserList().size();i++)
                   {
                       if(this.server.getUserList().get(i).getUsername().equals(this.user.getUsername()))
                       {
                           this.server.getUserList().remove(i);
                           break;
                       }
                   }

                   this.server.getUserList().add(this.user);

                   this.user.getQueryOutputStream().writeUTF("Welcome "+ this.user.getUsername());
                   this.user.getQueryOutputStream().flush();

               }

            }

        }
        catch (SocketException e)
        {
            System.out.println("user: "+this.user.getUsername() + " logged out");
            this.user.setActive(false);
            this.user.closeConnection();
            Thread.currentThread().interrupt();
            return;
        }
        catch (IOException e)
        {
            System.out.println(e);

        }

        while (true)
        {

            String msgFromClient = null;
            try
            {


                msgFromClient = this.user.getQueryInputStream().readUTF();



                System.out.println(this.user.getUsername() + ": " + msgFromClient);

                StringTokenizer stringTokenizer = new StringTokenizer(msgFromClient," ");
                Vector<String> tokens = new Vector<>();

                while (stringTokenizer.hasMoreTokens())
                {
                    tokens.add(stringTokenizer.nextToken());
                }

                if(tokens.elementAt(0).equalsIgnoreCase("a"))
                {
                    user.getQueryOutputStream().writeUTF(this.server.getAllUsers());
                    user.getQueryOutputStream().flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("b"))
                {
                    String res = "Showing your Files:\n";
                    String[] publicFiles = this.server.getPublicFiles(this.user.getUsername());
                    res += "Public:\n";
                    int count = 1;
                    for(String files : publicFiles)
                    {
                        res += count;
                        res += ". ";
                        res += files;
                        res += "\n";
                        count++;
                    }

                    String[] privateFiles = this.server.getPrivateFiles(this.user.getUsername());
                    res += "Private: \n";

                    count = 1;
                    for(String files : privateFiles)
                    {
                        res += count;
                        res += ". ";
                        res += files;
                        res += "\n";
                        count++;
                    }

                    this.user.getQueryOutputStream().writeUTF(res);
                    this.user.getQueryOutputStream().flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("c"))
                {
                    if(tokens.elementAt(1).equalsIgnoreCase("all"))
                    {
                        ArrayList<User> users = this.server.getUserList();

                        String res = "Showing public files of all clients\n";
                        for(User temp:users)
                        {
                            if(temp.getUsername().equals(this.user.getUsername()))
                            {
                                continue;
                            }
                            res += temp.getUsername();
                            res += ":\n";

                            String [] publicFiles = this.server.getPublicFiles(temp.getUsername());
                            int count = 0;
                            for(String s:publicFiles)
                            {
                                res += count;
                                res += ". ";
                                res += s;
                                res += "\n";
                                count++;
                            }
                        }

                        this.user.getQueryOutputStream().writeUTF(res);
                        this.user.getQueryOutputStream().flush();
                    }
                    else if(this.server.isValidUser(tokens.elementAt(1)))
                    {
                        String res = "Showing Public Files of " + tokens.elementAt(1);
                        res += ": \n";
                        String[] publicFiles = this.server.getPublicFiles(tokens.elementAt(1));

                        int count = 1;
                        for(String files : publicFiles)
                        {
                            res += count;
                            res += ". ";
                            res += files;
                            res += "\n";
                            count++;
                        }
                        this.user.getQueryOutputStream().writeUTF(res);
                        this.user.getQueryOutputStream().flush();
                    }
                    else
                    {
                        this.user.getQueryOutputStream().writeUTF("Not a valid user.");
                        this.user.getQueryOutputStream().flush();

                    }

                }
                else if(tokens.elementAt(0).equalsIgnoreCase("d"))
                {
                    int reqId = this.server.getRequestID();
                    String res = "\n<FileRequest_id>";
                    res += " ";
                    res += reqId;
                    res += " - ";

                    System.out.println(reqId);

                    for(int i=1;i<tokens.size();i++)
                    {
                        res += tokens.elementAt(i);
                        res += " ";
                    }

                    res += "\n";

                    ArrayList<User> userList = this.server.getUserList();

                    for(User temp_user : userList)
                    {
                        if(!this.user.getUsername().equals(temp_user.getUsername()))
                        {
                            Message msg = new Message(this.user.getUsername(),temp_user.getUsername());
                            msg.setMessage(res);

                            temp_user.addMessage(msg);
                        }
                    }
                    this.server.insertReqHashMap(reqId,this.user);
                    this.user.getQueryOutputStream().writeUTF("Request Successfully sent\n");
                    this.user.getQueryOutputStream().flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("sendMessage"))
                {
                    String tousername = tokens.elementAt(1);
                    ArrayList<User> userArrayList = this.server.getUserList();
                    boolean ok = false;

                    if(!tousername.equals(this.user.getUsername()))
                    {
                        for (int i = 0; i < userArrayList.size(); i++) {
                            if (userArrayList.get(i).getUsername().equals(tousername)) {
                                this.user.getQueryOutputStream().writeUTF("SEND_MESSAGE_USER_ACK" + " " + tousername + " " + this.user.getUsername());
                                this.user.getQueryOutputStream().flush();

                                ok = true;
                                break;
                            }
                        }
                    }
                    if(!ok)
                    {
                        this.user.getQueryOutputStream().writeUTF("SEND_MESSAGE_USER_ACK_ERROR");
                        this.user.getQueryOutputStream().flush();
                    }
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("Message"))
                {
                    String fromUsername = tokens.elementAt(1);
                    String toUsername = tokens.elementAt(2);

                    String msg = "";

                    for(int i=3;i<tokens.size();i++)
                    {
                        msg += tokens.elementAt(i);
                        msg += " ";
                    }


                    ArrayList<User> userArrayList = this.server.getUserList();

                    User from = null;
                    User to = null;

                    for(int i=0;i<userArrayList.size();i++)
                    {
                        if(userArrayList.get(i).getUsername().equals(fromUsername))
                        {
                            from = userArrayList.get(i);
                        }
                        else if(userArrayList.get(i).getUsername().equals(toUsername))
                        {
                            to = userArrayList.get(i);
                        }
                    }

                    Message message = new Message(from.getUsername(),to.getUsername(),msg);
                    to.addMessage(message);
                }

                else if(tokens.elementAt(0).equalsIgnoreCase("e"))
                {
                    //System.out.println("Unread Messages: \n");


                    String reply="\nUnread Messages:\n\n";

                    for(Message message : this.user.getMessageList())
                    {
                        if(!message.isRead())
                        {
                            reply += "From: " + message.getFromUserUsername();
                            reply += "\nMessage:\n";
                            reply += message.getMessage();
                            reply += "\n\n";
                        }
                    }


                    reply += "\nAlready read messages:\n";
                    for(Message message : this.user.getMessageList())
                    {
                        if(message.isRead())
                        {
                            reply += "\nFrom: " + message.getFromUserUsername();
                            reply += "\nMessage:\n";
                            reply += message.getMessage();
                            reply += "\n\n";
                        }
                    }

                    for(int i=0;i<this.user.getMessageList().size();i++)
                    {
                        if(!this.user.getMessageList().get(i).isRead())
                        {
                            this.user.getMessageList().get(i).makeRead();
                        }
                    }

                    this.user.getQueryOutputStream().writeUTF(reply);
                    this.user.getQueryOutputStream().flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("logout"))
                {
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    return;
                }
                else
                {
                    this.user.getQueryOutputStream().writeUTF("Invalid command");
                    this.user.getQueryOutputStream().flush();
                }
            }
            catch (SocketException e)
            {
                System.out.println("user: "+this.user.getUsername() + " logged out");

                this.user.setActive(false);
                this.user.closeConnection();
                break;
            }
            catch (IOException e) {
                System.out.println(e);
                break;

            }
            System.out.println(user.getUsername() + ": " +msgFromClient);
        }
    }


}
