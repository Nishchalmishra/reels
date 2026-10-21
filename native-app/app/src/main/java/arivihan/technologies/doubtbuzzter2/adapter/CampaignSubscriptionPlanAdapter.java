package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;

public class CampaignSubscriptionPlanAdapter extends RecyclerView.Adapter<CampaignSubscriptionPlanAdapter.SubscriptionCardViewHolder> {

    private Context context;
    private ArrayList<SubscriptionPlans> subscriptionPlans;
    private OnSubscriptionCardClickListener listener;

    public interface OnSubscriptionCardClickListener {
        void onCardClick(int position, SubscriptionPlans subscriptionPlan);
    }

    public CampaignSubscriptionPlanAdapter(Context context, ArrayList<SubscriptionPlans> subscriptionPlans, OnSubscriptionCardClickListener listener) {
        this.context = context;
        this.subscriptionPlans = subscriptionPlans;
        this.listener = listener;

    }

    @NonNull
    @Override
    public SubscriptionCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.campaign_subscription_card, parent, false);
        return new SubscriptionCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionCardViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SubscriptionPlans plan = subscriptionPlans.get(position);
        holder.discountPrice.setText(String.format(context.getString(R.string.price_format), plan.getFinalPrice()));
        holder.costPrice.setText(String.format(context.getString(R.string.price_format), plan.getCostPrice()));
        holder.costPrice.setPaintFlags(holder.costPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.pricePerMonth.setText(String.format(context.getString(R.string.price_format), plan.getPerMonthPrice()) + "/");
        holder.months.setText(plan.getDuration()+" "+
                plan.getDurationUnit().getFormattedName(plan.getDuration()));

        if (plan.isSelected()) {
            holder.cardLayout.setBackground(context.getDrawable(R.drawable.yellow_gradient_2));
            holder.priceCard.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            holder.cardLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.priceCard.setBackground(context.getDrawable(R.drawable.yellow_gradient_3));
        }

        holder.itemView.setOnClickListener(v -> {
            notifyDataSetChanged();
            listener.onCardClick(position, plan);
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionPlans.size();
    }

    public static class SubscriptionCardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardLayout;
        LinearLayout priceCard;
        TextView discountPrice;
        TextView costPrice;
        TextView pricePerMonth;
        TextView months;

        @SuppressLint("WrongViewCast")
        public SubscriptionCardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLayout = itemView.findViewById(R.id.card_layout);
            priceCard = itemView.findViewById(R.id.price_card);
            discountPrice = itemView.findViewById(R.id.discount_price);
            costPrice = itemView.findViewById(R.id.cost_price);
            pricePerMonth = itemView.findViewById(R.id.price_per_month);
            months = itemView.findViewById(R.id.months);
        }
    }
}
