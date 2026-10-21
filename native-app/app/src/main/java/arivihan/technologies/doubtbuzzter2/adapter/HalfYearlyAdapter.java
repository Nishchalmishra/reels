package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.crash_course.CrashCourseResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.PreExamPreparationResponse;

public class HalfYearlyAdapter extends RecyclerView.Adapter<HalfYearlyAdapter.HalfYearlyDynamicViewHolder> {

    private static final String TAG = HalfYearlyAdapter.class.getSimpleName();
    private static final int TYPE_HORIZONTAL = 0;
    private static final int TYPE_VERTICAL = 1;

    Context context;
    CrashCourseResponse crashCourseResponse;
    HalfYearlySectionAdapter.HalfYearlySectionInterface halfYearlySectionInterface;
    HalfYearlyHorizontalAdapter.HalfYearlyHorizontalInterface halfYearlyHorizontalInterface;
    private final CountDownVisibilityListener countDownVisibilityListener;

    public HalfYearlyAdapter(Context context,
                              CrashCourseResponse crashCourseResponse,
                              HalfYearlySectionAdapter.HalfYearlySectionInterface halfYearlySectionInterface,
                              HalfYearlyHorizontalAdapter.HalfYearlyHorizontalInterface halfYearlyHorizontalInterface,
                              CountDownVisibilityListener countDownVisibilityListener) {
        this.context = context;
        this.crashCourseResponse = crashCourseResponse;
        this.halfYearlySectionInterface = halfYearlySectionInterface;
        this.halfYearlyHorizontalInterface = halfYearlyHorizontalInterface;
        this.countDownVisibilityListener = countDownVisibilityListener;
    }

    public void updateData(CrashCourseResponse crashCourseResponse) {
        Log.d(TAG, "Crash course response: "+crashCourseResponse.toString());
        this.crashCourseResponse = crashCourseResponse;
        notifyDataSetChanged();
    }

    private boolean hasLiveClasses() {
        return crashCourseResponse != null
                && crashCourseResponse.getTodayClasses() != null
                && !CollectionUtils.isEmpty(crashCourseResponse.getTodayClasses().getClasses());
    }

    private boolean hasPreExamPreparation() {
        return crashCourseResponse != null
                && crashCourseResponse.getPreExamPreparation() != null;
    }

    @Override
    public int getItemCount() {
        if (crashCourseResponse == null) return 0;
        int count = 0;
        if (hasLiveClasses()) count++;
        if (hasPreExamPreparation()) count++;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasLiveClasses()) {
            return TYPE_HORIZONTAL;
        }
        return TYPE_VERTICAL;
    }

    @NonNull
    @Override
    public HalfYearlyDynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.half_yearly_item, parent, false);
        return new HalfYearlyDynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HalfYearlyDynamicViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == TYPE_HORIZONTAL) {
            TodayClassResponse liveClasses = crashCourseResponse.getTodayClasses();

            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.divider.setVisibility(View.GONE);

            if (StringUtils.isNotEmpty(liveClasses.getLayoutTitle())) {
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(liveClasses.getLayoutTitle());
            } else {
                holder.textView.setVisibility(View.GONE);
            }

            setMargins(holder.textView, 0, 0, 0, CommonMethods.dpToPx(context, 6));
            setMargins(holder.recyclerView,
                    CommonMethods.dpToPx(context, -8), 0,
                    CommonMethods.dpToPx(context, -8), CommonMethods.dpToPx(context, 15));

            if (countDownVisibilityListener != null) {
                countDownVisibilityListener.onCountDownVisibilityChanged(true);
            }

            HalfYearlyHorizontalAdapter adapter = new HalfYearlyHorizontalAdapter(
                    liveClasses.getClasses(), context, halfYearlyHorizontalInterface);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(adapter);

        } else {
            // VERTICAL - pre-exam preparation: subjects first, separator, then content
            PreExamPreparationResponse prep = crashCourseResponse.getPreExamPreparation();
            List<Object> sectionItems = new ArrayList<>();
            boolean hasSubjects = !CollectionUtils.isEmpty(prep.getPreExamSubjectList());
            boolean hasContent = !CollectionUtils.isEmpty(prep.getPreExamContentList());
            if (hasSubjects) {
                sectionItems.addAll(prep.getPreExamSubjectList());
            }
            if (hasSubjects && hasContent) {
                sectionItems.add(new HalfYearlySectionAdapter.SectionSeparator());
            }
            if (hasContent) {
                sectionItems.addAll(prep.getPreExamContentList());
            }

            if (sectionItems.isEmpty()) {
                holder.itemView.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            }

            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.textView.setVisibility(View.GONE);
            holder.divider.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

            HalfYearlySectionAdapter adapter = new HalfYearlySectionAdapter(context, sectionItems, halfYearlySectionInterface);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.recyclerView.setAdapter(adapter);
        }
    }

    public interface CountDownVisibilityListener {
        void onCountDownVisibilityChanged(boolean isVisible);
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(left, top, right, bottom);
            view.setLayoutParams(params);
        }
    }

    public static class HalfYearlyDynamicViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView textView;
        LinearLayout divider;

        public HalfYearlyDynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.search_recycler_item);
            textView = itemView.findViewById(R.id.title);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
