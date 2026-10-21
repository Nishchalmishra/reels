package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NotificationModel;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    List<NotificationModel> model;
    Context context;

    public NotificationAdapter(List<NotificationModel> model, Context context) {
        this.model = model;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.notification_layout, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.Title.setText(model.get(position).getTitle());

        if (StringUtils.isNotEmpty(model.get(position).getNotificationMessage()))
            holder.subTitle.setText(model.get(position).getNotificationMessage());

        if (StringUtils.isNotEmpty(model.get(position).getImageUrl())) {
            holder.noticeImg.setVisibility(View.VISIBLE);
            Glide.with(context).load(model.get(position).imageUrl).into(holder.noticeImg);
        }

        if (position == 0) {
            holder.newMsg.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.get(position).getDeepLink() != null) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(model.get(position).getDeepLink()));
                        context.startActivity(i);
                    } catch (Exception e) {
                        Log.e("INTENT", "Unable to start intent");
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.i("", "" + model.size());
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView Title, subTitle;
        ImageView newMsg, noticeImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Title = itemView.findViewById(R.id.title);
            newMsg = itemView.findViewById(R.id.dot);
            noticeImg = itemView.findViewById(R.id.image);
            subTitle = itemView.findViewById(R.id.body);

        }
    }
}
