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
import android.widget.RelativeLayout;
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

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CommentAdapter.class.getSimpleName();
    private static final int TYPE_COMMENT = 1;
    private ArrayList<CommentModel> commentList = new ArrayList<>();
    private String userId;
    private List<String> colorList;
    private Context context;

    public interface Callback {
        void onEdit(CommentModel comment, int position);

        void onRetryNewComment(CommentModel comment);

        void onNestedReplyReadMoreClick(CommentModel commentModel, CommentNestedReply commentNestedReply, int color);
    }

    private Callback callback;

    public CommentAdapter(Context context, String userId, List<String> colorList, ArrayList<CommentModel> models, Callback callback) {
        this.context = context;
        this.colorList = colorList;
        this.commentList = models;
        this.callback = callback;
        this.userId = userId;
    }

    public void addComments(List<CommentModel> newComments) {
        int startPosition = commentList.size();
        commentList.addAll(newComments);
        notifyItemRangeInserted(startPosition, newComments.size());
    }

    public void setComments(List<CommentModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentDiffCallback(commentList, newList));
        commentList.clear();
        commentList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void addCommentAtTop(CommentModel comment) {
//        if(!commentList.isEmpty() && commentList.get(0).isUserComment()){
////            comment.setUniqueId(commentList.get(0).getUniqueId());
//            updateComment(comment);
////            commentList.set(0, comment);
////            notifyItemChanged(0);
//            return;
//        }
        commentList.add(0, comment);
        notifyItemInserted(0);
    }

    public void updateComment(CommentModel updatedComment) {
        for (int i = 0; i < commentList.size(); i++) {
            if (commentList.get(i).getId().equals(updatedComment.getId())) {
                commentList.set(i, updatedComment);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeComment(CommentModel comment) {
        int index = commentList.indexOf(comment);
        if (index != -1) {
            commentList.remove(index);
            notifyItemRemoved(index);
        }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecture_comment, parent, false);
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

    private int getRandomColor() {
        if (colorList == null || colorList.isEmpty()) {
            return ContextCompat.getColor(context, R.color.arivihan_secondary_color);
        }
        int index = new Random().nextInt(colorList.size());
        return Color.parseColor(colorList.get(index));
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_initials, tv_user_name, tv_time, tv_comment_text, tv_dot;
        ImageView iv_profile_img;
        CardView cv_parent, cv_options, cv_profile;
        LinearLayout rl_parent;
        RecyclerView rv_nested_comments;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_initials = itemView.findViewById(R.id.tv_initials);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_dot = itemView.findViewById(R.id.tv_dot);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            iv_profile_img = itemView.findViewById(R.id.iv_profile_img);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            cv_options = itemView.findViewById(R.id.cv_options);
            cv_profile = itemView.findViewById(R.id.cv_profile);
            rl_parent = itemView.findViewById(R.id.rl_parent);
            rv_nested_comments = itemView.findViewById(R.id.rv_nested_comments);
        }

        void bind(CommentModel comment) {
            try {
                tv_user_name.setText(String.format(Locale.US, "@" + (StringUtils.isEmpty(comment.getUserName()) ? comment.getUserShortName() : comment.getUserName().replaceAll("\\s+", ""))));
                rv_nested_comments.setVisibility(View.GONE);
                if (StringUtils.isNotEmpty(comment.getTimeAgo())) {
                    tv_time.setText(comment.getTimeAgo());
                    tv_dot.setVisibility(View.VISIBLE);
                } else {
                    tv_time.setText("");
                    tv_dot.setVisibility(View.GONE);
                }
                HtmlTextUtils.setHtmlText(tv_comment_text, comment.getComment());
                tv_initials.setText(comment.getUserShortName());

                cv_options.setVisibility(View.GONE);
                int bgColor = getRandomColor();
                cv_profile.setCardBackgroundColor(bgColor);
                if (userId.equals(comment.getUserId())) {
                    cv_options.setVisibility(View.VISIBLE);
                }

                cv_options.setOnClickListener(cardView -> {
                    callback.onEdit(comment, getBindingAdapterPosition());
                });


                Glide.with(context).clear(iv_profile_img);
                if (!StringUtils.isEmpty(comment.getUserPic())) {
                    Glide.with(context)
                            .load(comment.getUserPic())
                            .error(R.drawable.white_logo)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target, boolean isFirstResource) {
                                    tv_initials.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(iv_profile_img);
                }

                enableDisableView(rl_parent, !comment.isLoading());

                List<CommentNestedReply> nestedReplies = comment.getNestedReply();
                if (nestedReplies != null && !nestedReplies.isEmpty()) {
                    rv_nested_comments.setLayoutManager(new LinearLayoutManager(context));
                    NestedCommentAdapter nestedCommentAdapter = new NestedCommentAdapter(context, comment, nestedReplies,
                            new NestedCommentAdapter.Callback() {
                                @Override
                                public void onReadMoreClick(CommentNestedReply commentNestedReply) {
                                    if (callback != null)
                                        callback.onNestedReplyReadMoreClick(comment, commentNestedReply, bgColor);
                                }
                            });
                    rv_nested_comments.setAdapter(nestedCommentAdapter);
                    rv_nested_comments.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    private void enableDisableView(View view, boolean isEnable) {
        try {
            if (isEnable) {
                view.setAlpha(1.0f);
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                view.setEnabled(true);
            } else {
                view.setAlpha(0.4f);
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.comment_divider_color));
                view.setEnabled(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "Error: " + ex.getMessage());
        }
    }

    static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<CommentModel> oldList;
        private final List<CommentModel> newList;

        CommentDiffCallback(List<CommentModel> oldList, List<CommentModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getComment().equals(newList.get(newItemPosition).getComment());
        }
    }

}
