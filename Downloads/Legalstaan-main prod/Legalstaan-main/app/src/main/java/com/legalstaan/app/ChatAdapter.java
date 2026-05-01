package com.legalstaan.app;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_bubble, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.tvMessage.setText(msg.getText());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.bubble.getLayoutParams();
        if (msg.isUser()) {
            params.gravity = Gravity.END;
            holder.bubble.setBackgroundResource(R.drawable.bubble_user);
        } else {
            params.gravity = Gravity.START;
            holder.bubble.setBackgroundResource(R.drawable.bubble_ai);
        }
        holder.bubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View bubble;
        TextView tvMessage;
        VH(View v) {
            super(v);
            bubble = v.findViewById(R.id.bubble_container);
            tvMessage = v.findViewById(R.id.tv_message);
        }
    }
}
