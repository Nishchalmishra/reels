package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ChatSupportActivity;
import arivihan.technologies.doubtbuzzter2.model.TutorSupport;

public class ChatSupportHistoryListAdapter extends RecyclerView.Adapter<ChatSupportHistoryListAdapter.HistoryViewHolder> {

    private List<TutorSupport> chatHistory;
    private Context context;

    public ChatSupportHistoryListAdapter(List<TutorSupport> chatHistory, Context context) {
        this.chatHistory = chatHistory;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View historyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_support_history_recyclerview_item, parent, false);
        return new HistoryViewHolder(historyView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.chat.setText(chatHistory.get(position).getChatRequest());
        holder.date.setText(chatHistory.get(position).getCreatedAt().toLocaleString());

        holder.itemView.setOnClickListener(V -> {
            try{
                Intent intent = new Intent(context, ChatSupportActivity.class);
                intent.putExtra("sessionId", chatHistory.get(position).getSessionId() + "");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }catch (Exception e){
                Log.i("ARIVIHAN_EXCEPTION","Unable to start activity ChatSupportActivity.class");
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatHistory.size();
    }

    protected static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView chat, date;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            chat = itemView.findViewById(R.id.last_chat_text);
            date = itemView.findViewById(R.id.chat_date);
        }
    }
}
