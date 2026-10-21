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

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SearchSuggestionViewHolder>{

    Context context;
    ArrayList<String> list;

    SuggestionOnClickListener suggestionOnClickListener;

    public SearchSuggestionAdapter(Context context, ArrayList<String> list, SuggestionOnClickListener suggestionOnClickListener) {
        this.context = context;
        this.list = list;
        this.suggestionOnClickListener=suggestionOnClickListener;
    }

    @NonNull
    @Override
    public SearchSuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_suggestion_item,parent,false);
        return new SearchSuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchSuggestionViewHolder holder, int position) {
        String title = list.get(position) ;
        if(title.endsWith("\n"))
            holder.text.setText(title.substring(0,title.length()-2));
        else
            holder.text.setText(title);
         holder.itemView.setOnClickListener(v -> suggestionOnClickListener.onSuggestionClick(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SearchSuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public SearchSuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.suggestion_text);
        }
    }

    public interface SuggestionOnClickListener{

        void onSuggestionClick(String query);
    }
}
