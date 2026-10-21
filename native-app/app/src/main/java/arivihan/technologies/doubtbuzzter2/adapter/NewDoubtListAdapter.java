package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DoubtSolutionStatus;
import arivihan.technologies.doubtbuzzter2.model.WebChatRecord;
import arivihan.technologies.doubtbuzzter2.model.doubt.DoubtSessionHistory;


public class NewDoubtListAdapter extends RecyclerView.Adapter<NewDoubtListAdapter.ViewHolder> {

    Context context;
    private final List<DoubtSessionHistory> list;
    private final OnDoubtClickListener listener;


    public NewDoubtListAdapter(Context context, List<DoubtSessionHistory> list, OnDoubtClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doubt_history_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoubtSessionHistory item = list.get(position);

            holder.imageCard.setVisibility(View.GONE);
            holder.doubtText.setVisibility(View.VISIBLE);

            holder.statusTagLayout.setVisibility(View.VISIBLE);
            holder.statusTagLayout.setBackground(context.getResources().getDrawable(R.drawable.best_value_card_drawable_blue));
            holder.doubtCard.setStrokeWidth(2);
            holder.doubtCard.setStrokeColor(context.getResources().getColor(R.color.doubt_highlight_blue));
            holder.doubtCard.setCardBackgroundColor(context.getResources().getColor(R.color.doubt_background_blue));
            holder.doubtCard.setCardElevation(0);
            holder.statusTagText.setText(context.getString(R.string.SOLVED_UNSEEN));
                holder.doubtText.setText(item.getFirstMessage());
        holder.subjectTagText.setText(TextUtils.isEmpty(item.getSubject()) ? "-" : item.getSubject());

        Log.d("DateParse", "Original date: " + item.getCreatedAt());

        try {
            String formattedDate = changeDate(item.getCreatedAt());
            Log.d("DateParse", "Formatted date: " + formattedDate);
            holder.date.setText(formattedDate);
        } catch (ParseException e) {
            Log.e("DateParse", "Error parsing date: " + item.getCreatedAt(), e);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoubtClick(item);
        });


    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }


    private String changeDate(String value) throws ParseException {
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "EEE MMM dd HH:mm:ss zzzz yyyy"
        };
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
                sdf.setLenient(false);
                return formatter.format(sdf.parse(value));
            } catch (ParseException ignored) {}
        }
        throw new ParseException("Unparseable date: " + value, 0);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView doubtCard;
        RelativeLayout statusTagLayout;
        TextView statusTagText;
        TextView doubtText;
        MaterialCardView subjectTag;
        TextView subjectTagText;
        TextView date;
        MaterialCardView imageCard;
        ImageView doubtImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            doubtCard = itemView.findViewById(R.id.doubt_card);
            statusTagLayout = itemView.findViewById(R.id.status_tag_layout);
            statusTagText = itemView.findViewById(R.id.status_tag_text);
            doubtText = itemView.findViewById(R.id.doubt_text);
            subjectTag = itemView.findViewById(R.id.subject_tag);
            subjectTagText = itemView.findViewById(R.id.subject_tag_text);
            date = itemView.findViewById(R.id.date);
            imageCard = itemView.findViewById(R.id.doubt_image_card);
            doubtImage = itemView.findViewById(R.id.doubt_image);
        }
    }


    public interface OnDoubtClickListener {
        void onDoubtClick(DoubtSessionHistory doubt);
    }

}
