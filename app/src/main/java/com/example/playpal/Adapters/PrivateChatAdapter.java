package com.example.playpal.Adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.playpal.Models.Message;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playpal.R;


import java.util.List;

public class PrivateChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private  int VIEW_TYPE_SENT = 1;
    private  int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String senderId;

    public PrivateChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.senderId = currentUserId;
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        Log.d("MessageCheck", "Sender: " + message.getSender() + ", Receiver: " + message.getReceiver() + ", CurrentUser: " + senderId);
        if (message.getSender() != null && message.getSender().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_sender_message, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_receiver_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(Message message) {
            messageTextView.setText(message.getMessage());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(Message message) {
            messageTextView.setText(message.getMessage());
        }
    }
}