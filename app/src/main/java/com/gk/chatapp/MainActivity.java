package com.gk.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static String serer_address = "http://10.64.33.43:3000";
    private static Socket client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public static void main(String[] args){
        try {
            client = IO.socket(serer_address);
            client.connect();
            if(client.connected()){
                System.out.println("ok");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
