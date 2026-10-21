package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionType;

public class SubscriptionTypeAdapter extends RecyclerView.Adapter<SubscriptionTypeAdapter.TypeViewHolder> {

    ArrayList<SubscriptionType> data;
    Context context;

    TypeClickListener typeClickListener;

    public SubscriptionTypeAdapter(ArrayList<SubscriptionType> data, Context context, TypeClickListener typeClickListener) {
        this.data = data;
        this.context = context;
        this.typeClickListener = typeClickListener;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscription_type_item, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        SubscriptionType subscriptionType = data.get(position);
        holder.typeTitle.setText(subscriptionType.getTitle());

        if (subscriptionType.is_selected()) {
            holder.typeTitle.setTextColor(ContextCompat.getColor(context, R.color.subscription_blue));
            holder.divider.setBackgroundColor(ContextCompat.getColor(context, R.color.subscription_blue));
        } else {
            holder.typeTitle.setTextColor(ContextCompat.getColor(context, R.color.subscription_type_grey));
            holder.divider.setBackgroundColor(ContextCompat.getColor(context, R.color.subscription_type_divider));
        }

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params instanceof RecyclerView.LayoutParams) {
            RecyclerView.LayoutParams rvParams = (RecyclerView.LayoutParams) params;
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;

            int margin = (int) (10 * displayMetrics.density);

            if (data.size() == 2) {
                if (position == 0) {
                    rvParams.width = (screenWidth * 1 / 4) - margin; // 25%
                } else {
                    rvParams.width = (screenWidth * 3 / 4) - margin; // 75%
                }

                holder.itemView.setLayoutParams(rvParams);
                holder.typeTitle.setGravity(Gravity.CENTER);
            } else {
                rvParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(rvParams);
                holder.typeTitle.setGravity(Gravity.START);
            }
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                holder.typeTitle, 9, 18, 1, TypedValue.COMPLEX_UNIT_SP
        );


        holder.itemView.setOnClickListener(v -> {
            if (!subscriptionType.is_selected()) {
                typeClickListener.onTypeClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class TypeViewHolder extends RecyclerView.ViewHolder {
        TextView typeTitle;
        View divider;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTitle = itemView.findViewById(R.id.type_title);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    public interface TypeClickListener{
        void onTypeClick(int position);
    }
}
