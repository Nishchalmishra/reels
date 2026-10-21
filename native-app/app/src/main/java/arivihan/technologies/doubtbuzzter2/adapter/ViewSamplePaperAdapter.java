package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;

public class ViewSamplePaperAdapter extends RecyclerView.Adapter<ViewSamplePaperAdapter.SamplePaperViewHolder> {

    private final static String TAG = ViewSamplePaperAdapter.class.getSimpleName();
    List<SamplePaperModel> models;
//    List<SamplePaperModel> listFull;
    ToppersNotesAdapter.OnNotesClickListener listener;
    Context context;

    public ViewSamplePaperAdapter(List<SamplePaperModel> models, ToppersNotesAdapter.OnNotesClickListener listener, Context context) {
        this.models = new ArrayList<>(models);
//        this.listFull = new ArrayList<>(models);
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public SamplePaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.notes_layout,parent,false);
        return new SamplePaperViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewSamplePaperAdapter.SamplePaperViewHolder holder, int position) {
        SamplePaperModel item = models.get(position);

        holder.tv_name.setText(item.getTitle());
//        boolean isLocked = item.isLocked();
        holder.rl_download.setVisibility(View.VISIBLE);
        holder.rl_download_progress.setVisibility(View.GONE);
        holder.iv_status_lock.setVisibility(View.GONE);
        holder.iv_status_download_completed.setVisibility(View.GONE);

        if (item.getProgress() > 0 || (StringUtils.isNotEmpty(item.getStatus()) && (item.getStatus().equals(NoteEntity.STATUS_IN_QUEUE)
                || item.getStatus().equals(NoteEntity.STATUS_PENDING) || item.getStatus().equals(NoteEntity.STATUS_DOWNLOADING)))) {
            holder.iv_status_lock.setVisibility(View.GONE);
            holder.iv_status_download_completed.setVisibility(View.GONE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.VISIBLE);

            holder.tv_progress.setText(item.getProgress() + "%");
            holder.pb_progress.setProgress(item.getProgress());
        }

        if (item.isDownloadComplete()) {
            holder.iv_status_lock.setVisibility(View.GONE);
            holder.iv_status_download_completed.setVisibility(View.VISIBLE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.GONE);
        }

        if (item.isLocked()) {
            holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.gray_8a));
            holder.iv_status_lock.setVisibility(View.VISIBLE);
            holder.iv_status_download_completed.setVisibility(View.GONE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.GONE);
        } else {
            holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.itemView.setOnClickListener(view -> {
            listener.onNotesClick(item.getTitle(), item.getDownloadLink(),item.getNotesType(),item.getSubject(), item.isLocked());
        });

        holder.rl_download.setOnClickListener(v -> {
            listener.onNoteDownloadClick(position, item.getDownloadLink(), item.isLocked(), item);
        });

        holder.rl_download_progress.setOnClickListener(v -> {
            listener.onNoteStatusClick(position, item);
        });

        holder.iv_status_download_completed.setOnClickListener(v -> {
            listener.onNoteStatusClick(position, item);
        });

        holder.iv_status_lock.setOnClickListener(v -> {
            listener.onNotesClick(item.getTitle(), item.getDownloadLink(),
                    item.getNotesType(),item.getSubject(),item.isLocked());
        });

    }

    public List<SamplePaperModel> getList() {
        return models;
    }
//    public void setListFull(List<SamplePaperModel> listFull) {
//        this.listFull = listFull;
//    }

    @Override
    public int getItemCount() {
        return models.size();
    }

//    @Override
//    public Filter getFilter() {
//        return filter;
//    }
//
//    private final Filter filter = new Filter() {
//
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            List<SamplePaperModel> filteredlist = new ArrayList<>();
//
//            if(constraint == null || constraint.length() == 0){
//                filteredlist.addAll(listFull);
//            } else{
//                String pattern = constraint.toString().toLowerCase().trim();
//                int index = 0;
//                for(SamplePaperModel cp : listFull){
//                    String samplePaper = cp.getTitle();
//                    if(samplePaper.toLowerCase().contains(pattern))
//                        filteredlist.add(cp);
//                    index++;
//                }
//            }
//
//            FilterResults results = new FilterResults();
//            results.values = filteredlist;
//            results.count = filteredlist.size();
//            return results;
//
//        }
//
//        @SuppressLint("NotifyDataSetChanged")
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            models.clear();
//            models.addAll((Collection<? extends SamplePaperModel>) filterResults.values);
//            notifyDataSetChanged();
//        }
//    };

    public static class SamplePaperViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name, tv_progress;
        private ProgressBar pb_progress;
        private RelativeLayout rl_download, rl_download_progress;
        private LinearLayout ll_name;
        private ImageView iv_status_lock, iv_status_download_completed;
        public SamplePaperViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            pb_progress = itemView.findViewById(R.id.pb_progress);
            rl_download = itemView.findViewById(R.id.rl_download);
            rl_download_progress = itemView.findViewById(R.id.rl_download_progress);
            iv_status_lock = itemView.findViewById(R.id.iv_status_lock);
            iv_status_download_completed = itemView.findViewById(R.id.iv_status_download_completed);
            ll_name = itemView.findViewById(R.id.ll_name);
        }
    }

}
