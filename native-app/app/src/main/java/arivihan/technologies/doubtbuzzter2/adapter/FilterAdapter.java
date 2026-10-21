package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DoubtFilterModel;
import arivihan.technologies.doubtbuzzter2.model.DoubtFilters;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    Context context;
    List<DoubtFilters> doubtFilters;
    List<DoubtFilterModel> doubtFilterModels;
    FilterOptionAdapter.FilterListener filterListener;
    FilterOptionAdapter filterOptionAdapter ;


    public FilterAdapter(Context context, List<DoubtFilters> doubtFilters, FilterOptionAdapter.FilterListener filterListener) {
        this.context = context;
        this.doubtFilters = doubtFilters;
        this.filterListener = filterListener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        doubtFilterModels=new ArrayList<>();
        filterOptionAdapter = new FilterOptionAdapter(context,doubtFilterModels,filterListener);
        View view = LayoutInflater.from(context).inflate(R.layout.filter_item_layout, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        filterViewHolder.recyclerView.setAdapter(filterOptionAdapter);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
       holder.title.setText(doubtFilters.get(position).getTitle());
       holder.recyclerView.setTag(position);
       doubtFilterModels.clear();
       doubtFilterModels.addAll(doubtFilters.get(position).getList());
       holder.recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return doubtFilters.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RecyclerView recyclerView;
        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.filter_title);
            recyclerView = itemView.findViewById(R.id.filter_option_rv);
        }
    }
}
