package com.mg.uav.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mg.uav.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dji.sdk.media.MediaFile;

/**
 * 图库适配器
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ItemHolder> {

    Context context;
    List<MediaFile> mediaFileList;

    public FileListAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<MediaFile> mediaFileList) {
        this.mediaFileList = mediaFileList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mediaFileList != null) {
            return mediaFileList.size();
        }else{
            return 0;
        }
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_info, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder mItemHolder, final int index) {

        final MediaFile mediaFile = mediaFileList.get(index);
        if (mediaFile != null) {
            if (mediaFile.getMediaType() != MediaFile.MediaType.MOV && mediaFile.getMediaType() != MediaFile.MediaType.MP4) {
                mItemHolder.file_time.setVisibility(View.GONE);
            } else {
                mItemHolder.file_time.setVisibility(View.VISIBLE);
                mItemHolder.file_time.setText(mediaFile.getDurationInSeconds() + " s");
            }
            mItemHolder.file_name.setText(mediaFile.getFileName());
            mItemHolder.file_type.setText(mediaFile.getMediaType().name());
            mItemHolder.file_size.setText(mediaFile.getFileSize() + " Bytes");
            mItemHolder.thumbnail_img.setImageBitmap(mediaFile.getThumbnail());
//            Glide.with(context).load(mediaFile.getRealFrameRate()).into(mItemHolder.thumbnail_img);
//            mItemHolder.thumbnail_img.setOnClickListener(ImgOnClickListener);
//            mItemHolder.thumbnail_img.setTag(mediaFile);
            mItemHolder.itemView.setTag(index);

//            if (lastClickViewIndex == index) {
//                mItemHolder.itemView.setSelected(true);
//            } else {
//                mItemHolder.itemView.setSelected(false);
//            }
            mItemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail_img;
        TextView file_name;
        TextView file_type;
        TextView file_size;
        TextView file_time;

        public ItemHolder(View itemView) {
            super(itemView);
            this.thumbnail_img = (ImageView) itemView.findViewById(R.id.filethumbnail);
            this.file_name = (TextView) itemView.findViewById(R.id.filename);
            this.file_type = (TextView) itemView.findViewById(R.id.filetype);
            this.file_size = (TextView) itemView.findViewById(R.id.fileSize);
            this.file_time = (TextView) itemView.findViewById(R.id.filetime);
        }
    }
}