package com.gk.chatapp.app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.gk.chatapp.utils.SocketIOUtils;

/**
 * Created by ke.gao on 2017/3/28.
 */

public class App extends Application{

    private static final String TAG = "App";

    private static App mInstance;
    public static synchronized App getInstance(){return mInstance;}

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SocketIOUtils.init();
        Log.d(TAG,"create");
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            return  true;
        }
        return  false;
    }
}
