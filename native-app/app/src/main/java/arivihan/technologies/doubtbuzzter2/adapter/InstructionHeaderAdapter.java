package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;

public class InstructionHeaderAdapter extends RecyclerView.Adapter<InstructionHeaderAdapter.ViewHolder> {

    List<List<String>> list;
    Context context;

    public InstructionHeaderAdapter(List<List<String>> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.instruction_header_layout, parent, false);
        return new InstructionHeaderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.header.setText(list.get(position).get(0));
        holder.post.setText((position + 1) + ". ");

        List<String> l = new ArrayList<>();
        for (int i = 1; i < list.get(position).size(); i++)
            l.add(list.get(position).get(i));

        InstructionBodyAdapter adapter = new InstructionBodyAdapter(context,l, false);
//        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(adapter);

        if (l.isEmpty()) {
            holder.instructionDetailCard.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView header, post;
        private RecyclerView recyclerView;
        CardView instructionDetailCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            post = itemView.findViewById(R.id.postNumber);
            recyclerView = itemView.findViewById(R.id.recyclerview_instruction);
            instructionDetailCard = itemView.findViewById(R.id.instruction_detail_card);
        }
    }
}
