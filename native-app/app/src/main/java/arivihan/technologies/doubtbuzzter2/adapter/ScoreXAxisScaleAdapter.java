package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class ScoreXAxisScaleAdapter extends RecyclerView.Adapter<ScoreXAxisScaleAdapter.ScoreXAxisScaleViewHolder> {
    Context context;
    ArrayList<String> list;

    public ScoreXAxisScaleAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ScoreXAxisScaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vertical_number_item,parent,false);
        return  new ScoreXAxisScaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreXAxisScaleViewHolder holder, int position) {
       holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ScoreXAxisScaleViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        public ScoreXAxisScaleViewHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.range);
        }
    }
}
