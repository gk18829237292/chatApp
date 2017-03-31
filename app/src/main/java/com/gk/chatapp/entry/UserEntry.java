package com.gk.chatapp.entry;

/**
 * Created by ke.gao on 2017/3/30.
 */

public class UserEntry {

    private String account;
    private String nickName;
    private String signature;

    private boolean onLine;

    public UserEntry() {
    }

    public UserEntry(String account, String nickName, String signature, boolean onLine) {
        this.account = account;
        this.nickName = nickName;
        this.signature = signature;
        this.onLine = onLine;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
