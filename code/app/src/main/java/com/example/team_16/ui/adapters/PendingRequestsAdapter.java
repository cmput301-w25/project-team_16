package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display pending follow requests (Accept/Reject).
 */
public class PendingRequestsAdapter
        extends RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder> {

    /**
     * Model for a single pending request
     */
    public static class PendingRequest {
        public String requestId;
        public String fromUserId;
        public String fromUsername;

        public PendingRequest(String requestId, String fromUserId, String fromUsername) {
            this.requestId = requestId;
            this.fromUserId = fromUserId;
            this.fromUsername = fromUsername;
        }
    }

    /**
     * Interface for handling user actions on each item.
     */
    public interface OnActionListener {
        void onAcceptClicked(PendingRequest request, int position);
        void onRejectClicked(PendingRequest request, int position);
    }

    private final List<PendingRequest> dataList = new ArrayList<>();
    private final OnActionListener actionListener;

    public PendingRequestsAdapter(OnActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Overwrite the entire data set
     */
    public void setData(List<PendingRequest> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * Remove one item from the list
     */
    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PendingRequestsAdapter.ViewHolder holder, int position) {
        PendingRequest item = dataList.get(position);

        holder.textUsername.setText(item.fromUsername);
        holder.textUserId.setText("@" + item.fromUsername);

        holder.buttonAccept.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAcceptClicked(item, position);
            }
        });
        holder.buttonReject.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRejectClicked(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageProfile;
        TextView textUsername, textUserId, textMood;
        ImageButton buttonAccept, buttonReject;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView      = (CardView) itemView;
            imageProfile  = itemView.findViewById(R.id.image_profile);
            textUsername  = itemView.findViewById(R.id.text_username);
            textUserId    = itemView.findViewById(R.id.text_user_id);
            textMood      = itemView.findViewById(R.id.text_mood);
            buttonAccept  = itemView.findViewById(R.id.button_accept);
            buttonReject  = itemView.findViewById(R.id.button_reject);
        }
    }
}
