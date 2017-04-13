package com.gk.chatapp.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final String TAG = StringUtils.class.getSimpleName();

    private static Pattern sTelNumPattern = Pattern.compile("^1\\d{10}$");
    private static Pattern sNickNamePattern = Pattern.compile("^\\w{1,10}$");

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if (lastPos < 0 || (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    public static String removeQuote(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    /**
     * 判断字符串是否为null或长度为0
     *
     * @param s 待校验字符串
     * @return {@code true}: 空<br> {@code false}: 不为空
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * 判断字符串是否为null或全为空格
     *
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

    public static String changeToString(String sdp) {
        int endIndex = sdp.indexOf("typ");
        sdp = sdp.substring(10, endIndex - 1);
        sdp = sdp.replace(" ", "\t\t");
        return sdp;
    }

    /**
     * @param telNum 电话号码
     * @return 电话号码是否合法
     */
    public static boolean checkTelNum(String telNum) {
        Matcher matcher = sTelNumPattern.matcher(telNum);
        return matcher.matches();
    }

    /**
     * @param nickName 昵称
     * @return 昵称是否合法
     */
    public static boolean checkNickName(String nickName) {
        Matcher matcher = sNickNamePattern.matcher(nickName);
        return matcher.matches();
    }
}