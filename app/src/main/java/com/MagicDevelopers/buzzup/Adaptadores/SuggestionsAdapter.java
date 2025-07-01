package com.MagicDevelopers.buzzup.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Modelos.UserSuggestion;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onProfileClick(UserSuggestion suggestion);
        void onFollowClick(UserSuggestion suggestion, boolean isFollowing);
    }

    private Context context;
    private List<UserSuggestion> suggestionList;
    private OnSuggestionClickListener listener;

    public SuggestionsAdapter(Context context, List<UserSuggestion> list, OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestionList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        UserSuggestion suggestion = suggestionList.get(position);

        holder.txtFullName.setText(suggestion.getFullName());
        holder.txtUsername.setText("@" + suggestion.getUsername());

        Glide.with(context)
                .load(suggestion.getProfileUrl())
                .placeholder(R.drawable.ic_person_placeholder)
                .into(holder.imgProfile);

        holder.itemView.setOnClickListener(v -> listener.onProfileClick(suggestion));

        updateFollowButton(holder.btnFollow, suggestion.isFollowing());

        holder.btnFollow.setOnClickListener(v -> {
            boolean newState = !suggestion.isFollowing();
            suggestion.setFollowing(newState);
            updateFollowButton(holder.btnFollow, newState);
            listener.onFollowClick(suggestion, newState);
        });
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    private void updateFollowButton(MaterialButton button, boolean isFollowing) {
        if (isFollowing) {
            button.setText("Siguiendo");
            button.setBackgroundTintList(context.getColorStateList(R.color.gray));
        } else {
            button.setText("Seguir");
            button.setBackgroundTintList(context.getColorStateList(R.color.primaryColor));
        }
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView txtFullName, txtUsername;
        MaterialButton btnFollow;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtFullName = itemView.findViewById(R.id.txtFullName);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}
