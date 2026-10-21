package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;

public class ChoiceTypeAdapter extends RecyclerView.Adapter<ChoiceTypeAdapter.ChoiceTypeViewHolder> {

    private Context context;
    private List<String> list;
    private FactoryShipQnADto factoryShipQnADto;
    private OptionListener optionListener;
    boolean optionClicked;
    private String[] option = {"A) ", "B) ", "C) ", "D) ", "E) ", "F) ", "G) ", "H) ", "I) "};

    public ChoiceTypeAdapter(Context context, List<String> list, FactoryShipQnADto factoryShipQnADto, OptionListener optionListener) {
        this.context = context;
        this.list = list;
        this.factoryShipQnADto = factoryShipQnADto;
        this.optionListener = optionListener;
    }

    @NonNull
    @Override
    public ChoiceTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_option_item, parent, false);
        return new ChoiceTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChoiceTypeViewHolder holder, int position) {


        if (factoryShipQnADto.getOptionType() != null) {
            if (factoryShipQnADto.getOptionType().equals("IMAGE")) {
                holder.optionText.setVisibility(View.GONE);
                Glide.with(context).load(list.get(position)).into(holder.OptionImage);
                holder.OptionImage.setVisibility(View.VISIBLE);
            }
        } else {
            holder.optionText.setText(list.get(position).trim());
            holder.OptionImage.setVisibility(View.GONE);
        }

        holder.optionNumber.setText(option[position]);
        try {
            if ((!factoryShipQnADto.isReattempt() && factoryShipQnADto.isCompleted()) || factoryShipQnADto.isReview()) {
                if (String.valueOf(position + 1).equals(factoryShipQnADto.getAnswer())) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.correct_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_green));
                } else if (factoryShipQnADto.getUserAnswer().equals(String.valueOf(position + 1)) &&
                        !factoryShipQnADto.getUserAnswer().equals(factoryShipQnADto.getAnswer())) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.wrong_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_red));
                }

            } else if (optionClicked) {
                updateSingleChoice(position, holder);
            }
        }catch (Exception e){
            Log.d( "onBindViewHolder:",e.getMessage());
        }

        holder.optionLayout.setOnClickListener(v -> {
            if(!factoryShipQnADto.isCompleted()|| factoryShipQnADto.isReattempt()){
                optionClicked = true;
                optionListener.setSelected(position);
            }
        });
    }

    private void updateSingleChoice(int position, ChoiceTypeViewHolder holder) {
        if (factoryShipQnADto.getUserAnswer().equals(String.valueOf(position + 1))) {
            holder.singleOption.setCardBackgroundColor(context.getResources().getColor(R.color.selected));
            holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_teal));
        } else {
            holder.singleOption.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ChoiceTypeViewHolder extends RecyclerView.ViewHolder {
        private TextView optionText;
        private RelativeLayout optionLayout;
        private TextView optionNumber;
        private ImageView done, wrong, OptionImage;

        private CardView singleOption;
        private LinearLayout singleOptionContainer;

        public ChoiceTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            optionText = itemView.findViewById(R.id.OP);
            optionLayout = itemView.findViewById(R.id.option_layout);
            optionNumber = itemView.findViewById(R.id.Option);
            done = itemView.findViewById(R.id.done);
            OptionImage = itemView.findViewById(R.id.OPImage);
            wrong = itemView.findViewById(R.id.wrong);
            singleOption = itemView.findViewById(R.id.single_option_card);
            singleOptionContainer = itemView.findViewById(R.id.single_option_card_container);

        }
    }

}
