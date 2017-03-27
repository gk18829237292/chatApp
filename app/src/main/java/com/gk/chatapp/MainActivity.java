package com.gk.chatapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Objects;

public class MainActivity extends Activity {

    private static String serer_address = "http://10.64.33.43:3000";
    private static Socket client;

    private EditText et_title,et_content;
    private Button btn_submit,btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

    }

    private void initView(){
        et_title = (EditText) findViewById(R.id.et_title);
        et_content = (EditText) findViewById(R.id.et_content);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        btn_login = (Button) findViewById(R.id.btn_loging);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("connect : " + client.connected());
                String title = et_title.getText().toString();
                String content = et_content.getText().toString();

                client.emit(title,content);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_title.getText().toString();
                String content = et_content.getText().toString();

                client.emit("login",title,content);
            }
        });
    }

    private void initData(){
        try {
            client = IO.socket(serer_address);
            client.on("userInfo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    for(Object arg:args){
                        try {
                            String str = new String(arg.toString().getBytes("ISO-8859-1"),"utf-8");
                            System.out.println(str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        System.out.println(arg);
                    }
                }
            });
            client.on("disconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("dis");
                }
            });

            client.on("connect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("connect");
                }
            });
            client.on("reconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("reconnect");
                }
            });
            client.on("login", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    int result = (int) args[0];
                    if (result == 0){
                        Toast.makeText(MainActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
