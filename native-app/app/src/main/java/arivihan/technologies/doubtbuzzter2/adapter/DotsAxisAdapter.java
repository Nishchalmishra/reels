package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;

public class DotsAxisAdapter extends RecyclerView.Adapter<DotsAxisAdapter.DotsAxisViewHolder>{

    Context context;
    ArrayList<String> list;

    public DotsAxisAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DotsAxisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.dot_view,parent,false);
        return new DotsAxisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DotsAxisViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DotsAxisViewHolder extends RecyclerView.ViewHolder {
        public DotsAxisViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
