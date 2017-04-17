package com.gk.chatapp.app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.entry.UserEntry;
import com.gk.chatapp.utils.SocketIoUtils;
import com.gk.chatapp.utils.SprefUtils;
import com.gk.chatapp.utils.UserStatus;

/**
 * Created by ke.gao on 2017/3/28.
 */

public class App extends Application{

    private static final String TAG = "App";

    private static App mInstance;
    public static synchronized App getInstance(){return mInstance;}
    public static SprefUtils mSpref;

    public UserEntry myEntry;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mSpref = new SprefUtils(getApplicationContext());
        SocketIoUtils.init();
        UserStatus.init();
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

    public UserEntry getMyEntry() {
        return myEntry;
    }

    public void setMyEntry(UserEntry myEntry) {
        this.myEntry = myEntry;
    }

    public static void setmSpref(SprefUtils mSpref) {
        App.mSpref = mSpref;
    }

    public static SprefUtils getmSpref() {
        return mSpref;
    }
}
