package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public class ClientQueryThread implements Runnable
{

    private  Client client;
    DataInputStream queryInputStream;
    DataOutputStream queryOutputStream;

    DataInputStream fileInputStream;
    DataOutputStream fileOutputStream;

    private boolean showUI;

   private Socket socket;

    public ClientQueryThread(Client client,DataInputStream queryInputStream, DataOutputStream queryOutputStream,DataInputStream fileInputStream,DataOutputStream fileOutputStream, Socket clientFileSocket)
    {
        this.queryInputStream = queryInputStream;
        this.queryOutputStream = queryOutputStream;
        this.fileInputStream = fileInputStream;
        this.fileOutputStream = fileOutputStream;
        this.socket = clientFileSocket;
        this.client = client;
    }

    @Override
    public void run()
    {
        try {
            String msgFromServer = queryInputStream.readUTF();

            System.out.println("Server: " + msgFromServer);

        }
        catch (IOException e)
        {
            System.out.println(e);
            return;

        }
        String username = null;
        while(true)
        {


            if(showUI)
            {
                System.out.println("\n");
                System.out.println("Username: " + username + "\n");
                System.out.println("1.\ta  -  Show users active status\n");

                System.out.println("2.\tb  -  Show your all files stored in server");
                System.out.println("3.\tb_download <file_name> <directory> - to download own files from public/private directory\n");

                System.out.println("4.\tc <username/all> -  Show others public files");
                System.out.println("5.\tc_download <username> <file_name> - download public file of that user\n");

                System.out.println("6.\td <description_of_file> - make a file request\n");
                System.out.println("7.\te - View inbox\n");
                System.out.println("8.\tf - show local files\n");


                System.out.println("9.\tupload <file_name> <directory> - upload your file to public/private directory\n");
                System.out.println("10.\tupload_on_request <file_name> <req_id> - upload requested file\n");

                System.out.println("11.\tsendmessage <user_name> - sends message to specific user\n");

                System.out.println("12.\tlogout\n");
            }


            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();




            StringTokenizer stringTokenizer = new StringTokenizer(msg," ");
            Vector<String> tokens = new Vector<>();

            while (stringTokenizer.hasMoreTokens())
            {
                tokens.add(stringTokenizer.nextToken());
            }


            try
            {

                if(tokens.elementAt(0).equalsIgnoreCase("upload")) //upload file_name
                {

                    String file_name = tokens.elementAt(1);
                    File file = new File("local/"+file_name);
                    FileInputStream fileStream = new FileInputStream(file);

                    msg += " ";
                    msg += file.length();


                    fileOutputStream.writeUTF(msg);
                    fileOutputStream.flush();

                    fileStream.close();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("upload_on_request")) //upload_on request file_name req_id
                {
                    String fileName = tokens.elementAt(1);

                    File file = new File("local/"+fileName);

                    msg += " ";
                    msg += file.length();

                    fileOutputStream.writeUTF(msg);
                    fileOutputStream.flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("c_download") || tokens.elementAt(0).equalsIgnoreCase("b_download"))  //download user_name file_name
                {
                    fileOutputStream.writeUTF(msg);
                    fileOutputStream.flush();
                }
                else if(tokens.elementAt(0).equalsIgnoreCase("f"))
                {
                    String [] allfiles = new File("local/").list();

                    int count = 1;

                    for(String files : allfiles)
                    {
                        System.out.println(count + ". " + files);
                        count++;
                    }
                    System.out.println("\n");
                    continue;

                }
                else if(tokens.elementAt(0).equalsIgnoreCase("logout"))
                {

                    queryOutputStream.writeUTF(msg);
                    queryOutputStream.flush();

                    fileOutputStream.writeUTF(msg);
                    fileOutputStream.flush();

                    queryOutputStream.close();
                    queryInputStream.close();
                    fileOutputStream.close();
                    fileInputStream.close();

                    Thread.currentThread().interrupt();
                    return;

                }

                else
                {
                    queryOutputStream.writeUTF(msg);
                    queryOutputStream.flush();


                }

            }
            catch (IOException e)
            {
                System.out.println(e);
                break;
            }

            try
            {
                String msgFromServer = queryInputStream.readUTF();

                StringTokenizer serverMsgTokenizer = new StringTokenizer(msgFromServer," ");
                Vector<String> serverMsgTokens = new Vector<>();

                while (serverMsgTokenizer.hasMoreTokens())
                {
                    serverMsgTokens.add(serverMsgTokenizer.nextToken());
                }

                if(serverMsgTokens.elementAt(0).equalsIgnoreCase("SEND_MESSAGE_USER_ACK"))
                {
                    System.out.println("Server: Enter your message: ");

                    String message = scanner.nextLine();
                    String tousername = serverMsgTokens.elementAt(1);
                    String fromusername = serverMsgTokens.elementAt(2);

                    this.queryOutputStream.writeUTF("Message"+" "+fromusername+" "+tousername+" "+message);
                    this.queryOutputStream.flush();

                    System.out.println(message);
                }
                else if(serverMsgTokens.elementAt(0).equalsIgnoreCase("SEND_MESSAGE_USER_ACK_ERROR"))
                {
                    System.out.println("Server: Invalid username");
                }
                else if(serverMsgTokens.elementAt(0).equalsIgnoreCase("ERROR"))
                {
                    System.out.println(msgFromServer);

                    fileOutputStream.writeUTF("Error");
                    fileOutputStream.flush();

                    queryInputStream.close();
                    queryOutputStream.close();
                    fileInputStream.close();
                    fileOutputStream.close();

                    break;
                }
                else if(serverMsgTokens.elementAt(0).equalsIgnoreCase("welcome"))
                {
                    showUI = true;
                    username = serverMsgTokens.elementAt(1);

                    new File("Downloads/" + username +"/").mkdirs();

                    System.out.println(msgFromServer);
                }
                else
                {
                    System.out.println("Server:\n" + msgFromServer);
                    System.out.println();
                }


            }
            catch (IOException e)
            {
                System.out.println(e);
                break;
            }

        }
    }
}
