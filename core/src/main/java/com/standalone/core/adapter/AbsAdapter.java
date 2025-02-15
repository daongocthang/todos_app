package com.standalone.core.adapter;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected List<T> itemList = new ArrayList<>();

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public T getItem(int pos) {
        return itemList.get(pos);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setItemList(List<T> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }
}
