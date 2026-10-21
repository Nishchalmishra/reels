package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubjectEnum;
import arivihan.technologies.doubtbuzzter2.model.ToDoSubjectSelectionModel;

public class ToDoSubjectSelectionAdapter extends RecyclerView.Adapter<ToDoSubjectSelectionAdapter.ToDoSubjectSelectionViewHolder> {

    private Context context;
    private ArrayList<ToDoSubjectSelectionModel> subjectModels;
    private OnSelectionClickListener onSelectionClickListener;

    public ToDoSubjectSelectionAdapter(Context context, ArrayList<ToDoSubjectSelectionModel> subjectModels, OnSelectionClickListener onSelectionClickListener) {
        this.context = context;
        this.subjectModels = subjectModels;
        this.onSelectionClickListener = onSelectionClickListener;
    }

    @NonNull
    @Override
    public ToDoSubjectSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.to_do_step_1_subject_card,parent,false);
        return new ToDoSubjectSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoSubjectSelectionViewHolder holder, int position) {
        ToDoSubjectSelectionModel subjectModel = subjectModels.get(position);
        holder.subject_title.setText(subjectModel.getSubjectName());
        if(subjectModel.isSelected())
            holder.selection_card.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
        else
            holder.selection_card.setCardBackgroundColor(context.getColor(R.color.white));

        holder.cv_parent.setOnClickListener(view -> {
            onSelectionClickListener.onSelectionChange(position,subjectModel.isSelected());
        });
        setSubjectIcon(subjectModel.getSubjectCode(),holder);
    }

    private void setSubjectIcon(String subjectCode, ToDoSubjectSelectionViewHolder holder){
        SubjectEnum subjectEnum = SubjectEnum.fromString(subjectCode);
        switch (subjectEnum){
            case PHYSICS :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.magnetism)).into(holder.subject_icon);
                break;
            case CHEMISTRY :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.test_tube)).into(holder.subject_icon);
                break;
            case BIOLOGY :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.bacterium)).into(holder.subject_icon);
                break;
            case MATHEMATICS :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.calculator)).into(holder.subject_icon);
                break;
            case HINDI :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.hin)).into(holder.subject_icon);
                break;
            case ENGLISH :
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.eng)).into(holder.subject_icon);
                break;
            default:
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.magnetism)).into(holder.subject_icon);
        }

    }

    @Override
    public int getItemCount() {
        return subjectModels.size();
    }

    public static class ToDoSubjectSelectionViewHolder extends RecyclerView.ViewHolder{
        ImageView subject_icon;
        TextView subject_title;
        MaterialCardView selection_card;
        LinearLayout selection_card_layout;
        CardView cv_parent;

        public ToDoSubjectSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            subject_icon = itemView.findViewById(R.id.subject_icon);
            subject_title = itemView.findViewById(R.id.subject_title);
            selection_card = itemView.findViewById(R.id.selection_card);
            selection_card_layout = itemView.findViewById(R.id.selection_card_layout);
            cv_parent = itemView.findViewById(R.id.cv_parent);
        }
    }

    public interface OnSelectionClickListener{
        default void onSelectionChange(int position, boolean selected){};
    }
}
