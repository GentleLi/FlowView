package com.liyi.view;


import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFlowAdapter {
    public int getViewType(int position) {
        return 0;
    }

    public abstract int getCount();

    public abstract View getView(int position, View convertView, ViewGroup parent);
}