package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;
import java.util.Vector;

public class ClientFileThread implements Runnable
{

    private Client client;
    private DataInputStream fileInputStream;
    private DataOutputStream fileOutputStream;

    private Socket clientSocketFile;

    private boolean proceed;
    public ClientFileThread(Client client, DataInputStream fileInputStream,DataOutputStream fileOutputStream,Socket socket)
    {
        this.client = client;
        this.fileInputStream = fileInputStream;
        this.fileOutputStream = fileOutputStream;
        this.clientSocketFile = socket;
    }
    @Override
    public void run()
    {



        while (true)
        {
            String serverMsg = null;

            try {
                serverMsg = fileInputStream.readUTF();
            } catch (IOException e) {

                //System.out.println(e);
                break;
            }


            StringTokenizer stringTokenizer = new StringTokenizer(serverMsg," ");
            Vector<String>tokens = new Vector<>();

            while (stringTokenizer.hasMoreTokens()) {
                tokens.add(stringTokenizer.nextToken());
            }

            if(tokens.elementAt(0).equalsIgnoreCase("upload"))
            {
                int file_id =Integer.parseInt(tokens.elementAt(1));
                String file_name = tokens.elementAt(2);
                String file_type = tokens.elementAt(3);
                int CHUNK_SIZE = Integer.parseInt(tokens.elementAt(4));

                try {
                    clientSocketFile.setSoTimeout(30 * 1000);
                }
                catch (SocketException e) {
                    System.out.println(e);
                    break;
                }

                try
                {
                    if(tokens.size() > 5)
                    {
                        int reqId = Integer.parseInt(tokens.elementAt(5));
                        this.client.sendFile(true,file_id,file_name,file_type,CHUNK_SIZE,fileInputStream,fileOutputStream,reqId);
                    }
                    else
                        this.client.sendFile(false,file_id,file_name,file_type,CHUNK_SIZE,fileInputStream,fileOutputStream,-1);
                }

                catch (IOException e) {
                    System.out.println(e);
                    break;
                }

                try {
                    clientSocketFile.setSoTimeout(0);
                } catch (SocketException e) {
                    System.out.println(e);
                    break;
                }
            }

            else if(tokens.elementAt(0).equalsIgnoreCase("download"))
            {
                // fileOutputStream.writeUTF("download"+" "+fileName+" "+fileLength+" "+getMAX_CHUNK_SIZE());
                String file_name = tokens.elementAt(1);
                int file_size = Integer.parseInt(tokens.elementAt(2));
                int CHUNK_SIZE = Integer.parseInt(tokens.elementAt(3));
                String username = tokens.elementAt(4);
                try {
                    this.client.receiveFile(username,file_name,file_size,CHUNK_SIZE,fileInputStream);
                } catch (Exception e) {
                    System.out.println(e);

                    break;
                }
            }
            else
            {
                System.out.println(serverMsg);
            }

        }
    }


}
