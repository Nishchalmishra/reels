
package arivihan.technologies.doubtbuzzter2.adapter.competitive.subject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CompetitiveChapterNameAdapter extends RecyclerView.Adapter<CompetitiveChapterNameAdapter.ViewHolder> implements Filterable {

    List<ChapterNameModel> list;
    List<ChapterNameModel> listFull;

    private OnChapClickListener onChapClickListener;
    private Context context;
    private String subjectName;
    private boolean custom;
    private RecyclerView recyclerView;
    private LinearLayout noResultFoundLayout;
    private static final String TAG = CompetitiveChapterNameAdapter.class.getSimpleName();


    public CompetitiveChapterNameAdapter(Context context, List<ChapterNameModel> list, OnChapClickListener onChapClickListener, String subjectName, LinearLayout noResultFoundLayout, RecyclerView recyclerView ) {
        this.list = list;
        this.onChapClickListener = onChapClickListener;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.subjectName = subjectName;
        this.noResultFoundLayout = noResultFoundLayout;
        this.recyclerView = recyclerView;
    }

    public CompetitiveChapterNameAdapter(Context context, boolean custom, List<ChapterNameModel> list, OnChapClickListener onChapClickListener, String subjectName) {
        this.custom = custom;
        this.list = list;
        this.onChapClickListener = onChapClickListener;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.subjectName = subjectName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.competitive_chapter_name_layout, parent, false);
        return new ViewHolder(view, onChapClickListener);
    }

    private void resetViews(ViewHolder holder) {
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.info_image.setVisibility(View.GONE);
        holder.tv_live_info.setVisibility(View.GONE);
        holder.tv_live_info.setText("");
        holder.tv_live_rescheduled.setVisibility(View.GONE);
        holder.tv_live_rescheduled.setText("");
        holder.tv_live_rescheduled.setPaintFlags(
                holder.tv_live_rescheduled.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.teacher.setVisibility(View.VISIBLE);
        holder.image.setAlpha(1f);
        holder.chapter.setTextColor(context.getColor(R.color.black));
    }

    private void showUpcoming(ViewHolder holder, ChapterNameModel model) {
        String msg = model.getChapterLiveDto().getStatusMessage();
        holder.cvUpcoming.setVisibility(View.VISIBLE);
        holder.info.setVisibility(View.GONE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(context.getColor(R.color.gray_88));
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(context, msg) : msg);
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showLive(ViewHolder holder, ChapterNameModel model) {
        String msg = model.getChapterLiveDto().getStatusMessage();
        holder.info.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.VISIBLE);
        holder.teacher.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(context, msg) : msg);
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showLiveScheduled(ViewHolder holder, ChapterNameModel model) {
        String msg = model.getChapterLiveDto().getStatusMessage();
        holder.info.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.VISIBLE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(context, msg) : msg);
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showRecorded(ViewHolder holder) {
        holder.info.setVisibility(View.VISIBLE);
        holder.info_image.setVisibility(View.VISIBLE);
        holder.image.setAlpha(1.0f);
        holder.tv_live_info.setVisibility(View.GONE);
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
    }

    private void showConverting(ViewHolder holder, ChapterNameModel model) {
        String msg = model.getChapterLiveDto().getStatusMessage();
        holder.info.setVisibility(View.GONE);
        holder.info_image.setVisibility(View.VISIBLE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(context.getColor(R.color.gray_88));
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(context, msg) : msg);
        } else {
            holder.info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showRescheduled(ViewHolder holder, ChapterNameModel model) {
        holder.cvReschedule.setVisibility(View.VISIBLE);
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(context.getColor(R.color.gray_88));
        holder.info.setVisibility(View.GONE);
        holder.tv_live_rescheduled.setVisibility(View.VISIBLE);
        holder.tv_live_rescheduled.setPaintFlags(
                holder.tv_live_rescheduled.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Log.d(TAG, "onBindViewHolder-chapterNameModel.getRescheduledMessage(): " + model.getRescheduledMessage());
        if (StringUtils.isNotEmpty(model.getRescheduledMessage())) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(model.getRescheduledMessage())
                    ? StyledTextBuilder.fromJson(context, model.getRescheduledMessage())
                    : model.getRescheduledMessage());
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
        if (StringUtils.isNotEmpty(model.getChapterLiveDto().getStatusMessage())) {
            holder.tv_live_rescheduled.setVisibility(View.VISIBLE);
            holder.tv_live_rescheduled.setText(CommonMethods.isValidJson(model.getChapterLiveDto().getStatusMessage())
                    ? StyledTextBuilder.fromJson(context, model.getChapterLiveDto().getStatusMessage())
                    : model.getChapterLiveDto().getStatusMessage());
            holder.tv_live_rescheduled.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        resetViews(holder);

        holder.newBadge.setVisibility(View.GONE);
        holder.lockImage.setVisibility(View.GONE);
        holder.teacher.setBackground(context.getResources().getDrawable(R.color.bg_color));

        holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        ChapterNameModel chapterNameModel = list.get(position);

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        holder.newBadge.setVisibility(chapterNameModel.isNewLecture() ? View.VISIBLE : View.GONE);

        Glide.with(context).load(chapterNameModel.getImageUrl()).into(holder.image);

        holder.chapter.setText((position + 1) + ". " + chapterNameModel.getChapter());

        String video_key = "last_watched_video" + subjectName
                + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())
                + chapterNameModel.isBoard();
        int last_watched_video = MyApplication.getIntData(video_key);

        if (chapterNameModel.getChapterType() == null
                || chapterNameModel.getChapterType().equalsIgnoreCase(ChapterType.RECORDED_CHAPTER.name())) {

            if (!chapterNameModel.isCompleted()) {
                holder.info.setVisibility(View.GONE);
                holder.info_image.setVisibility(View.GONE);
                holder.teacher.setText(context.getString(R.string.coming_soon_text));
                holder.teacher.setBackground(context.getDrawable(R.color.mock_light_blue2));
            } else {
                holder.info.setVisibility(View.VISIBLE);
                holder.info_image.setVisibility(View.VISIBLE);
                holder.teacher.setText(chapterNameModel.getName());
                holder.teacher.setBackground(context.getDrawable(R.color.white));
            }

            if (chapterNameModel.isLocked()) {
                holder.lockImage.setVisibility(View.VISIBLE);
                holder.chapter.setTextColor(context.getColor(R.color.mock_dark_grey));
            } else {
                holder.lockImage.setVisibility(View.GONE);
                holder.chapter.setTextColor(last_watched_video == position
                        ? context.getColor(R.color.highlight_color)
                        : context.getColor(R.color.black));
            }

        } else {

            if (chapterNameModel.isLocked()) {
                holder.lockImage.setVisibility(View.VISIBLE);
                holder.chapter.setTextColor(context.getColor(R.color.mock_dark_grey));
                holder.cvUpcoming.setVisibility(View.GONE);
                holder.cvReschedule.setVisibility(View.GONE);
                holder.dialogAnim.setVisibility(View.GONE);
                holder.imgLive.setVisibility(View.GONE);
                holder.tv_live_info.setVisibility(View.GONE);
            } else {
                holder.lockImage.setVisibility(View.GONE);
                holder.chapter.setTextColor(last_watched_video == position
                        ? context.getColor(R.color.highlight_color)
                        : context.getColor(R.color.black));
            }

            if (StringUtils.isNotEmpty(chapterNameModel.getName())) {
                holder.teacher.setText(chapterNameModel.getName());
                holder.teacher.setVisibility(View.VISIBLE);
            } else {
                holder.teacher.setVisibility(View.GONE);
            }

            holder.info.setVisibility(View.VISIBLE);
            holder.info_image.setVisibility(View.VISIBLE);
            holder.detail_layout.setAlpha(1.0f);

            if (!chapterNameModel.isLocked()
                    && chapterNameModel.getChapterType() != null
                    && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                    && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                    && chapterNameModel.getChapterLiveDto() != null) {

                switch (ChapterStatus.fromString(chapterNameModel.getChapterStatus())) {

                    case UPCOMING:
                        showUpcoming(holder, chapterNameModel);
                        break;

                    case LIVE:
                        showLive(holder, chapterNameModel);
                        break;

                    case LIVE_SCHEDULED:
                        showLiveScheduled(holder, chapterNameModel);
                        break;

                    case RECORDED:
                        showRecorded(holder);
                        break;

                    case CONVERTING:
                        showConverting(holder, chapterNameModel);
                        break;

                    case RESCHEDULED:
                        showRescheduled(holder, chapterNameModel);
                        break;

                    default:
                        holder.info.setVisibility(View.VISIBLE);
                        break;
                }

            } else {
                holder.tv_live_info.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (!chapterNameModel.isLocked()) MyApplication.saveData(video_key, position);
            onChapClickListener.onChapClick(position);
        });

        holder.info.setOnClickListener(v -> {
            if (!chapterNameModel.isLocked()) {
                holder.info.setEnabled(false);
                onChapClickListener.onInfoClicked(chapterNameModel.getTeacherIntroImage());
                holder.info.setEnabled(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ChapterNameModel> filteredlist = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(listFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (ChapterNameModel cp : listFull) {
                    String chap = cp.getChapter();
                    if (chap.toLowerCase().contains(pattern)) filteredlist.add(cp);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredlist;
            results.count = filteredlist.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            if (filterResults == null || filterResults.values == null) {
                notifyDataSetChanged();
                return;
            }
            Log.i("EMPTYRESULTGIVDLS", "EMPTY :: " + filterResults.count);
            if (filterResults.count == 0 && noResultFoundLayout != null) {
                noResultFoundLayout.setVisibility(View.VISIBLE);
            } else if (noResultFoundLayout != null) {
                noResultFoundLayout.setVisibility(View.GONE);
            }
            list.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        OnChapClickListener onChapClickListener;
        private ImageView image;
        private LinearLayout lockImage;
        private CardView newBadge;
        public TextView chapter;
        public TextView teacher;
        public TextView tv_live_info, tv_live_rescheduled;
        LinearLayout divider;
        public LinearLayout info;
        ImageView info_image;
        LinearLayout detail_layout;
        View cvUpcoming;
        View cvReschedule;
        View dialogAnim;
        View imgLive;

        public ViewHolder(@NonNull View itemView, OnChapClickListener onChapClickListener) {
            super(itemView);
            image = itemView.findViewById(R.id.thumbnail_image);
            chapter = itemView.findViewById(R.id.chapter_name);
            detail_layout = itemView.findViewById(R.id.detail_layout);
            newBadge = itemView.findViewById(R.id.new_badge);
            tv_live_info = itemView.findViewById(R.id.tv_live_information);
            tv_live_rescheduled = itemView.findViewById(R.id.tv_live_rescheduled);
            teacher = itemView.findViewById(R.id.chapter_teacher);
            divider = itemView.findViewById(R.id.divider);
            lockImage = itemView.findViewById(R.id.thumbnail_locked_image);
            info = itemView.findViewById(R.id.info_layout);
            info_image = itemView.findViewById(R.id.info_button);
            cvUpcoming = itemView.findViewById(R.id.cv_upcoming);
            cvReschedule = itemView.findViewById(R.id.cv_rescheduled);
            dialogAnim = itemView.findViewById(R.id.dialog_animation);
            imgLive = itemView.findViewById(R.id.img_live);
            this.onChapClickListener = onChapClickListener;
        }
    }

    public interface OnChapClickListener {
        void onChapClick(int position);
        void onInfoClicked(String imageUrl);
    }
}