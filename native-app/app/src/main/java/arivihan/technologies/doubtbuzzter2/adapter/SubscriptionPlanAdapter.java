package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.telephony.SubscriptionPlan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;

public class SubscriptionPlanAdapter extends RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder> {

    ArrayList<SubscriptionPlans> data;
    Context context;
    PlanClickListener planClickListener;


    public SubscriptionPlanAdapter(ArrayList<SubscriptionPlans> data, Context context, PlanClickListener planClickListener) {
        this.data = data;
        this.context = context;
        this.planClickListener = planClickListener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscription_plans_layout, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        SubscriptionPlans subscriptionPlan = data.get(position);

          if(subscriptionPlan.isSelected()) {
              holder.strokeCard.setStrokeWidth(5);
              holder.strokeCard.setStrokeColor(context.getResources().getColor(R.color.subscription_highlight_color));
              holder.strokeCard.setCardBackgroundColor(context.getResources().getColor(R.color.mock_light_blue));
          }else{
              holder.strokeCard.setStrokeWidth(0);
              holder.strokeCard.setCardBackgroundColor(context.getResources().getColor(R.color.white));
          }

        if(subscriptionPlan.isBestValue()) {
            holder.bestValueBadge.setVisibility(View.VISIBLE);
        }else {
            holder.bestValueBadge.setVisibility(View.GONE);
        }

          holder.plan_name.setText(subscriptionPlan.getName());
          holder.pricePerMonth.setText(String.format(context.getString(R.string.price_per_month),subscriptionPlan.getPerMonthPrice()));
          holder.savingBadgeText.setText(String.format(context.getString(R.string.you_save),subscriptionPlan.getSavingBadgeText()));
          holder.costPrice.setText(String.format(context.getString(R.string.price_format),subscriptionPlan.getCostPrice()));
          holder.costPrice.setPaintFlags(holder.costPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
          holder.finalPrice.setText(String.format(context.getString(R.string.price_format),subscriptionPlan.getFinalPrice()));
          holder.itemView.setOnClickListener(v -> {
              if(!subscriptionPlan.isSelected())
                planClickListener.onPlanClick(position);
          });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

     static class PlanViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView strokeCard;
        ConstraintLayout bestValueBadge;
        TextView savingBadgeText;
        TextView plan_name;
        TextView pricePerMonth;
        TextView costPrice;
        TextView finalPrice;
        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            strokeCard = itemView.findViewById(R.id.stroke_card);
            bestValueBadge = itemView.findViewById(R.id.best_value_layout);
            savingBadgeText = itemView.findViewById(R.id.saving_badge_text);
            plan_name = itemView.findViewById(R.id.plan_name);
            pricePerMonth = itemView.findViewById(R.id.price_per_month);
            costPrice = itemView.findViewById(R.id.cost_price);
            finalPrice = itemView.findViewById(R.id.final_price);
        }
    }

    public interface PlanClickListener{
        void onPlanClick(int position);
    }
}
