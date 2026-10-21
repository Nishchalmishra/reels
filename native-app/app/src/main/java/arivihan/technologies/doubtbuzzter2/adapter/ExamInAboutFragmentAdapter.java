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


import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class ExamInAboutFragmentAdapter extends RecyclerView.Adapter<ExamInAboutFragmentAdapter.ViewHolder> {

    ArrayList<NameIdModel> model;
    OnExamPreparationClick onExamPreparationClick;

    boolean isSecondaryExam;

    public ExamInAboutFragmentAdapter(ArrayList<NameIdModel> model, OnExamPreparationClick onExamPreparationClick, boolean isSecondaryExam) {
        this.model = model;
        this.onExamPreparationClick = onExamPreparationClick;
        this.isSecondaryExam = isSecondaryExam;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.classlayoutinaboutfragment, parent, false);
        return new ViewHolder(view, onExamPreparationClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NameIdModel m = model.get(position);
        holder.title.setText(m.getName());
        if (!m.isSelected()) {
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_grey_text));
            holder.icon_unselected.setVisibility(View.VISIBLE);
            holder.icon_selected.setVisibility(View.GONE);
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
            holder.title.setTextColor(Color.WHITE);
            holder.icon_unselected.setVisibility(View.GONE);
            holder.icon_selected.setVisibility(View.VISIBLE);
        }


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!m.isSelected()){
                    m.setSelected(true);
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
                    holder.title.setTextColor(Color.WHITE);
                    holder.icon_unselected.setVisibility(View.GONE);
                    holder.icon_selected.setVisibility(View.VISIBLE);

                    for(NameIdModel course : model){
                        if(!course.getName().equals(m.getName())){
                            course.setSelected(false);
                        }
                    }
                }

                if(!CollectionUtils.isEmpty(model)) {
                    onExamPreparationClick.onexampreparationclick(position, model.get(position).getId(),
                            model.get(position).getName(), isSecondaryExam, model.get(position).getBoardCode(), model.get(position).getNextCardOnClick());

                    Map<String, String> params = new HashMap<>();
                    params.put("Steps", "secondary_exam_selected");
                    params.put("exam", model.get(position).getId());
                    params.put("examName", model.get(position).getName());

                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView title;
        ImageView icon_selected, icon_unselected;
        CardView card;
        OnExamPreparationClick onExamPreparationClick;

        public ViewHolder(@NonNull View itemView, OnExamPreparationClick onExamPreparationClick) {
            super(itemView);
            title = itemView.findViewById(R.id.text);
            icon_selected = itemView.findViewById(R.id.status1_selected);
            icon_unselected = itemView.findViewById(R.id.status1_unselected);
            card = itemView.findViewById(R.id.topic);
            this.onExamPreparationClick = onExamPreparationClick;
//            itemView.setOnClickListener(this);
        }

//        @Override
//        public void onClick(View view) {
//            onExamPreparationClick.onexampreparationclick(-1, "-1", model.get(getAdapterPosition()).getName(), isSecondaryExam);
//        }
    }

    public interface OnExamPreparationClick {
        void onexampreparationclick(int position, String courseId, String course_name, boolean isSecondaryExam,
                                    String boardCode,String nextCardToOpen);
    }

}
