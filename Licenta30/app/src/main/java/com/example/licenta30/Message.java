package com.example.licenta30;

public class Message {
    private static String message;
    public Message(String newMessag){
        message = newMessag;
    }

    public static String getMessage() {
        return message;
    }

    public static void setMessage(String message) {
        Message.message = message;
    }
}
