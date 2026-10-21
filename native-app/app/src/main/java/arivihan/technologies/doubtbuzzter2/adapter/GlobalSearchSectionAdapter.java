package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.ActivityTestIntro;
import arivihan.technologies.doubtbuzzter2.activity.ChapterListActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.activity.QuickNotesActivity;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.model.SectionActionType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class GlobalSearchSectionAdapter extends RecyclerView.Adapter<GlobalSearchSectionAdapter.GlobalSearchSectionViewHolder> {

    Context context;
    ArrayList<SearchResult> list;

    public GlobalSearchSectionAdapter(Context context, ArrayList<SearchResult> list, GlobalSearchSectionInterface globalSearchSectionInterface, String sectionType) {
        this.context = context;
        this.list = list;
        this.globalSearchSectionInterface = globalSearchSectionInterface;
        this.sectionType = sectionType;
    }

    GlobalSearchSectionInterface globalSearchSectionInterface;
    List<String> id = new ArrayList<>();
    List<String> key = new ArrayList<>();

    String sectionType;

    Intent intent;

    public GlobalSearchSectionAdapter(Context context, ArrayList<SearchResult> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public GlobalSearchSectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.global_search_sections_item, parent, false);
        return new GlobalSearchSectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlobalSearchSectionViewHolder holder, int position) {
        SearchResult searchResult = list.get(position);
        if (StringUtils.isNotEmpty(searchResult.getT())) {
            holder.displayName.setVisibility(View.VISIBLE);
            String name = searchResult.getT();
            if (name.endsWith("\n"))
                holder.displayName.setText(name.substring(0, name.length() - 2));
            else
                holder.displayName.setText(name);
        }
        if (StringUtils.isNotEmpty(searchResult.getSt())) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(searchResult.getSt());
        }
        if (StringUtils.isNotEmpty(searchResult.getDes())) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(searchResult.getDes());
        }
        Log.e("setSectionIcon:", list.get(position).toString());

        if (list.get(position).getType() != null) {
            setSectionIcon(SectionActionType.valueOf(list.get(position).getType()), holder);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.get(position).getType() != null) {
                    setOnClick(SectionActionType.valueOf(list.get(position).getType()), position);
                }
            }
        });

    }


    private void setOnClick(SectionActionType action, int position) {
        SearchResult searchResult = list.get(position);
        switch (action) {
            case NOTES:
                intent = new Intent(context, QuickNotesActivity.class);
                splitIdAndKey(list.get(position).getId(), list.get(position).getCtype(), intent);
                break;
            case QNS:
                intent = new Intent(context, MicrolectureListActivity.class);
                intent.putExtra("chapterName", "");
                intent.putExtra("openPractice", true);
                intent.putExtra("chapterId", searchResult.getId());
                Log.d("questionQNS", searchResult.getId());
                Log.d("questionQNS", searchResult.getCtype());
                splitIdAndKey(searchResult.getId(), searchResult.getCtype(), intent);
                break;
            case QUICK_NOTE:
                globalSearchSectionInterface.addToRecentSearch(list.get(position).getT());
                globalSearchSectionInterface.onItemClick(searchResult.getType(), searchResult.getType(), searchResult.getId());
                break;
            case TEST_SERIES:
                if (sectionType.equalsIgnoreCase(searchResult.getType())) {
                    globalSearchSectionInterface.addToRecentSearch(list.get(position).getT());
                    intent = new Intent(context, ActivityTestIntro.class);
                    intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
                    intent.putExtra("subject", list.get(position).getT());
                    intent.putExtra("combined", true);
                    intent.putExtra("Id", "jee");
                    intent.putExtra("fromGlobalSearch", true);
                    splitIdAndKey(searchResult.getId(), list.get(position).getCtype(),intent);
                } else {
                    globalSearchSectionInterface.onItemClick(sectionType,searchResult.getType(), searchResult.getT());
                }
                break;
            case PREVIOUS_YEARS_PAPERS:
            case SAMPLE_PAPER:
                if (sectionType.equalsIgnoreCase(searchResult.getType())) {
                    globalSearchSectionInterface.addToRecentSearch(list.get(position).getT());
                    globalSearchSectionInterface.onItemClick(searchResult.getType(), searchResult.getType(), list.get(position).getTurl());
                } else {
                    globalSearchSectionInterface.onItemClick(sectionType, searchResult.getType(), searchResult.getT());
                }
                break;
            case CHAPTER_NOTES:
                globalSearchSectionInterface.addToRecentSearch(list.get(position).getT());
                globalSearchSectionInterface.onItemClick(searchResult.getType(), searchResult.getType(), list.get(position).getCurl());
                break;
            case REELS:
                intent = new Intent(context, ReelsActivity.class);
                splitIdAndKey(searchResult.getId(), list.get(position).getCtype(), intent);
                break;
            case ASK_A_DOUBT:
                globalSearchSectionInterface.onItemClick(searchResult.getType(), searchResult.getType(), searchResult.getT());
                break;
            case PHYSICS_SUBJECT:
            case CHEMISTRY_SUBJECT:
            case MATHS_SUBJECT:
            case HINDI_SUBJECT:
            case ENGLISH_SUBJECT:
            case BIOLOGY_SUBJECT:
                intent = new Intent(context, ChapterListActivity.class);
                splitIdAndKey(searchResult.getId(), searchResult.getCtype(), intent);
                break;
            case CONTACT_US:
                globalSearchSectionInterface.onItemClick(searchResult.getType(), searchResult.getType(), searchResult.getT());
                break;

        }
    }

    void splitIdAndKey(String ids, String keys, Intent intent) {
        if (ids != null && keys != null) {
            id = Arrays.asList(ids.split("##"));
            key = Arrays.asList(keys.split("##"));
            for (int i = 0; i < id.size(); i++) {
                intent.putExtra(key.get(i), id.get(i));
            }
        }
        context.startActivity(intent);
    }

    private void setSectionIcon(SectionActionType action, GlobalSearchSectionViewHolder holder) {
        ArivihanLanguageEnum languageEnum = ArivihanLanguageEnum.ENGLISH;
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            languageEnum = ArivihanLanguageEnum.HINDI;
        }
        int imageResId = switch (action) {
            case NOTES -> R.drawable.notes;
            case TEST_SERIES -> R.drawable.test_series;
            case PREVIOUS_YEARS_PAPERS, PRACTICE -> R.drawable.previous_year;
            case REELS -> R.drawable.reels;
            case CONTACT_US -> R.drawable.contact_us;
            case ASK_A_DOUBT -> R.drawable.ask_doubt;
            case PHYSICS_SUBJECT -> (languageEnum == ArivihanLanguageEnum.HINDI)
                    ? R.drawable.physics_hindi
                    : R.drawable.letter_p;
            case CHEMISTRY_SUBJECT -> (languageEnum == ArivihanLanguageEnum.HINDI)
                    ? R.drawable.chemistry_hindi
                    : R.drawable.letter_c;
            case MATHS_SUBJECT -> (languageEnum == ArivihanLanguageEnum.HINDI)
                    ? R.drawable.math_hindi
                    : R.drawable.letter_m;
            case BIOLOGY_SUBJECT -> (languageEnum == ArivihanLanguageEnum.HINDI)
                    ? R.drawable.biology_hindi
                    : R.drawable.letter_b;
            case HINDI_SUBJECT -> R.drawable.ha;
            case ENGLISH_SUBJECT -> R.drawable.en;
            default -> R.drawable.previous_year;
        };

        Glide.with(holder.displayImage.getContext())
                .load(imageResId)
                .into(holder.displayImage);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class GlobalSearchSectionViewHolder extends RecyclerView.ViewHolder {

        TextView displayName;
        TextView subtitle;
        TextView description;
        ImageView displayImage;

        public GlobalSearchSectionViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            subtitle = itemView.findViewById(R.id.subtitle);
            description = itemView.findViewById(R.id.description);
            displayImage = itemView.findViewById(R.id.display_image);
        }
    }

    public interface GlobalSearchSectionInterface {
        void onItemClick(String headingType, String type, String title);

        void addToRecentSearch(String title);
    }
}
