package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class ClassInAboutFragmentAdapter extends RecyclerView.Adapter<ClassInAboutFragmentAdapter.ViewHolder> {

    ArrayList<NameIdModel> model;
    OnClassClick onClassClick;

    public ClassInAboutFragmentAdapter(ArrayList<NameIdModel> model, OnClassClick onClassClick) {
        this.model = model;
        this.onClassClick = onClassClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.classlayoutinaboutfragment,parent,false);
        return new ViewHolder(view,onClassClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NameIdModel m = model.get(position);
        holder.title.setText(m.getName());

        if(!m.isSelected()){
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_grey_text));
            holder.icon_unselected.setVisibility(View.VISIBLE);
            holder.icon_selected.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    m.setSelected(true);
                    setAllCardUnselected(model,position);
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
                    holder.title.setTextColor(Color.WHITE);
                    holder.icon_unselected.setVisibility(View.GONE);
                    holder.icon_selected.setVisibility(View.VISIBLE);
                onClassClick.onclassclick(m.getId(),m.getName());
                Map<String,String>params= new HashMap<>();
                params.put("Steps","class_selected");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(),params);
            }
        });
    }

    private void setAllCardUnselected(ArrayList<NameIdModel> Model, int position) {
        for(int i=0;i<Model.size();i++){
            if(i != position){
                Model.get(i).setSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView icon_selected,icon_unselected;
        CardView card;
        OnClassClick onClassClick;

        public ViewHolder(@NonNull View itemView,OnClassClick onClassClick) {
            super(itemView);
            title = itemView.findViewById(R.id.text);
            icon_selected = itemView.findViewById(R.id.status1_selected);
            icon_unselected = itemView.findViewById(R.id.status1_unselected);
            card = itemView.findViewById(R.id.topic);
            this.onClassClick = onClassClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClassClick.onclassclick(model.get(getAdapterPosition()).getId(),"subject");
        }
    }

    public interface OnClassClick{
        void onclassclick(String position,String clas);
    }

}
