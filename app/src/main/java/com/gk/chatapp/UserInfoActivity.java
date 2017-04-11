package com.gk.chatapp;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
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

import java.lang.annotation.Target;

public class UserInfoActivity extends ActionBarActivity {

    private static final String TAG = "UserInfoActivity";

    private ImageView iv_image;
    private TextView tv_account;
    private EditText et_nickname, et_signature;
    private Button btn_change;

    PopupWindow pop;
    LinearLayout ll_popup;

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

        String url = intent.getStringExtra(Constant.URL_STRING);
        if(url == null){
            url = Constant.DEF_URL;
        }
        ImageUtil.displayRoundImage(iv_image,url,null);

        boolean flag = intent.getBooleanExtra(Constant.CAN_EDIT,false);
        if(flag){
            changeEnable(true);
            btn_change.setVisibility(View.VISIBLE);
        }else {
            changeEnable(false);
            btn_change.setVisibility(View.GONE);
        }

        //设置监听事件
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_change.getText() == "编辑"){
                    btn_change.setText("保存");
                    changeEnable(true);
                }else{
                    btn_change.setText("编辑");
                    //执行更新操作

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

    }

    private void initData(){

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera, 1);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent picture = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, 2);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
    }

    private void changeEnable(boolean flag){
        iv_image.setEnabled(flag);
//        tv_account.setEnabled(flag);
        et_nickname.setEnabled(flag);
        et_signature.setEnabled(flag);

    }
}
