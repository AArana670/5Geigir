package com.example.a5geigir;

public class TokenProvider {

    private static TokenProvider instance = null;

    private TokenProvider(){}

    public static TokenProvider getInstance(){
        if(instance == null)
            instance = new TokenProvider();
        return instance;
    }

    public String getToken(){
        return "";
    }

}
