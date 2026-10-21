package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class VernacularGridAdapter extends RecyclerView.Adapter<VernacularGridAdapter.ViewHolder>  {

    private List<VernacularItem> vernacularItemList;
    private Context context;
    private VernacularAction action;
    private boolean type = false;

    public VernacularGridAdapter(List<VernacularItem> vernacularItemList, Context context, VernacularAction action) {
        this.vernacularItemList = vernacularItemList;
        this.context = context;
        this.action = action;
    }

    public VernacularGridAdapter(List<VernacularItem> vernacularItemList, Context context, VernacularAction action,Boolean type) {
        this.vernacularItemList = vernacularItemList;
        this.context = context;
        this.action = action;
        this.type = type;
    }

    @NonNull
    @Override
    public VernacularGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        view = inflater.inflate(R.layout.lang_layout,parent,false);
        if (type) view = inflater.inflate(R.layout.lang_layout2,parent,false);
        return new VernacularGridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VernacularGridAdapter.ViewHolder holder, int position) {
        VernacularItem vernacularItem = vernacularItemList.get(position);
        if (vernacularItem.isChecked()) {
            holder.cardView.setBackgroundResource(R.drawable.vernacular_background);
        } else {
            holder.cardView.setBackgroundResource(R.drawable.vernacular_background_white);
        }
        if(!vernacularItem.board)
            setImageUrl(vernacularItem, holder);
        else {
            String image_url = vernacularItem.getUrl();
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round);

            Glide.with(context).load(image_url).apply(options).into(holder.imageView);
        }
        holder.cardView.setOnClickListener(view -> {
            for (VernacularItem t : vernacularItemList) {
                t.setChecked(false);
            }
            action.changeImage(vernacularItem);
            vernacularItem.setChecked(!vernacularItem.isChecked());
            notifyDataSetChanged();
            MyApplication.saveData(PreferenceKeys.PROGRESS_RELOAD.getKey(), true);

            HashMap<String,String> params= new HashMap<>();
            params.put("Language",vernacularItem.getName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.language_switch.getActionName(),params);
        });
        holder.language.setText(vernacularItem.getName());
    }

    private void setImageUrl(VernacularItem vernacularItem, VernacularGridAdapter.ViewHolder holder) {
        if ("en".equalsIgnoreCase(vernacularItem.getId())) {
            Glide.with(context).load(R.drawable.ic__24).into(holder.imageView);
        } else if ("hi".equalsIgnoreCase(vernacularItem.getId())) {
            Glide.with(context).load(R.drawable.taj_mahal).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return vernacularItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView language;
        private ImageView imageView;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            language = itemView.findViewById(R.id.langText);
            imageView = itemView.findViewById(R.id.langImage);
            cardView = itemView.findViewById(R.id.card);
        }
    }

 public interface VernacularAction {
        void changeImage(VernacularItem item);
 }
}
