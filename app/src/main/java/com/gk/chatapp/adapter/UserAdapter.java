package com.gk.chatapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.gk.chatapp.entry.UserEntry;

import java.util.List;

/**
 * Created by ke.gao on 2017/3/30.
 */

public class UserAdapter extends BaseAdapter{

    private Context mContext;
    private List<UserEntry> mUserEntries;

    public UserAdapter(Context context, List<UserEntry> mUserEntries){
        this.mContext = context;
        this.mUserEntries = mUserEntries;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

}
