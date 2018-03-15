package com.xl.project.project1_0;

/**
 * Created by xl on 2017/11/20.
 */


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Timy on 2017/10/22.
 */

public abstract class CommonAdapter<T> extends RecyclerView.Adapter<ViewHolder>{
    protected Context mContext;
    protected int mLayoutID;
    protected List<T> mDatas;
    private OnItemClickListener mOnItemClickListener = null;


    public interface OnItemClickListener{//设置接口函数
        void onClick(int position);
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener= (OnItemClickListener) onItemClickListener;
    }
    public CommonAdapter(Context context, int layoutID, List<T> datas){
        mContext=context;
        mLayoutID=layoutID;
        mDatas=datas;
    }
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType){
        ViewHolder viewHolder = ViewHolder.get(mContext, parent, mLayoutID);
        return viewHolder;
    }
    @Override
    public int getItemCount(){
        Log.i(TAG,"count: "+mDatas.size());
        return mDatas.size();
    }

    @Override
    public void  onBindViewHolder(final ViewHolder holder, int position){
        convert(holder,mDatas.get(position));
        if(mOnItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    mOnItemClickListener.onClick(holder.getAdapterPosition());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v){
                    mOnItemClickListener.onLongClick(holder.getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public abstract void convert(ViewHolder holder, T t);
}

