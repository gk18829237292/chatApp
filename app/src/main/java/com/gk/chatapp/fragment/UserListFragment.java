package com.gk.chatapp.fragment;


import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.gk.chatapp.R;
import com.gk.chatapp.UserInfoActivity;
import com.gk.chatapp.VideoCallActivity;
import com.gk.chatapp.adapter.UserAdapter;
import com.gk.chatapp.app.App;
import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.entry.UserEntry;
import com.gk.chatapp.model.DrawerItem;
import com.gk.chatapp.utils.SprefUtils;
import com.gk.chatapp.utils.UserStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "UserListFragment";

    private List<UserEntry> mUserEntry = new ArrayList<>();
    private SwipeRefreshLayout mItemContainer;
    private ListView userListView;

    private UserAdapter mUserAdapter;

    private int drawTag;

    PopupWindow pop;
    LinearLayout ll_popup;

    private LayoutInflater inflater;

    private int mClickPosition = -1;

    public UserListFragment() {
        // Required empty public constructor
        drawTag = DrawerItem.DRAWER_ITEM_TAG_RECENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserEntry.addAll(UserStatus.getUserList(DrawerItem.DRAWER_ITEM_TAG_RECENT));
        mUserAdapter = new UserAdapter(getActivity(),mUserEntry);

        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_recent_user,container,false);

        mItemContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemContainer.setOnRefreshListener(this);
        initPopupWindow(drawTag);
        userListView = (ListView) rootView.findViewById(R.id.list_view);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userListView.setItemChecked(position,false);
                mClickPosition = position;
                ll_popup.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.activity_translate_in));
                pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
        });
        userListView.setAdapter(mUserAdapter);


        return rootView;
    }

    private void initData(){

    }

    @Override
    public void onRefresh() {
        Log.d(TAG,"refresh");
        mItemContainer.setRefreshing(true);
        updateComplete();
        mItemContainer.setRefreshing(false);
    }

    public void updateComplete(){
        mUserEntry.clear();
        mUserEntry.addAll(UserStatus.getUserList(drawTag));
        mUserAdapter.notifyDataSetChanged();
    }

    //如果不同 则更新
    public void setDrawTag(int drawTag) {
        if(this.drawTag !=drawTag){
            mUserEntry.clear();
            mUserEntry.addAll(UserStatus.getUserList(drawTag));
            mUserAdapter.notifyDataSetChanged();
            this.drawTag = drawTag;
            initPopupWindow(drawTag);
        }
    }

    public int getDrawTag() {
        return drawTag;
    }

    public void initPopupWindow(int drawTag){
        pop = new PopupWindow(getActivity());
        View view = inflater.inflate(R.layout.item_popup_main_activity, null);
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button btn_call = (Button) view.findViewById(R.id.item_popupwindows_call);
        Button btn_info = (Button) view.findViewById(R.id.item_popupwindows_info);
        Button btn_remove= (Button) view.findViewById(R.id.item_popupwindows_remove);
        Button btn_cancel = (Button) view.findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();

                UserEntry entry =mUserEntry.get(mClickPosition);
                SprefUtils mSpref = App.getmSpref();
                Set<String> recentUser = mSpref.getStringSet(Constant.PARAM_RECENT_USER+App.getInstance().getMyEntry().getAccount(),new HashSet<String>());
                recentUser.add(entry.getAccount());
                Log.d(TAG,recentUser.toString());
                mSpref.putCommit(Constant.PARAM_RECENT_USER+App.getInstance().getMyEntry().getAccount(),recentUser);

                UserStatus.addRecent(entry.getAccount());

                updateComplete();

                Intent intent = new Intent(getActivity(), VideoCallActivity.class);
                intent.putExtra(Constant.PARAM_IS_CALLER,Constant.IS_CALLER_YES);
                intent.putExtra(Constant.ACCOUNT,entry.getAccount());
                startActivity(intent);
            }
        });

        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"to show info");
                if(mClickPosition >= 0 && mClickPosition < mUserEntry.size()){
                    UserEntry entry =mUserEntry.get(mClickPosition);
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra(Constant.CAN_EDIT,false);
                    intent.putExtra(Constant.ACCOUNT,entry.getAccount());
                    intent.putExtra(Constant.NICKNAME,entry.getNickName());
                    intent.putExtra(Constant.SIGNATURE,entry.getSignature());
                    intent.putExtra(Constant.URL_STR,entry.getImg_url());
                    startActivity(intent);
                }
            }
        });

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserEntry entry =mUserEntry.get(mClickPosition);
                SprefUtils mSpref = App.getmSpref();

                Set<String> recentUser = mSpref.getStringSet(Constant.PARAM_RECENT_USER+App.getInstance().getMyEntry().getAccount(),new HashSet<String>());
                recentUser.remove(entry.getAccount());
                mSpref.putCommit(Constant.PARAM_RECENT_USER+App.getInstance().getMyEntry().getAccount(),recentUser);

                UserStatus.removeRecent(entry.getAccount());

                mUserEntry.remove(mClickPosition);
                mUserAdapter.notifyDataSetChanged();

                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });

        if(drawTag == DrawerItem.DRAWER_ITEM_TAG_RECENT){
            view.findViewById(R.id.ll_recent).setVisibility(View.VISIBLE);
        }else{
            view.findViewById(R.id.ll_recent).setVisibility(View.GONE);
        }

    }

}
