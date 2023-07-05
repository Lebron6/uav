package com.mg.uav.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mg.uav.R;
import com.mg.uav.entity.MissionList;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dji.sdk.media.MediaFile;

/**
 * 航线列表
 */

public class MissionListAdapter extends RecyclerView.Adapter<MissionListAdapter.ItemHolder> {

    Context context;
    MissionList missionList;

    public MissionListAdapter(Context context) {
        this.context = context;
    }

    public void setData(MissionList missionList) {
        this.missionList = missionList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (missionList != null&&missionList.getData()!=null) {
            return missionList.getData().size();
        }
        return 0;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission_list, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder mItemHolder, final int index) {

        MissionList.DataDTO missionData = missionList.getData().get(index);
        if (missionData != null) {
            if (mOnItemClickLitener != null) {
                mItemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = mItemHolder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(mItemHolder.itemView, pos);
                    }
                });
            }
            mItemHolder.tv_name.setText("航线名称："+missionData.getName());
            mItemHolder.tv_date.setText("更新日期："+missionData.getUpdateTime());
            mItemHolder.tv_position.setText("地址："+missionData.getCreateBy());
            Glide.with(context).load(missionData.getImageUrl()).into(mItemHolder.iv_map);
        }
    }
    private OnItemClickLitener mOnItemClickLitener;

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView iv_map;
        TextView tv_name;
        TextView tv_date;
        TextView tv_position;

        public ItemHolder(View itemView) {
            super(itemView);
            this.iv_map = (ImageView) itemView.findViewById(R.id.iv_map);
            this.tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            this.tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            this.tv_position = (TextView) itemView.findViewById(R.id.tv_position);
        }
    }
}