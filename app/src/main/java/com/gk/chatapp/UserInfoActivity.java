package com.gk.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.ImageUtil;
import com.gk.chatapp.utils.ProgressDialogFactory;
import com.gk.chatapp.utils.SocketIoUtils;
import com.gk.chatapp.utils.ToastUtils;
import com.gk.chatapp.utils.UploadUtils;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONObject;

import java.io.File;

import io.socket.emitter.Emitter;

public class UserInfoActivity extends ActionBarActivity {

    private static final String TAG = "UserInfoActivity";
    private String token = null;
    private String key =null;


    private ImageView iv_image;
    private TextView tv_account;
    private EditText et_nickname, et_signature;
    private Button btn_change;

    private String filePath;

    PopupWindow pop;
    LinearLayout ll_popup;

    private ProgressDialog mPdialog;
    private String account,nickname,signature;
    private String img_url = Constant.DEF_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        initView();
        initData();
    }

    private void initView(){
        //设置ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("个人中心");

        iv_image = (ImageView) findViewById(R.id.iv_image);
        tv_account = (TextView) findViewById(R.id.tv_account);
        et_nickname = (EditText) findViewById(R.id.et_nickname);
        et_signature = (EditText) findViewById(R.id.et_signature);
        btn_change = (Button) findViewById(R.id.btn_change);

        //填充
        Intent intent = getIntent();
        tv_account.setText(intent.getStringExtra(Constant.ACCOUNT));
        et_nickname.setText(intent.getStringExtra(Constant.NICKNAME));
        et_signature.setText(intent.getStringExtra(Constant.SIGNATURE));

        img_url = intent.getStringExtra(Constant.URL_STR);
        if(img_url == null){
            img_url = Constant.DEF_URL;
        }
        ImageUtil.displayRoundImage(iv_image,Constant.IMG_BASEURL_STR+img_url,null);
        boolean flag = intent.getBooleanExtra(Constant.CAN_EDIT,false);
        changeEnable(false);
        if(flag){
            btn_change.setVisibility(View.VISIBLE);
        }else {
            btn_change.setVisibility(View.GONE);
        }

        //设置监听事件
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_change.getText() == "编辑"){
                    changeEnable(true);
                }else if(checkNickname() && checkSignature()){
                    changeEnable(false);
                    //更新User
                    account = tv_account.getText().toString();
                    SocketIoUtils.sendMessage("updateUser",account,nickname,signature,img_url);
                }
            }
        });

        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"click it");
                showPopupWindow();
                ll_popup.startAnimation(AnimationUtils.loadAnimation(
                        UserInfoActivity.this, R.anim.activity_translate_in));
                pop.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            }
        });

        mPdialog = ProgressDialogFactory.getProgressDialog(UserInfoActivity.this,"上传中");

    }

    private void initData(){
        SocketIoUtils.registerListener("token_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                token = (String) args[0];
                UploadUtils.getUploadManager().put(filePath, key, token, new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject response) {
                        Log.d(TAG,"key is : " + key);
                        if(info.isOK()){
                            Log.d(TAG,"success");
                            img_url = key;
                            uploadSuccess(key);
                        }else if(info.needRetry()){
                            Log.d(TAG,"failed  needRetry " + info.toString());
                            uploadFailed();
                        }else {
                            Log.d(TAG,"failed  " + info.toString());
                        }
                    }
                },null);
            }
        });

        SocketIoUtils.registerListener("update_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                boolean result = (boolean) args[0];
                if(result){
                    updateSuccess();
                }else{
                    updateFiled();
                }
            }
        });
    }

    public void showPopupWindow(){
        pop = new PopupWindow(UserInfoActivity.this);
        View view = getLayoutInflater().inflate(R.layout.item_popupwindows, null);
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
        Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
        Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog();
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                filePath = Environment.getExternalStorageDirectory() +"/" + System.currentTimeMillis() + ".jpg";
                camera.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(filePath)));
                startActivityForResult(camera, Constant.CAMERA_INT);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent picture = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, Constant.PICTURE_INT);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ll_popup.clearAnimation();
            }
        });
    }

    private void changeEnable(boolean flag){
        iv_image.setEnabled(flag);
        et_nickname.setEnabled(flag);
        et_signature.setEnabled(flag);
        if(flag){
            btn_change.setText("保存");
        }else{
            btn_change.setText("编辑");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED){
            return;
        }
        if(requestCode == Constant.PICTURE_INT){
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null,null);
            if (cursor != null && cursor.moveToFirst()) {
                filePath= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }

        }
        this.key = System.currentTimeMillis() +".jpg";
        SocketIoUtils.sendMessage("getToken",Constant.BUCKET_STR,key);
        showDialog();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadSuccess(final String fileName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideDialog();
                ToastUtils.showShortToast(getApplicationContext(),"上传成功");
                ImageUtil.displayRoundImage(iv_image,Constant.IMG_BASEURL_STR+fileName,null);
            }
        });
    }

    private void uploadFailed(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideDialog();
                ToastUtils.showShortToast(getApplicationContext(),"上传失败");
            }
        });

    }

    private void showDialog(){
        if(!mPdialog.isShowing()){
            mPdialog.show();
        }
    }

    private void hideDialog(){
        if(mPdialog.isShowing()){
            mPdialog.dismiss();
        }
    }

    private void updateSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShortToast(getApplicationContext(),"更新成功");
            }
        });
    }

    private void updateFiled(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShortToast(getApplicationContext(),"更新失败，请重试");
                changeEnable(true);
            }
        });
    }

    private boolean checkNickname(){
        nickname = et_nickname.getText().toString();
        et_nickname.setError(null);
        if(nickname.length() == 0 ){
            et_nickname.setError("请输入用户昵称");
            return false;
        }
        return true;
    }

    private boolean checkSignature(){
        signature = et_signature.getText().toString();
        et_signature.setError(null);
        if(signature.length() == 0){
            et_signature.setError("请输入个性签名");
            return false;
        }
        return true;
    }
}
