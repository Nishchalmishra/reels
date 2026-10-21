package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.LeaderboardMockTestModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class LeaderboardMockTestAdapter extends RecyclerView.Adapter<LeaderboardMockTestAdapter.ViewHolder>  {

    List<LeaderboardMockTestModel> model;
    Context context;

    public LeaderboardMockTestAdapter(List<LeaderboardMockTestModel> model,Context context) {
        this.model = model;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_rank_layout, parent, false);
        return new LeaderboardMockTestAdapter.ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        holder.points.setText(model.get(position).getPoints()+"");
        holder.name.setText(model.get(position).getUserName());
        holder.rank.setText(model.get(position).getRank()+"");
        holder.timeTaken.setVisibility(View.GONE);

        if(model.get(position).self){
            holder.Mypoints.setText(model.get(position).getPoints()+"");
            holder.Myname.setText(model.get(position).getUserName());
            holder.Myrank.setText(model.get(position).getRank()+"");
            holder.timeTaken.setVisibility(View.GONE);
            holder.mainCard.setVisibility(View.VISIBLE);
            if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                Glide.with(context).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(holder.Myprofile);
            }
            holder.otherCard.setVisibility(View.GONE);

        }


    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView rank,name,points, timeTaken;
        ImageView profile;
        TextView Myrank,Myname,Mypoints, MytimeTaken;
        ImageView Myprofile;
        CardView mainCard,otherCard;

        public ViewHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            name = itemView.findViewById(R.id.name);
            points = itemView.findViewById(R.id.points);
            timeTaken = itemView.findViewById(R.id.timeTaken);
            profile = itemView.findViewById(R.id.image);
            Myname = itemView.findViewById(R.id.Myname);
            Mypoints = itemView.findViewById(R.id.Mypoints);
            Myrank = itemView.findViewById(R.id.Myrank);
            MytimeTaken = itemView.findViewById(R.id.MytimeTaken);
            Myprofile = itemView.findViewById(R.id.Myimage);
            otherCard = itemView.findViewById(R.id.otherCard);
            mainCard = itemView.findViewById(R.id.myCard);
        }

    }
}
