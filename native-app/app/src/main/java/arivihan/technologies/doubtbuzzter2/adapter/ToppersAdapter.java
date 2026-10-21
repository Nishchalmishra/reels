package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;

public class ToppersAdapter extends RecyclerView.Adapter<ToppersAdapter.ToppersViewHolder> {

    Context context;
    ArrayList<ToppersHomeScreenModel> list;
    OnTopperClickListener onTopperClickListener;

    public ToppersAdapter(Context context, ArrayList<ToppersHomeScreenModel> list,OnTopperClickListener onTopperClickListener) {
        this.context = context;
        this.list = list;
        this.onTopperClickListener=onTopperClickListener;
    }

    @NonNull
    @Override
    public ToppersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.toppers_layout, parent, false);
        return new ToppersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToppersViewHolder holder, int position) {
        ToppersHomeScreenModel toppersHomeScreenModel = list.get(position);
        Glide.with(context)
                .load(toppersHomeScreenModel.getPosterUrl())
                .into(holder.toppersImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onTopperClickListener.onTopperClick(toppersHomeScreenModel.getVideoUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ToppersViewHolder extends RecyclerView.ViewHolder {
        ImageView toppersImage;

        public ToppersViewHolder(@NonNull View itemView) {
            super(itemView);
            toppersImage = itemView.findViewById(R.id.toppers_image);
        }
    }

    public interface OnTopperClickListener {
        void onTopperClick(String videoUrl);
    }
}
