package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
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
import arivihan.technologies.doubtbuzzter2.activity.SeeAllActivity;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.model.SearchResultResponse;

public class GlobalSearchAdapter extends RecyclerView.Adapter<GlobalSearchAdapter.SearchDynamicViewHolder> {
    Context context;
    ArrayList<SearchResultResponse> list;
    GlobalSearchInterface globalSearchInterface;

    GlobalSearchSectionAdapter.GlobalSearchSectionInterface globalSearchSectionInterface;
    GlobalSearchVideoAdapter.GlobalSearchVideoInterface globalSearchVideoInterface;

    public GlobalSearchAdapter(Context context, ArrayList<SearchResultResponse> list, GlobalSearchInterface globalSearchInterface, GlobalSearchSectionAdapter.GlobalSearchSectionInterface globalSearchSectionInterface, GlobalSearchVideoAdapter.GlobalSearchVideoInterface globalSearchVideoInterface) {
        this.context = context;
        this.list = list;
        this.globalSearchInterface = globalSearchInterface;
        this.globalSearchSectionInterface = globalSearchSectionInterface;
        this.globalSearchVideoInterface = globalSearchVideoInterface;
    }

    public GlobalSearchAdapter(Context context, ArrayList<SearchResultResponse> list, GlobalSearchInterface globalSearchInterface, GlobalSearchSectionAdapter.GlobalSearchSectionInterface globalSearchSectionInterface) {
        this.context = context;
        this.list = list;
        this.globalSearchInterface = globalSearchInterface;
        this.globalSearchSectionInterface = globalSearchSectionInterface;
    }

    public GlobalSearchAdapter(Context context, ArrayList<SearchResultResponse> list, GlobalSearchInterface globalSearchInterface) {
        this.context = context;
        this.list = list;
        this.globalSearchInterface=globalSearchInterface;
    }

    @NonNull
    @Override
    public SearchDynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.global_search_item,parent,false);
        return new SearchDynamicViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchDynamicViewHolder holder, int position) {
        SearchResultResponse searchResultResponse = list.get(position);
        if(position==0)
            holder.divider.setVisibility(View.GONE);
        if(StringUtils.isNotEmpty(searchResultResponse.getTitle())){
            holder.titleLayout.setVisibility(View.VISIBLE);
            holder.textView.setText(searchResultResponse.getTitle());
        }
        if(searchResultResponse.getData().size() == 0 &&
                !searchResultResponse.getType().equalsIgnoreCase("DOUBT") ){
            holder.textView.setText(list.get(position).getTitle());
        }
        if(list.get(position).getData().size() ==0 && !list.get(position).getType().equalsIgnoreCase("DOUBT") ){
                holder.itemView.setVisibility(View.GONE);
                holder.titleLayout.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else if(searchResultResponse.getGroup().equals("video") ){
            GlobalSearchVideoAdapter adapter = new GlobalSearchVideoAdapter(context, searchResultResponse.getData(),globalSearchVideoInterface);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(adapter);
        } else if(searchResultResponse.getGroup().equals("text")){
            if( searchResultResponse.getTitle()!=null && !searchResultResponse.getTitle().isEmpty() && searchResultResponse.getScreen().equalsIgnoreCase("result"))
                holder.seeAll.setVisibility(View.VISIBLE);
            GlobalSearchSectionAdapter adapter = new GlobalSearchSectionAdapter(context, list.get(position).getData(),globalSearchSectionInterface,list.get(position).getType());
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.recyclerView.setAdapter(adapter);
        }

        holder.seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalSearchInterface.onSeeAllClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SearchDynamicViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView textView;

        LinearLayout seeAll;
        LinearLayout titleLayout;
        LinearLayout divider;
        public SearchDynamicViewHolder(@NonNull View itemView) {
            super(itemView);
                recyclerView = itemView.findViewById(R.id.search_recycler_item);
                textView = itemView.findViewById(R.id.title);
                seeAll = itemView.findViewById(R.id.see_all_text);
                titleLayout = itemView.findViewById(R.id.title_layout);
                divider = itemView.findViewById(R.id.divider);
        }
    }

    public interface GlobalSearchInterface{
        void fetchDoubt(int position,SearchResultResponse searchResultResponse);
        void onSeeAllClick(int position);
    }
}
