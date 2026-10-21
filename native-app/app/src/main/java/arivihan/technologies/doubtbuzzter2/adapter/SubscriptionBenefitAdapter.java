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

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;

public class SubscriptionBenefitAdapter extends RecyclerView.Adapter<SubscriptionBenefitAdapter.SubscriptionBenefitViewHolder>{

    Context context;
    ArrayList<SubscriptionBenefitsModel> list;
    BenefitClickListener clickListener;

    public SubscriptionBenefitAdapter(Context context, ArrayList<SubscriptionBenefitsModel> list, BenefitClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SubscriptionBenefitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscription_benefit_item,parent,false);
        return new SubscriptionBenefitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionBenefitViewHolder holder, int position) {
         if(position==0) {
             holder.topDash.setVisibility(View.INVISIBLE);
             holder.midddleDash.setVisibility(View.INVISIBLE);
         }else {
             holder.topDash.setVisibility(View.VISIBLE);
             holder.midddleDash.setVisibility(View.VISIBLE);
         }

         holder.title.setText(list.get(position).getTitle());
         holder.description.setText(list.get(position).getDescription());

         if(position==list.size()-1) {
             holder.icon.setVisibility(View.GONE);
             holder.trophyIcon.setVisibility(View.VISIBLE);
             holder.divider.setVisibility(View.INVISIBLE);
             holder.bottomDash.setVisibility(View.GONE);
         }else{
             holder.icon.setVisibility(View.VISIBLE);
             holder.trophyIcon.setVisibility(View.GONE);
             holder.divider.setVisibility(View.VISIBLE);
             holder.bottomDash.setVisibility(View.VISIBLE);
         }

         holder.itemView.setOnClickListener(view -> clickListener.onGoToClick(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SubscriptionBenefitViewHolder extends RecyclerView.ViewHolder {

        LinearLayout topDash;
        LinearLayout midddleDash;
        ImageView icon;
        ImageView trophyIcon;
        ImageView goTo;
        TextView title;
        LinearLayout bottomDash;
        LinearLayout divider;
        TextView description;

        public SubscriptionBenefitViewHolder(@NonNull View itemView) {
            super(itemView);
            topDash = itemView.findViewById(R.id.top_dash);
            midddleDash = itemView.findViewById(R.id.middle_dash);
            icon = itemView.findViewById(R.id.icon);
            trophyIcon = itemView.findViewById(R.id.trophy_icon);
            goTo = itemView.findViewById(R.id.go_to);
            title = itemView.findViewById(R.id.title);
            bottomDash = itemView.findViewById(R.id.bottom_dash);
            description = itemView.findViewById(R.id.description);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    public interface BenefitClickListener{
        void onGoToClick(SubscriptionBenefitsModel subscriptionBenefitsModel);
    }
}
