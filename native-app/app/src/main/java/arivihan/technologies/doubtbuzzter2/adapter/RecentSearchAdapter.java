package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder> {

    Context context;
    ArrayList<String> list;

    RecentSearchOnClickListener suggestionOnClickListener;

    public RecentSearchAdapter(Context context, ArrayList<String> list, RecentSearchOnClickListener suggestionOnClickListener) {
        this.context = context;
        this.list = list;
        this.suggestionOnClickListener=suggestionOnClickListener;
    }

    @NonNull
    @Override
    public RecentSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.global_recent_search_item,parent,false);
        return new RecentSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentSearchViewHolder holder, int position) {
        String title = list.get(position) ;
        if(title.endsWith("\n"))
            holder.title.setText(title.substring(0,title.length()-2));
        else
            holder.title.setText(title);
       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               suggestionOnClickListener.onRecentSearchClick(list.get(position));
           }
       });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RecentSearchViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        public RecentSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recent_search_title);
        }
    }

    public interface RecentSearchOnClickListener {

        void onRecentSearchClick(String query);
    }


}

