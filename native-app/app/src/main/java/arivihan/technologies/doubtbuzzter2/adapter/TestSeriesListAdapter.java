package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesListAdapter extends RecyclerView.Adapter<TestSeriesListAdapter.TestSeriesListViewHolder> {

    Context context;
    List<TestSeriesSetDto> list;
    List<TestSeriesSetDto> listFull;

    ArrayList<TestSeriesQuestionDto> testSeriesQuestionList = null;
    MockTestClickListener mockTestClickListener;
    String listId;


    public TestSeriesListAdapter(Context context, List<TestSeriesSetDto> list, MockTestClickListener mockTestClickListener, String listId) {
        this.context = context;
        this.list = list;
        this.listFull = list;
        this.mockTestClickListener = mockTestClickListener;
        this.listId = listId;
    }

    @NonNull
    @Override
    public TestSeriesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.test_series_list_item, parent, false);
        return new TestSeriesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestSeriesListViewHolder holder, int position) {

        TestSeriesSetDto testSeriesSetDto = list.get(position);
        String lastSelectedId = MyApplication.getStringData(String.format(context.getString(R.string.lastTestAttempted),
                listId,
                MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
        String lastTestAttemptedInSubject = MyApplication.getStringData(String.format(context.getString(R.string.lastTestAttemptedInSubject),
                listId,
                testSeriesSetDto.getTestSeriesTypes().get(0).getSubject(),
                MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
        holder.title.setText(testSeriesSetDto.getTestSeriesName());
        if (testSeriesSetDto.isEvaluationInProgress()) {
            holder.icon.setVisibility(View.GONE);
            holder.checkingAnimation.setVisibility(View.VISIBLE);
            holder.checkingAnimation.loop(true);
            holder.checkingAnimation.playAnimation();
            holder.attemptStatusText.setText(context.getString(R.string.checking));
            holder.attemptStatusText.setTextColor(context.getResources().getColor(R.color.subscription_blue));
            holder.attemptStatusLine.setVisibility(View.GONE);
            holder.attemptStatusIcon.setVisibility(View.GONE);
            holder.testDetail.setVisibility(View.GONE);
            holder.userDetail.setVisibility(View.GONE);
            holder.inProgressText.setVisibility(View.VISIBLE);
            holder.viewResult.setVisibility(View.INVISIBLE);
            holder.freeBadge.setVisibility(View.INVISIBLE);
            if ((StringUtils.isNotEmpty(lastSelectedId) && lastSelectedId.equals(testSeriesSetDto.getTestSeriesId()))
                    || (StringUtils.isNotEmpty(lastTestAttemptedInSubject) && lastTestAttemptedInSubject.equals(testSeriesSetDto.getTestSeriesId())))
                holder.parentCard.setStrokeColor(context.getResources().getColor(R.color.subscription_blue));
            else
                holder.parentCard.setStrokeColor(context.getResources().getColor(R.color.mock_light_grey));
        } else {
            holder.icon.setVisibility(View.VISIBLE);
            Glide.with(context).load(testSeriesSetDto.getIconUrl()).into(holder.icon);
            holder.checkingAnimation.setVisibility(View.GONE);
            holder.checkingAnimation.cancelAnimation();
            holder.inProgressText.setVisibility(View.GONE);
            if (testSeriesSetDto.isLock()) {
                holder.parentCard.setStrokeColor(context.getResources().getColor(R.color.light_red));
                holder.freeBadge.setVisibility(View.INVISIBLE);
                holder.testDetail.setVisibility(View.VISIBLE);
                holder.userDetail.setVisibility(View.GONE);
                holder.testTime.setText(CommonMethods.getTimeInMinutes(testSeriesSetDto.getTotalTimeToAttempt()) + context.getString(R.string.mins));
                holder.testMarks.setText(testSeriesSetDto.getTotalScore() + context.getString(R.string.Marks));
                holder.numberOfQuestions.setText(testSeriesSetDto.getTotalQuestions() + context.getString(R.string.Qns));
                holder.attemptStatusText.setText(context.getString(R.string.locked));
                holder.attemptStatusText.setTextColor(context.getResources().getColor(R.color.locked_red));
                holder.attemptStatusLine.setBackgroundColor(context.getResources().getColor(R.color.locked_red));
                Glide.with(context).load(context.getResources().getDrawable(R.drawable.red_lock)).into(holder.attemptStatusIcon);
                holder.attemptStatusIcon.setScaleX(1);
                holder.attemptStatusIcon.setScaleY(1);
                holder.bottomLayout.setBackgroundColor(context.getResources().getColor(R.color.mock_light_red));
                holder.viewResult.setVisibility(View.INVISIBLE);
            } else {

                if ((StringUtils.isNotEmpty(lastSelectedId) && lastSelectedId.equals(testSeriesSetDto.getTestSeriesId()))
                        || (StringUtils.isNotEmpty(lastTestAttemptedInSubject) && lastTestAttemptedInSubject.equals(testSeriesSetDto.getTestSeriesId())))
                    holder.parentCard.setStrokeColor(context.getResources().getColor(R.color.subscription_blue));
                else
                    holder.parentCard.setStrokeColor(context.getResources().getColor(R.color.mock_light_grey));

                holder.bottomLayout.setBackgroundColor(context.getResources().getColor(R.color.bottom_layout_color));
                if (testSeriesSetDto.isAttempted()) {
                    holder.freeBadge.setVisibility(View.INVISIBLE);
                    holder.testDetail.setVisibility(View.GONE);
                    holder.userDetail.setVisibility(View.VISIBLE);
                    holder.viewResult.setVisibility(View.VISIBLE);
                    holder.scoreText.setText(CommonMethods.cleanDecimalNumber(testSeriesSetDto.getPrecisionUserScore()) + "/" + testSeriesSetDto.getTotalScore());
                    holder.attemptStatusText.setText(context.getString(R.string.reattempt));
                    holder.attemptStatusText.setTextColor(context.getResources().getColor(R.color.medium_marks_text));
                    holder.attemptStatusLine.setBackgroundColor(context.getResources().getColor(R.color.medium_marks_text));
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.reattempt_yellow)).into(holder.attemptStatusIcon);
                    holder.attemptStatusIcon.setScaleX(0.9f);
                    holder.attemptStatusIcon.setScaleY(0.9f);

                } else {
                    holder.freeBadge.setVisibility(View.VISIBLE);
                    holder.testDetail.setVisibility(View.VISIBLE);
                    holder.userDetail.setVisibility(View.GONE);
                    holder.viewResult.setVisibility(View.INVISIBLE);
                    holder.testTime.setText(CommonMethods.getTimeInMinutes(testSeriesSetDto.getTotalTimeToAttempt()) + context.getString(R.string.mins));
                    holder.testMarks.setText(testSeriesSetDto.getTotalScore() + context.getString(R.string.Marks));
                    holder.numberOfQuestions.setText(testSeriesSetDto.getTotalQuestions() + context.getString(R.string.Qns));
                    holder.attemptStatusText.setText(context.getString(R.string.start_now));
                    holder.attemptStatusText.setTextColor(context.getResources().getColor(R.color.subscription_blue));
                    holder.attemptStatusLine.setBackgroundColor(context.getResources().getColor(R.color.subscription_blue));
                    Glide.with(context).load(context.getResources().getDrawable(R.drawable.blue_arrow_start)).into(holder.attemptStatusIcon);
                    holder.attemptStatusIcon.setScaleX(1.5f);
                    holder.attemptStatusIcon.setScaleY(1.5f);
                }
                if (testSeriesSetDto.isPremium() && !testSeriesSetDto.isLock() && !testSeriesSetDto.isAttempted()) {
                    holder.freeBadge.setVisibility(View.INVISIBLE);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {

            if (testSeriesSetDto.isLock()) {
                mockTestClickListener.onLockedMockTestClicked();
            } else {
                if (!testSeriesSetDto.isEvaluationInProgress()) {
                    mockTestClickListener.onItemClicked(testSeriesSetDto, position);
                }
            }

        });

        holder.viewResult.setOnClickListener(v -> {
            mockTestClickListener.onAvailabilityStatusClicked(testSeriesSetDto);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class TestSeriesListViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout freeBadge;
        TextView title;
        LinearLayout testDetail;
        TextView testTime;
        TextView testMarks;
        TextView numberOfQuestions;
        LinearLayout userDetail;
        TextView scoreText;
        LinearLayout bottomLayout;
        LinearLayout attemptStatusLine;
        TextView attemptStatusText;
        ImageView attemptStatusIcon;
        TextView inProgressText;
        LinearLayout viewResult;
        MaterialCardView parentCard;
        ImageView icon;
        LottieAnimationView checkingAnimation;


        public TestSeriesListViewHolder(@NonNull View itemView) {
            super(itemView);
            freeBadge = itemView.findViewById(R.id.free_badge);
            title = itemView.findViewById(R.id.title);
            testDetail = itemView.findViewById(R.id.test_detail);
            testTime = itemView.findViewById(R.id.test_time);
            testMarks = itemView.findViewById(R.id.test_marks);
            numberOfQuestions = itemView.findViewById(R.id.number_of_question);
            userDetail = itemView.findViewById(R.id.user_detail);
            scoreText = itemView.findViewById(R.id.score_text);
            bottomLayout = itemView.findViewById(R.id.bottom_layout);
            attemptStatusLine = itemView.findViewById(R.id.attempt_status_line);
            attemptStatusText = itemView.findViewById(R.id.attempt_status_text);
            attemptStatusIcon = itemView.findViewById(R.id.attempt_status_icon);
            inProgressText = itemView.findViewById(R.id.in_progress_text);
            parentCard = itemView.findViewById(R.id.parent_card);
            viewResult = itemView.findViewById(R.id.view_result_layout);
            icon = itemView.findViewById(R.id.icon);
            checkingAnimation = itemView.findViewById(R.id.checking_animation);
        }
    }

    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<TestSeriesSetDto> filteredlist = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(listFull);
            } else {
                String pattern = CommonMethods.normalizeSpaces(constraint.toString().toLowerCase().trim());
                for (TestSeriesSetDto testSeriesSetDto : listFull) {
                    try {
                        String name = CommonMethods.normalizeSpaces(testSeriesSetDto.getTestSeriesName().trim());
                        if (name.toLowerCase().contains(pattern)) {
                            filteredlist.add(testSeriesSetDto);
                        }
                    } catch (Exception ignored) {}
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
            list.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };



    public interface MockTestClickListener {
        void onLockedMockTestClicked();

        void onAvailabilityStatusClicked(TestSeriesSetDto testSeriesSetDto);

        void onItemClicked(TestSeriesSetDto testSeriesSetDto, int position);
    }

}
