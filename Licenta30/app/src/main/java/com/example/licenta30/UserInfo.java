package com.example.licenta30;

public class UserInfo {
    private static UserInfo instance = null;
    private final String email;
    private UserInfo(String email){
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    public static void setInstance(UserInfo instance) { UserInfo.instance = instance; }
    public static synchronized UserInfo getInstance(String email) {
        if (instance == null) {
            instance = new UserInfo(email);
        }
        return instance;
    }
}
