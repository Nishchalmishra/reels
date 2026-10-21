package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.competitive.BannerType;
import arivihan.technologies.doubtbuzzter2.model.competitive.banner.BannerDto;

public class CompetitiveBannerView extends LinearLayout {

    private static final String TAG = CompetitiveBannerView.class.getSimpleName();
    private FrameLayout contentContainer;
    private LinearLayout textWrapper;
    private ImageView gifImageView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private LottieAnimationView lottieAnimationView;

    public CompetitiveBannerView(Context context) {
        super(context);
        init(context);
    }

    public CompetitiveBannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CompetitiveBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_banner, this, true);
        lottieAnimationView = findViewById(R.id.lottieView);
        contentContainer = findViewById(R.id.contentContainer);
        textWrapper = findViewById(R.id.textWrapper);
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
    }

    public void setBannerData(BannerDto banner) {

        if (banner == null) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);

        // Reset all views
        textWrapper.setVisibility(GONE);
        titleTextView.setText("");
        subtitleTextView.setText("");
        subtitleTextView.setVisibility(GONE);
        lottieAnimationView.cancelAnimation();

        contentContainer.setOnClickListener(view -> {
            if (banner.getActivityNavigationConfig() != null) {
                MyApplication.openActivity(getContext(),
                        banner.getActivityNavigationConfig().getClassName(), banner.getActivityNavigationConfig().getParams());
            }
        });

        Log.d(TAG, "---> Banner: "+banner.toString());
        // ===== CASE 2: TEXT BANNER =====
        if (banner.getBannerType() == BannerType.GIF_ONLY) {

            Log.d(TAG, "---> Type: "+banner.getBannerType());
            textWrapper.setVisibility(GONE);
            if (StringUtils.isNotEmpty(banner.getImageUrl())) {
                lottieAnimationView.setFailureListener(t -> {
                    Log.e(TAG, "setBannerData: Lottie failed to load: " + banner.getImageUrl(), t);
                    lottieAnimationView.setVisibility(GONE);
                });
                lottieAnimationView.setAnimationFromUrl(banner.getImageUrl());
                lottieAnimationView.playAnimation();
            } else {
                lottieAnimationView.setVisibility(GONE);
            }
        } else {
            if (StringUtils.isNotEmpty(banner.getImageUrl())) {
                lottieAnimationView.setVisibility(VISIBLE);
                lottieAnimationView.setFailureListener(t -> {
                    Log.e(TAG, "setBannerData: Lottie failed to load: " + banner.getImageUrl(), t);
                    lottieAnimationView.setVisibility(GONE);
                });
                lottieAnimationView.setAnimationFromUrl(banner.getImageUrl());
                lottieAnimationView.playAnimation();
            } else {
                lottieAnimationView.setVisibility(GONE);
            }
            textWrapper.setVisibility(VISIBLE);
            if (StringUtils.isNotEmpty(banner.getTitle())) {
                titleTextView.setText(banner.getTitle());
                titleTextView.setVisibility(VISIBLE);
            } else {
                titleTextView.setVisibility(GONE);
            }

            String subTitle = banner.getSubTitle();

            if (StringUtils.isNotEmpty(subTitle)) {

                if(Objects.equals(banner.getTotalTopicCount(), banner.getRemainingCount())) {
                    subTitle = subTitle
                            .replace("{remain}/{total}", String.valueOf(banner.getTotalTopicCount()));
                }

                subTitle = subTitle
                        .replace("{total}", String.valueOf(banner.getTotalTopicCount()))
                        .replace("{remain}", String.valueOf(banner.getRemainingCount()));

                subtitleTextView.setText(subTitle);
                subtitleTextView.setVisibility(VISIBLE);

            } else {
                subtitleTextView.setVisibility(GONE);
            }
        }
    }

}
