package com.MagicDevelopers.buzzup.HOME.Fragments.Upload;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnAlbumSelectedListener {
        void onAlbumSelected(String bucketName);
    }

    public static class AlbumInfo {
        public final String name;
        public final String coverUri;
        public int count;
        public AlbumInfo(String name, String coverUri, int count) {
            this.name = name;
            this.coverUri = coverUri;
            this.count = count;
        }
    }

    private final List<AlbumInfo> albums = new ArrayList<>();
    private OnAlbumSelectedListener listener;

    public AlbumBottomSheetDialogFragment(Map<String, AlbumInfo> albumMap) {
        albums.addAll(albumMap.values());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAlbumSelectedListener) {
            listener = (OnAlbumSelectedListener) context;
        } else {
            throw new RuntimeException(
                    context.getClass().getSimpleName()
                            + " debe implementar OnAlbumSelectedListener");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(
                R.layout.bottom_sheet_albums, container, false);

        // Cancelar
        root.findViewById(R.id.btnCancelAlbums)
                .setOnClickListener(v -> dismiss());

        // Título
        TextView title = root.findViewById(R.id.txtTitleAlbums);
        title.setText("Seleccionar álbum");

        // RecyclerView
        RecyclerView rv = root.findViewById(R.id.recyclerAlbums);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv.setAdapter(new RecyclerView.Adapter<AlbumVH>() {
            @NonNull @Override
            public AlbumVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View item = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_album, parent, false);
                return new AlbumVH(item);
            }
            @Override
            public void onBindViewHolder(@NonNull AlbumVH holder, int position) {
                AlbumInfo ai = albums.get(position);
                holder.name.setText(ai.name.isEmpty() ? "Recientes" : ai.name);
                holder.count.setText(String.valueOf(ai.count));
                Glide.with(holder.cover)
                        .load(Uri.parse(ai.coverUri))
                        .centerCrop()
                        .into(holder.cover);
                holder.itemView.setOnClickListener(v -> {
                    listener.onAlbumSelected(ai.name);
                    dismiss();
                });
            }
            @Override
            public int getItemCount() {
                return albums.size();
            }
        });

        return root;
    }

    static class AlbumVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView name, count;
        AlbumVH(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.imageAlbumCover);
            name  = itemView.findViewById(R.id.textAlbumName);
            count = itemView.findViewById(R.id.textAlbumCount);
        }
    }
}
