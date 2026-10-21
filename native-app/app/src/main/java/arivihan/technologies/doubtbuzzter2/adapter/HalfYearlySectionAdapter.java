package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyIconType;
import arivihan.technologies.doubtbuzzter2.model.crash_course.PreExamContentResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.PreExamSubjectResponse;

public class HalfYearlySectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION = 1;
    private static final int VIEW_TYPE_SEPARATOR = 2;

    Context context;
    List<Object> list;
    HalfYearlySectionInterface halfYearlySectionInterface;

    public HalfYearlySectionAdapter(Context context, List<Object> list, HalfYearlySectionInterface halfYearlySectionInterface) {
        this.context = context;
        this.list = list;
        this.halfYearlySectionInterface = halfYearlySectionInterface;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) instanceof SectionSeparator ? VIEW_TYPE_SEPARATOR : VIEW_TYPE_SECTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEPARATOR) {
            View view = new View(context);
            float density = context.getResources().getDisplayMetrics().density;
            int verticalMargin = (int) (8 * density);
            int heightPx = (int) (1 * density);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, heightPx);
            lp.setMargins(0, verticalMargin, 0, verticalMargin);
            view.setLayoutParams(lp);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_300));
            return new SeparatorViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.half_yearly_section_item, parent, false);
        return new HalfYearlySectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SeparatorViewHolder) return;

        HalfYearlySectionViewHolder sectionHolder = (HalfYearlySectionViewHolder) holder;
        Object item = list.get(position);
        Log.d("HalfYearlySectionAdapter", "onBindViewHolder: " + item);

        String title = "";
        HalfYearlyIconType iconType = null;

        if (item instanceof PreExamContentResponse content) {
            title = content.getTitle();
            iconType = HalfYearlyIconType.fromString(content.getIconType());
        } else if (item instanceof PreExamSubjectResponse subject) {
            title = subject.getName();
            iconType = HalfYearlyIconType.fromString(subject.getIconType());
        }

        if (StringUtils.isNotEmpty(title)) {
            sectionHolder.displayName.setVisibility(View.VISIBLE);
            sectionHolder.displayName.setText(title);
        }

        setSectionIcon(iconType, sectionHolder);

        Object finalItem = item;
        sectionHolder.itemView.setOnClickListener(v -> halfYearlySectionInterface.onSectionClick(finalItem));
    }

    private void setSectionIcon(HalfYearlyIconType action, HalfYearlySectionViewHolder holder) {
        ArivihanLanguageEnum languageEnum = ArivihanLanguageEnum.ENGLISH;
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            languageEnum = ArivihanLanguageEnum.HINDI;
        }
        if (action != null) {
            switch (action) {
                case TIME_TABLE:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.calender_notes));
                    break;
                case NOTES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.notes));
                    break;
                case TEST_SERIES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.test_series));
                    break;
                case PHYSICS_SUBJECT:
                    int physicsIconResId = R.drawable.letter_p;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) physicsIconResId = R.drawable.physics_hindi;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, physicsIconResId));
                    break;
                case CHEMISTRY_SUBJECT:
                    int chemistryIconResId = R.drawable.letter_c;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) chemistryIconResId = R.drawable.chemistry_hindi;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, chemistryIconResId));
                    break;
                case MATHEMATICS_SUBJECT:
                    int mathIconResId = R.drawable.letter_m;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) mathIconResId = R.drawable.math_hindi;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, mathIconResId));
                    break;
                case BIOLOGY_SUBJECT:
                    int biologyResId = R.drawable.letter_b;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) biologyResId = R.drawable.biology_hindi;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, biologyResId));
                    break;
                case HISTORY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.history_hi_ic));
                    break;
                case GEOGRAPHY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.geography_hi_ic));
                    break;
                case HINDI_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ha));
                    break;
                case BUSINESS_STUDIES_SUBJECT:
                    int business_studies = R.drawable.business_studies_en_ic;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) business_studies = R.drawable.business_studies_hi_ic;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, business_studies));
                    break;
                case ACCOUNTANCY_SUBJECT:
                    int accResID = R.drawable.accountancy_en_ic;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) accResID = R.drawable.accountancy_hi_ic;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, accResID));
                    break;
                case ECONOMICS_SUBJECT:
                    int ecoResID = R.drawable.economics_en_ic;
                    if (languageEnum == ArivihanLanguageEnum.HINDI) ecoResID = R.drawable.economics_hi_ic;
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, ecoResID));
                    break;
                case POLITICAL_SCIENCE_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.political_science_hi_ic));
                    break;
                case SOCIOLOGY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.political_science_hi_ic));
                    break;
                case ENGLISH_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.en));
                    break;
                case CHEMISTRY_NOTES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.previous_year));
                    break;
                case PHYSICS_NOTES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.physics_notes));
                    break;
                case BIOLOGY_NOTES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.biology_mind_maps));
                    break;
                case MATHEMATICS_NOTES:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.maths_formula));
                    break;
                case ASK_DOUBT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.img_pre_exam_ask_doubt));
                    break;
                default:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.previous_year));
                    break;
            }
        } else {
            holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.previous_year));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HalfYearlySectionViewHolder extends RecyclerView.ViewHolder {
        TextView displayName;
        ImageView displayImage;

        public HalfYearlySectionViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            displayImage = itemView.findViewById(R.id.display_image);
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        public SeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class SectionSeparator {}

    public interface HalfYearlySectionInterface {
        void onSectionClick(Object item);
    }
}
