package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.DoubtHistoryActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;

public class PreviousDoubtListAdapter extends RecyclerView.Adapter<PreviousDoubtListAdapter.PreviousListViewHolder> {

    DoubtChatHistoryData doubtList;
    Context context;
    boolean allowBack;

    public PreviousDoubtListAdapter(Context context, DoubtChatHistoryData doubtList, boolean allowBack) {
        this.doubtList = doubtList;
        this.context = context;
        this.allowBack = allowBack;
    }

    @NonNull
    @Override
    public PreviousListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_view_item_previous_doubt, viewGroup, false);
        return new PreviousListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviousListViewHolder previousListViewHolder, int i) {
        previousListViewHolder.doubtText.setText(doubtList.getList().get(i).getTitle());

        if (doubtList.getList().get(i).isContainUnwatchedVideo()) {
            previousListViewHolder.unwatchedVideoIndicator.setVisibility(View.VISIBLE);
        }

        previousListViewHolder.itemView.setOnClickListener(ev -> {
            String sessionId = doubtList.getList().get(i).getSessionId();
            CommonNavigator.navigateToDoubtChatSession(context, sessionId, allowBack);
        });
    }

    @Override
    public int getItemCount() {
        return doubtList.getList().size();
    }


    public class PreviousListViewHolder extends RecyclerView.ViewHolder {

        TextView doubtText;

        View doubtDivider;
        CardView unwatchedVideoIndicator;

        public PreviousListViewHolder(@NonNull View itemView) {
            super(itemView);

            doubtText = itemView.findViewById(R.id.doubt_text);
            unwatchedVideoIndicator = itemView.findViewById(R.id.unwatched_video_indicator);
        }
    }
}
