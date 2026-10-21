package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;

/**
 * Coming Soon screen — shown when user selects "Other Classes".
 *
 * Not scrollable. Content sticks to top. Card always visible.
 * Image responsive: fills available space above card, shrinks to 0.5x if tight, GONE if tiny.
 * Background (gradient + cloud + bird) positioned at illustration vertical center.
 */
public class NeetComingSoonFragment extends Fragment {

    public static final String TAG = NeetComingSoonFragment.class.getSimpleName();
    private static final String REMOTE_CONFIG_KEY = "neet_class_selection_config_v1";
    private static final int CLOUD_HEIGHT_DP = 80;
    private static final int CLOUD_OVERLAP_DP = 30;

    private View bgGradient;
    private ImageView bgCloud;
    private ImageView bgBird;
    private ImageView ivIllustration;
    private FrameLayout flIllustrationContainer;
    private ImageView ivIcon;
    private TextView tvHeading;
    private TextView tvSubtext;
    private View cvContentCard;
    private LinearLayout llContent;

    public static NeetComingSoonFragment newInstance() {
        return new NeetComingSoonFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neet_coming_soon, container, false);

        bgGradient = view.findViewById(R.id.bg_gradient);
        bgCloud = view.findViewById(R.id.bg_cloud);
        bgBird = view.findViewById(R.id.bg_bird);
        ivIllustration = view.findViewById(R.id.iv_illustration);
        flIllustrationContainer = view.findViewById(R.id.fl_illustration_container);
        ivIcon = view.findViewById(R.id.iv_icon);
        tvHeading = view.findViewById(R.id.tv_heading);
        tvSubtext = view.findViewById(R.id.tv_subtext);
        cvContentCard = view.findViewById(R.id.cv_content_card);
        llContent = view.findViewById(R.id.ll_content);

        loadContent();
        loadIllustration();
        loadIcon();
        startAnimation();
        adjustLayoutAfterMeasure(view);

        return view;
    }

    private void startAnimation() {
        if (!isAdded()) return;
        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        flIllustrationContainer.startAnimation(slideIn);
        cvContentCard.startAnimation(slideIn);
    }

    // ── Responsive layout + background positioning ──────────────────────────

    /**
     * After first layout pass:
     * 1. Measure total available height and card height
     * 2. Available for image = total - card - margins
     * 3. If image natural height fits → show full, set container to that height
     * 4. If only 0.5x fits → constrain container to 0.5x
     * 5. If even 0.5x doesn't fit → hide image entirely
     * 6. Position background (gradient + cloud) at illustration vertical center
     */
    private void adjustLayoutAfterMeasure(View root) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isAdded()) return;
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float density = getResources().getDisplayMetrics().density;
                int totalHeight = llContent.getHeight();
                int cardHeight = cvContentCard.getHeight();
                int imageNaturalHeight = flIllustrationContainer.getHeight();
                int marginsPx = (int) (110 * density); // 90dp top margin + 20dp bottom

                int availableForImage = totalHeight - cardHeight - marginsPx;

                if (availableForImage <= 0) {
                    // No space at all — hide image + background
                    hideImageAndBackground();
                } else if (availableForImage < imageNaturalHeight / 2) {
                    // Even 0.5x doesn't fit — hide image
                    hideImageAndBackground();
                } else if (availableForImage < imageNaturalHeight) {
                    // Constrain to available space (between 0.5x and full)
                    setImageContainerHeight(availableForImage);
                    positionBackground(flIllustrationContainer.getTop(), availableForImage);
                } else {
                    // Full size fits — constrain to natural height (no stretching)
                    setImageContainerHeight(imageNaturalHeight);
                    positionBackground(flIllustrationContainer.getTop(), imageNaturalHeight);
                }
            }
        });
    }

    private void setImageContainerHeight(int heightPx) {
        ViewGroup.LayoutParams params = flIllustrationContainer.getLayoutParams();
        params.height = heightPx;
        flIllustrationContainer.setLayoutParams(params);
    }

    private void hideImageAndBackground() {
        flIllustrationContainer.setVisibility(View.GONE);
        bgGradient.setVisibility(View.GONE);
        bgCloud.setVisibility(View.GONE);
        bgBird.setVisibility(View.GONE);
    }

    /**
     * @param containerTop  absolute Y of image container (includes 116dp margin)
     * @param containerHeight  height of image container after constraining
     */
    private void positionBackground(int containerTop, int containerHeight) {
        try {
            if (!isAdded() || bgGradient == null || bgCloud == null) return;

            float density = getResources().getDisplayMetrics().density;
            int cloudHeightPx = (int) (CLOUD_HEIGHT_DP * density);
            int cloudOverlapPx = (int) (CLOUD_OVERLAP_DP * density);

            int illustrationCenter = containerTop + (containerHeight / 2);
            int cloudTop = Math.max(0, illustrationCenter - (cloudHeightPx / 2));
            int gradientHeight = cloudTop + cloudOverlapPx;

            ViewGroup.LayoutParams gradientParams = bgGradient.getLayoutParams();
            gradientParams.height = gradientHeight;
            bgGradient.setLayoutParams(gradientParams);

            FrameLayout.LayoutParams cloudParams = (FrameLayout.LayoutParams) bgCloud.getLayoutParams();
            cloudParams.gravity = Gravity.TOP;
            cloudParams.topMargin = cloudTop;
            bgCloud.setLayoutParams(cloudParams);
        } catch (Exception e) {
            Log.e(TAG, "Failed to position background", e);
        }
    }

    // ── Content loading ─────────────────────────────────────────────────────

    private void loadContent() {
        String heading = "Aapki class abhi app par available nahi hai.";
        String subtext = "Abhi app par sirf 10th, 11th aur 12th classes available hain. Aapki class ke liye bhi hum jald hi content aur features lekar aa rahe hain.";
        String styledHeadingJson = null;

        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject comingSoon = root.optJSONObject("comingSoon");
                if (comingSoon != null) {
                    JSONArray styledArr = comingSoon.optJSONArray("styledHeading");
                    if (styledArr != null && styledArr.length() > 0) {
                        styledHeadingJson = styledArr.toString();
                    }

                    String h = comingSoon.optString("heading", "");
                    if (StringUtils.isNotBlank(h)) heading = h;

                    String s = comingSoon.optString("subtext", "");
                    if (StringUtils.isNotBlank(s)) subtext = s;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load comingSoon config", e);
        }

        if (StringUtils.isNotBlank(styledHeadingJson) && isAdded()) {
            SpannableStringBuilder styled = StyledTextBuilder.fromJson(requireContext(), styledHeadingJson);
            if (styled.length() > 0) {
                tvHeading.setText(styled);
            } else {
                tvHeading.setText(heading);
            }
        } else {
            tvHeading.setText(heading);
        }

        tvSubtext.setText(subtext);
    }

    private void loadIllustration() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject comingSoon = root.optJSONObject("comingSoon");
                if (comingSoon != null) {
                    String url = comingSoon.optString("illustrationUrl", "");
                    if (StringUtils.isNotBlank(url) && isAdded()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.img_ritesh_sir_portrait)
                                .error(R.drawable.img_ritesh_sir_portrait)
                                .into(ivIllustration);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load comingSoon illustration", e);
        }
    }

    private void loadIcon() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject comingSoon = root.optJSONObject("comingSoon");
                if (comingSoon != null) {
                    String url = comingSoon.optString("iconUrl", "");
                    if (StringUtils.isNotBlank(url) && isAdded()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.img_rocket_teal)
                                .error(R.drawable.img_rocket_teal)
                                .into(ivIcon);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load comingSoon icon", e);
        }
    }
}
