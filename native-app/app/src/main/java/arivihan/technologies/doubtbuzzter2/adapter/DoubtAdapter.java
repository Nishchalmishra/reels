package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.List;
import java.util.TimeZone;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ChatSupportActivity;
import arivihan.technologies.doubtbuzzter2.model.DoubtModel;

public class DoubtAdapter extends RecyclerView.Adapter<DoubtAdapter.ViewHolder> {

    private List<DoubtModel> doubt;
    OnDoubtClick onDoubtClick;
    Context context;
    String ActivityStatus;

    FastDateFormat fdf = FastDateFormat.getInstance("dd-MM-yyyy", TimeZone.getTimeZone("IST"));

    public DoubtAdapter(List<DoubtModel> doubt, OnDoubtClick onDoubtClick, Context context, String ActivityStatus) {
        this.doubt = doubt;
        this.onDoubtClick = onDoubtClick;
        this.context = context;
        this.ActivityStatus = ActivityStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_layout_doubtlist, parent, false);
        return new DoubtAdapter.ViewHolder(view, onDoubtClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoubtModel model = doubt.get(position);
        if (!model.isPseudoDoubt()) {
            holder.subject.setText(model.getSubject());
            holder.format.setText("Required Format : " + model.getPreferredFormat());
        } else {
            holder.subject.setVisibility(View.GONE);
            holder.format.setVisibility(View.GONE);
            holder.chapter.setVisibility(View.GONE);
        }
        if (model.getDescription().length() != 0) {
            holder.Discip.setVisibility(View.VISIBLE);
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(model.getDescription());
        }
        if (model.isUnwatched()) {
            holder.badge.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
        }
        holder.description.setText(model.getDescription());
        holder.chapter.setText(model.getChapter());
        holder.date.setText(String.format("%s%s", context.getString(R.string.askedOn), fdf.format(model.getAskedOn())));
        if (model.getDoubtType().equals("TEXT")) {
            holder.doubt.setText(StringUtils.capitalize(model.getText()));
        } else {
            holder.doubt.setVisibility(View.GONE);
            holder.doubtImage.setVisibility(View.VISIBLE);

            Glide.with(context).load(model.getImageUrl()).into(holder.doubtImage);
        }

        if (ActivityStatus.equals("SolvedDoubt")) {
            holder.undone.setText("Solved");
            holder.undone.setVisibility(View.GONE);
            holder.done.setVisibility(View.GONE);
            holder.solutionImage.setVisibility(View.VISIBLE);
            if (model.isChatHistoryExists()) {
                holder.chatHistoryBtn.setVisibility(View.VISIBLE);
            } else {
                holder.chatHistoryBtn.setVisibility(View.GONE);
            }
        } else {
            if (model.getSolved() == true) {
                holder.done.setVisibility(View.VISIBLE);
                holder.undone.setVisibility(View.GONE);
                holder.chatHistoryBtn.setVisibility(View.GONE);
            } else {
                holder.done.setVisibility(View.GONE);
                holder.undone.setVisibility(View.VISIBLE);
            }
        }

        holder.solutionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDoubtClick.ondoubtclick(position);
            }
        });


        holder.chatHistoryBtn.setOnClickListener((view -> {
            try {
                Intent intent = new Intent(context, ChatSupportActivity.class);
                intent.putExtra("doubtId", doubt.get(position).getDoubtId());
                intent.putExtra("type", "history");
                context.startActivity(intent);
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity ChatSupportActivity.class");
            }
        }));

    }

    @Override
    public int getItemCount() {
        return doubt.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        OnDoubtClick onDoubtClick;
        TextView doubt, description, chapter, subject, format, Discip;
        ImageView doubtImage, badge, chatHistoryBtn;
        TextView done, undone;
        CardView solutionImage;
        TextView date;


        public ViewHolder(@NonNull View itemView, OnDoubtClick onDoubtClick) {
            super(itemView);
            doubt = itemView.findViewById(R.id.doubtText);
            description = itemView.findViewById(R.id.descriptionText);
            chapter = itemView.findViewById(R.id.chapName);
            solutionImage = itemView.findViewById(R.id.solution_card);
            Discip = itemView.findViewById(R.id.Descrip);
            subject = itemView.findViewById(R.id.SubName);
            badge = itemView.findViewById(R.id.badge);
            format = itemView.findViewById(R.id.formatRequired);
            doubtImage = itemView.findViewById(R.id.doubtImage);
            done = itemView.findViewById(R.id.doubtStatus1);
            date = itemView.findViewById(R.id.date);
            undone = itemView.findViewById(R.id.doubtStatus2);
            chatHistoryBtn = itemView.findViewById(R.id.chat_history_btn);
            itemView.setOnClickListener(this);

            this.onDoubtClick = onDoubtClick;
        }

        @Override
        public void onClick(View view) {

        }
    }

    public interface OnDoubtClick {
        void ondoubtclick(int position);
    }
}
