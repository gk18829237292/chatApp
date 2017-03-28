package com.gk.chatapp.view;

import android.content.Context;
import android.widget.ListView;

/**
 * Created by pc_home on 2016/12/4.
 */

public class NoScrollListView extends ListView {

    public NoScrollListView(Context context) {
        super(context);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
