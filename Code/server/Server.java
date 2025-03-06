package server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private ServerSocket serverSocketQuery;
    private ServerSocket serverSocketFileStrem;

    private ArrayList<User> userList = new ArrayList<User>();

    private int requestID;

    private int MIN_CHUNK_SIZE;

    private long  MAX_BUFFER_SIZE;

    private int MAX_CHUNK_SIZE;

    private long CURRENT_BUFFER_SIZE;

    private int currentFileId;

    private User user;

    private HashMap<Integer, User> reqhashMap;

    private HashMap<Integer, String> fileMap;
    private HashMap<Integer,User> userFileMap;

    Server(int MAX_BUFFER_SIZE,int MIN_CHUNK_SIZE,int MAX_CHUNK_SIZE) {

        System.out.println("Sever Started");

        this.CURRENT_BUFFER_SIZE = 0;

        this.setMAX_BUFFER_SIZE(MAX_BUFFER_SIZE);
        this.setMIN_CHUNK_SIZE(MIN_CHUNK_SIZE);
        this.setMAX_CHUNK_SIZE(MAX_CHUNK_SIZE);

        requestID = 0;

        currentFileId = 0;

        reqhashMap = new HashMap<Integer, User>();
        fileMap = new HashMap<>();
        userFileMap = new HashMap<>();

        try {
            serverSocketQuery = new ServerSocket(33333);
            serverSocketFileStrem = new ServerSocket(44444);

            while (true)
            {
                Socket querySocket = serverSocketQuery.accept();
                DataInputStream queryInputStream = new DataInputStream(querySocket.getInputStream());
                DataOutputStream queryOutputStream = new DataOutputStream(querySocket.getOutputStream());

                Socket fileStreamSocket = serverSocketFileStrem.accept();
                DataInputStream fileInputStream = new DataInputStream(fileStreamSocket.getInputStream());
                DataOutputStream fileOutputStream = new DataOutputStream(fileStreamSocket.getOutputStream());

                User user = new User(queryInputStream,queryOutputStream,fileInputStream,fileOutputStream);

                new Thread(new ServerQueryThread(user,this,querySocket)).start();

                new Thread(new ServerFileThread(user,this,fileStreamSocket)).start();

            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public synchronized void setMAX_BUFFER_SIZE(int MAX_BUFFER_SIZE) {
        this.MAX_BUFFER_SIZE = MAX_BUFFER_SIZE * 1024;
    }

    public synchronized void setMAX_CHUNK_SIZE(int MAX_CHUNK_SIZE) {
        this.MAX_CHUNK_SIZE = MAX_CHUNK_SIZE * 1024;
    }

    public synchronized void setMIN_CHUNK_SIZE(int MIN_CHUNK_SIZE) {
        this.MIN_CHUNK_SIZE = MIN_CHUNK_SIZE * 1024;
    }

    public synchronized  long getMAX_BUFFER_SIZE() {
        return this.MAX_BUFFER_SIZE;
    }

    public synchronized int getMAX_CHUNK_SIZE() {
        return MAX_CHUNK_SIZE;
    }

    public synchronized int getMIN_CHUNK_SIZE() {
        return MIN_CHUNK_SIZE;
    }

    public synchronized long getCURRENT_BUFFER_SIZE() {
        return CURRENT_BUFFER_SIZE;
    }
    public synchronized  void increaseCURRENT_BUFFER_SIZE(int size)
    {
        this.CURRENT_BUFFER_SIZE += size;
    }
    public synchronized  void decreaseCURRENT_BUFFER_SIZE(int size)
    {
        this.CURRENT_BUFFER_SIZE -= size;
    }

    public ServerSocket getServerSocketQuery() {
        return serverSocketQuery;
    }

    public ServerSocket getServerSocketFileStrem() {
        return serverSocketFileStrem;
    }

    public void addUser(User user)
    {
        this.userList.add(user);
    }



    public static void main(String args[]) {
        Server server = new Server(5000,5,10);

    }

    public boolean isLoggedIn(String clientUsername)
    {

        for(int i=0;i<userList.size();i++)
        {
            if(userList.get(i).getUsername().equals(clientUsername))
            {
                return userList.get(i).getActiveStatus();
            }
        }
        return false;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public User isInUserList(String clientUsername)
    {
        for(int i=0;i<userList.size();i++)
        {
            if(userList.get(i).getUsername().equals(clientUsername))
            {

                return userList.get(i);
            }
        }

        return null;
    }

    public String getAllUsers()
    {

        String res = "";
        for(int i=0;i<userList.size();i++)
        {
            res += userList.get(i).getUsername();

            if(userList.get(i).isActive)
            {
                res += " <  Online > ";
            }
            else
            {
                res += " <  Offline > ";
            }

            res += "\n";
        }
        return res;
    }


    public String[] getPublicFiles(String clientUsername)
    {
        return new File("Files/"+clientUsername+"/Public").list();
    }

    public String[] getPrivateFiles(String clientUsername) {
        return new File("Files/"+clientUsername+"/Private").list();
    }

    public boolean isValidUser(String clientUsername) {
        for(int i=0;i<userList.size();i++)
        {
            if(userList.get(i).getUsername().equals(clientUsername))
            {
                return true;
            }

        }
        return false;
    }
    public synchronized int getRequestID()
    {

        int temp = requestID;

        requestID++;

        return temp;
    }
    public synchronized int getFileID() {
        int temp = this.currentFileId;
        this.currentFileId++;
        return temp;
    }

    public void insertFileMap(int file_id,String file_name)
    {
        this.fileMap.put(file_id,file_name);
    }
    public void insertUserFileMap(int file_id,User user)
    {
        this.userFileMap.put(file_id,user);
    }
    public String getFileFromFileMap(int file_id)
    {
        return this.fileMap.get(file_id);
    }
    public User getUserFromFileMap(int file_id)
    {
        return this.userFileMap.get(file_id);
    }

    public void insertReqHashMap(int reqId,User user)
    {
        reqhashMap.put(reqId,user);
    }
    public void removeFromReqHashMap(int requestID)
    {
        reqhashMap.remove(requestID);
    }
    public User getUserWhoRequestedFile(int requestID)
    {
        return reqhashMap.get(requestID);
    }

    public void receiveFile(String username,int file_id,String fileName, String fileType, int fileSize, DataInputStream fileInputStream, DataOutputStream fileOutputStream, int CHUNK_SIZE,boolean isReq, int reqID) throws IOException
    {
        int hasRead = 0;
        FileOutputStream fileStream = new FileOutputStream("files/"+username+"/"+fileType+"/"+fileName);
        int readSize = 0;
        int size = fileSize;     // read file size
        byte[] buffer = new byte[CHUNK_SIZE];

        this.increaseCURRENT_BUFFER_SIZE(CHUNK_SIZE);


        while (size > 0)
        {

            try
            {

                hasRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size));
            }
            catch (SocketTimeoutException socketTimeoutException)
            {

                this.decreaseCURRENT_BUFFER_SIZE(CHUNK_SIZE);

                System.out.println("Uploading File of " + username + " failed");

                fileStream.close();

                return;
            }
            catch (SocketException socketException)
            {

                fileStream.close();

                this.decreaseCURRENT_BUFFER_SIZE(CHUNK_SIZE);

                File file = new File("files/"+username+"/"+fileType+"/"+fileName);

                file.delete();

                this.userFileMap.remove(file_id);
                this.fileMap.remove(file_id);


                return;
            }

            readSize += hasRead;

            if(hasRead != -1)
            {
                fileStream.write(buffer,0,hasRead);
            }
            else
            {
                break;
            }

            size -= hasRead;      // read upto file size
            // send ACK
            fileOutputStream.writeUTF("ACK");
            fileOutputStream.flush();

            System.out.println(hasRead);


        }

        this.decreaseCURRENT_BUFFER_SIZE(CHUNK_SIZE);

        fileStream.close();
        // check confirmation and validate file size
        String msg = fileInputStream.readUTF();
        if(msg.equalsIgnoreCase("complete"))
        {
            File file = new File("files/"+username+"/"+fileType+"/"+fileName);
            if(file.length() != readSize)
            {
                System.out.println("File size mismatch");

                fileOutputStream.writeUTF("ERROR! File size didnt match. Try again! \n");
                fileOutputStream.flush();
                file.delete();
                this.userFileMap.remove(file_id);
                this.fileMap.remove(file_id);
            }
            else
            {
                fileOutputStream.writeUTF("RECEIVE_SUCCESS");
                fileOutputStream.flush();

                if(isReq)
                {
                    User user = this.reqhashMap.get(reqID);
                    User sourceUser = null;
                    for(User srcUser : userList)
                    {
                        if(srcUser.getUsername().equals(username))
                        {
                            sourceUser = srcUser;
                            break;
                        }
                    }
                    Message message = new Message(sourceUser.getUsername() , user.getUsername());
                    message.setMessage("Your requested file with reqID " + reqID + " has been uploaded by " + username + "\n");
                    user.addMessage(message);
                }
            }
        }
        else
        {
            File file = new File("files/"+username+"/"+fileType+"/"+fileName);
            file.delete();

            this.userFileMap.remove(file_id);
            this.fileMap.remove(file_id);
        }

    }

    public void sendFile(String uploader_username, String fileName, String fileType, DataOutputStream fileOutputStream,String downloader_username) throws IOException {

        File file = new File("files/"+uploader_username+"/"+fileType+"/"+fileName);
        FileInputStream fileStream = new FileInputStream(file);

        try{
            long fileLength = file.length();

            fileOutputStream.writeUTF("download"+" "+fileName+" "+fileLength+" "+getMAX_CHUNK_SIZE() + " " + downloader_username);

            fileOutputStream.flush();

            int hasRead = 0;
            byte[] buffer = new byte[getMAX_CHUNK_SIZE()];

            while ((hasRead=fileStream.read(buffer))!=-1){

                fileOutputStream.write(buffer,0,hasRead);
                fileOutputStream.flush();

            }

            fileOutputStream.writeUTF("download_complete");
            fileOutputStream.flush();
            fileStream.close();

        }catch (Exception e)
        {
            fileStream.close();
        }
    }

    public HashMap<Integer, String> getFileMap() {
        return fileMap;
    }

    public HashMap<Integer, User> getReqhashMap() {
        return reqhashMap;
    }

    public HashMap<Integer, User> getUserFileMap() {
        return userFileMap;
    }
}

