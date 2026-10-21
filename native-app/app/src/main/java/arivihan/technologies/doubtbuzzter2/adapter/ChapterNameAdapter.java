package arivihan.technologies.doubtbuzzter2.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum;

public class ChapterNameAdapter extends RecyclerView.Adapter<ChapterNameAdapter.ViewHolder> implements Filterable {

    List<ChapterNameModel> list;
    List<ChapterNameModel> listFull;

    private OnChapClickListener onChapClickListener;
    private Context context;
    private String subjectName;
    private boolean custom;
    private LinearLayout noResultFoundLayout;
    RecyclerView recyclerView;
    private static final String TAG = ChapterNameAdapter.class.getSimpleName();

    //theme
    private ThemeEnum currActivityTheme = ThemeEnum.LIGHT;

    private int highlightPosition = -1;

    public void setHighlightPosition(int pos) {
        highlightPosition = pos;
    }


    public ChapterNameAdapter(Context context, List<ChapterNameModel> list, OnChapClickListener onChapClickListener, String subjectName, LinearLayout noResultFoundLayout, RecyclerView recyclerView) {
        this.list = list;
        this.onChapClickListener = onChapClickListener;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.subjectName = subjectName;
        this.noResultFoundLayout = noResultFoundLayout;
        this.recyclerView = recyclerView;
    }

    public ChapterNameAdapter(Context context, List<ChapterNameModel> list, OnChapClickListener onChapClickListener, String subjectName, LinearLayout noResultFoundLayout, RecyclerView recyclerView, ThemeEnum currActivityTheme) {
        this.list = list;
        this.onChapClickListener = onChapClickListener;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.subjectName = subjectName;
        this.noResultFoundLayout = noResultFoundLayout;
        this.recyclerView = recyclerView;
        this.currActivityTheme = currActivityTheme;
    }

    public ChapterNameAdapter(Context context, boolean custom, List<ChapterNameModel> list, OnChapClickListener onChapClickListener, String subjectName) {
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
        View view = inflater.inflate(R.layout.chapter_name_layout, parent, false);
        return new ViewHolder(view, onChapClickListener);
    }

    private void resetViews(ViewHolder holder) {
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.info.setVisibility(View.GONE);
        holder.info_image.setVisibility(View.GONE);
        holder.tv_live_info.setVisibility(View.GONE);
        holder.tv_live_info.setText("");
        holder.tv_live_rescheduled.setVisibility(View.GONE);
        holder.tv_live_rescheduled.setText("");
        holder.tv_live_rescheduled.setPaintFlags(
                holder.tv_live_rescheduled.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.teacher.setVisibility(View.VISIBLE);
        holder.image.setAlpha(1f);
        holder.chapter.setTextColor(getChapterNameColor());
        holder.teacher.setTextColor(getChapterNameColor());
    }

    private void showUpcoming(ViewHolder holder, ChapterNameModel chapterNameModel, Context ctx) {
        String msg = chapterNameModel.getChapterLiveDto().getStatusMessage();
        holder.cvUpcoming.setVisibility(View.VISIBLE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.info.setVisibility(View.GONE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(ContextCompat.getColor(ctx, R.color.gray_88));
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(ctx, msg) : msg);
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showLive(ViewHolder holder, ChapterNameModel chapterNameModel, Context ctx) {
        String msg = chapterNameModel.getChapterLiveDto().getStatusMessage();
        holder.dialogAnim.setVisibility(View.VISIBLE);
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.info.setVisibility(View.GONE);
        holder.teacher.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(ctx, msg) : msg);
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showLiveScheduled(ViewHolder holder, ChapterNameModel chapterNameModel, Context ctx) {
        String msg = chapterNameModel.getChapterLiveDto().getStatusMessage();
        holder.imgLive.setVisibility(View.VISIBLE);
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.info.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(ctx, msg) : msg);
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

    private void showConverting(ViewHolder holder, ChapterNameModel chapterNameModel, Context ctx) {
        String msg = chapterNameModel.getChapterLiveDto().getStatusMessage();
        holder.info_image.setVisibility(View.VISIBLE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(ContextCompat.getColor(ctx, R.color.gray_88));
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.cvReschedule.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(msg)) {
            holder.info.setVisibility(View.GONE);
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(msg)
                    ? StyledTextBuilder.fromJson(ctx, msg) : msg);
        } else {
            holder.info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setVisibility(View.GONE);
        }
    }

    private void showRescheduled(ViewHolder holder, ChapterNameModel chapterNameModel, Context ctx) {
        holder.cvReschedule.setVisibility(View.VISIBLE);
        holder.cvUpcoming.setVisibility(View.GONE);
        holder.dialogAnim.setVisibility(View.GONE);
        holder.imgLive.setVisibility(View.GONE);
        holder.image.setAlpha(0.4f);
        holder.chapter.setTextColor(ContextCompat.getColor(ctx, R.color.gray_88));
        holder.info.setVisibility(View.GONE);
        holder.tv_live_rescheduled.setVisibility(View.VISIBLE);
        holder.tv_live_rescheduled.setPaintFlags(
                holder.tv_live_rescheduled.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Log.d(TAG, "onBindViewHolder-chapterNameModel.getRescheduledMessage(): " + chapterNameModel.getRescheduledMessage());
        if (StringUtils.isNotEmpty(chapterNameModel.getRescheduledMessage())) {
            holder.tv_live_info.setVisibility(View.VISIBLE);
            holder.tv_live_info.setText(CommonMethods.isValidJson(chapterNameModel.getRescheduledMessage())
                    ? StyledTextBuilder.fromJson(ctx, chapterNameModel.getRescheduledMessage()) : chapterNameModel.getRescheduledMessage());
        } else {
            holder.tv_live_info.setVisibility(View.GONE);
        }
        if (StringUtils.isNotEmpty(chapterNameModel.getChapterLiveDto().getStatusMessage())) {
            holder.tv_live_rescheduled.setVisibility(View.VISIBLE);
            CharSequence statusText = CommonMethods.isValidJson(chapterNameModel.getChapterLiveDto().getStatusMessage())
                    ? StyledTextBuilder.fromJson(ctx, chapterNameModel.getChapterLiveDto().getStatusMessage()) : chapterNameModel.getChapterLiveDto().getStatusMessage();
            SpannableString greyStatusText = new SpannableString(statusText);
            greyStatusText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, android.R.color.darker_gray)), 0, greyStatusText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tv_live_rescheduled.setText(greyStatusText);
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Context ctx = holder.itemView.getContext();

        resetViews(holder);

        holder.detail_layout.setBackgroundColor(Color.TRANSPARENT);
        holder.detail_layout.setAlpha(1.0f);
        holder.itemView.setActivated(false);
        holder.itemView.setSelected(false);

        holder.image.clearColorFilter();

        holder.newBadge.setVisibility(View.GONE);
        holder.lockImage.setVisibility(View.GONE);

        holder.itemView.setBackgroundColor(getBG_Color());

        holder.teacher.setText("");
        holder.teacher.setBackgroundColor(getBG_Color());

        holder.divider.setBackgroundColor(getDividerColor());
        holder.lockImage.setBackgroundColor(getLockColor());
        holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        ChapterNameModel chapterNameModel = list.get(position);

        // Shimmer setup
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

        Glide.with(ctx).load(chapterNameModel.getImageUrl()).into(holder.image);

        holder.chapter.setText((position + 1) + ". " + chapterNameModel.getChapter());

        String video_key = "last_watched_video"
                + subjectName
                + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())
                + chapterNameModel.isBoard();
        int last_watched_video = MyApplication.getIntData(video_key);

        if (chapterNameModel.getChapterType() == null
                || chapterNameModel.getChapterType().equalsIgnoreCase(ChapterType.RECORDED_CHAPTER.name())) {

            // --- RECORDED CHAPTER BRANCH ---
            boolean isComingSoon = StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                    && chapterNameModel.getChapterStatus().equals(ChapterStatus.COMING_SOON.name())
                    && chapterNameModel.getChapterLiveDto() != null;

            if (isComingSoon && !chapterNameModel.isLocked()) {
                // Unlocked COMING_SOON: show upcoming state
                showUpcoming(holder, chapterNameModel, ctx);
                holder.lockImage.setVisibility(View.GONE);
                holder.chapter.setTextColor(ContextCompat.getColor(ctx, R.color.gray_88));
                holder.teacher.setTextColor(ContextCompat.getColor(ctx, R.color.gray_88));

            } else if (!chapterNameModel.isCompleted() && !isComingSoon) {
                holder.info.setVisibility(View.GONE);
                holder.info_image.setVisibility(View.GONE);
                holder.teacher.setVisibility(View.VISIBLE);
                holder.teacher.setText(ctx.getString(R.string.coming_soon_text));
                holder.teacher.setBackgroundColor(ContextCompat.getColor(ctx, R.color.mock_light_blue2));

                if (chapterNameModel.isLocked()) {
                    holder.lockImage.setVisibility(View.VISIBLE);
                    holder.chapter.setTextColor(getChapterNameLockColor());
                    holder.teacher.setTextColor(getChapterNameLockColor());
                } else {
                    holder.lockImage.setVisibility(View.GONE);
                    holder.chapter.setTextColor(last_watched_video == position
                            ? ContextCompat.getColor(ctx, R.color.highlight_color)
                            : getChapterNameColor());
                    holder.teacher.setTextColor(getChapterNameColor());
                }
            } else {
                holder.info.setVisibility(View.VISIBLE);
                holder.info_image.setVisibility(View.VISIBLE);
                holder.teacher.setVisibility(View.VISIBLE);
                String teacherName = chapterNameModel.getName();
                holder.teacher.setText((teacherName != null && !teacherName.isEmpty()) ? teacherName : "Arivihan");
                holder.teacher.setBackgroundColor(getBG_Color());

                if (chapterNameModel.isLocked()) {
                    holder.lockImage.setVisibility(View.VISIBLE);
                    holder.chapter.setTextColor(getChapterNameLockColor());
                    holder.teacher.setTextColor(getChapterNameLockColor());
                } else {
                    holder.lockImage.setVisibility(View.GONE);
                    holder.chapter.setTextColor(last_watched_video == position
                            ? ContextCompat.getColor(ctx, R.color.highlight_color)
                            : getChapterNameColor());
                    holder.teacher.setTextColor(getChapterNameColor());
                }
            }

        } else {

            // --- LIVE / OTHER CHAPTER BRANCH ---
            if (chapterNameModel.isLocked()) {
                holder.lockImage.setVisibility(View.VISIBLE);
                holder.chapter.setTextColor(getChapterNameLockColor());
                holder.teacher.setTextColor(getChapterNameLockColor());
                holder.cvUpcoming.setVisibility(View.GONE);
                holder.cvReschedule.setVisibility(View.GONE);
                holder.dialogAnim.setVisibility(View.GONE);
                holder.imgLive.setVisibility(View.GONE);
                holder.tv_live_info.setVisibility(View.GONE);
            } else {
                holder.lockImage.setVisibility(View.GONE);
                holder.chapter.setTextColor(last_watched_video == position
                        ? ContextCompat.getColor(ctx, R.color.highlight_color)
                        : getChapterNameColor());
                holder.teacher.setTextColor(getChapterNameColor());
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
                        showUpcoming(holder, chapterNameModel, ctx);
                        break;

                    case LIVE:
                        showLive(holder, chapterNameModel, ctx);
                        break;

                    case LIVE_SCHEDULED:
                        showLiveScheduled(holder, chapterNameModel, ctx);
                        break;

                    case RECORDED:
                        showRecorded(holder);
                        break;

                    case CONVERTING:
                        showConverting(holder, chapterNameModel, ctx);
                        break;

                    case RESCHEDULED:
                        showRescheduled(holder, chapterNameModel, ctx);
                        break;

                    default:
                        holder.info.setVisibility(View.VISIBLE);
                        break;
                }

            } else {
                holder.tv_live_info.setVisibility(View.GONE);
            }
        }

        if (position == highlightPosition) {
            highlightPosition = -1;
            int fromColor = Color.argb(70, 33, 150, 243);
            holder.itemView.setBackgroundColor(fromColor);
            ValueAnimator anim = ValueAnimator.ofArgb(fromColor, getBG_Color());
            anim.setDuration(1800);
            anim.setStartDelay(500);
            anim.addUpdateListener(a -> {
                if (holder.itemView.isAttachedToWindow()) {
                    holder.itemView.setBackgroundColor((int) a.getAnimatedValue());
                }
            });
            anim.start();
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

    int getChapterNameColor(){
        return currActivityTheme == ThemeEnum.DARK ? ContextCompat.getColor(context, R.color.chapter_name_dark) : ContextCompat.getColor(context, R.color.chapter_name_light);
    }

    int getBG_Color(){
        return currActivityTheme == ThemeEnum.DARK ? ContextCompat.getColor(context, R.color.bg_color_dark) : ContextCompat.getColor(context, R.color.bg_color_light);
    }

    int getChapterNameLockColor(){
        return currActivityTheme == ThemeEnum.DARK ? ContextCompat.getColor(context, R.color.chapter_name_lock_dark) : ContextCompat.getColor(context, R.color.chapter_name_lock_light);
    }

    int getDividerColor(){
        return currActivityTheme == ThemeEnum.DARK ? ContextCompat.getColor(context, R.color.divider_color_dark) : ContextCompat.getColor(context, R.color.divider_color_light);
    }

    int getLockColor(){
        return currActivityTheme == ThemeEnum.DARK ? ContextCompat.getColor(context, R.color.lock_color_dark) : ContextCompat.getColor(context, R.color.lock_color_light);
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
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            list.clear();
            if (filterResults != null && filterResults.values != null) {
                list.addAll((List<ChapterNameModel>) filterResults.values);
            }
            notifyDataSetChanged();
            if (noResultFoundLayout != null) {
                if (list.isEmpty()) {
                    noResultFoundLayout.setVisibility(View.VISIBLE);
                } else {
                    noResultFoundLayout.setVisibility(View.GONE);
                }
            }
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
        View cvUpcoming, cvReschedule;
        View dialogAnim;
        View imgLive;

        public ViewHolder(@NonNull View itemView, OnChapClickListener onChapClickListener) {
            super(itemView);
            image = itemView.findViewById(R.id.thumbnail_image);
            chapter = itemView.findViewById(R.id.chapter_name);
            detail_layout = itemView.findViewById(R.id.detail_layout);
            newBadge = itemView.findViewById(R.id.new_badge);
            tv_live_info = itemView.findViewById(R.id.tv_live_information);
            tv_live_rescheduled=itemView.findViewById(R.id.tv_live_rescheduled);
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