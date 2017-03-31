package com.gk.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gk.chatapp.R;
import com.gk.chatapp.entry.UserEntry;
import com.gk.chatapp.utils.ImageUtil;

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
        return mUserEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_user_items,parent,false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.iv_image);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.status = (TextView) convertView.findViewById(R.id.tv_status);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageUtil.displayRoundImage(holder.image,"http://pengaja.com/uiapptemplate/newphotos/listviews/draganddrop/travel/0.jpg",null);
        holder.name.setText("小明 " + position);
        return convertView;
    }

    private class ViewHolder{
        public ImageView image;
        public TextView name,status;
    }
}
