package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class CampaignFeatureAdapter extends RecyclerView.Adapter<CampaignFeatureAdapter.CampaignFeatureViewHolder>{


    ArrayList<String> data;
    Context context;

    public CampaignFeatureAdapter(ArrayList<String> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public CampaignFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.campaign_subscription_feature, parent, false);
        return new CampaignFeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignFeatureViewHolder holder, int position) {

        if(position==0) holder.topDash.setVisibility(View.INVISIBLE);
        else holder.topDash.setVisibility(View.VISIBLE);

        if(position==data.size()-1) {
            holder.dot.setImageDrawable(context.getDrawable(R.drawable.trophy_icon));
            holder.bottomDash.setVisibility(View.INVISIBLE);
        }else {
            holder.dot.setImageDrawable(context.getDrawable(R.drawable.ic_done_blue));
            holder.bottomDash.setVisibility(View.VISIBLE);
        }
        holder.feature.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class CampaignFeatureViewHolder extends RecyclerView.ViewHolder {

        TextView feature;
        LinearLayout topDash;
        LinearLayout bottomDash;
        ImageView dot;
        public CampaignFeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            feature=itemView.findViewById(R.id.feature_text);
            topDash=itemView.findViewById(R.id.top_dash);
            bottomDash=itemView.findViewById(R.id.bottom_dash);
            dot=itemView.findViewById(R.id.dot_view);
        }
    }
}
