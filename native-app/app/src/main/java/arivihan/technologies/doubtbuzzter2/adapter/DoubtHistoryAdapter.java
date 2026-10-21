package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ArivihanSession;

public class DoubtHistoryAdapter extends RecyclerView.Adapter<DoubtHistoryAdapter.DoubtHistoryViewHolder> {

    Context context;
    List<ArivihanSession> list;
    DoubtClickListener doubtClickListener;

    public DoubtHistoryAdapter(Context context, List<ArivihanSession> list, DoubtClickListener doubtClickListener) {
        this.context = context;
        this.list = list;
        this.doubtClickListener = doubtClickListener;
    }

    @NonNull
    @Override
    public DoubtHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doubt_history_item, parent, false);
        return new DoubtHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoubtHistoryViewHolder holder, int position) {
        ArivihanSession session = list.get(position);

        // arivihan-sessions are always text doubts with no image and no solution status tag.
        holder.imageCard.setVisibility(View.GONE);
        holder.doubtText.setVisibility(View.VISIBLE);
        holder.statusTagLayout.setVisibility(View.INVISIBLE);
        holder.statusTagText.setText(" ");

        holder.doubtCard.setStrokeWidth(0);
        holder.doubtCard.setCardBackgroundColor(context.getResources().getColor(R.color.white));
        holder.doubtCard.setCardElevation(4);

        holder.doubtText.setText(session.getFirstMessage());

        if (StringUtils.isNotEmpty(session.getSubject())) {
            holder.subjectTag.setVisibility(View.VISIBLE);
            holder.subjectTagText.setText(session.getSubject());
        } else {
            holder.subjectTag.setVisibility(View.GONE);
        }

        holder.date.setText(changeDate(session.getUpdatedAt() != null ? session.getUpdatedAt() : session.getCreatedAt()));

        holder.itemView.setOnClickListener(v ->
                doubtClickListener.onDoubtClick(session.getChatId(), "TEXT", null));
    }

    /**
     * Formats an ISO timestamp such as {@code 2026-05-27T07:31:41.772426} to {@code dd/MM/yyyy}.
     */
    private String changeDate(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = parser.parse(datePart);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DoubtHistoryViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView doubtCard;
        RelativeLayout statusTagLayout;
        TextView statusTagText;
        TextView doubtText;
        MaterialCardView subjectTag;
        TextView subjectTagText;
        TextView date;
        MaterialCardView imageCard;
        ImageView doubtImage;

        public DoubtHistoryViewHolder(@NonNull View itemView) {
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

    public interface DoubtClickListener {
        void onDoubtClick(String chatSessionId, String type, String status);
        //void loadData();
    }
}
