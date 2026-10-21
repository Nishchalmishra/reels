package arivihan.technologies.doubtbuzzter2.adapter.microlecture.comment;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentNestedReply;
import arivihan.technologies.doubtbuzzter2.utils.HtmlTextUtils;

public class DoubtCommentResponseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DoubtCommentResponseAdapter.class.getSimpleName();
    private static final int TYPE_COMMENT = 1;
    private ArrayList<CommentNestedReply> commentList = new ArrayList<>();
    private CommentModel commentModel;
    private Context context;

    public DoubtCommentResponseAdapter(Context context, CommentModel commentModel, List<CommentNestedReply> models) {
        this.context = context;
        this.commentModel = commentModel;
        this.commentList = (ArrayList<CommentNestedReply>) models;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_COMMENT;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecture_nested_doubt_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).bind(commentList.get(position));
        }
    }

    public List<CommentNestedReply> getList() {
        return commentList;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_comment_text;
        ImageView iv_profile_img;
        CardView cv_parent, cv_profile;
        LinearLayout rl_parent;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            iv_profile_img = itemView.findViewById(R.id.iv_profile_img);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            cv_profile = itemView.findViewById(R.id.cv_profile);
            rl_parent = itemView.findViewById(R.id.rl_parent);
        }

        void bind(CommentNestedReply comment) {
            try {
                int accentColor = ContextCompat.getColor(context, R.color.arivihan_secondary_color);
                String readMoreLabel = context.getString(R.string.read_more);
                String readLessLabel = context.getString(R.string.read_less);
                applyReadMoreToggle(tv_comment_text, comment, accentColor, readMoreLabel, readLessLabel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void applyReadMoreToggle(TextView textView, CommentNestedReply comment,
                                         int accentColor, String readMoreLabel, String readLessLabel) {
            HtmlTextUtils.setupReadMoreWithToggle(textView, comment.getMessageText(),
                    comment.isExpanded(), HtmlTextUtils.getMaxChar(), accentColor,
                    readMoreLabel, readLessLabel,
                    (tv, fullText, wasExpanded) -> {
                        comment.setExpanded(!wasExpanded);
                        applyReadMoreToggle(tv, comment, accentColor, readMoreLabel, readLessLabel);
                    });
        }
    }

}
