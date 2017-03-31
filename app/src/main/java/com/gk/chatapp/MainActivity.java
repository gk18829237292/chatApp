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
import com.gk.chatapp.fragment.RecentUserFragment;
import com.gk.chatapp.model.DrawerItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;


    private List<DrawerItem> mDrawerItems;

    private Handler mHandler;

    private TextView mName, mAccount;

//    public  Fragment fragment;

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
        }
        mDrawLayout.setDrawerListener(mDrawerToggle);
        //TODO 加菜单

    }

    private void init(){

        //init imageloader
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        }

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
        mName = (TextView) headerView.findViewById(R.id.txt_name);
        mAccount = (TextView) headerView.findViewById(R.id.txt_account);
        mDrawerList.addHeaderView(headerView);
    }

    //TODO 这里做修改 分两个Fragment ,一个是全部 一个是不分
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
        Fragment fragment = null;
        switch (drawerTag){
            case DrawerItem.DRAWER_ITEM_TAG_RECENT:
                fragment = new RecentUserFragment();
                break;
            case DrawerItem.DRAWER_ITEM_TAG_ONLINEUSER:

                break;
            case DrawerItem.DRAWER_ITEM_TAG_ALLUSER:

                break;
            case DrawerItem.DRAWER_ITEM_TAG_LOGOUT:
                logout();
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
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.recent, DrawerItem.DRAWER_ITEM_TAG_RECENT));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.onlineUser, DrawerItem.DRAWER_ITEM_TAG_ONLINEUSER));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.allUser, DrawerItem.DRAWER_ITEM_TAG_ALLUSER));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_left_menus,R.string.string_logout,DrawerItem.DRAWER_ITEM_TAG_LOGOUT));
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

    private void logout(){
        Intent intent= new Intent(MainActivity.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("flag",true);
        startActivity(intent);
    }

}
