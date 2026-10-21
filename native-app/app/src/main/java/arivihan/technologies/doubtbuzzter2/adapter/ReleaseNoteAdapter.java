package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;

public class ReleaseNoteAdapter extends RecyclerView.Adapter<ReleaseNoteAdapter.ReleaseNoteViewHolder>{


    List<String> list;
    Context context;

    public ReleaseNoteAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ReleaseNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.release_notes_item,parent,false);
        return new ReleaseNoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleaseNoteViewHolder holder, int position) {
        holder.note.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class ReleaseNoteViewHolder extends RecyclerView.ViewHolder {
        TextView note;
        public ReleaseNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            note = itemView.findViewById(R.id.note);
        }
    }
}
