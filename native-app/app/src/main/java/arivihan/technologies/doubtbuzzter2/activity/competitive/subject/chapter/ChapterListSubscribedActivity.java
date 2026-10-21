package arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.customView.gradient.AnimatedBorderView;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter.ChapterListByBatchFragment;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;

public class ChapterListSubscribedActivity extends AppCompatActivity
        implements EdgeToEdgeHelper.SelfManagedInsets {

    private static final String TAG = ChapterListSubscribedActivity.class.getSimpleName();

    // UI Variables
    private ShimmerFrameLayout sfl_shimmer;
    private LinearLayout ll_parent_layout, back_button_layout, ll_shimmer, ll_data;
    private TextView activity_title;
    private MaterialCardView newLectureLiveButton, newLecturePremiumButton;

    private String title;
    private String subjectId;
    private String subjectCode;
    private String batchId;
    private String scrollToChapterId;
    private AnimatedBorderView borderViewLive, borderViewPremium;
    private HomeScreenViewModel homeScreenViewModel;
    private ChapterListByBatchFragment liveFragment, premiumFragment;
    private SearchView searchView;
    private boolean hasSearchText;
    private ImageView back_arrow;
    private View view_divider;
    private View revealOverlay;
    private LinearLayout live_btn_bg;
    private TextView tv_live_btn, tv_premium_btn;
    private LinearLayout ll_section_buttons;
    private boolean isPremiumHidden;
    private String navigationFrom;

    public static enum SectionType {
        LIVE,
        PREMIUM
    }

    private SectionType currentType;
    private ThemeEnum currentActivityTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Resolve currentType early so we can apply the right night mode BEFORE super.onCreate()
        if (savedInstanceState != null) {
            currentType = SectionType.valueOf(
                    savedInstanceState.getString("lecture_type", SectionType.LIVE.name()));
        } else {
            currentType = SectionType.LIVE;
            String type = getIntent().getStringExtra("lecture_type");
            if (type != null) {
                try { currentType = SectionType.valueOf(type); } catch (Exception ignored) {}
            }
        }

        currentActivityTheme = (currentType == SectionType.PREMIUM) ? ThemeEnum.DARK : ThemeEnum.LIGHT;
        applyNightMode(currentType);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list_subscribed);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && searchView.hasFocus()) {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }
                    searchView.clearFocus();
                } else {
                    finish();
                }
            }
        });

        loadData(savedInstanceState);

        Map<String, String> navParams = new HashMap<>();
        navParams.put("navigation_from", navigationFrom);
        navParams.put("subjectId", subjectId != null ? subjectId : "");
        navParams.put("subjectCode", subjectCode != null ? subjectCode : "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_list_screen_view.getActionName(), navParams);
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.CHAPTER_LIST_SCREEN.name(), AnalyticsEvent.CHAPTER_LIST_SCREEN.name());

        ll_shimmer = findViewById(R.id.ll_shimmer);
        ll_data = findViewById(R.id.ll_data);
        sfl_shimmer = findViewById(R.id.sfl_shimmer);
        ll_parent_layout = findViewById(R.id.ll_parent_layout);
        EdgeToEdgeHelper.padForSystemBars(ll_parent_layout);
        activity_title = findViewById(R.id.activity_title);
        back_button_layout = findViewById(R.id.back_button_layout);
        back_arrow = findViewById(R.id.back_arrow);
        view_divider = findViewById(R.id.view_divider);
        revealOverlay = findViewById(R.id.reveal_overlay);
        live_btn_bg = findViewById(R.id.live_btn_bg);
        tv_live_btn = findViewById(R.id.tv_live_btn);
        tv_premium_btn = findViewById(R.id.premiumText);
        ll_section_buttons = findViewById(R.id.ll_section_buttons);

        //simplify header tab text - using remote config
        simplifyHeaderTabText();

        applyGradientToText(tv_premium_btn);

        // border views
        borderViewLive = findViewById(R.id.border_animation_live);
        borderViewPremium = findViewById(R.id.border_animation_premium);

        // Set up live border gradient (always configured, play only when active)
        borderViewLive.setGradient(
                new int[]{0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                        0x0026C6DA, 0xFF26C6DA, 0xFF26C6DA, 0x8026C6DA, 0x0026C6DA},
                new float[]{0.00f, 0.05f, 0.166f, 0.30f, 0.45f, 0.60f, 0.70f, 0.838f, 0.92f, 1.00f});

        // Set up premium border gradient (always configured, play only when active)
        borderViewPremium.setGradient(
                new int[]{0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                        0x00D3B06F, 0xFFD3B06F, 0xFFD3B06F, 0x80D3B06F, 0x00F1D59F},
                new float[]{0.00f, 0.05f, 0.166f, 0.30f, 0.45f, 0.60f, 0.70f, 0.838f, 0.92f, 1.00f});

        // Check remote config to see if premium should be hidden for this subject/batch code
        isPremiumHidden = shouldHidePremium();
        if (isPremiumHidden) {
            currentType = SectionType.LIVE;
            ll_section_buttons.setVisibility(View.GONE);
        }

        // Play the border for the initially selected type
        applyBorderAnimation(currentType);

        // btn, live, premium
        newLectureLiveButton = findViewById(R.id.newLectureLiveButton);
        newLecturePremiumButton = findViewById(R.id.newLecturePremiumButton);

        // onclick live
        newLectureLiveButton.setOnClickListener(view -> {
            Map<String, String> liveBtnParams = new HashMap<>();
            liveBtnParams.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_live_button_click.getActionName(), liveBtnParams);

            if (currentType == SectionType.LIVE) return;
            currentType = SectionType.LIVE;
            applyBorderAnimation(currentType);
            runCircularReveal(currentType);
        });

        // premium click — circular reveal then switch (only when premium is not hidden)
        if (!isPremiumHidden) {
            newLecturePremiumButton.setOnClickListener(view -> {
                Map<String, String> premiumBtnParams = new HashMap<>();
                premiumBtnParams.put("navigation_from", navigationFrom);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_premium_button_click.getActionName(), premiumBtnParams);

                if (currentType == SectionType.PREMIUM) return;
                currentType = SectionType.PREMIUM;
                applyBorderAnimation(currentType);
                runCircularReveal(currentType);
            });
        }

        // search
        setSearchView();

        // back btn
        back_button_layout.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_premium_back_button_click.getActionName(),params);

            getOnBackPressedDispatcher().onBackPressed();
        });

        if (StringUtils.isEmpty(title)) {
            title = MyApplication.getFirebaseRemoteConfig().getString("chapters_list_subscribed_screen_title_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        }
        Log.d(TAG, "onCreate-title: " + title);
        if (StringUtils.isNotEmpty(title)) {
            activity_title.setText(title);
        }

        ll_shimmer.setVisibility(View.GONE);
        sfl_shimmer.setVisibility(View.GONE);

        // Parse batchId to Integer (may be null if not provided)
        Integer batchIdInt = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        if (StringUtils.isNotEmpty(batchId)) {
            try {
                batchIdInt = Integer.parseInt(batchId);
            } catch (NumberFormatException ignored) {
            }
        }

        // Embed fragments — both when premium is available, only live when premium is hidden
        if (savedInstanceState == null) {
            Log.d(TAG, "ScrollTrace: creating liveFragment with scrollToChapterId=" + scrollToChapterId);
            liveFragment = ChapterListByBatchFragment.newInstance(
                    batchIdInt, "", "", subjectId, null, false, SectionType.LIVE.name(), scrollToChapterId);
            if (!isPremiumHidden) {
                premiumFragment = ChapterListByBatchFragment.newInstance(
                        batchIdInt, "", "", subjectId, null, false, SectionType.PREMIUM.name());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, liveFragment, SectionType.LIVE.name())
                        .add(R.id.fragment_container, premiumFragment, SectionType.PREMIUM.name())
                        .hide(currentType == SectionType.LIVE ? premiumFragment : liveFragment)
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, liveFragment, SectionType.LIVE.name())
                        .commit();
            }
        } else {
            liveFragment = (ChapterListByBatchFragment)
                    getSupportFragmentManager().findFragmentByTag(SectionType.LIVE.name());
            premiumFragment = (ChapterListByBatchFragment)
                    getSupportFragmentManager().findFragmentByTag(SectionType.PREMIUM.name());
        }
    }

    private void applyNightMode(SectionType type) {
        getDelegate().setLocalNightMode(
                type == SectionType.PREMIUM
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        setSystemBarsStyle(this, type == SectionType.LIVE);
    }



    private void simplifyHeaderTabText() {
        try {
            String keyValue = MyApplication.getFirebaseRemoteConfig().getString("chapter_list_headings_v1");
            JSONObject jsonObject = new JSONObject(keyValue);

            //long
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            JSONObject live = jsonObject.optJSONObject("live");
            JSONObject premium = jsonObject.optJSONObject("premium");

            tv_live_btn.setText(live != null ? live.optString(lang, getString(R.string.live)) : getString(R.string.live));
            tv_premium_btn.setText(premium != null ? premium.optString(lang, getString(R.string.extra_classes)) : getString(R.string.extra_classes));
        } catch (Exception e) {
            tv_live_btn.setText(R.string.live);
            tv_premium_btn.setText(R.string.extra_classes);
        }
    }



    /**
     * Only the icon appearance is set here. The bars themselves are transparent app wide, so the
     * layout's own bg_color background is what shows behind them.
     */
    public void setSystemBarsStyle(Activity activity, boolean lightMode) {
        Window window = activity.getWindow();

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());

        if (controller != null) {
            controller.setAppearanceLightStatusBars(lightMode);
            controller.setAppearanceLightNavigationBars(lightMode);
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Manually refresh activity-level views that don't auto-update when night mode changes
        // without recreation (because configChanges="uiMode" is declared in the manifest).
        int bgColor = ContextCompat.getColor(this, R.color.bg_color);
        getWindow().getDecorView().setBackgroundColor(bgColor);
        if (ll_parent_layout != null)
            ll_parent_layout.setBackgroundColor(bgColor);
        if (ll_data != null)
            ll_data.setBackgroundColor(bgColor);
        if (activity_title != null)
            activity_title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        if (back_arrow != null)
            back_arrow.setColorFilter(ContextCompat.getColor(this, R.color.text_primary));
        if (view_divider != null)
            view_divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        // Live card background and text — these have day/night variants but ColorDrawable
        // doesn't auto-re-fetch after inflation when configChanges="uiMode" is handled here.
        if (live_btn_bg != null)
            live_btn_bg.setBackgroundColor(ContextCompat.getColor(this, R.color.new_lecture_live_btn));
        if (tv_live_btn != null)
            tv_live_btn.setTextColor(ContextCompat.getColor(this, R.color.new_lecture_live_btn_text));
        applySearchViewTheme();

        //theme check
        currentActivityTheme = (currentType == SectionType.PREMIUM) ? ThemeEnum.DARK : ThemeEnum.LIGHT;

        // Notify both fragments so they refresh their container backgrounds and adapter colors
        if (liveFragment != null && liveFragment.isAdded())
            liveFragment.onThemeChanged(currentActivityTheme);
        if (premiumFragment != null && premiumFragment.isAdded())
            premiumFragment.onThemeChanged(currentActivityTheme);
    }

    private void applyBorderAnimation(SectionType type) {
        if (type == SectionType.LIVE) {
            borderViewPremium.stopAnimation();
            borderViewLive.playAnimation(0);
        } else {
            borderViewLive.stopAnimation();
            borderViewPremium.playAnimation(0);
        }
    }

    /**
     * Returns true if the premium section should be hidden for the given batch/subject code.
     * Remote config key: "hide_premium_section_batch_codes" — comma-separated list of codes, e.g. "MATH,PHY"
     */
    private boolean shouldHidePremium() {
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        if (StringUtils.isEmpty(batchCode)) return false;
        String raw = MyApplication.getFirebaseRemoteConfig()
                .getString("hide_premium_section_batch_codes");
        if (StringUtils.isEmpty(raw)) return false;
        for (String code : raw.split(",")) {
            if (batchCode.trim().equalsIgnoreCase(code.trim())) return true;
        }
        return false;
    }


    private void runCircularReveal(SectionType type) {
        MaterialCardView fromButton = (type == SectionType.PREMIUM) ? newLectureLiveButton    : newLecturePremiumButton;
        MaterialCardView toButton   = (type == SectionType.PREMIUM) ? newLecturePremiumButton : newLectureLiveButton;
        Fragment showFrag           = (type == SectionType.PREMIUM) ? premiumFragment         : liveFragment;
        Fragment hideFrag           = (type == SectionType.PREMIUM) ? liveFragment            : premiumFragment;

        if (revealOverlay == null || !revealOverlay.isAttachedToWindow() || toButton == null) {
            applyNightMode(type);
            getSupportFragmentManager().beginTransaction()
                    .show(showFrag).hide(hideFrag).commit();
            return;
        }

        revealOverlay.setBackgroundColor(type == SectionType.LIVE? 0xFFFFFFFF : 0xFF121212);

        int[] toLoc      = new int[2];
        int[] overlayLoc = new int[2];
        toButton.getLocationOnScreen(toLoc);
        revealOverlay.getLocationOnScreen(overlayLoc);

        int cx = (toLoc[0] - overlayLoc[0]) + toButton.getWidth()  / 2;
        int cy = (toLoc[1] - overlayLoc[1]) + toButton.getHeight() / 2;

        float maxRadius = (float) Math.hypot(
                Math.max(cx, revealOverlay.getWidth() - cx),
                Math.max(cy, revealOverlay.getHeight() - cy));

        fromButton.setClickable(false);
        toButton.setClickable(false);

        revealOverlay.setVisibility(View.VISIBLE);
        Animator anim = ViewAnimationUtils.createCircularReveal(revealOverlay, cx, cy, 0f, maxRadius);
        anim.setDuration(450);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                applyNightMode(type);
                getSupportFragmentManager().beginTransaction()
                        .show(showFrag).hide(hideFrag).commitAllowingStateLoss();

                revealOverlay.postDelayed(() -> {
                    revealOverlay.setVisibility(View.INVISIBLE);
                    fromButton.setClickable(true);
                    toButton.setClickable(true);
                }, 100);
            }
        });
        anim.start();
    }

    // apply text gradient golden
    private void applyGradientToText(final TextView textView) {
        if (textView == null) return;

        textView.post(() -> {
            TextPaint paint = textView.getPaint();

            Shader shader = new LinearGradient(
                    0f, 0f,
                    textView.getWidth(), textView.getTextSize(),
                    new int[]{0xFFEF9F0B, 0xFFFBD950, 0xFFFEEC77, 0xFFFBD950, 0xFFEA9C0B},
                    new float[]{0.1484f, 0.3647f, 0.5228f, 0.6656f, 0.8516f},
                    Shader.TileMode.CLAMP
            );

            paint.setShader(shader);
            textView.setTextColor(Color.WHITE);
            textView.invalidate();
        });
    }

    private void applySearchViewTheme() {
        if (searchView == null) return;
        int iconColor = ContextCompat.getColor(this, R.color.text_primary);
        int textColor = ContextCompat.getColor(this, R.color.text_primary);
        int hintColor = ContextCompat.getColor(this, R.color.text_secondary);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        if (searchIcon != null) searchIcon.setColorFilter(iconColor);

        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeIcon != null) closeIcon.setColorFilter(iconColor);

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(textColor);
            searchEditText.setHintTextColor(hintColor);
        }
    }

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));
        applySearchViewTheme();

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            Map<String,String> params = new HashMap<>();
            params.put("Screen Name:", String.valueOf(currentType));
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_search_click.getActionName(),params);
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            activity_title.setVisibility(View.GONE);
        });

        searchView.setOnCloseListener(() -> {
            activity_title.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setFiltersToFragments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                hasSearchText = !query.isEmpty();
                setFiltersToFragments(query);
                return true;
            }
        });
    }

    private void setFiltersToFragments(String query) {
        try {
            if (liveFragment != null && liveFragment.isAdded()) {
                liveFragment.setFilter(query);
            }
            if (premiumFragment != null && premiumFragment.isAdded()) {
                premiumFragment.setFilter(query);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // Reset to follow-system so the night mode doesn't leak into other activities
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onDestroy();
    }

    // load data
    private void loadData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (savedInstanceState != null) {
            currentType = SectionType.valueOf(savedInstanceState.getString("lecture_type", SectionType.LIVE.name()));
            title = savedInstanceState.getString("title");
            subjectId = savedInstanceState.getString("SubjectId");
            subjectCode = savedInstanceState.getString("SubjectCode");
            batchId = savedInstanceState.getString("batchId");
        } else {
            currentType = SectionType.LIVE;
            String type = intent.getStringExtra("lecture_type");
            if (type != null) currentType = SectionType.valueOf(type);

            title = intent.getStringExtra("title");
            subjectId = intent.getStringExtra("SubjectId");
            subjectCode = intent.getStringExtra("SubjectCode");
            batchId = intent.getStringExtra("batchId");
            scrollToChapterId = intent.getStringExtra("scrollToChapterId");

            if (data != null) {
                if (data.getQueryParameter("title") != null) title = data.getQueryParameter("title");
                if (data.getQueryParameter("SubjectId") != null) subjectId = data.getQueryParameter("SubjectId");
                if (data.getQueryParameter("SubjectCode") != null) subjectCode = data.getQueryParameter("SubjectCode");
                if (data.getQueryParameter("batchId") != null) batchId = data.getQueryParameter("batchId");
                if (data.getQueryParameter("scrollToChapterId") != null) scrollToChapterId = data.getQueryParameter("scrollToChapterId");
                navigationFrom = "deeplink";
            } else {
                String screen = intent.getStringExtra("Screen");
                navigationFrom = StringUtils.isNotEmpty(screen) ? screen : "unknown";
            }
            Log.d(TAG, "ScrollTrace: loadData -> subjectId=" + subjectId + ", scrollToChapterId=" + scrollToChapterId + ", navigationFrom=" + navigationFrom);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lecture_type", currentType.toString());
        outState.putString("title", title);
        outState.putString("SubjectId", subjectId);
        outState.putString("SubjectCode", subjectCode);
        outState.putString("batchId", batchId);
    }
}
