package com.MagicDevelopers.buzzup.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Modelos.Upload.MediaModel;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MediaModel item);
    }

    private List<MediaModel> mediaList;
    private OnItemClickListener listener;

    public GalleryAdapter(List<MediaModel> mediaList, OnItemClickListener listener) {
        this.mediaList = mediaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaModel item = mediaList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(item.getUri())
                .into(holder.mediaThumbnail);

        holder.iconVideo.setVisibility(item.isVideo() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaThumbnail, iconVideo;

        ViewHolder(View itemView) {
            super(itemView);
            mediaThumbnail = itemView.findViewById(R.id.mediaThumbnail);
            iconVideo = itemView.findViewById(R.id.iconVideo);
        }
    }
}
