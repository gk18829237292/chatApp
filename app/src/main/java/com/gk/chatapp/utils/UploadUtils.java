package com.gk.chatapp.utils;

import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

/**
 * Created by ke.gao on 2017/4/12.
 */

public class UploadUtils {

    public String uptoken = "";

    static Configuration config = new Configuration.Builder().zone(Zone.httpAutoZone).build();
    // 重用uploadManager。一般地，只需要创建一个uploadManager对象
    static UploadManager uploadManager = new UploadManager(config);

    public static UploadManager getUploadManager(){
        return uploadManager;
    }




}
