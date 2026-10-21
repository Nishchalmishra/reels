package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
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
import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.ActivityTestIntro;
import arivihan.technologies.doubtbuzzter2.activity.ChapterListActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.model.SectionActionType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SeeAllAdapter extends RecyclerView.Adapter<SeeAllAdapter.SeeAllViewHolder>{

    Context context;
    ArrayList<SearchResult> list;

    SeeAllItemClickListener seeAllItemClickListener;

    public SeeAllAdapter(Context context, ArrayList<SearchResult> list, SeeAllItemClickListener seeAllItemClickListener) {
        this.context = context;
        this.list = list;
        this.seeAllItemClickListener = seeAllItemClickListener;
    }

    public SeeAllAdapter(Context context, ArrayList<SearchResult> list) {
        this.context = context;
        this.list = list;
    }

    Intent intent;
    List<String> id = new ArrayList<>();
    List<String> key = new ArrayList<>();
    @NonNull
    @Override
    public SeeAllViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.global_search_sections_item, parent, false);
        return new SeeAllViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeeAllViewHolder holder, int position) {
        SearchResult searchResult = list.get(position);
        if(StringUtils.isNotEmpty(searchResult.getT())) {
            holder.displayName.setVisibility(View.VISIBLE);
            String title = searchResult.getT();
            if (title.endsWith("\n"))
                holder.displayName.setText(title.substring(0, title.length() - 2));
            else
                holder.displayName.setText(title);
        }
        if(StringUtils.isNotEmpty(searchResult.getSt())) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(searchResult.getSt());
        }
        if(StringUtils.isNotEmpty(searchResult.getDes())) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(searchResult.getDes());
        }
        setSectionIcon(SectionActionType.valueOf(searchResult.getType()), holder);
        holder.itemView.setOnClickListener(v -> setOnClick(SectionActionType.valueOf(searchResult.getType()), position));

        if (position == list.size() - 2 ) {
            seeAllItemClickListener.loadData();
        }
    }

    private void setOnClick(SectionActionType action, int position) {
        seeAllItemClickListener.addToRecentSearch(list.get(position).getT());
        switch (action) {
            case QNS:
                intent = new Intent(context, MicrolectureListActivity.class);
                intent.putExtra("chapterName", "");
                intent.putExtra("openPractice", true);
                splitIdAndKey(list.get(position).getId(), list.get(position).getCtype(), intent);
                break;
            case QUICK_NOTE:
                seeAllItemClickListener.onSeeAllItemClicked(list.get(position).getType(),list.get(position).getId());
                break;
            case TEST_SERIES:
            case MICRO_LECTURE:
                intent = new Intent(context, ActivityTestIntro.class);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
                intent.putExtra("subject", list.get(position).getT());
                intent.putExtra("combined", true);
                intent.putExtra("fromGlobalSearch", true);
                splitIdAndKey(list.get(position).getId(),list.get(position).getCtype(),intent);
                break;
            case CHAPTER_NOTES:
                seeAllItemClickListener.onSeeAllItemClicked(list.get(position).getType(),list.get(position).getCurl());
                break;
            case PREVIOUS_YEARS_PAPERS:
            case SAMPLE_PAPER:
                seeAllItemClickListener.onSeeAllItemClicked(list.get(position).getType(),list.get(position).getTurl());
                break;
            case REELS:
                intent = new Intent(context, ReelsActivity.class);
                splitIdAndKey(list.get(position).getId(),list.get(position).getCtype(),intent);
                break;
            case PHYSICS_SUBJECT:
            case CHEMISTRY_SUBJECT:
            case ENGLISH_SUBJECT:
            case HINDI_SUBJECT:
            case MATHS_SUBJECT:
            case BIOLOGY_SUBJECT:
                intent = new Intent(context, ChapterListActivity.class);
                splitIdAndKey(list.get(position).getId(),list.get(position).getCtype(),intent);
                break;
            case CONTACT_US:
                intent = new Intent(context, HomeScreen.class);
                intent.putExtra(PreferenceKeys.SELECTED_TAB.getKey(),"5");
                intent.putExtra("openContactUs",true);
                splitIdAndKey(list.get(position).getId(),list.get(position).getCtype(),intent);
                break;

        }
    }

    void splitIdAndKey(String ids, String keys, Intent intent){
        if(ids!=null && keys!=null) {
            id = Arrays.asList(ids.split("##"));
            key = Arrays.asList(keys.split("##"));
            for (int i = 0; i < id.size(); i++) {
                intent.putExtra(key.get(i), id.get(i));
            }
        }
        context.startActivity(intent);
    }


    private void setSectionIcon(SectionActionType action, SeeAllViewHolder holder) {
        switch (action) {
            case NOTES:
            case CHAPTER_NOTES:
            case QUICK_NOTE:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.notes));
                break;
            case TEST_SERIES:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.test_series));
                break;
            case PREVIOUS_YEARS_PAPERS:
            case PRACTICE:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.previous_year));
                break;
            case REELS:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.reels));
                break;
            case CONTACT_US:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.contact_us));
                break;
            case ASK_A_DOUBT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ask_doubt));
                break;
            case PHYSICS_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.letter_p));
                break;
            case CHEMISTRY_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.letter_c));
                break;
            case MATHS_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.letter_m));
                break;
            case BIOLOGY_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.letter_b));
                break;
            case HINDI_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ha));
                break;
            case ENGLISH_SUBJECT:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.en));
                break;
            default:
                holder.displayImage.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.previous_year));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SeeAllViewHolder extends RecyclerView.ViewHolder {

        TextView displayName;
        TextView subtitle;
        TextView description;
        ImageView displayImage;
        public SeeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            subtitle = itemView.findViewById(R.id.subtitle);
            description = itemView.findViewById(R.id.description);
            displayImage = itemView.findViewById(R.id.display_image);
        }
    }

    public interface SeeAllItemClickListener{
        void onSeeAllItemClicked(String type, String title);

        void loadData();

        void addToRecentSearch(String title);

    }
}
