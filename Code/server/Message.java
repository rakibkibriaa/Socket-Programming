package server;

import java.security.PublicKey;

public class Message {
    private boolean isRead;



    private String fromUsername;
    private String toUsername;

    private String message;


    public Message(String fromUsername,String toUsername)
    {
        isRead = false;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
    }
    public Message(String fromUsername,String toUsername,String msgbody)
    {
        isRead = false;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.message = msgbody;
    }


    public boolean isRead() {
        return isRead;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    void makeRead()
    {
        this.isRead = true;
    }


    public String getFromUserUsername() {
        return fromUsername;
    }
}
