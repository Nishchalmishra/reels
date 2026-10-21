package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlySection;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlySectionEnum;

public class HalfYearlyCompetitiveAdapter extends RecyclerView.Adapter<HalfYearlyCompetitiveAdapter.HalfYearlyDynamicViewHolder> {

    private static final String TAG = HalfYearlyCompetitiveAdapter.class.getSimpleName();

    Context context;
    ArrayList<HalfYearlySection> sections;
    HalfYearlyCompetitiveSectionAdapter.CompetitiveSectionInterface sectionInterface;
    HalfYearlyCompetitiveHorizontalAdapter.CompetitiveHorizontalInterface horizontalInterface;
    private final HalfYearlyAdapter.CountDownVisibilityListener countDownVisibilityListener;

    public HalfYearlyCompetitiveAdapter(Context context,
                                        ArrayList<HalfYearlySection> sections,
                                        HalfYearlyCompetitiveSectionAdapter.CompetitiveSectionInterface sectionInterface,
                                        HalfYearlyCompetitiveHorizontalAdapter.CompetitiveHorizontalInterface horizontalInterface,
                                        HalfYearlyAdapter.CountDownVisibilityListener countDownVisibilityListener) {
        this.context = context;
        this.sections = sections;
        this.sectionInterface = sectionInterface;
        this.horizontalInterface = horizontalInterface;
        this.countDownVisibilityListener = countDownVisibilityListener;
    }

    public void updateData(ArrayList<HalfYearlySection> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sections == null ? 0 : sections.size();
    }

    @NonNull
    @Override
    public HalfYearlyDynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.half_yearly_item, parent, false);
        return new HalfYearlyDynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HalfYearlyDynamicViewHolder holder, int position) {
        HalfYearlySection section = sections.get(position);

        if (section == null || section.getSectionItems() == null || section.getSectionItems().isEmpty()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (StringUtils.isNotEmpty(section.getTitle())) {
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setText(section.getTitle());
        } else {
            holder.textView.setVisibility(View.GONE);
        }

        holder.divider.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

        if (section.getSectionType() == HalfYearlySectionEnum.HORIZONTAL) {
            if (countDownVisibilityListener != null) {
                countDownVisibilityListener.onCountDownVisibilityChanged(true);
            }
            setMargins(holder.textView, 0, 0, 0, CommonMethods.dpToPx(context, 6));
            setMargins(holder.recyclerView,
                    CommonMethods.dpToPx(context, -8), 0,
                    CommonMethods.dpToPx(context, -8), CommonMethods.dpToPx(context, 15));
            HalfYearlyCompetitiveHorizontalAdapter adapter = new HalfYearlyCompetitiveHorizontalAdapter(
                    section.getSectionItems(), context, horizontalInterface);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(adapter);
        } else {
            HalfYearlyCompetitiveSectionAdapter adapter = new HalfYearlyCompetitiveSectionAdapter(
                    context, section.getSectionItems(), sectionInterface);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.recyclerView.setAdapter(adapter);
        }
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
