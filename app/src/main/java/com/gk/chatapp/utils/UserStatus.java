package com.gk.chatapp.utils;

import com.gk.chatapp.entry.UserEntry;
import com.gk.chatapp.fragment.UserListFragment;
import com.gk.chatapp.model.DrawerItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import io.socket.emitter.Emitter;

/**
 * Created by ke.gao on 2017/3/31.
 */

public class UserStatus {

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
