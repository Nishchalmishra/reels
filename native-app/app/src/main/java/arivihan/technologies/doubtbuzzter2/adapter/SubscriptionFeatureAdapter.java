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
import arivihan.technologies.doubtbuzzter2.model.SubscriptionData;

public class SubscriptionFeatureAdapter extends RecyclerView.Adapter<SubscriptionFeatureAdapter.FeatureViewHolder>{


    ArrayList<String> data;
    Context context;

    public SubscriptionFeatureAdapter(ArrayList<String> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscription_feature_layout, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {

       if(position==0) holder.topDash.setVisibility(View.INVISIBLE);
       else holder.topDash.setVisibility(View.VISIBLE);

       if(position==data.size()-1) {
           holder.dot.setVisibility(View.GONE);
           holder.trophy.setVisibility(View.VISIBLE);
           holder.bottomDash.setVisibility(View.INVISIBLE);
       }else {
           holder.dot.setVisibility(View.VISIBLE);
           holder.trophy.setVisibility(View.GONE);
           holder.bottomDash.setVisibility(View.VISIBLE);
       }
       holder.feature.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

     static class FeatureViewHolder extends RecyclerView.ViewHolder {

        TextView feature;
        LinearLayout topDash;
        LinearLayout bottomDash;
        MaterialCardView dot;
        ImageView trophy;
        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            feature=itemView.findViewById(R.id.feature_text);
            topDash=itemView.findViewById(R.id.top_dash);
            bottomDash=itemView.findViewById(R.id.bottom_dash);
            dot=itemView.findViewById(R.id.dot_view);
            trophy=itemView.findViewById(R.id.trophy_icon);
        }
    }
}
