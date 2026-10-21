package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.util.Log;
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
import arivihan.technologies.doubtbuzzter2.model.AddSubjectAboutFragmentModel;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class AddSubjectAboutFragmentAdapter extends RecyclerView.Adapter<AddSubjectAboutFragmentAdapter.ViewHolder> {

    ArrayList<AddSubjectAboutFragmentModel> model;
    OnSubjectClick onSubjectClick;

    public AddSubjectAboutFragmentAdapter(ArrayList<AddSubjectAboutFragmentModel> model, OnSubjectClick onSubjectClick) {
        this.model = model;
        this.onSubjectClick = onSubjectClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.classlayoutinaboutfragment, parent, false);
        return new ViewHolder(view, onSubjectClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddSubjectAboutFragmentModel m = model.get(position);
        holder.title.setText(m.getName());
        if (!m.getAdditional()) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
            holder.title.setTextColor(Color.WHITE);
            holder.icon_unselected.setVisibility(View.GONE);
            holder.icon_selected.setVisibility(View.VISIBLE);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> params= new HashMap<>();
                params.put("Steps","Subject_selection");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(),params);
                if (m.getAdditional() && m.getSelected().equals("0")) {
                    Log.i("selected", " " + m.getSelected() + "  " + holder.title.getText().toString());
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
                    holder.title.setTextColor(Color.WHITE);
                    holder.icon_unselected.setVisibility(View.GONE);
                    holder.icon_selected.setVisibility(View.VISIBLE);
                    onSubjectClick.onsubjectclick(m.getName(), m.getId());
                    m.setSelected("1");
                } else if (m.getAdditional() && m.getSelected().equals("1")) {
                    holder.card.setCardBackgroundColor(Color.WHITE);
                    holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_grey_text));
                    holder.icon_unselected.setVisibility(View.VISIBLE);
                    holder.icon_selected.setVisibility(View.GONE);
                    onSubjectClick.onsubjectclick(m.getName(), m.getId());
                    m.setSelected("0");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView icon_selected, icon_unselected;
        CardView card;
        OnSubjectClick onSubjectClick;

        public ViewHolder(@NonNull View itemView, OnSubjectClick onSubjectClick) {
            super(itemView);
            title = itemView.findViewById(R.id.text);
            icon_selected = itemView.findViewById(R.id.status1_selected);
            icon_unselected = itemView.findViewById(R.id.status1_unselected);
            card = itemView.findViewById(R.id.topic);
            this.onSubjectClick = onSubjectClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSubjectClick.onsubjectclick(model.get(getAdapterPosition()).getName(), model.get(getAdapterPosition()).getId());
        }
    }

    public interface OnSubjectClick {
        void onsubjectclick(String clas, String id);
    }
}
