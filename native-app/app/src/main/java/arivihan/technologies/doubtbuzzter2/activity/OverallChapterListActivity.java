package arivihan.technologies.doubtbuzzter2.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.OverallChapterListFragment;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;

public class OverallChapterListActivity extends AppCompatActivity
        implements TodayTaskFragment.TodayTaskListener {

    private LinearLayout back_button_layout;
    private TextView activity_title;
    private SearchView searchView;

    private String title;
    private String batchAddOnCode;
    private String navigationFrom;
    private String featureTag;
    private boolean overall;
    private boolean showTodayClass;

    private OverallChapterListFragment fragment;
    private FrameLayout todayTaskContainer;
    private LinearLayout ll_today_task_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall_chapter_list);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && searchView.hasFocus()) {
                    CommonMethods.hideKeyboard(OverallChapterListActivity.this, searchView);
                    searchView.clearFocus();
                } else {
                    finish();
                }
            }
        });

        Uri deepLinkUri = getIntent().getData();
        if (deepLinkUri != null) {
            title = deepLinkUri.getQueryParameter("title");
            batchAddOnCode = deepLinkUri.getQueryParameter("batchAddOnCode");
            String screen = deepLinkUri.getQueryParameter("screen");
            navigationFrom = StringUtils.isNotEmpty(screen) ? screen : "deeplink";
            overall = Boolean.parseBoolean(deepLinkUri.getQueryParameter("overall"));
            showTodayClass = !"false".equalsIgnoreCase(deepLinkUri.getQueryParameter("showTodayClass"));
            featureTag = deepLinkUri.getQueryParameter("FEATURE_TAG");
        } else {
            title = getIntent().getStringExtra("title");
            batchAddOnCode = getIntent().getStringExtra("batchAddOnCode");
            String screen = getIntent().getStringExtra("Screen");
            navigationFrom = StringUtils.isNotEmpty(screen) ? screen : "unknown";
            overall = getIntent().getBooleanExtra("overall", true);
            showTodayClass = getIntent().getBooleanExtra("showTodayClass", true);
            featureTag = getIntent().getStringExtra("FEATURE_TAG");
        }

        Map<String, String> navParams = new HashMap<>();
        navParams.put("navigation_from", navigationFrom);
        navParams.put("batchAddOnCode", batchAddOnCode != null ? batchAddOnCode : "");

        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.chap_list_screen_view.getActionName(),
                navParams
        );
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.CHAPTER_LIST_SCREEN.name(), AnalyticsEvent.CHAPTER_LIST_SCREEN.name());

        back_button_layout = findViewById(R.id.back_button_layout);
        activity_title = findViewById(R.id.activity_title);
        todayTaskContainer = findViewById(R.id.today_task_container);
        ll_today_task_layout = findViewById(R.id.ll_today_task_layout);

        back_button_layout.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (!showTodayClass) {
            ll_today_task_layout.setVisibility(View.GONE);
            findViewById(R.id.today_task_divider).setVisibility(View.GONE);
            // Remove the top margin on the fragment container so tabs sit flush under the toolbar
            View fragmentContainer = findViewById(R.id.fragment_container);
            LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) fragmentContainer.getLayoutParams();
            lp.topMargin = 0;
            fragmentContainer.setLayoutParams(lp);
        } else {
            // Hide by default; shown only when TodayTaskFragment reports data
            ll_today_task_layout.setVisibility(View.GONE);
        }

        if (StringUtils.isEmpty(title)) {
            title = MyApplication.getFirebaseRemoteConfig().getString(
                    "adaptive_chapters_by_subject_screen_title_"
                            + MyApplication.getStringData(
                            PreferenceKeys.MY_LANG.getKey())
            );
        }

        if (StringUtils.isNotEmpty(title)) {
            activity_title.setText(title);
        }

        setSearchView();

        if (savedInstanceState == null) {
            if (showTodayClass) {
                todayTaskContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.today_task_container,
                                new TodayTaskFragment(this, "OverallChapterListActivity")
                        )
                        .commit();
            }
            fragment = OverallChapterListFragment.newInstance(batchAddOnCode, overall, featureTag);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            fragment
                    )
                    .commit();
        } else {
            fragment = (OverallChapterListFragment)
                    getSupportFragmentManager()
                            .findFragmentById(
                                    R.id.fragment_container
                            );
        }
    }

    // ── TodayTaskFragment.TodayTaskListener ──────────────────────────────────

    @Override
    public void onDataFetched(TodayTaskFragment source, int size) {
        ll_today_task_layout.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideContinueSection() {
        // no-op for this screen
    }

    @Override
    public void hideBadgeView() {
        // no-op for this screen
    }

    @Override
    public void onAnimatedCardTagClicked(String tag) {
        // no-op for this screen
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void setSearchView() {

        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search));

        ImageView searchButton =
                searchView.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {

            Map<String, String> p = new HashMap<>();
            p.put("navigation_from", navigationFrom);

            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.chap_search_btn_click.getActionName(),
                    p
            );

            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            activity_title.setVisibility(View.GONE);
        });

        searchView.setOnCloseListener(() -> {

            Map<String, String> p = new HashMap<>();
            p.put("navigation_from", navigationFrom);

            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.chap_search_close_btn_click.getActionName(),
                    p
            );

            activity_title.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        passFilterToFragment(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        passFilterToFragment(query);
                        return true;
                    }
                });
    }

    private void passFilterToFragment(String query) {

        if (fragment == null) {
            fragment = (OverallChapterListFragment)
                    getSupportFragmentManager()
                            .findFragmentById(
                                    R.id.fragment_container
                            );
        }

        if (fragment != null && fragment.isAdded()) {
            fragment.setFilter(query);
        }
    }
}