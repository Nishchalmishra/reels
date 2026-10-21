package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class SolutionImageAdapter extends RecyclerView.Adapter<SolutionImageAdapter.ViewHolder> {

    ArrayList<String> image = new ArrayList<>();
    Context context;

    public SolutionImageAdapter(ArrayList<String> image, Context context) {
        this.image = image;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.solution_image_layout,parent,false);
        return new SolutionImageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(image.get(position)).into(holder.imageSol);
    }

    @Override
    public int getItemCount() {
        return image.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageSol;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSol = itemView.findViewById(R.id.solutionImage);
        }
    }
}
