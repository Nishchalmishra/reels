package arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.UnlockActivity;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter.ChapterListByTypeFragment;
import arivihan.technologies.doubtbuzzter2.listener.competitive.RefreshActivityListener;
import arivihan.technologies.doubtbuzzter2.model.competitive.ChapterListTabConfig;

/**
 * Hosts type-based tabs driven by {@code chapter_list_by_type_config} remote config
 * (keyed by BATCH_CODE).  Each tab is a {@link ChapterListByTypeFragment} which replicates
 * the full {@code ChapterListBySubjectsActivity} UX (live classes, banner, subject tabs,
 * chapter fragments) and passes the tab's {@code type} value into API calls.
 *
 * <p>Intent extras / deeplink query params:
 * <ul>
 *   <li>{@code title}     – toolbar title</li>
 *   <li>{@code type}      – pre-select the tab whose type matches this value</li>
 *   <li>{@code boardCode} – forwarded to each fragment</li>
 * </ul>
 */
public class ChapterListByTypeActivity extends BaseActivity implements RefreshActivityListener {

    private static final String TAG = ChapterListByTypeActivity.class.getSimpleName();

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_SUBJECT_ID = "SubjectId";

    // UI
    private RelativeLayout rl_parent_layout;
    private LinearLayout back_button_layout;
    private TextView activity_title;
    private SearchView searchView;
    private TabLayout tabs;
    private ViewPager2 viewpager;
    private ViewPagerAdapter viewPagerAdapter;

    // Data
    private String title;
    private String initialType;
    private String initialSubjectId;
    private List<ChapterListTabConfig> tabConfigs = new ArrayList<>();
    private boolean refreshPage = false;
    private String featureType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list_by_type);

        Uri data = getIntent().getData();
        if (data != null) {
            title = data.getQueryParameter(EXTRA_TITLE);
            initialType = data.getQueryParameter(EXTRA_TYPE);
            initialSubjectId = data.getQueryParameter(EXTRA_SUBJECT_ID);
        } else {
            title = getIntent().getStringExtra(EXTRA_TITLE);
            initialType = getIntent().getStringExtra(EXTRA_TYPE);
            initialSubjectId = getIntent().getStringExtra(EXTRA_SUBJECT_ID);
        }

        featureType = getIntent().getStringExtra("type");
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        back_button_layout = findViewById(R.id.back_button_layout);
        activity_title = findViewById(R.id.activity_title);
        tabs = findViewById(R.id.tabs);
        viewpager = findViewById(R.id.viewpager);

        if (StringUtils.isNotEmpty(title)) {
            activity_title.setText(title);
        }

        back_button_layout.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_screen_back_button_click.getActionName());
            onBackPressedActivity();
        });

        setSearchView();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        });

        loadTabsFromConfig();
    }

    // -------------------------------------------------------------------------
    // Tab config
    // -------------------------------------------------------------------------

    private void loadTabsFromConfig() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("chapter_list_by_type_config");
            Type type = new TypeToken<Map<String, List<ChapterListTabConfig>>>() {}.getType();
            Map<String, List<ChapterListTabConfig>> configMap = new Gson().fromJson(json, type);
            if (configMap != null && configMap.containsKey(batchCode)) {
                tabConfigs = configMap.get(batchCode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (tabConfigs == null || tabConfigs.isEmpty()) {
            return;
        }

        viewPagerAdapter = new ViewPagerAdapter(this, tabConfigs);
        viewpager.setAdapter(viewPagerAdapter);
        viewpager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

        if (tabConfigs.size() == 1) {
            // Single tab — hide the tab strip entirely
            tabs.setVisibility(View.GONE);
        } else {
            // Dynamic mode: fixed+fill for ≤3 tabs, scrollable for more
            if (tabConfigs.size() <= 3) {
                tabs.setTabMode(TabLayout.MODE_FIXED);
                tabs.setTabGravity(TabLayout.GRAVITY_FILL);
            } else {
                tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
                tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
            }

//            Typeface urbanistMedium = ResourcesCompat.getFont(this, R.font.urbanist_medium);

            new TabLayoutMediator(tabs, viewpager, (tab, position) -> {
                View customView = LayoutInflater.from(this)
                        .inflate(R.layout.layout_tab_text_16, null);
                TextView tabText = customView.findViewById(R.id.tabText);
                tabText.setText(tabConfigs.get(position).getName());
                tabText.setTextColor(ContextCompat.getColor(ChapterListByTypeActivity.this, R.color.text_secondary));
                tab.setCustomView(customView);
            }).attach();

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    View view = tab.getCustomView();
                    if (view != null) {
                        TextView tv = view.findViewById(R.id.tabText);
                        tv.setTextColor(ContextCompat.getColor(ChapterListByTypeActivity.this, R.color.black_12)); // selected
                    }
                    int position = tab.getPosition();
                    if (position >= 0 && position < tabConfigs.size()) {
                        ChapterListTabConfig config = tabConfigs.get(position);
                        Map<String, String> params = new HashMap<>();
                        params.put("tab_name", config.getName());
                        params.put("tab_type", config.getType());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_type_selected.getActionName(), params);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    View view = tab.getCustomView();
                    if (view != null) {
                        TextView tv = view.findViewById(R.id.tabText);
                        tv.setTextColor(ContextCompat.getColor(ChapterListByTypeActivity.this, R.color.text_secondary)); // unselected
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    // no-op
                }
            });

            tabs.setVisibility(View.VISIBLE);
        }

        // Pre-select tab matching initialType
        viewpager.post(() -> {
            try {
                int targetIndex = 0;
                if (StringUtils.isNotEmpty(initialType)) {
                    for (int i = 0; i < tabConfigs.size(); i++) {
                        if (initialType.equalsIgnoreCase(tabConfigs.get(i).getType())) {
                            targetIndex = i;
                            break;
                        }
                    }
                }
                viewpager.setCurrentItem(targetIndex, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Search — delegate to the currently visible ChapterListByTypeFragment
    // -------------------------------------------------------------------------

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));
//        MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_bottom_nav_click.getActionName());

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            try {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_screen_search_btn_click.getActionName());
                searchView.setIconified(false);
                searchView.setMaxWidth(Integer.MAX_VALUE);
                activity_title.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        });

        searchView.setOnCloseListener(() -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_screen_search_close_btn_click.getActionName());
            activity_title.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                forwardFilterToCurrentFragment(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                forwardFilterToCurrentFragment(s);
                return true;
            }
        });
    }

    private void forwardFilterToCurrentFragment(String filter) {
        try {
            if (viewPagerAdapter == null) return;
            Fragment fragment = viewPagerAdapter.getFragment(viewpager.getCurrentItem());
            if (fragment instanceof ChapterListByTypeFragment) {
                ((ChapterListByTypeFragment) fragment).setFilter(filter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void onBackPressedActivity() {
        if (searchView != null && searchView.hasFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
            return;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setChapterCompletionScreen();
        if (refreshPage) {
            refreshPage = false;
            refreshCurrentFragment();
        }
    }

    @Override
    public void refreshPage(boolean refresh) {
        refreshPage = refresh;
    }

    @Override
    public void doRefreshNow(boolean refresh) {
        if (refresh) refreshCurrentFragment();
    }

    private void setChapterCompletionScreen() {
        boolean showChapterCompletionScreen = MyApplication.getBoolData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey());
        if (showChapterCompletionScreen) {
            Intent intent = new Intent(this, UnlockActivity.class);
            intent.putExtra(UnlockActivity.BUNDLE_CONDITION_TYPE, "chapter");
            startActivity(intent);

            MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), false);
        }
//        MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), true);

    }
    private void refreshCurrentFragment() {
        if (viewPagerAdapter == null) return;
        Fragment current = viewPagerAdapter.getFragment(viewpager.getCurrentItem());
        if (current instanceof ChapterListByTypeFragment) {
            ((ChapterListByTypeFragment) current).doRefreshNow(true);
        }
    }

    // -------------------------------------------------------------------------
    // ViewPager adapter
    // -------------------------------------------------------------------------

    class ViewPagerAdapter extends FragmentStateAdapter {

        private final List<ChapterListTabConfig> configs;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        ViewPagerAdapter(@NonNull FragmentActivity fa, List<ChapterListTabConfig> configs) {
            super(fa);
            this.configs = configs;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ChapterListTabConfig cfg = configs.get(position);
            Fragment fragment = ChapterListByTypeFragment.newInstance(title, cfg.getType(), initialSubjectId, cfg.isTodaysClassVisibility(),featureType);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return configs.size();
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }
    }
}
