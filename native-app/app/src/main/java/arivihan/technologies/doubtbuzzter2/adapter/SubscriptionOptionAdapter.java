package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionOptions;

public class SubscriptionOptionAdapter extends RecyclerView.Adapter<SubscriptionOptionAdapter.OptionsViewHolder> {

    ArrayList<SubscriptionOptions> data;
    Context context;
    OptionClickListener optionClickListener;
    public boolean isComboSelected;

    public SubscriptionOptionAdapter(ArrayList<SubscriptionOptions> data, Context context, OptionClickListener optionClickListener, boolean isComboSelected) {
        this.data = data;
        this.context = context;
        this.optionClickListener = optionClickListener;
        this.isComboSelected=isComboSelected;
    }

    @NonNull
    @Override
    public OptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscription_option_item, parent, false);
        return new OptionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionsViewHolder holder, int position) {

            if(position==data.size()-1){
                holder.divider.setVisibility(View.GONE);
            }else
                holder.divider.setVisibility(View.VISIBLE);
            SubscriptionOptions subscriptionOptions = data.get(position);
            if(subscriptionOptions.isSelected()){
                holder.checkBox.setStrokeWidth(0);
                holder.checkBox.setBackground(context.getResources().getDrawable(R.drawable.checkmark));
            }else{
                holder.checkBox.setStrokeWidth(4);
                holder.checkBox.setStrokeColor(context.getResources().getColor(R.color.black));
                holder.checkBox.setBackground(context.getResources().getDrawable(R.color.white));
            }



            holder.optionTitle.setText(subscriptionOptions.getName());
            holder.optionPrice.setText(String.format(context.getString(R.string.price_format), subscriptionOptions.getCostPrice()));


        holder.optionLayout.setOnClickListener(v -> {
            optionClickListener.onOptionClick(position);

        });

    }

    public void setComboSelected(boolean isComboSelected){
        this.isComboSelected=isComboSelected;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

     static class OptionsViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView checkBox;
        TextView optionTitle;
        TextView optionPrice;
        LinearLayout divider;
        LinearLayout optionLayout;
        public OptionsViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_box);
            optionTitle=itemView.findViewById(R.id.option_title);
            optionPrice=itemView.findViewById(R.id.option_price);
            divider=itemView.findViewById(R.id.divider);
            optionLayout=itemView.findViewById(R.id.option_layout);
        }
    }

    public interface OptionClickListener{
         void onOptionClick(int position);
    }
}
