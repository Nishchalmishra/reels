package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.listener.DoubtChatActivityActions;
import arivihan.technologies.doubtbuzzter2.model.OptionResponse;

import arivihan.technologies.doubtbuzzter2.R;

public class DoubtChatDisambiguationAdapter extends RecyclerView.Adapter<DoubtChatDisambiguationAdapter.ViewHolder> {

    List<OptionResponse> options;

    boolean clickable;

    DoubtChatActivityActions actions;

    public DoubtChatDisambiguationAdapter(List<OptionResponse> options, DoubtChatActivityActions actions, boolean clickable) {
        this.options = options;
        this.actions = actions;
        this.clickable = clickable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.doubt_chat_diambiguation_recycler_view_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.textView.setText(convertToRichText(options.get(i).getTitle()));

        if (clickable) {
            viewHolder.textCardView.setOnClickListener(v -> {
                if (options.size() - 1 >= i) {
                    actions.onDisambiguationItemClick(i, options.get(i).isSomethingElseOption());
                }
            });
        }
    }

    public SpannableStringBuilder convertToRichText(String text) {
        String[] list = text.split("<b>");
        SpannableStringBuilder finalResponse = new SpannableStringBuilder();
        int startingIndex = 0;
        int endingIndex = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].contains("(bold)")) {
                String add = list[i].replace("(bold)", "");
                finalResponse.append(add);
                endingIndex = finalResponse.length();
                finalResponse.setSpan(new StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0);
            } else {
                finalResponse.append(list[i]);
                startingIndex = finalResponse.length();
            }
        }

        return finalResponse;
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        CardView textCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCardView = itemView.findViewById(R.id.doubt_text_card);
            textView = itemView.findViewById(R.id.doubt_text);
        }
    }
}
