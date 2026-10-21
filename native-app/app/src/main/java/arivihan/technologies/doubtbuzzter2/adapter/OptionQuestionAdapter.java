
package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.OptionModel;

public class OptionQuestionAdapter extends RecyclerView.Adapter<OptionQuestionAdapter.ViewHolder> {

    ArrayList<OptionModel> optionModel;
    OnOptionClick onOptionClick;
    Context context;

    public OptionQuestionAdapter(ArrayList<OptionModel> optionModel,OnOptionClick onOptionClick,Context context) {
        this.optionModel = optionModel;
        this.onOptionClick = onOptionClick;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.optionlayout,parent,false);
        return new ViewHolder(view,onOptionClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OptionModel model = optionModel.get(position);
        if(model.getOptionType() != null && model.getOptionType().equals("IMAGE")) {
            holder.OptionImage.setVisibility(View.VISIBLE);
            holder.Option.setVisibility(View.GONE);
            Glide.with(context).load(model.getOption()).into(holder.OptionImage);
        }
        else {
            holder.OptionImage.setVisibility(View.GONE);
            holder.Option.setVisibility(View.VISIBLE);
            holder.Option.setText(model.getOption());
        }
        char c = (char) (65+position);
        holder.option_num.setText(""+c);
        if(model.getSubmit().equals("1")){
            if(position == model.correctOption){
                holder.l1.setBackgroundColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.l2.setBackgroundColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.l3.setBackgroundColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.l4.setBackgroundColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.Option.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.option_num.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                holder.correct.setVisibility(View.VISIBLE);
            }
            if(position == model.getSelectedOption() && position != model.correctOption){
                holder.l1.setBackgroundColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.l2.setBackgroundColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.l3.setBackgroundColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.l4.setBackgroundColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.Option.setTextColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.option_num.setTextColor(ContextCompat.getColor(context, R.color.wrong_answer_text_color));
                holder.wrong.setVisibility(View.VISIBLE);
            }
        }
        if(model.selectedOption == position && model.getSubmit().equals("-1")){
            holder.l1.setBackgroundColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
            holder.l2.setBackgroundColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
            holder.l3.setBackgroundColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
            holder.l4.setBackgroundColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
            holder.Option.setTextColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
            holder.option_num.setTextColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
        }

    }

    @Override
    public int getItemCount() {
        return optionModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView option_num;
        private TextView Option;
        private LinearLayout l1,l2,l3,l4;
        private ImageView correct,wrong,OptionImage;
        private CardView card;
        OnOptionClick onOptionClick;

        public ViewHolder(@NonNull View itemView,OnOptionClick onOptionClick) {
            super(itemView);
            option_num = itemView.findViewById(R.id.Option);
            Option = itemView.findViewById(R.id.OP);
            card = itemView.findViewById(R.id.card);
            OptionImage = itemView.findViewById(R.id.OPImage);
            correct = itemView.findViewById(R.id.done);
            wrong = itemView.findViewById(R.id.wrong);
            l1 = itemView.findViewById(R.id.top);
            l2 = itemView.findViewById(R.id.down);
            l3 = itemView.findViewById(R.id.left);
            l4 = itemView.findViewById(R.id.right);
            this.onOptionClick = onOptionClick;
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onOptionClick.onClick(getAdapterPosition());
        }
    }

    public interface OnOptionClick{
        void onClick(int position);
    }

}

