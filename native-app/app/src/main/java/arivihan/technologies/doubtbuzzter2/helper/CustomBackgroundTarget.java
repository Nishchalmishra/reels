package arivihan.technologies.doubtbuzzter2.helper;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class CustomBackgroundTarget extends CustomTarget<Drawable> {
    private final View view;

    public CustomBackgroundTarget(View view) {
        this.view = view;
    }

    @Override
    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
        view.setBackground(resource);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        view.setBackground(placeholder);
    }
}
