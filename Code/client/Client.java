package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {

    private DataInputStream queryInputStream;
    private DataOutputStream queryOutputStream;

    private DataInputStream fileInputStream;
    private DataOutputStream fileOutputStream;

    private Socket clientQuerySocket;

    private Socket clientFileSocket;


    public Client(String serverAddress, int serverPort) {

        try
        {
            this.clientQuerySocket = new Socket("127.0.0.1",33333);


            this.queryInputStream = new DataInputStream(clientQuerySocket.getInputStream());
            this.queryOutputStream = new DataOutputStream(clientQuerySocket.getOutputStream());

            this.clientFileSocket = new Socket("127.0.0.1",44444);

            this.fileInputStream = new DataInputStream(clientFileSocket.getInputStream());
            this.fileOutputStream = new DataOutputStream(clientFileSocket.getOutputStream());

            new Thread(new ClientQueryThread(this,queryInputStream,queryOutputStream,fileInputStream,fileOutputStream,clientQuerySocket)).start();

            new Thread(new ClientFileThread(this,fileInputStream,fileOutputStream,clientFileSocket)).start();


        } catch (Exception e) {
            System.out.println(e);
        }
    }



    public static void main(String args[]) {
        String serverAddress = "127.0.0.1";
        int serverPort = 33333;
        Client client = new Client(serverAddress, serverPort);

        new File("local/").mkdirs();
    }

    public void sendFile(boolean isReq, int file_id, String fileName, String fileType, int CHUNK_SIZE, DataInputStream fileInputStream, DataOutputStream fileOutputStream,int reqId) throws IOException {

        File file = new File("local/"+fileName);
        FileInputStream fileStream = new FileInputStream(file);

        long fileLength = file.length();

        fileOutputStream.writeUTF("uploading"+" "+file_id+" "+fileLength +" " + fileType+" "+CHUNK_SIZE + " " + isReq + " " + reqId);
        fileOutputStream.flush();

        // break file into chunks

        byte[] buffer = new byte[CHUNK_SIZE];
        int hasRead;
        while ((hasRead=fileStream.read(buffer))!=-1){

            System.out.println(hasRead);
            fileOutputStream.write(buffer,0,hasRead);
            fileOutputStream.flush();

            try {

                String msg = fileInputStream.readUTF();
                if(!msg.equals("ACK"))
                {
                    System.out.println("Did not receive ACK ");
                    break;
                }

            }
            catch (SocketTimeoutException socketTimeoutException){

                fileStream.close();
                System.out.println("TIMEOUT");
                fileOutputStream.writeUTF("TIMEOUT"+" "+fileType+" "+fileName+" "+file_id);
                fileOutputStream.flush();

                return;
            }
        }
        fileStream.close();

        // send confirmation
        fileOutputStream.writeUTF("complete");
        fileOutputStream.flush();

        String msg = fileInputStream.readUTF();
        if(msg.equalsIgnoreCase("RECEIVE_SUCCESS")) System.out.println("File Upload Completed");
        else System.out.println(msg);
    }

    public void receiveFile(String username, String fileName, int fileSize, int CHUNK_SIZE, DataInputStream fileInputStream) throws IOException {
        int hasRead = 0;
        FileOutputStream fileStream = new FileOutputStream("Downloads/"+username+"/"+fileName);

        int size = fileSize;     // read file size
        byte[] buffer = new byte[CHUNK_SIZE];

        while (size > 0 )
        {
//
            try {
                hasRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size));

                if (hasRead != -1) {
                    fileStream.write(buffer, 0, hasRead);
                    size -= hasRead;
                } else {
                    break;
                }
            }
            catch (Exception e)
            {
                File file = new File("Downloads/"+username+"/"+fileName);
                file.delete();
                fileStream.close();
                return;
            }

        }

        if(fileInputStream.readUTF().equalsIgnoreCase("download_complete"))
        {
            System.out.println("File successfully Downloaded");
        }
        else
        {
            System.out.println("Error while downloading the file.. Try again later");
        }

        fileStream.close();
    }


}


