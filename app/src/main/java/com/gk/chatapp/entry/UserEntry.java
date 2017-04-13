package com.gk.chatapp.entry;

import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.StringUtils;

/**
 * Created by ke.gao on 2017/3/30.
 */

public class UserEntry {

    private String account;
    private String nickName;
    private String signature;
    private String img_url;

    private boolean onLine;

    public UserEntry() {
    }

    public UserEntry(String account, String nickName, String signature,String img_url, boolean onLine) {
        this.account = account;
        this.nickName = nickName;
        this.signature = signature;
        this.onLine = onLine;
        this.img_url = img_url;
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

    public String getImg_url() {
        if(StringUtils.isEmpty(img_url)){
            img_url = Constant.DEF_URL;
        }
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    @Override
    public String toString() {
        return "UserEntry{" +
                "account='" + account + '\'' +
                ", nickName='" + nickName + '\'' +
                ", signature='" + signature + '\'' +
                ", img_url='" + img_url + '\'' +
                ", onLine=" + onLine +
                '}';
    }
}
