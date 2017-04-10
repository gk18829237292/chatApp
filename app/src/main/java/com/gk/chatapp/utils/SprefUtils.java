package com.gk.chatapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 存储一些配置数据
 *
 * @author andywu
 */
public class SprefUtils {

    private SharedPreferences mSpref;
    private Editor mEditor;

    public SprefUtils(Context context) {
        if (context == null) {
            Logger.e("Init Failed, context is null!");
            return;
        }
        mSpref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        mEditor = mSpref.edit();
    }

    public SprefUtils(Context context, String sprefName) {
        if (context == null) {
            Logger.e("Init Failed, context is null!");
            return;
        }
        mSpref = context.getSharedPreferences(sprefName, Context.MODE_PRIVATE);
        mEditor = mSpref.edit();
    }

    /**
     * 构造方法。
     *
     * @param context
     * @param sprefName 键值表名称。
     * @param mode      打开的模式。值为Context.MODE_APPEND, Context.MODE_PRIVATE,
     *                  Context.WORLD_READABLE, Context.WORLD_WRITEABLE.
     */
    @SuppressLint("CommitPrefEdits")
    public SprefUtils(Context context, String sprefName, int mode) {
        if (context == null) {
            Logger.e("Init Failed, context is null!");
            return;
        }
        mSpref = context.getSharedPreferences(sprefName, mode);
        mEditor = mSpref.edit();
    }

    public boolean initSuccess() {
        if (mSpref == null) {
            Logger.e("NullException--->Init failed!");
            return false;
        }
        return true;
    }

    public Editor getEditor() {
        return mEditor;
    }

    /**
     * 获取保存着的boolean对象。
     *
     * @param key      键名
     * @param defValue 当不存在时返回的默认值。
     * @return 返回获取到的值，当不存在时返回默认值。
     */
    public boolean getBoolean(String key, boolean defValue) {
        if (!initSuccess()) {
            return false;
        }
        return mSpref.getBoolean(key, defValue);
    }

    /**
     * 获取保存着的int对象。
     *
     * @param key      键名
     * @param defValue 当不存在时返回的默认值。
     * @return 返回获取到的值，当不存在时返回默认值。
     */
    public int getInt(String key, int defValue) {
        if (!initSuccess()) {
            return defValue;
        }
        return mSpref.getInt(key, defValue);
    }

    /**
     * 获取保存着的long对象。
     *
     * @param key      键名
     * @param defValue 当不存在时返回的默认值。
     * @return 返回获取到的值，当不存在时返回默认值。
     */
    public long getLong(String key, long defValue) {
        if (!initSuccess()) {
            return defValue;
        }
        return mSpref.getLong(key, defValue);
    }

    /**
     * 获取保存着的float对象。
     *
     * @param key      键名
     * @param defValue 当不存在时返回的默认值。
     * @return 返回获取到的值，当不存在时返回默认值。
     */
    public float getFloat(String key, float defValue) {
        if (!initSuccess()) {
            return defValue;
        }
        return mSpref.getFloat(key, defValue);
    }

    /**
     * 获取保存着的String对象。
     *
     * @param key      键名
     * @param defValue 当不存在时返回的默认值。
     * @return 返回获取到的值，当不存在时返回默认值。
     */
    public String getString(String key, String defValue) {
        if (!initSuccess()) {
            return defValue;
        }
        return mSpref.getString(key, defValue);
    }

    /**
     * 获取保存的数组
     * @param key
     * @param defValue
     * @return
     */
    public Set<String> getStringSet(String key,Set<String> defValue){
        if(!initSuccess()){
            return defValue;
        }
        return mSpref.getStringSet(key,defValue);
    }

    /**
     * 获取所有键值对。
     *
     * @return 获取到的所胡键值对。
     */
    public Map<String, ?> getAll() {
        if (!initSuccess()) {
            return new HashMap<>();
        }
        return mSpref.getAll();
    }

    /**
     * 设置一个键值对，它将在{@linkplain #commit()}被调用时保存。<br/>
     * 注意：当保存的value不是boolean, byte(会被转换成int保存),int, long, float,
     * String等类型时将调用它的toString()方法进行值的保存。
     *
     * @param key   键名称。
     * @param value 值。
     * @return 引用的KV对象。
     */
    public SprefUtils put(String key, Object value) {

        if (!initSuccess()) {
            return this;
        }

        if (value == null) {
            Logger.e( "NullException--->value");
            return this;
        }

        if (TextUtils.isEmpty(key)) {
            Logger.e("NullException--->key");
            return this;
        }

        if (value instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer || value instanceof Byte) {
            mEditor.putInt(key, (int) value);
        } else if (value instanceof Long) {
            mEditor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            mEditor.putFloat(key, (Float) value);
        } else if (value instanceof String) {
            mEditor.putString(key, (String) value);
        }else if(value instanceof Set){
            mEditor.putStringSet(key,(Set<String>)value);
        } else {
            mEditor.putString(key, value.toString());
        }
        return this;
    }

    /**
     * 设置一个键值对并提交.<br/>
     * 注意：当保存的value不是boolean, byte(会被转换成int保存),int, long, float,
     * String等类型时将调用它的toString()方法进行值的保存。
     *
     * @param key   键名称。
     * @param value 值。
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public boolean putCommit(String key, Object value) {


        return put(key, value).commit();

    }

    /**
     * 存储多个键值对
     *
     * @param keyValueMap 键值对列表（可以使用本类中默认的键值对，如需取用{@link #getAll()}）
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public SprefUtils put(Map<String, ?> keyValueMap) {

        if (!initSuccess()) {
            return this;
        }

        if (keyValueMap == null || keyValueMap.size() <= 0) {
            return this;
        }

        for (Map.Entry<String, ?> entry : keyValueMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (TextUtils.isEmpty(key) && value != null) {
                put(key, value);
            }
        }

        return this;
    }

    /**
     * 提交存储多个键值对
     *
     * @param keyValueMap 键值对列表（可以使用本类中默认的键值对，如需取用{@link #getAll()}）
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public void putCommit(Map<String, ?> keyValueMap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            put(keyValueMap).commit();
        } else {
            put(keyValueMap).apply();
        }
    }

    /**
     * 移除键值对。
     *
     * @param key 要移除的键名称。
     * @return 引用的KV对象。
     */
    public SprefUtils remove(String key) {
        if (!initSuccess()) {
            return this;
        }
        mEditor.remove(key);
        return this;
    }

    /**
     * 移除键值对。
     *
     * @param key 要移除的键名称。
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public void removeCommit(String key) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            remove(key).commit();
        } else {
            remove(key).apply();
        }
    }

    /**
     * 清除所有键值对。
     *
     * @return 引用的KV对象。
     */
    public SprefUtils clear() {
        if (!initSuccess()) {
            return this;
        }
        mEditor.clear();
        return this;
    }

    /**
     * 清除所有键值对并提交。
     *
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public boolean clearCommit() {
        return clear().commit();
    }

    /**
     * 是否包含某个键。
     *
     * @param key 查询的键名称。
     * @return 当且仅当包含该键时返回true, 否则返回false.
     */
    public boolean contains(String key) {
        if (!initSuccess()) {
            return false;
        }
        return mSpref.contains(key);
    }

    /**
     * 返回是否提交成功。
     *
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public boolean commit() {
        if (!initSuccess()) {
            return false;
        }
        return mEditor.commit();
    }

    /**
     * 快速提交，内存同步，存储异步。
     *
     * @return 当且仅当提交成功时返回true, 否则返回false.
     */
    public void apply() {
        if (!initSuccess()) {
            return ;
        }
        mEditor.apply();
    }


    /**
     * 注册数据监听器对象
     *
     * @param listener
     */
    public void registerOnSprefChangeListener(OnSharedPreferenceChangeListener listener) {
        if (!initSuccess()) {
            return;
        }
        mSpref.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 取消注册监听器对象
     *
     * @param listener
     */
    public void unregisterOnSprefChangeListener(OnSharedPreferenceChangeListener listener) {
        if (!initSuccess()) {
            return;
        }
        mSpref.unregisterOnSharedPreferenceChangeListener(listener);
    }

}