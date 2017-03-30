package com.gk.chatapp;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v7.widget.Toolbar;
import android.widget.ListView;


import com.gk.chatapp.adapter.DrawAdapter;
import com.gk.chatapp.model.DrawerItem;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private List<DrawerItem> mDrawerItems;

    private Handler mHandler;

    public Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    private void initView(){
        prepareNaigationDrawerItems();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawLayout = (DrawerLayout) findViewById(R.id.layout_drawer);
        mDrawLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawLayout,toolbar,R.string.open,R.string.close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        mDrawerList = (ListView) findViewById(R.id.list_view);
        mDrawerList.setAdapter(new DrawAdapter(this,mDrawerItems));


    }

    private void prepareNaigationDrawerItems(){
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.recent, DrawerItem.DRAWER_ITEM_TAG_LIST_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.onlineUser, DrawerItem.DRAWER_ITEM_TAG_SHAPE_IMAGE_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_shape_image_views, R.string.allUser, DrawerItem.DRAWER_ITEM_TAG_SHAPE_IMAGE_VIEWS));
        mDrawerItems.add(new DrawerItem(R.string.drawer_icon_left_menus,R.string.string_logout,DrawerItem.DRAWER_ITEM_TAG_LEFT_MENUS));
    }



}
