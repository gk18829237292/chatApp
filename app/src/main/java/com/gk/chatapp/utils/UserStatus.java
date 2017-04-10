package com.gk.chatapp.utils;

import android.nfc.Tag;
import android.util.Log;

import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.entry.UserEntry;
import com.gk.chatapp.fragment.UserListFragment;
import com.gk.chatapp.model.DrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import io.socket.emitter.Emitter;

/**
 * Created by ke.gao on 2017/3/31.
 */

public class UserStatus {

    private static final String TAG = "UserStatus";

    public static final Map<String,UserEntry> recentUserList = new HashMap<>();
    public static final Map<String,UserEntry> onLineUserList = new HashMap<>();
    public static final Map<String,UserEntry> allUserList = new HashMap<>();

    public static Collection<UserEntry> getUserList(int drawTag){
        Collection<UserEntry> userEntries = null;
        switch (drawTag){
            case DrawerItem.DRAWER_ITEM_TAG_RECENT:
                userEntries = recentUserList.values();
                break;
            case DrawerItem.DRAWER_ITEM_TAG_ONLINEUSER:
                userEntries = onLineUserList.values();
                break;
            case DrawerItem.DRAWER_ITEM_TAG_ALLUSER:
                userEntries = allUserList.values();
                break;
        }
        return userEntries;
    }

    public static void init(){
        SocketIoUtils.registerListener("newUser", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                UserEntry entry = new UserEntry((String)args[0],(String)args[1],(String)args[2],false);
                onNewUser(entry);
            }
        });

        SocketIoUtils.registerListener("login", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String account = (String) args[0];
                onLogin(account);
            }
        });

        SocketIoUtils.registerListener("logout", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String account = (String) args[0];
                onLogout(account);
            }
        });

        SocketIoUtils.registerListener("getAllUser_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray jsonArray = new JSONArray(args[0].toString());
                    for(int i =0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        UserEntry entry = new UserEntry(jsonObject.getString(Constant.ACCOUNT),jsonObject.getString(Constant.NICKNAME),jsonObject.getString(Constant.SIGNATURE),false);
                        allUserList.put(entry.getAccount(),entry);
                    }
                    SocketIoUtils.sendMessage("getAllUserOnline","");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        SocketIoUtils.registerListener("getAllUserOnline_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String[] onlineUsers= (String[]) args[0];
                for(String account:onlineUsers){
                    onLineUserList.put(account,allUserList.get(account));
                }
            }
        });

        SocketIoUtils.sendMessage("getAllUser","");
    }

    public static void onNewUser(UserEntry userEntry){
        if(!allUserList.containsKey(userEntry.getAccount())){
            allUserList.put(userEntry.getAccount(),userEntry);
        }
    }

    public static void onLogin(String account){
        UserEntry entry = allUserList.get(account);
        entry.setOnLine(true);
        onLineUserList.put(account,entry);
    }

    public static void onLogout(String account){
        allUserList.get(account).setOnLine(false);
        if(onLineUserList.containsKey(account)){
            onLineUserList.remove(account);
        }
    }



}
