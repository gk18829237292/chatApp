package com.gk.chatapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import com.gk.chatapp.adapter.DrawAdapter;
import com.gk.chatapp.model.DrawerItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private static final int REQUESTFORUPDATE_TASK = 123;
    private static final int REQUESTFORUPDATE_DEPART = 456;
    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private List<DrawerItem> mDrawerItems;

    private Handler mHandler;

    private TextView txt_name,txt_depart;

    public  Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareNaigationDrawerItems();
        init();

        //TODO 看代码 看看作用
        if(savedInstanceState == null){
            int position = 0;
            selectItem(position+1,mDrawerItems.get(position).getTag());
            //TODO change
//            mDrawLayout.openDrawer(mDrawerList);
        }

        mDrawLayout.setDrawerListener(mDrawerToggle);


        //TODO 加菜单

    }

    private void init(){
        //TODO toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawLayout = (DrawerLayout) findViewById(R.id.layout_drawer);
        //TODO 可以重写设置更新
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawLayout,toolbar, R.string.open, R.string.close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        mDrawLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);



        mDrawerList = (ListView) findViewById(R.id.list_view);
        mDrawerList.setAdapter(new DrawAdapter(this,mDrawerItems));
        mHandler = new Handler();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("click : "+ i);
                if(i > 0){
                    selectItem(i,mDrawerItems.get(i-1).getTag());
                }
            }
        });

        View headerView = getLayoutInflater().inflate(R.layout.nav_header_main,mDrawerList,false);
        txt_name = (TextView) headerView.findViewById(R.id.txt_name);
        txt_depart = (TextView) headerView.findViewById(R.id.txt_depart);



        mDrawerList.addHeaderView(headerView);




    }

    private void selectItem(int position,int drawTag){
        Fragment fragment = getFragmentByDrawerTag(drawTag);
        if(fragment != null){
            commitFragment(fragment);
            mDrawerList.setItemChecked(position, true);
            setTitle(mDrawerItems.get(position-1).getTitle());
            mDrawLayout.closeDrawer(mDrawerList);
        }

    }

    private Fragment getFragmentByDrawerTag(int drawerTag) {

        switch (drawerTag){
            case DrawerItem.DRAWER_ITEM_TAG_LIST_VIEWS:
                break;
            case DrawerItem.DRAWER_ITEM_TAG_SHAPE_IMAGE_VIEWS:
                break;
            case DrawerItem.DRAWER_ITEM_TAG_LEFT_MENUS:
                //TODO
                Intent intent= new Intent(MainActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("flag",true);
                startActivity(intent);
                break;
        }
        return  fragment;
    }

    public void commitFragment(Fragment fragment) {
        // Using Handler class to avoid lagging while
        // committing fragment in same time as closing
        // navigation drawer
        mHandler.post(new CommitFragmentRunnable(fragment));
    }

    //TODO 这里实例化填充的东西
    private void prepareNaigationDrawerItems(){
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.recent, DrawerItem.DRAWER_ITEM_TAG_LIST_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.onlineUser, DrawerItem.DRAWER_ITEM_TAG_LIST_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.allUser, DrawerItem.DRAWER_ITEM_TAG_SHAPE_IMAGE_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_left_menus,R.string.string_logout,DrawerItem.DRAWER_ITEM_TAG_LEFT_MENUS));
    }

    private class CommitFragmentRunnable implements Runnable {

        private Fragment fragment;

        public CommitFragmentRunnable(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void run() {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //TODO 菜单选项 空白处理就好
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
