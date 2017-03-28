package com.gk.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.gk.chatapp.app.App;
import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.ProgressDialogFactory;
import com.gk.chatapp.utils.SocketIoUtils;
import com.gk.chatapp.utils.ToastUtils;
import com.gk.chatapp.view.FloatLabeledEditText;

public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";

    private static ProgressDialog pDialog;

    private FloatLabeledEditText et_account,et_password,et_nickName,et_sex;

    private TextView tv_register;

    private String account,password,nickName,sex;

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
        et_nickName = (FloatLabeledEditText) findViewById(R.id.et_nickname);
        et_sex = (FloatLabeledEditText) findViewById(R.id.et_sex);

        tv_register = (TextView) findViewById(R.id.tv_register);
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!App.getInstance().isConnected()){
                    ToastUtils.showShortToast(RegisterActivity.this,"请检查网络连接");
                }else if(checkAccount() && checkPassword() && checkNickName() && checkSex()){
                    showDialog();
                    SocketIoUtils.sendMessage("register",account,password,nickName,sex);
                }
            }
        });
    }

    private void initDate(){
        SocketIoUtils.registerListener(Constant.REGISTER_RESULT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final boolean result = (boolean) args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result){
                            registerSuccess();
                        }else {
                            registerFail();
                        }
                    }
                });
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

    private boolean checkNickName(){
        nickName = et_nickName.getText().toString();
        et_nickName.setError(null);
        if(nickName.length() == 0){
            et_nickName.setError("昵称不可为空");
            return false;
        }
        return true;
    }

    private boolean checkSex(){
        sex = et_sex.getText().toString();
        et_sex.setError(null);
        if(sex.length() == 0){
            et_sex.setError("性别不可为空");
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
        hideDialog();
        ToastUtils.showShortToast(RegisterActivity.this,"注册成功");
        //返回登录界面
    }

    private void registerFail(){
        hideDialog();
        ToastUtils.showShortToast(RegisterActivity.this,"注册失败，请检查用户名唯一性");
    }

}
