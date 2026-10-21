package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.model.WebChatRecord;

public class PreviousDoubtListAdapterNew extends RecyclerView.Adapter<PreviousDoubtListAdapterNew.PreviousListViewHolderNew> {
    List<WebChatRecord> doubtList;
    Context context;
    boolean allowBack;

    public PreviousDoubtListAdapterNew(Context context, List<WebChatRecord> doubtList, boolean allowBack) {
        this.doubtList = doubtList;
        this.context = context;
        this.allowBack = allowBack;
    }

    @NonNull
    @Override
    public PreviousListViewHolderNew onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_view_item_previous_doubt, viewGroup, false);
        return new PreviousListViewHolderNew(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviousListViewHolderNew holder, int position) {
        if(position==doubtList.size()-1)
            holder.doubt_divider.setVisibility(View.GONE);
        else
            holder.doubt_divider.setVisibility(View.VISIBLE);
        WebChatRecord webChatRecord = doubtList.get(position);
        holder.doubtText.setText(webChatRecord.getTitle());

        if (webChatRecord.isContainUnwatchedVideo()) {
            holder.unwatchedVideoIndicator.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(ev -> {
            String sessionId = webChatRecord.getSessionId();
            CommonNavigator.navigateToDoubtChatSession(context, sessionId, allowBack);
        });
    }


    @Override
    public int getItemCount() {
        return doubtList.size();
    }


    public class PreviousListViewHolderNew extends RecyclerView.ViewHolder {

        TextView doubtText;
        CardView unwatchedVideoIndicator;
        LinearLayout doubt_divider;

        public PreviousListViewHolderNew(@NonNull View itemView) {
            super(itemView);

            doubtText = itemView.findViewById(R.id.doubt_text);
            unwatchedVideoIndicator = itemView.findViewById(R.id.unwatched_video_indicator);
            doubt_divider = itemView.findViewById(R.id.doubt_divider);
        }
    }
}
