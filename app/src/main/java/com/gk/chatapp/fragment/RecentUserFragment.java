package com.gk.chatapp.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gk.chatapp.R;
import com.gk.chatapp.adapter.UserAdapter;
import com.gk.chatapp.entry.UserEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentUserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private ListView mListView;
    private List<UserEntry> mUserEntry = new ArrayList<>();
    private SwipeRefreshLayout mItemContainer;
    private ListView userListView;

    private UserAdapter userAdapter;

    public RecentUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserEntry.add(new UserEntry());
        mUserEntry.add(new UserEntry());
        mUserEntry.add(new UserEntry());
        mUserEntry.add(new UserEntry());
        userAdapter = new UserAdapter(getActivity(),mUserEntry);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recent_user,container,false);

        mItemContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemContainer.setOnRefreshListener(this);

        userListView = (ListView) rootView.findViewById(R.id.list_view);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userListView.setItemChecked(position,false);
            }
        });
        userListView.setAdapter(userAdapter);


        return rootView;
    }

    @Override
    public void onRefresh() {

    }

}
