package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class ViewPagerAdapterSetStarted extends PagerAdapter {

    Context context;
    ArrayList<String> message;

    public ViewPagerAdapterSetStarted(Context context, ArrayList<String> message) {
        this.context = context;
        this.message = message;
    }

    @Override
    public int getCount() {
        return message.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.getstartedlayout,null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView imagesView = view.findViewById(R.id.image_view);

//        if(position == 0)
//            Glide.with(context).load(R.drawable.background_login).into(imagesView);
//        if(position == 1)
//            Glide.with(context).load(R.drawable.background_login).into(imagesView);
//        if(position == 2)
//            Glide.with(context).load(R.drawable.background_login).into(imagesView);
        container.addView(view,0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
