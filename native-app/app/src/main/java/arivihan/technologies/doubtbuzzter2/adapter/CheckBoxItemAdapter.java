package arivihan.technologies.doubtbuzzter2.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ItemSelectionDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.subject.CompetitiveChapterDto;

public class CheckBoxItemAdapter extends RecyclerView.Adapter<CheckBoxItemAdapter.ViewHolder> implements Filterable {

    private final static String TAG = CheckBoxItemAdapter.class.getSimpleName();
    List<ItemSelectionDto> models;
    List<ItemSelectionDto> listFull;
    OnItemSelectionListener listener;
    Context context;

    public CheckBoxItemAdapter(Context context, List<ItemSelectionDto> models, OnItemSelectionListener listener) {
        this.models = new ArrayList<>(models);
        this.listFull = new ArrayList<>(models);
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_checkbox_text, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemSelectionDto item = models.get(position);

        holder.tv_name.setText(CommonMethods.capitalizeWords(item.getName()));

        // Sync checkbox state
        holder.checkbox.setChecked(item.isSelected());

        // Click anywhere on row toggles checkbox
        holder.rl_parent_layout.setOnClickListener(v -> {
            boolean newState = !item.isSelected();
            item.setSelected(newState);
            holder.checkbox.setChecked(newState);

            listener.onItemSelected(item, position, newState);
        });

        holder.view_bottom_bar.setVisibility(position != models.size() - 1 ? View.VISIBLE : View.GONE);

    }

    public List<ItemSelectionDto> getList() {
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

            List<ItemSelectionDto> filteredlist = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(listFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                int index = 0;
                for (ItemSelectionDto cp : listFull) {
                    String name = cp.getName();
                    if (name.toLowerCase().contains(pattern))
                        filteredlist.add(cp);
                    index++;
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredlist;
            results.count = filteredlist.size();
            return results;

        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            models.clear();
            if (filterResults == null || filterResults.values == null) {
                notifyDataSetChanged();
                return;
            }
            models.addAll((Collection<? extends ItemSelectionDto>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rl_parent_layout;
        private CheckBox checkbox;
        private TextView tv_name;
        private View view_bottom_bar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            checkbox = itemView.findViewById(R.id.checkbox);
            rl_parent_layout = itemView.findViewById(R.id.rl_parent_layout);
            view_bottom_bar = itemView.findViewById(R.id.view_bottom_bar);
        }
    }

    public interface OnItemSelectionListener {
        void onItemSelected(ItemSelectionDto item, int position, boolean isSelected);
    }


}
