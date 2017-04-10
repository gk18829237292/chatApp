package com.gk.chatapp.utils;


import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by ke.gao on 2017/3/28.
 */

public class SocketIoUtils {
    private static String serverUrl="http://10.64.33.43:3000";

    private static Socket client;

    public static void init(){
        try {
            client = IO.socket(serverUrl);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void registerListener(String event, Emitter.Listener listener){
        client.on(event,listener);
    }

    public static void unRegisterListener(String event){
        client.off(event);
    }

    public static void unRegisterListener(String event, Emitter.Listener listener){
        client.off(event,listener);
    }

    public static synchronized void sendMessage(String event, Object... args){
        client.emit(event,args);
    }

    public static void close(){
        client.close();
    }
}
