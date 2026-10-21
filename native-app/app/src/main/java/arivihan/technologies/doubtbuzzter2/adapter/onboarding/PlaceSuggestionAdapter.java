package arivihan.technologies.doubtbuzzter2.adapter.onboarding;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    public interface Listener { void onPlaceClicked(AutocompletePrediction p); }

    private final List<AutocompletePrediction> items = new ArrayList<>();
    private final Listener listener;

    public PlaceSuggestionAdapter(Listener listener) { this.listener = listener; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AutocompletePrediction p = items.get(position);
        boolean isLast = position==items.size()-1;
        holder.tvName.setText(p.getPrimaryText(null));
        holder.tvAddress.setText(p.getSecondaryText(null));
        holder.card.setOnClickListener(v -> listener.onPlaceClicked(p));

        holder.view_bottom_bar.setVisibility(isLast?View.GONE:View.VISIBLE);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void setList(List<AutocompletePrediction> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvName, tvAddress;
        View view_bottom_bar;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cv_item_parent_layout);
            tvName = itemView.findViewById(R.id.tv_header);
            tvAddress = itemView.findViewById(R.id.tv_sub_header);
            view_bottom_bar = itemView.findViewById(R.id.view_bottom_bar);
        }
    }
}
