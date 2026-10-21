package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BoardQuestionActivity;
import arivihan.technologies.doubtbuzzter2.fragments.LongAnswerQuestionFragment2;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;

public class MatchTheseAdapterLeft extends RecyclerView.Adapter<MatchTheseAdapterLeft.MatchTheseViewHolderLeft> {

    ArrayList<MatchTheseLeftOption> list;
    Context context;
    Integer height;

    int mheight;

    public MatchTheseAdapterLeft(ArrayList<MatchTheseLeftOption> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MatchTheseViewHolderLeft onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.match_these_item,parent,false);
        return new MatchTheseViewHolderLeft(view);
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchTheseViewHolderLeft holder, int position) {
        String option = list.get(position).getValue();
        holder.textView.setText(list.get(position).getOption_number() + ". " + option);
        if (height != null) {
            holder.textView.setHeight(height);
        }
        if(BoardQuestionActivity.isMatchTheseSubmitted || LongAnswerQuestionFragment2.isMatchTheseSubmittedMock) {
            if (list.get(position).isCorrectlyMatched()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    holder.cardView.setOutlineAmbientShadowColor(context.getResources().getColor(R.color.match_green));
                    holder.cardView.setOutlineSpotShadowColor(context.getResources().getColor(R.color.match_green));
                }
                holder.linearLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.match_these_border_green));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    holder.cardView.setOutlineAmbientShadowColor(context.getResources().getColor(R.color.match_red));
                    holder.cardView.setOutlineSpotShadowColor(context.getResources().getColor(R.color.match_red));

                }
                holder.linearLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.match_these_border_red));           }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MatchTheseViewHolderLeft extends RecyclerView.ViewHolder {
        CardView cardView;
       TextView textView;
       LinearLayout linearLayout;
        public MatchTheseViewHolderLeft(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.match_these_cardView);
            textView=itemView.findViewById(R.id.option_mt);
            linearLayout = itemView.findViewById(R.id.match_these_linear_layout);
        }
    }
}
