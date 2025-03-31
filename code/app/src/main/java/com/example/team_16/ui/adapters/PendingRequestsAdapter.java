/**
 * Adapter for displaying and managing pending follow requests in the Mood Tracker app.
 * Handles the display of incoming follow requests and provides callbacks for accepting or rejecting them.
 */

package com.example.team_16.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_16.R;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestsAdapter
        extends RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder> {

    /**
     * Represents a single pending follow request with user details
     */
    public static class PendingRequest {
        public String requestId;
        public String fromUserId;
        public String fromUsername;
        public String fromUserImageUrl;

        /**
         * Creates a new pending request
         * @param requestId The unique identifier of the follow request
         * @param fromUserId The ID of the user who sent the request
         * @param fromUsername The username of the user who sent the request
         * @param fromUserImageUrl The profile image URL of the user who sent the request
         */
        public PendingRequest(String requestId, String fromUserId,
                              String fromUsername, String fromUserImageUrl) {
            this.requestId = requestId;
            this.fromUserId = fromUserId;
            this.fromUsername = fromUsername;
            this.fromUserImageUrl = fromUserImageUrl;
        }
    }

    /**
     * Interface for handling accept actions on follow requests
     */
    @FunctionalInterface
    public interface OnAcceptListener {
        /**
         * Called when a follow request is accepted
         * @param request The accepted request
         * @param position The position of the request in the adapter
         */
        void onAcceptClicked(PendingRequest request, int position);
    }

    /**
     * Interface for handling reject actions on follow requests
     */
    @FunctionalInterface
    public interface OnRejectListener {
        /**
         * Called when a follow request is rejected
         * @param request The rejected request
         * @param position The position of the request in the adapter
         */
        void onRejectClicked(PendingRequest request, int position);
    }

    /**
     * Interface for handling both accept and reject actions on follow requests
     */
    public interface OnActionListener {
        /**
         * Called when a follow request is accepted
         * @param request The accepted request
         * @param position The position of the request in the adapter
         */
        void onAcceptClicked(PendingRequest request, int position);

        /**
         * Called when a follow request is rejected
         * @param request The rejected request
         * @param position The position of the request in the adapter
         */
        void onRejectClicked(PendingRequest request, int position);
    }

    /**
     * Interface for handling clicks on request items
     */
    @FunctionalInterface
    public interface OnItemClickListener {
        /**
         * Called when a request item is clicked
         * @param userId The ID of the user who sent the request
         */
        void onItemClick(String userId);
    }

    private final List<PendingRequest> dataList = new ArrayList<>();
    private final OnActionListener actionListener;
    private final OnItemClickListener itemClickListener;

    /**
     * Creates a new adapter for pending follow requests
     * @param acceptListener Listener for accept actions
     * @param rejectListener Listener for reject actions
     * @param itemClickListener Listener for item clicks
     */
    public PendingRequestsAdapter(
            OnAcceptListener acceptListener,
            OnRejectListener rejectListener,
            OnItemClickListener itemClickListener
    ) {
        this.itemClickListener = itemClickListener;

        this.actionListener = new OnActionListener() {
            @Override
            public void onAcceptClicked(PendingRequest request, int position) {
                acceptListener.onAcceptClicked(request, position);
            }
            @Override
            public void onRejectClicked(PendingRequest request, int position) {
                rejectListener.onRejectClicked(request, position);
            }
        };
    }

    public void setData(List<PendingRequest> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < dataList.size()) {
            dataList.remove(position);
            notifyItemRemoved(position);
        }
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

        if (holder.imageProfile != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.fromUserImageUrl)
                    .placeholder(R.drawable.image)
                    .fallback(R.drawable.image)
                    .circleCrop()
                    .into(holder.imageProfile);
        } else {
            Log.e("PendingRequestsAdapter",
                    "imageProfile is null for position: " + position);
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item.fromUserId);
            }
        });

        holder.buttonAccept.setOnClickListener(v ->
                actionListener.onAcceptClicked(item, holder.getAdapterPosition())
        );

        holder.buttonReject.setOnClickListener(v ->
                actionListener.onRejectClicked(item, holder.getAdapterPosition())
        );
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
            imageProfile  = itemView.findViewById(R.id.profile_image);
            textUsername  = itemView.findViewById(R.id.text_username);
            textUserId    = itemView.findViewById(R.id.text_user_id);
            textMood      = itemView.findViewById(R.id.text_mood);
            buttonAccept  = itemView.findViewById(R.id.button_accept);
            buttonReject  = itemView.findViewById(R.id.button_reject);
        }
    }
}
