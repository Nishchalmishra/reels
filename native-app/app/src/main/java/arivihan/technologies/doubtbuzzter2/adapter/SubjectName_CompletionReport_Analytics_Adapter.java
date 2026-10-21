package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class SubjectName_CompletionReport_Analytics_Adapter extends RecyclerView.Adapter<SubjectName_CompletionReport_Analytics_Adapter.ViewHolder> {

  ArrayList<String> name;

    public SubjectName_CompletionReport_Analytics_Adapter(ArrayList<String> name) {
        this.name = name;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.chaptername_subject_completion_numberview_analytics,parent,false);
        return new  SubjectName_CompletionReport_Analytics_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.subject.setText(name.get(position));
            if(position == 0)
                holder.first.setVisibility(View.VISIBLE);
            if(position == 1)
                holder.second.setVisibility(View.VISIBLE);
            if(position == 2)
                holder.third.setVisibility(View.VISIBLE);
            if(position == 3)
                holder.forth.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView subject;
        ImageView first,second,third,forth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.subjectName);
            first = itemView.findViewById(R.id.blueCircle);
            second = itemView.findViewById(R.id.redCircle);
            third = itemView.findViewById(R.id.greenCircle);
            forth = itemView.findViewById(R.id.circle4);
        }
    }
}
