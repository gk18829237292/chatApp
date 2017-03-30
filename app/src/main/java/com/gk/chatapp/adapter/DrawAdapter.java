package com.gk.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.gk.chatapp.R;
import com.gk.chatapp.model.DrawerItem;

import java.util.List;

/**
 * Created by pc_home on 2016/12/2.
 */

public class DrawAdapter extends BaseAdapter{

    private List<DrawerItem> mDrawerItems;
    private LayoutInflater mInflater;

    public DrawAdapter(Context context, List<DrawerItem> items) {
        this.mDrawerItems = items;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDrawerItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mDrawerItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mDrawerItems.get(i).getTag();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_view_item_navigation_drawer,parent,false);
            holder = new ViewHolder();
            holder.icon = (TextView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        DrawerItem item = mDrawerItems.get(position);
        holder.icon.setText(item.getIcon());
        holder.title.setText(item.getTitle());
        return convertView;
    }

    private static class ViewHolder{
        public TextView icon;
        public /*Roboto*/TextView title;
    }
}
