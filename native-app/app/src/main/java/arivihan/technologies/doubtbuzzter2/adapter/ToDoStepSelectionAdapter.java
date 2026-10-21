package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class ToDoStepSelectionAdapter extends RecyclerView.Adapter<ToDoStepSelectionAdapter.ToDoStepSelectionViewHolder> {

    private Context context;
    private final ArrayList<Boolean> list;

    public ToDoStepSelectionAdapter(Context context, ArrayList<Boolean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ToDoStepSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.step_selection_layout,parent,false);
        return new ToDoStepSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoStepSelectionViewHolder holder, int position) {
        Boolean selected = list.get(position);
        ViewGroup.MarginLayoutParams cardParams = (ViewGroup.MarginLayoutParams) holder.step_card.getLayoutParams();

        if(selected==null || !selected){
            cardParams.width = dpToPx(6);
            holder.step_card.setCardBackgroundColor(context.getColor(R.color.unselected_step_blue));
        }else{
            cardParams.width = dpToPx(20);
            holder.step_card.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
        }
        cardParams.height = dpToPx(6);
        cardParams.setMargins(5, 0, 5, 0);
        holder.step_card.setLayoutParams(cardParams);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ToDoStepSelectionViewHolder extends RecyclerView.ViewHolder{
        CardView step_card;

        public ToDoStepSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            step_card = itemView.findViewById(R.id.step_card);
        }
    }
}
