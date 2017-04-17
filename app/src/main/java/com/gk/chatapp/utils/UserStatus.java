package com.gk.chatapp.utils;

import android.nfc.Tag;
import android.util.Log;

import com.gk.chatapp.app.App;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
                UserEntry entry = new UserEntry((String)args[0],(String)args[1],(String)args[2],(String)args[3],false);
                onNewUser(entry);
            }
        });

        SocketIoUtils.registerListener("login_client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String account = (String) args[0];
                Log.d(TAG,account +" login");
                onLogin(account);
            }
        });

        SocketIoUtils.registerListener("logout_client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String account = (String) args[0];
                Log.d(TAG,account +" logout");
                onLogout(account);
            }
        });

        SocketIoUtils.registerListener("getAllUser_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray userList = new JSONArray(args[0].toString());
                    allUserList.clear();
                    Log.d(TAG,userList.toString());
                    for(int i =0;i<userList.length();i++){
                        JSONObject jsonObject = userList.getJSONObject(i);
                        UserEntry entry = new UserEntry(jsonObject.getString(Constant.ACCOUNT),jsonObject.getString(Constant.NICKNAME),jsonObject.getString(Constant.SIGNATURE),jsonObject.getString(Constant.IMAGE),false);
                        allUserList.put(entry.getAccount(),entry);
                    }
                    String str = args[1].toString();
                    JSONObject userListOnline = new JSONObject(str);
                    onLineUserList.clear();
                    Iterator<String> iterator = userListOnline.keys();
                    while (iterator.hasNext()){
                        String account = iterator.next();
                        if(!allUserList.containsKey(account)) {
                            continue;
                        }
                        allUserList.get(account).setOnLine(true);
                        onLineUserList.put(account,allUserList.get(account));
                    }
                    removeMySelf();
                    //填充 最近联系
                    fillRecent();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        SocketIoUtils.registerListener("userUpdate_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String account = (String) args[0];
                if(allUserList.containsKey(account)){
                    String nickName = (String) args[1];
                    String signature = (String) args[2];
                    String image= (String) args[3];
                    allUserList.get(account).setNickName(nickName);
                    allUserList.get(account).setSignature(signature);
                    allUserList.get(account).setImg_url(image);
                }
            }
        });

//        SocketIoUtils.sendMessage("getAllUser","");
    }

    public static void onNewUser(UserEntry userEntry){
        if(!allUserList.containsKey(userEntry.getAccount())){
            allUserList.put(userEntry.getAccount(),userEntry);
        }
    }

    public static void onLogin(String account){
        if(allUserList.containsKey(account)){
            UserEntry entry = allUserList.get(account);
            entry.setOnLine(true);
            onLineUserList.put(account,entry);
        }
    }

    public static void onLogout(String account){
        if(allUserList.containsKey(account)){
            allUserList.get(account).setOnLine(false);
        }
        if(onLineUserList.containsKey(account)){
            onLineUserList.remove(account);
        }
    }

    public static void removeMySelf(){
        if(App.getInstance().getMyEntry() != null){
            String account = App.getInstance().getMyEntry().getAccount();
            if(allUserList.containsKey(account)) allUserList.remove(account);
            if(onLineUserList.containsKey(account)) onLineUserList.remove(account);
            if(recentUserList.containsKey(account)) recentUserList.remove(account);
        }
    }

    public static void removeRecent(String account){
        if(recentUserList.containsKey(account)){
            recentUserList.remove(account);
        }
    }

    public static void addRecent(String account){
        if(!recentUserList.containsKey(account) && allUserList.containsKey(account)){
            recentUserList.put(account,allUserList.get(account));
        }
    }

    public static void fillRecent(){
        for(String account:App.getmSpref().getStringSet(Constant.PARAM_RECENT_USER+App.getInstance().getMyEntry().getAccount(),new HashSet<String>())){
            addRecent(account);
        }
    }

    public static void logout(){
        recentUserList.clear();
        onLineUserList.clear();
        allUserList.clear();

    }

}
