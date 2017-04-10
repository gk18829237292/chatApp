package com.gk.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.gk.chatapp.app.App;
import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.ProgressDialogFactory;
import com.gk.chatapp.utils.SocketIoUtils;
import com.gk.chatapp.utils.ToastUtils;
import com.gk.chatapp.view.FloatLabeledEditText;

import io.socket.emitter.Emitter;

public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";

    private static ProgressDialog pDialog;

    private FloatLabeledEditText et_account,et_password,et_nickname,et_signuture;

    private TextView tv_register;

    private String account,password,nickname, signature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        initDate();
    }

    private void initView(){
        pDialog = ProgressDialogFactory.getProgressDialog(RegisterActivity.this,"正在注册···");

        et_account = (FloatLabeledEditText) findViewById(R.id.et_account);
        et_password = (FloatLabeledEditText) findViewById(R.id.et_password);
        et_nickname = (FloatLabeledEditText) findViewById(R.id.et_nickname);
        et_signuture = (FloatLabeledEditText) findViewById(R.id.et_signature);

        tv_register = (TextView) findViewById(R.id.tv_register);
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!App.getInstance().isConnected()){
                    ToastUtils.showShortToast(RegisterActivity.this,"请检查网络连接");
                }else if(checkAccount() && checkPassword() && checkNickname() &&checkSignature() ){
                    showDialog();
                    SocketIoUtils.sendMessage("register",account,password,nickname,signature);
                }
            }
        });
    }

    private void initDate(){
        SocketIoUtils.registerListener(Constant.REGISTER_RESULT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final boolean result = (boolean) args[0];
                if (result){
                    registerSuccess();
                }else {
                    registerFail();
                }

            }
        });
    }

    private boolean checkAccount(){
        account = et_account.getText().toString();
        et_account.setError(null);
        if(account.length() == 0){
            et_account.setError("用户名不可为空");
            return false;
        }
        return true;
    }

    private boolean checkPassword(){
        password = et_password.getText().toString();
        et_password.setError(null);
        if(password.length() == 0){
            et_password.setError("密码不可为空");
            return false;
        }
        return true;
    }

    private boolean checkNickname(){
        nickname = et_nickname.getTextString();
        et_nickname.setError(null);
        if(nickname.length() ==0){
            et_nickname.setError("昵称不可为空");
            return false;
        }
        return true;
    }

    private boolean checkSignature(){
        signature = et_signuture.getText().toString();
        et_signuture.setError(null);
        if(signature.length() == 0){
            et_signuture.setError("个性签名不可为空");
            return false;
        }
        return true;
    }



    private void showDialog(){
        if(!pDialog.isShowing()){
            pDialog.show();
        }
    }

    private void hideDialog(){
        if(pDialog.isShowing()){
            pDialog.dismiss();
        }
    }

    private void registerSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideDialog();
                ToastUtils.showShortToast(RegisterActivity.this,"注册成功");
                //返回登录界面
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constant.ACCOUNT,account);
                intent.putExtra(Constant.PASSWORD,password);
                startActivity(intent);
            }
        });

    }

    private void registerFail(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideDialog();
                ToastUtils.showShortToast(RegisterActivity.this,"注册失败，请检查用户名唯一性");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketIoUtils.unRegisterListener(Constant.REGISTER_RESULT);
    }
}
