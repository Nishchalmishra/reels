package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BoardQuestionActivity;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;

public class UploadedImageRecyclerAdapter extends RecyclerView.Adapter<UploadedImageRecyclerAdapter.UploadedImageViewHolder> {

    private static final String TAG = UploadedImageRecyclerAdapter.class.getSimpleName();
    Context context;
    ArrayList<Uri> list;

    AddClickListener addClickListener;

    LectureContentType lectureContentType;


    public UploadedImageRecyclerAdapter(Context context, ArrayList<Uri> list, AddClickListener addClickListener, LectureContentType questionType) {
        this.context = context;
        this.list = list;
        this.addClickListener = addClickListener;
        this.lectureContentType = questionType;
    }

    @NonNull
    @Override
    public UploadedImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.board_extraction_image_item, parent, false);
        return new UploadedImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadedImageViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (lectureContentType.equals(LectureContentType.VERY_SHORT_ANSWERS)) {
            if (!list.isEmpty() || position != 0)
                holder.add_image.setVisibility(View.GONE);

            if (position == 0 && list.isEmpty())
                holder.add_image.setVisibility(View.VISIBLE);
        } else if (lectureContentType.equals(LectureContentType.SHORT_ANSWERS)) {
            if (list.size() == 2 || position != 0)
                holder.add_image.setVisibility(View.GONE);

            if (position == 0 && list.size() < 2)
                holder.add_image.setVisibility(View.VISIBLE);

        } else {
            if (list.size() == 3 || position != 0)
                holder.add_image.setVisibility(View.GONE);

            if (position == 0 && list.size() < 3)
                holder.add_image.setVisibility(View.VISIBLE);

        }


        holder.add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BoardQuestionActivity.isTextExtracting)
                    addClickListener.addClicked();
            }
        });

        holder.delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BoardQuestionActivity.isTextExtracting)
                    addClickListener.deleteClicked(position);
            }
        });

        holder.crop_image.setOnClickListener(v -> {
            addClickListener.addCropClicked(position);
        });


        Glide.with(context).load(list.get(position)).into(holder.imageView);
//        holder.imageView.setImageURI(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UploadedImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageView add_image;
        ImageView delete_image;
        ImageView crop_image;

        public UploadedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.extracted_image);
            add_image = itemView.findViewById(R.id.add_image_card);
            delete_image = itemView.findViewById(R.id.delete_image);
            crop_image = itemView.findViewById(R.id.crop_image);

        }
    }


    public interface AddClickListener {
        void addClicked();

        void deleteClicked(int position);

        void addCropClicked(int position);
    }
}
