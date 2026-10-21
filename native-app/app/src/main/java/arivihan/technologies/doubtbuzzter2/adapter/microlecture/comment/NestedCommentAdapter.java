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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentNestedReply;
import arivihan.technologies.doubtbuzzter2.utils.HtmlTextUtils;

public class NestedCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NestedCommentAdapter.class.getSimpleName();
    private static final int TYPE_COMMENT = 1;
    private ArrayList<CommentNestedReply> commentList = new ArrayList<>();
    private CommentModel commentModel;
    private Context context;
//    private boolean isExpanded = false;

    private Callback callback;

    public interface Callback {
        void onReadMoreClick(CommentNestedReply commentNestedReply);
    }

    public NestedCommentAdapter(Context context, CommentModel commentModel, List<CommentNestedReply> models, Callback callback){
        this.context = context;
        this.commentModel = commentModel;
        this.commentList = (ArrayList<CommentNestedReply>) models;
        this.callback = callback;
    }

    public void addComments(List<CommentNestedReply> newComments) {
        int startPosition = commentList.size();
        commentList.addAll(newComments);
        notifyItemRangeInserted(startPosition, newComments.size());
    }

    public void setComments(List<CommentNestedReply> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentDiffCallback(commentList, newList));
        commentList.clear();
        commentList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateComment(CommentNestedReply updatedComment) {
        for (int i = 0; i < commentList.size(); i++) {
            if (commentList.get(i).getReplyId().equals(updatedComment.getReplyId())) {
                commentList.set(i, updatedComment);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeComment(CommentNestedReply comment) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecture_nested_comment, parent, false);
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
        TextView tv_user_name, tv_comment_text, tv_user_comment;
        ImageView iv_profile_img;
        CardView cv_parent, cv_profile;
        LinearLayout rl_parent;
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_user_comment = itemView.findViewById(R.id.tv_user_comment);
            tv_comment_text = itemView.findViewById(R.id.tv_comment_text);
            iv_profile_img = itemView.findViewById(R.id.iv_profile_img);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            cv_profile = itemView.findViewById(R.id.cv_profile);
            rl_parent = itemView.findViewById(R.id.rl_parent);
        }

        void bind(CommentNestedReply comment) {
            try {
                tv_user_name.setText(String.format(Locale.US,"@"+(StringUtils.isEmpty(commentModel.getUserName())?commentModel.getUserShortName() : commentModel.getUserName().replaceAll("\\s+", ""))));
                HtmlTextUtils.setHtmlText(tv_user_comment, commentModel.getComment());

//                tv_comment_text.setText(comment.getMessageText());

                //Glide.with(context).clear(iv_profile_img);
                int accentColor = ContextCompat.getColor(context, R.color.arivihan_secondary_color);
                HtmlTextUtils.setupReadMore(tv_comment_text, comment.getMessageText(), HtmlTextUtils.getMaxChar(),
                        accentColor, context.getString(R.string.read_more), () -> {
                            if (callback != null) {
                                callback.onReadMoreClick(comment);
                            }
                        });
//                if(!StringUtils.isEmpty(comment.getUserPic())) {
//                    Glide.with(context)
//                            .load(comment.getUserPic())
//                            .error(R.drawable.white_logo)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
//                                                            Target<Drawable> target, boolean isFirstResource) {
//                                    tv_initials.setVisibility(View.GONE);
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model,
//                                                               Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    return false;
//                                }
//                            })
//                            .into(iv_profile_img);
//                }

            } catch (Exception ex){
                ex.printStackTrace();
            }

        }
    }

    static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<CommentNestedReply> oldList;
        private final List<CommentNestedReply> newList;

        CommentDiffCallback(List<CommentNestedReply> oldList, List<CommentNestedReply> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getReplyId().equals(newList.get(newItemPosition).getReplyId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getMessageText().equals(newList.get(newItemPosition).getMessageText());
        }
    }

}
