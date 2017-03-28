package com.gk.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.gk.chatapp.app.App;
import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.ProgressDialogFactory;
import com.gk.chatapp.utils.SocketIOUtils;
import com.gk.chatapp.utils.ToastUtils;
import com.gk.chatapp.view.FloatLabeledEditText;

import static com.gk.chatapp.constant.Constant.LOGIN_EVENT;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private FloatLabeledEditText tv_account,tv_password;
    private TextView tv_register,tv_login;

    private String account,password;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
    }

    private void initView(){
        tv_account= (FloatLabeledEditText) findViewById(R.id.tv_account);
        tv_password= (FloatLabeledEditText) findViewById(R.id.tv_password);
        tv_register = (TextView) findViewById(R.id.tv_register);
        tv_login = (TextView) findViewById(R.id.tv_login);

        pDialog = ProgressDialogFactory.getProgressDialog(LoginActivity.this);

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!App.getInstance().isConnected()){
                    ToastUtils.showShortToast(LoginActivity.this,"请检查网络连接");
                }else if(checkAccount() && checkPassword()){
                    showDialog();
                    SocketIOUtils.sendMessage(Constant.LOGIN_EVENT,account,password);
                }
            }
        });

    }

    private void initData(){

        SocketIOUtils.registerListener(Constant.LOGIN_RESULT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final boolean result = (boolean) args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result){
                            loginSuccess();
                        }else {
                            loginFail();
                        }
                    }
                });
            }
        });
    }

    private boolean checkAccount(){
        account = tv_account.getText().toString();
        tv_login.setError(null);
        if(account.length() == 0){
            tv_login.setError("用户名不可为空");
            return false;
        }
        return true;
    }

    private boolean checkPassword(){
        password = tv_password.getText().toString();
        tv_password.setError(null);
        if(password.length() == 0){
            tv_password.setError("密码不可以为空");
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

    private void loginSuccess(){
        hideDialog();
        ToastUtils.showShortToast(LoginActivity.this,"登录成功");
    }

    private void loginFail(){
        hideDialog();
        ToastUtils.showShortToast(LoginActivity.this,"登录失败");
    }

}
