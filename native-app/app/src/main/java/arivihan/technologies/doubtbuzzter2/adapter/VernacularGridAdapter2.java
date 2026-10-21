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

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class VernacularGridAdapter2 extends RecyclerView.Adapter<VernacularGridAdapter2.ViewHolder> {

    private List<VernacularItem> vernacularItemList;
    private Context context;
    private VernacularAction2 action;
    private boolean type = false;

    public VernacularGridAdapter2(List<VernacularItem> vernacularItemList, Context context, VernacularAction2 action) {
        this.vernacularItemList = vernacularItemList;
        this.context = context;
        this.action = action;
    }

    public VernacularGridAdapter2(List<VernacularItem> vernacularItemList, Context context, VernacularAction2 action, Boolean type) {
        this.vernacularItemList = vernacularItemList;
        this.context = context;
        this.action = action;
        this.type = type;
    }

    @NonNull
    @Override
    public VernacularGridAdapter2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        view = inflater.inflate(R.layout.classlayoutinaboutfragment, parent, false);
        return new VernacularGridAdapter2.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VernacularGridAdapter2.ViewHolder holder, int position) {
        VernacularItem vernacularItem = vernacularItemList.get(position);
        holder.title.setText(vernacularItem.name);
        if(vernacularItem.checked){
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.navy_blue));
            holder.title.setTextColor(Color.WHITE);
            holder.icon_unselected.setVisibility(View.GONE);
            holder.icon_selected.setVisibility(View.VISIBLE);
        } else{
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.medium_grey_text));
            holder.icon_unselected.setVisibility(View.VISIBLE);
            holder.icon_selected.setVisibility(View.GONE);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<vernacularItemList.size();i++) {
                    vernacularItemList.get(i).setChecked(false);
                }
                vernacularItemList.get(position).setChecked(true);
                notifyDataSetChanged();
                action.changeImage(vernacularItem);
                MyApplication.saveData(PreferenceKeys.PROGRESS_RELOAD.getKey(), true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return vernacularItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon_selected, icon_unselected;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text);
            icon_selected = itemView.findViewById(R.id.status1_selected);
            icon_unselected = itemView.findViewById(R.id.status1_unselected);
            card = itemView.findViewById(R.id.topic);
        }
    }

    public interface VernacularAction2 {
        void changeImage(VernacularItem item);
    }
}