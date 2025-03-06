package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Vector;

public class User
{
    private String username;
    private DataInputStream queryInputStream;
    private DataOutputStream queryOutputStream;
    private DataInputStream fileInputStream;
    private DataOutputStream fileOutputStream;

    private Socket socket;
    private Vector<Message> messageList;
    boolean isActive;

    public User(DataInputStream queryInputStream, DataOutputStream queryOutputStream, DataInputStream fileInputStream, DataOutputStream fileOutputStream)
    {
        this.queryInputStream = queryInputStream;
        this.queryOutputStream = queryOutputStream;
        this.fileInputStream = fileInputStream;
        this.fileOutputStream =  fileOutputStream;

        this.isActive = true;

        messageList = new Vector<Message>();

    }

    public void setUsername(String username) {
        this.username = username;

    }

    public String getUsername() {
        return username;
    }

    public DataInputStream getQueryInputStream() {
        return queryInputStream;
    }

    public DataOutputStream getQueryOutputStream() {
        return queryOutputStream;
    }

    public DataInputStream getFileInputStream() {
        return fileInputStream;
    }

    public DataOutputStream getFileOutputStream() {
        return fileOutputStream;
    }
    public boolean getActiveStatus()
    {
        return this.isActive;
    }
    public void addMessage(Message message)
    {
        this.messageList.add(message);

    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Vector<Message> getMessageList() {
        return messageList;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setFileInputStream(DataInputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }

    public void setFileOutputStream(DataOutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
    }

    public void setQueryInputStream(DataInputStream queryInputStream) {
        this.queryInputStream = queryInputStream;
    }

    public void setQueryOutputStream(DataOutputStream queryOutputStream) {
        this.queryOutputStream = queryOutputStream;
    }

    public void setMessageList(Vector<Message> messageList) {
        this.messageList = messageList;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void closeConnection()
    {
        try
        {
            this.queryInputStream.close();
            this.queryOutputStream.close();
            this.fileInputStream.close();
            this.fileOutputStream.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }


    }
}
