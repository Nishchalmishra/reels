package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.PopularLiveClassesModel;

public class PopularLiveClassesAdapter extends RecyclerView.Adapter<PopularLiveClassesAdapter.PopularLiveClassesViewHolder>{

    private List<PopularLiveClassesModel> cm;

    public PopularLiveClassesAdapter(List<PopularLiveClassesModel> popularclasseslive) {
        cm = popularclasseslive;
    }

    @NonNull
    @Override
    public PopularLiveClassesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.popular_live_classes_layout,parent,false);
        return new PopularLiveClassesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularLiveClassesViewHolder holder, int position) {
        PopularLiveClassesModel c = cm.get(position);
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);
        holder.image.setImageResource(c.getImages());

    }

    @Override
    public int getItemCount() {
        return cm.size();
    }

    public class PopularLiveClassesViewHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public PopularLiveClassesViewHolder(@NonNull View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.profile_icon);
        }
    }
}
