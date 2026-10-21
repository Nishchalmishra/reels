package arivihan.technologies.doubtbuzzter2.adapter.microlecture.comment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentNestedReply;
import arivihan.technologies.doubtbuzzter2.utils.HtmlTextUtils;

public class DoubtCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DoubtCommentAdapter.class.getSimpleName();
    private static final int TYPE_COMMENT = 1;
    private ArrayList<CommentModel> commentList = new ArrayList<>();
    private Context context;

    public DoubtCommentAdapter(Context context, ArrayList<CommentModel> models){
        this.context = context;
        this.commentList = models;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecture_doubt_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).bind(commentList.get(position));
        }
    }

    public List<CommentModel> getList() {
        return commentList;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_comment_text;
        CardView cv_parent;
        LinearLayout rl_parent;
        RecyclerView rv_nested_comments;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            rl_parent = itemView.findViewById(R.id.rl_parent);
            rv_nested_comments = itemView.findViewById(R.id.rv_nested_comments);
        }

        void bind(CommentModel comment) {
            try {
                rv_nested_comments.setVisibility(View.GONE);
                HtmlTextUtils.setHtmlText(tv_comment_text, comment.getComment());

                List<CommentNestedReply> nestedReplies = comment.getNestedReply();
                if(nestedReplies!=null && !nestedReplies.isEmpty()) {
                    rv_nested_comments.setLayoutManager(new LinearLayoutManager(context));
                    DoubtCommentResponseAdapter nestedCommentAdapter = new DoubtCommentResponseAdapter(context, comment, nestedReplies);
                    rv_nested_comments.setAdapter(nestedCommentAdapter);
                    rv_nested_comments.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
