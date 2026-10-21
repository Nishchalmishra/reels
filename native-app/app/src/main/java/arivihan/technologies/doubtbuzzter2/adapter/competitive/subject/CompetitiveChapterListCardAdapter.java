package arivihan.technologies.doubtbuzzter2.adapter.competitive.subject;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ItemSelectionDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.subject.CompetitiveChapterDto;
import kotlinx.serialization.StringFormat;
import lombok.Setter;

public class CompetitiveChapterListCardAdapter extends RecyclerView.Adapter<CompetitiveChapterListCardAdapter.ViewHolder> implements Filterable {

    private final static String TAG = CompetitiveChapterListCardAdapter.class.getSimpleName();
    List<CompetitiveChapterDto> models;
    List<CompetitiveChapterDto> listFull;

    @Setter
    List<String> filterPriorityList;
    OnCompetitiveChapterClickListener listener;
    Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private String selectedChapterId;

    public CompetitiveChapterListCardAdapter(Context context, List<CompetitiveChapterDto> models, List<String> filterPriorityList, OnCompetitiveChapterClickListener listener, String selectedChapterId) {
        this.models = new ArrayList<>(models);
        this.listFull = new ArrayList<>(models);
        this.filterPriorityList = filterPriorityList;
        this.listener = listener;
        this.context = context;
        this.selectedChapterId = selectedChapterId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_competitive_chapter_list_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompetitiveChapterDto item = models.get(position);

        holder.tv_name.setText(item.getTitle());

        holder.cv_item_parent_layout.setOnClickListener(view -> {
            listener.onChapterClick(position, item);
            int lastPosition = selectedPosition;
            selectedPosition = position;
            selectedChapterId = item.getChapterId();
            if (lastPosition >= 0) {
                notifyItemChanged(lastPosition);
            }
            notifyItemChanged(selectedPosition);
        });
        holder.tv_topics.setText(String.format(item.getNoOfTopics() + " " + context.getString(R.string.topics)));
        holder.cv_pyq_information.setCardBackgroundColor(Color.parseColor(item.getChapterTrend().getBgColor()));
        holder.tv_pyq_information.setText(CommonMethods.isValidJson(item.getChapterTrend().getChapterTrendText()) ? StyledTextBuilder.fromJson(context, item.getChapterTrend().getChapterTrendText()) : item.getChapterTrend().getChapterTrendText());
        holder.tv_pyq_information.setTextColor(Color.parseColor(item.getChapterTrend().getTextColor()));

        holder.cv_pyq_information.setVisibility(StringUtils.isNotEmpty(item.getChapterTrend().getChapterTrendText()) ? View.VISIBLE : View.GONE);

        Glide.with(context).clear(holder.iv_icon);
        Glide.with(context)
                .load(item.getChapterIconUrl())
                .error(R.drawable.bio_neet)
                .into(holder.iv_icon);

        // Reset previous animation
        stopBlink(holder);

        if (item.getChapterId().equals(selectedChapterId)) {
            startBlink(holder);
        }
    }

    public List<CompetitiveChapterDto> getList() {
        return models;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<CompetitiveChapterDto> filteredlist = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(listFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (CompetitiveChapterDto cp : listFull) {
                    String name = cp.getTitle();
                    if (name.toLowerCase().contains(pattern))
                        filteredlist.add(cp);
                }
            }

//            Log.d(TAG, "Filter Adapter: " + filterPriorityList.size());
            if (filterPriorityList != null && !filterPriorityList.isEmpty()) {
                List<CompetitiveChapterDto> tempFilteredlist = new ArrayList<>();
                for (CompetitiveChapterDto competitiveChapterDto : filteredlist) {
                    if (StringUtils.isNotEmpty(competitiveChapterDto.getChapterPriority()) && filterPriorityList.contains(competitiveChapterDto.getChapterPriority())) {
                        // Keep Dto
                        tempFilteredlist.add(competitiveChapterDto);
                    }
                }

                filteredlist.clear();
                filteredlist.addAll(tempFilteredlist);

                Log.d(TAG, "Filter Option change : " + filteredlist.size());
            }

            FilterResults results = new FilterResults();
            results.values = filteredlist;
            results.count = filteredlist.size();
            return results;

        }

//        @SuppressLint("NotifyDataSetChanged")
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            if(filterResults!=null && filterResults.values!=null && models!=null) {
//                models.clear();
//                models.addAll((Collection<? extends CompetitiveChapterDto>) filterResults.values);
//                notifyDataSetChanged();
//            }
//        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (results == null || results.values == null || models == null) return;

            models.clear();
            models.addAll((List<CompetitiveChapterDto>) results.values);
            notifyDataSetChanged();

            if (listener != null) {
                listener.onChapterFilter(models);
            }
        }
    };

    private void startBlink(ViewHolder holder) {

        MaterialCardView card = holder.cv_item_parent_layout;

        int strokeColor = ContextCompat.getColor(
                card.getContext(),
                R.color.arivihan_secondary_color
        );

        // Base setup
        card.setStrokeWidth(2);
        card.setStrokeColor(strokeColor);
        card.setAlpha(1f);

        holder.blinkAnimator = ValueAnimator.ofFloat(0f, 1f);
        holder.blinkAnimator.setDuration(900);
        holder.blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        holder.blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        holder.blinkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        holder.blinkAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();

            // Stroke fade (light → bright → light)
            int alpha = (int) (80 + (175 * value)); // 80 → 255
            card.setStrokeColor(ColorUtils.setAlphaComponent(strokeColor, alpha));

            // Elevation pulse
            float elevation = 1f + (2f * value); // 1dp → 3dp
            card.setCardElevation(elevation);
            if (value > 0.2f) {
                card.setStrokeWidth(2);
            } else {
                card.setStrokeWidth(0);
            }
        });

        holder.blinkAnimator.start();
    }

//    private void startBlink(ViewHolder holder) {
//
//        MaterialCardView card = holder.cv_item_parent_layout;
//
//        int strokeColor =
//                ContextCompat.getColor(card.getContext(), R.color.arivihan_secondary_color);
//
//        holder.blinkAnimator = ValueAnimator.ofFloat(0f, 1f);
//        holder.blinkAnimator.setDuration(1000);
//        holder.blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        holder.blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
//
//        holder.blinkAnimator.addUpdateListener(animation -> {
//            float value = (float) animation.getAnimatedValue();
//
//            if (value > 0.5f) {
//                card.setStrokeWidth(2);
//                card.setStrokeColor(strokeColor);
//                card.setCardElevation(4f);
//                card.setAlpha(1f);
//            } else {
//                card.setStrokeWidth(0);
//                card.setCardElevation(4f);
////                card.setAlpha(0.8f);
//            }
//        });
//
//        holder.blinkAnimator.start();
//    }

    private void stopBlink(ViewHolder holder) {

        if (holder.blinkAnimator != null) {
            holder.blinkAnimator.cancel();
            holder.blinkAnimator = null;
        }

        MaterialCardView card = holder.cv_item_parent_layout;

        card.animate()
                .setDuration(100)
                .alpha(1f)
                .withEndAction(() -> {
                    card.setStrokeWidth(2);
                    card.setCardElevation(3f);
                    card.setStrokeColor(ContextCompat.getColor(
                            card.getContext(),
                            R.color.grey_extracted_layout
                    ));
                })
                .start();
    }

//    private void stopBlink(ViewHolder holder) {
//
//        if (holder.blinkAnimator != null) {
//            holder.blinkAnimator.cancel();
//            holder.blinkAnimator = null;
//        }
//
//        MaterialCardView card = holder.cv_item_parent_layout;
//        card.setStrokeWidth(0);
//        card.setCardElevation(4f);
//        card.setAlpha(1f);
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cv_item_parent_layout, cv_pyq_information;
        private LinearLayout ll_item_parent_layout;
        private TextView tv_name, tv_pyq_information, tv_topics;
        private ImageView iv_right_arrow, iv_icon;
        private ValueAnimator blinkAnimator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_right_arrow = itemView.findViewById(R.id.iv_right_arrow);
            cv_item_parent_layout = itemView.findViewById(R.id.cv_item_parent_layout);
            ll_item_parent_layout = itemView.findViewById(R.id.ll_item_parent_layout);
            cv_pyq_information = itemView.findViewById(R.id.cv_pyq_information);
            tv_pyq_information = itemView.findViewById(R.id.tv_pyq_information);
            tv_topics = itemView.findViewById(R.id.tv_topics);
            iv_icon = itemView.findViewById(R.id.iv_icon);
        }
    }

    public interface OnCompetitiveChapterClickListener {
        void onChapterClick(int position, CompetitiveChapterDto competitiveChapterDto);

        void onChapterFilter(List<CompetitiveChapterDto> list);
    }

}
