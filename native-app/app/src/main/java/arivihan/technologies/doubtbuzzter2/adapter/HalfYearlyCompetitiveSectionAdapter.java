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
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyIconType;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyItem;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class HalfYearlyCompetitiveSectionAdapter extends RecyclerView.Adapter<HalfYearlyCompetitiveSectionAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<HalfYearlyItem> list;
    private final CompetitiveSectionInterface sectionInterface;

    public HalfYearlyCompetitiveSectionAdapter(Context context, ArrayList<HalfYearlyItem> list,
                                               CompetitiveSectionInterface sectionInterface) {
        this.context = context;
        this.list = list;
        this.sectionInterface = sectionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.half_yearly_section_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HalfYearlyItem halfYearlyItem = list.get(position);
        Log.d("CompetitiveSectionAdapter", "onBindViewHolder: " + halfYearlyItem);

        if (StringUtils.isNotEmpty(halfYearlyItem.getTitle())) {
            holder.displayName.setVisibility(View.VISIBLE);
            holder.displayName.setText(halfYearlyItem.getTitle());
        }

        if (halfYearlyItem.getContentType() != null) {
            setSectionIcon(halfYearlyItem.getIconType(), holder);
        }

        holder.itemView.setOnClickListener(v -> sectionInterface.onSectionClick(halfYearlyItem));
    }

    private void setSectionIcon(HalfYearlyIconType action, ViewHolder holder) {
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
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.physics_hindi : R.drawable.letter_p));
                    break;
                case CHEMISTRY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.chemistry_hindi : R.drawable.letter_c));
                    break;
                case MATHEMATICS_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.math_hindi : R.drawable.letter_m));
                    break;
                case BIOLOGY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.biology_hindi : R.drawable.letter_b));
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
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.business_studies_hi_ic : R.drawable.business_studies_en_ic));
                    break;
                case ACCOUNTANCY_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.accountancy_hi_ic : R.drawable.accountancy_en_ic));
                    break;
                case ECONOMICS_SUBJECT:
                    holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context,
                            languageEnum == ArivihanLanguageEnum.HINDI ? R.drawable.economics_hi_ic : R.drawable.economics_en_ic));
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName;
        ImageView displayImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            displayImage = itemView.findViewById(R.id.display_image);
        }
    }

    public interface CompetitiveSectionInterface {
        void onSectionClick(HalfYearlyItem halfYearlyItem);
    }
}
