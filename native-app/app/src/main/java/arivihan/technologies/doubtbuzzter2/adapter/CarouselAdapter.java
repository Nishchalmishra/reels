package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.listener.OnCarouselClickListener;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private final List<CarouselResponse> items;
    private final Context context;
    private final OnCarouselClickListener listener;

    public CarouselAdapter(List<CarouselResponse> items,
                           Context context,
                           OnCarouselClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carousel, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        setMargins(holder.card,
                CommonMethods.dpToPx(context, -16),
                0,
                CommonMethods.dpToPx(context, -16),
                0);
        holder.bind(items.get(position % items.size()));
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(left, top, right, bottom);
            view.setLayoutParams(params);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        MaterialCardView card;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            card = itemView.findViewById(R.id.card);
        }

        void bind(CarouselResponse dto) {

            Glide.with(context)
                    .load(dto.getUrl())
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                Log.d("clickedCarousel", "bind: ");
                MyApplication.logFirebaseBundleCreation("carousal_click");
                if (listener != null)
                    listener.onCarouselClicked(dto);
            });
        }
    }
}
