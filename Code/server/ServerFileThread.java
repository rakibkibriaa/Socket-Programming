package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;
import java.util.Vector;

public class ServerFileThread implements Runnable
{

    private Socket serverSocketFile;
    private User user;
    private Server server;
    public ServerFileThread(User user,Server server)
    {
        this.user = user;
        this.server = server;
    }


    public ServerFileThread(User user, Server server, Socket serverSocketFileStrem) {
        this.user = user;
        this.server = server;
        this.serverSocketFile = serverSocketFileStrem;
    }

    @Override
    public void run() {


        while (true) {
            String clientMsg = "";
            try
            {
                clientMsg = this.user.getFileInputStream().readUTF();
            }
            catch (SocketException e)
            {
                System.out.println("user: "+this.user.getUsername() + " logged out");
                this.user.setActive(false);
                this.user.closeConnection();
                Thread.currentThread().interrupt();
                break;
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
            System.out.println(this.user.getUsername() + " : ");

            StringTokenizer stringTokenizer = new StringTokenizer(clientMsg, " ");
            Vector<String> tokens = new Vector<>();

            while (stringTokenizer.hasMoreTokens()) {
                tokens.add(stringTokenizer.nextToken());
            }

            if (tokens.elementAt(0).equalsIgnoreCase("upload"))
            {

                String file_name = tokens.elementAt(1);
                String file_type = tokens.elementAt(2);

                int file_size = Integer.parseInt(tokens.elementAt(3));

                try {
                    this.user.getQueryOutputStream().writeUTF("Received Your upload Request\n");
                    this.user.getQueryOutputStream().flush();
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e)
                {
                    System.out.println(e);
                }

                if (this.server.getCURRENT_BUFFER_SIZE() + file_size > this.server.getMAX_BUFFER_SIZE())
                {
                    System.out.println(this.server.getCURRENT_BUFFER_SIZE());
                    System.out.println(file_size);
                    System.out.println(this.server.getMAX_BUFFER_SIZE());
                    try {
                        this.user.getFileOutputStream().writeUTF("Sorry! Current Buffer Size Full\n Please Try again later");
                        this.user.getFileOutputStream().flush();
                    }
                    catch (SocketException e)
                    {
                        System.out.println("user: "+this.user.getUsername() + " logged out");
                        this.user.setActive(false);
                        this.user.closeConnection();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    catch (IOException e) {
                        System.out.println(e);
                    }
                }
                else
                {
                    int CHUNK_SIZE = (int) (Math.random() * (this.server.getMAX_CHUNK_SIZE() - this.server.getMIN_CHUNK_SIZE() + 1) + this.server.getMIN_CHUNK_SIZE());
                    //int CHUNK_SIZE = 1;
                    int file_id = this.server.getFileID();
                    this.server.insertFileMap(file_id,file_name);
                    this.server.insertUserFileMap(file_id,user);


                    try {
                        this.user.getFileOutputStream().writeUTF("upload" + " " + file_id + " " + file_name + " " + file_type + " " + CHUNK_SIZE);
                        this.user.getFileOutputStream().flush();

                    }
                    catch (SocketException e)
                    {
                        System.out.println("user: "+this.user.getUsername() + " logged out");
                        this.user.setActive(false);
                        this.user.closeConnection();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    catch (IOException e) {
                        System.out.println(e);
                    }
                }


            }
            if (tokens.elementAt(0).equalsIgnoreCase("upload_on_request"))
            {

                String file_name = tokens.elementAt(1);
                String reqId = tokens.elementAt(2);
                String file_type = "Public";

                if(this.server.getUserWhoRequestedFile(Integer.parseInt(reqId)) != null)
                {
                    int file_size = Integer.parseInt(tokens.elementAt(3));

                    try {
                        this.user.getQueryOutputStream().writeUTF("Received Your upload Request\n");
                        this.user.getQueryOutputStream().flush();
                    }
                    catch (SocketException e)
                    {
                        System.out.println("user: "+this.user.getUsername() + " logged out");
                        this.user.setActive(false);
                        this.user.closeConnection();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    catch (IOException e) {
                        System.out.println(e);
                    }

                    if (this.server.getCURRENT_BUFFER_SIZE() + file_size > this.server.getMAX_BUFFER_SIZE())
                    {

                        try {
                            this.user.getFileOutputStream().writeUTF("Sorry! Current Buffer Size Full\n Please Try again later");
                            this.user.getFileOutputStream().flush();
                        }
                        catch (SocketException e)
                        {
                            System.out.println("user: "+this.user.getUsername() + " logged out");
                            this.user.setActive(false);
                            this.user.closeConnection();
                            Thread.currentThread().interrupt();
                            break;
                        }
                        catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                    else
                    {
                        int CHUNK_SIZE = (int) (Math.random() * (this.server.getMAX_CHUNK_SIZE() - this.server.getMIN_CHUNK_SIZE() + 1) + this.server.getMIN_CHUNK_SIZE());

                        int file_id = this.server.getFileID();


                        this.server.insertFileMap(file_id,file_name);
                        this.server.insertUserFileMap(file_id,user);

                        try {
                            this.user.getFileOutputStream().writeUTF("upload" + " " + file_id + " " + file_name + " " + file_type + " " + CHUNK_SIZE + " " + reqId);
                            this.user.getFileOutputStream().flush();

                        }
                        catch (SocketException e)
                        {
                            System.out.println("user: "+this.user.getUsername() + " logged out");
                            this.user.setActive(false);
                            this.user.closeConnection();
                            Thread.currentThread().interrupt();
                            break;
                        }
                        catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                }
                else
                {
                    try {
                        this.user.getQueryOutputStream().writeUTF("Sorry your request ID is invalid");
                        this.user.getFileOutputStream().flush();
                    }
                    catch (SocketException e)
                    {
                        System.out.println("user: "+this.user.getUsername() + " logged out");
                        this.user.setActive(false);
                        this.user.closeConnection();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    catch (IOException e) {
                        System.out.println(e);
                    }

                }



            }
            if (tokens.elementAt(0).equalsIgnoreCase("uploading"))
            {

                int file_id = Integer.parseInt(tokens.elementAt(1));
                int file_size = Integer.parseInt(tokens.elementAt(2));
                String file_Type = tokens.elementAt(3);
                int CHUNK_SIZE = Integer.parseInt(tokens.elementAt(4));
                boolean isReq  = Boolean.parseBoolean(tokens.elementAt(5));
                int reqId = Integer.parseInt(tokens.elementAt(6));

                String file_Name = this.server.getFileFromFileMap(file_id);

                try {
                    this.serverSocketFile.setSoTimeout(30 * 1000);
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }

                try {
                    this.server.receiveFile(this.user.getUsername(), file_id, file_Name, file_Type, file_size, this.user.getFileInputStream(), this.user.getFileOutputStream(), CHUNK_SIZE,isReq,reqId);
                }
                catch (SocketException socketException)
                {
                    File file = new File("files/"+this.user.getUsername()+"/"+file_Type+"/"+file_Name);

                    file.delete();

                    this.server.getUserFileMap().remove(file_id);
                    this.server.getFileMap().remove(file_id);

                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e)
                {
                    File file = new File("files/"+this.user.getUsername()+"/"+file_Type+"/"+file_Name);
                    file.delete();
                    this.server.getUserFileMap().remove(file_id);
                    this.server.getFileMap().remove(file_id);
                    try {
                        this.user.getFileOutputStream().writeUTF("Exception Occured");
                        this.user.getFileOutputStream().flush();
                    }
                    catch (SocketException socketException)
                    {
                        System.out.println("user: "+this.user.getUsername() + " logged out");
                        this.user.setActive(false);
                        this.user.closeConnection();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    catch (IOException ex) {
                        System.out.println(ex);
                    }

                }
                try {
                    this.serverSocketFile.setSoTimeout(0);
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }

            }
            if(tokens.elementAt(0).equalsIgnoreCase("c_download"))
            {
                String uploader_user_name = tokens.elementAt(1);
                String file_name = tokens.elementAt(2);
                String file_type = "Public";

                try {
                    this.user.getQueryOutputStream().writeUTF("Download Started");
                    this.user.getQueryOutputStream().flush();
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e) {
                    System.out.println(e);
                }

                try {
                    this.server.sendFile(uploader_user_name,file_name,file_type,this.user.getFileOutputStream(),this.user.getUsername());
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e) {
                    System.out.println(e);
                }


            }
            if(tokens.elementAt(0).equalsIgnoreCase("b_download")) //b_download file_name file_type
            {
                String file_type = tokens.elementAt(2);
                String file_name = tokens.elementAt(1);


                try {
                    this.user.getQueryOutputStream().writeUTF("Download Started");
                    this.user.getQueryOutputStream().flush();
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e) {
                    System.out.println(e);
                }

                try {
                    this.server.sendFile(this.user.getUsername(),file_name,file_type,this.user.getFileOutputStream(),this.user.getUsername());
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e) {
                    System.out.println(e);
                }


            }
            if(tokens.elementAt(0).equals("TIMEOUT"))
            {
                String fileType = tokens.elementAt(1);
                String fileName = tokens.elementAt(2);
                String file_id = tokens.elementAt(3);
                File file = new File("files/"+this.user.getUsername()+"/"+fileType+"/"+fileName);
                System.out.println(file.delete());

                this.server.getUserFileMap().remove(file_id);
                this.server.getFileMap().remove(file_id);

                try {
                    this.user.getFileOutputStream().writeUTF("File " + fileName + " Deleted");
                    this.user.getFileOutputStream().flush();
                }
                catch (SocketException e)
                {
                    System.out.println("user: "+this.user.getUsername() + " logged out");
                    this.user.setActive(false);
                    this.user.closeConnection();
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (IOException e) {
                    System.out.println(e);
                }
            }
            if(tokens.elementAt(0).equalsIgnoreCase("Error"))
            {
                this.user.closeConnection();
                Thread.currentThread().interrupt();
                break;
            }
            if (tokens.elementAt(0).equalsIgnoreCase("logout"))
            {
                this.user.setActive(false);
                this.user.closeConnection();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


}



