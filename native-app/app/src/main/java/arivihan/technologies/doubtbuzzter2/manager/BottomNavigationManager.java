package arivihan.technologies.doubtbuzzter2.manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.BottomNavSubscriptionConfig;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.BottomNavTabConfig;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.BottomNavTabNavigation;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.BottomNavTabType;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.CompetitiveBatchNavConfig;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.CompetitiveBottomNavTab;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.HalfYearlyActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.AnalyticFragment;
import arivihan.technologies.doubtbuzzter2.fragments.MainDashboard;
import arivihan.technologies.doubtbuzzter2.fragments.ProfileFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.home.CompetitiveHomeScreenFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.learn.CompetitiveLearnFragment;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.NavigationConfig;

/**
 * Manages all bottom navigation concerns for HomeScreen:
 * menu inflation, icon/badge configuration, click handling, and resume-time redirects.
 * <p>
 * Follows the single-responsibility principle — HomeScreen delegates all nav
 * logic here and only implements {@link Delegate} for the minimal callbacks needed.
 */
public class BottomNavigationManager {

    private static final String TAG = BottomNavigationManager.class.getSimpleName();

    // ─────────────────────────────────────────────
    // Delegate — implemented by HomeScreen
    // ─────────────────────────────────────────────

    /**
     * Callbacks that HomeScreen must satisfy to allow this manager to drive navigation.
     */
    public interface Delegate {
        /**
         * Replace the main fragment container with the given fragment.
         */
        void setFragment(Fragment fragment);

        /**
         * Destroy the HowTo video player (hide container + release player).
         */
        void destroyHowTo();

        /**
         * Remove the HowTo video view from the layout.
         */
        void removeHowTo();

        /**
         * Trigger a fresh home-screen data reload (called on resume-from-background).
         */
        void reloadData();

        /**
         * Whether the Todo section is currently visible (passed to ProfileFragment).
         */
        boolean isTodoVisible();
    }

    // ─────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────

    private final AppCompatActivity activity;
    private final BottomNavigationView bottomNav;
    private final Delegate delegate;
    private final FirebaseAnalytics firebaseAnalytics;

    /**
     * Debounce flag — prevents rapid double-clicks from triggering two navigations.
     */
    private boolean isNavigating = false;

    // Custom center-tab overlay views (non-subscribed standard nav only)
    private ImageView centerTabImage;
    private LottieAnimationView centerTabLottie;
    // Container + badge for the local-drawable overlay (REFER_NOTES / SUPER_COIN path)
    private android.widget.FrameLayout centerTabContainer;
    private android.widget.TextView centerTabBadge;

    private boolean newUser;

    /**
     * BASIC / PLUS / PRO. Defaults to BASIC if missing.
     */
    private String userSubscriptionType;

    /**
     * Menu item id for the CHAT tab (-1 if not present in current config).
     */
    private int chatMenuItemId = -1;

    /**
     * Realtime Database reference + listener for the chat unseen-count badge.
     */
    private DatabaseReference chatUnseenRef;
    private ValueEventListener chatBadgeListener;

    /**
     * Ordered tab configs for the current subscriptionType (resolved from
     * {@code non_competitive_bottom_nav_config}).  Null when config is unavailable.
     */
    private List<BottomNavTabConfig> nonCompetitiveTabConfigs;

    /**
     * Per-tab resolved navigation (batchCode lookup applied).
     * Index matches {@link #nonCompetitiveTabConfigs}. Entry is null for non-NAVIGATE tabs.
     */
    private List<BottomNavTabNavigation> resolvedTabNavigations;

    /**
     * Competitive batch nav config resolved for the current batchCode.
     * Null when config is unavailable or user is non-competitive.
     */
    private CompetitiveBatchNavConfig competitiveNavConfig;

    /**
     * Kept for backward-compat callers; no longer used for navigation (comes from config).
     */
    private String examId;
    private String examName;

    /**
     * True when HomeScreen was launched with is_user_onboarded=true (first onboarding session).
     */
    private boolean isUserOnboarded = false;

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    public BottomNavigationManager(AppCompatActivity activity,
                                   BottomNavigationView bottomNav,
                                   Delegate delegate) {
        this.activity = activity;
        this.bottomNav = bottomNav;
        this.delegate = delegate;
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    /**
     * Full initialisation — call once from {@code Activity.onCreate()}.
     *
     * @param newUser     value from the launching Intent's "NewUser" extra
     * @param selectedTab value from the launching Intent's SELECTED_TAB extra ("3", "5", or null)
     * @param examId      competitive exam id for HalfYearlyActivity (may be null)
     * @param examName    competitive exam name for HalfYearlyActivity (may be null)
     */
    public void setup(boolean newUser, String selectedTab, String examId, String examName) {
        this.newUser = newUser;
        this.examId = examId;
        this.examName = examName;
        this.userSubscriptionType = readSubscriptionType();

        setupMenu();
        if (MyApplication.isCompetitiveProfile()) loadCompetitiveNavConfig();
        else loadNonCompetitiveTabConfig();
        updateUI();
        setupListener();
        handleInitialTab(selectedTab);
    }

    public void setExam(String examId, String examName) {
        this.examId = examId;
        this.examName = examName;
    }

    /**
     * Re-evaluates badge / icon / title for the analytics (centre) tab.
     * Call after subscription status changes so the UI stays in sync.
     */
    public void updateUI() {
        logMenuState();

        if (MyApplication.isCompetitiveProfile()) {
            userSubscriptionType = readSubscriptionType();
            loadCompetitiveNavConfig();
            if (SubscriptionType.hasActiveSubscription()) {
                applySubscribedUI();
            } else {
                applyUnsubscribedUI();
            }
        } else {
            userSubscriptionType = readSubscriptionType();
            loadNonCompetitiveTabConfig();
            applyNonCompetitiveUI();
        }
    }

    /**
     * Show or hide the bottom navigation bar.
     */
    public void setVisibility(int visibility) {
        bottomNav.setVisibility(visibility);
    }

    /**
     * Mark the home tab as selected (without triggering the click listener).
     */
    public void selectHomeTab() {
        checkAndSetChecked(R.id.item_home);
    }

    /**
     * @return the currently selected menu item id.
     */
    public int getSelectedItemId() {
        return bottomNav.getSelectedItemId();
    }

    /**
     * Update exam data used when the competitive analytics tab opens HalfYearlyActivity.
     */
    public void setExamData(String examId, String examName) {
        this.examId = examId;
        this.examName = examName;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void setIsUserOnboarded(boolean isUserOnboarded) {
        this.isUserOnboarded = isUserOnboarded;
    }

    /**
     * Updates the chat tab badge text dynamically.
     * The badge is hidden when {@code text} is null, empty, "0", or represents a
     * number ≤ 0.  Has no effect if there is no CHAT tab in the current nav config.
     */
    public void updateChatBadge(String text) {
        if (chatMenuItemId == -1) return;
        if (!shouldShowChatBadge(text)) {
            bottomNav.removeBadge(chatMenuItemId);
        } else {
            applyChatBadge(chatMenuItemId, text);
        }
    }

    /**
     * Attaches a Firebase Realtime Database listener on
     * {@code users/virtual_chat_unseen_message_count/<userId>} and keeps the
     * chat badge in sync with every value change.
     * <p>
     * Safe to call multiple times — re-attaching is a no-op when a listener is
     * already active.  Call from {@code Activity.onResume()}.
     */
    public void startListeningForChatBadge() {
        if (chatBadgeListener != null) return; // already listening

        chatUnseenRef = MyApplication.getVirtualChatUnseenCountReference();
        chatBadgeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists() || snapshot.getValue() == null) {
                        updateChatBadge(null);
                        return;
                    }
                    long count = snapshot.getValue(Long.class);
                    updateChatBadge(count > 0 ? count > 20 ? count + "+" : String.valueOf(count) : null);
                } catch (Exception e) {
                    Log.e(TAG, "startListeningForChatBadge onDataChange: " + e.getMessage());
                    updateChatBadge(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "startListeningForChatBadge onCancelled: " + error.getMessage());
            }
        };
        chatUnseenRef.addValueEventListener(chatBadgeListener);
    }

    /**
     * Detaches the chat badge listener to stop receiving updates and avoid leaks.
     * Call from {@code Activity.onPause()} and {@code Activity.onDestroy()}.
     */
    public void stopListeningForChatBadge() {
        if (chatBadgeListener != null && chatUnseenRef != null) {
            chatUnseenRef.removeEventListener(chatBadgeListener);
        }
        chatBadgeListener = null;
        chatUnseenRef = null;
    }

    // ─────────────────────────────────────────────
    // Private — menu & initial tab
    // ─────────────────────────────────────────────

    private void setupMenu() {
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(MyApplication.isCompetitiveProfile()
                ? R.menu.competitive_bottom_navigation_menu
                : R.menu.bottom_navigation_menu);
    }

    private void handleInitialTab(String selectedTab) {
        if ("5".equals(selectedTab)) {
            checkAndSetChecked(R.id.item_profile);
            navigateToProfile(null);
        } else if ("3".equals(selectedTab)) {
            checkAndSetChecked(R.id.item_analytics);
            delegate.setFragment(new AnalyticFragment());
        } else {
            checkAndSetChecked(R.id.item_home);
            Bundle bundle = new Bundle();
            bundle.putBoolean("NewUser", newUser);
            bundle.putBoolean("is_user_onboarded", isUserOnboarded);
            navigateToDashboard(bundle);
        }
    }

    // ─────────────────────────────────────────────
    // Private — non-competitive tab config (new system)
    // ─────────────────────────────────────────────

    private String readSubscriptionType() {
        String type = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());
        return StringUtils.isNotEmpty(type) ? type : SubscriptionType.BASIC.name();
    }

    /**
     * Parses {@code non_competitive_bottom_nav_config} for the current subscriptionType,
     * then for each tab that has a {@code remoteConfigKey}, fetches that key and resolves
     * the per-batchCode {@link BottomNavTabNavigation}.  Results are cached in
     * {@link #nonCompetitiveTabConfigs} and {@link #resolvedTabNavigations}.
     */
    private void loadNonCompetitiveTabConfig() {
        nonCompetitiveTabConfigs = null;
        resolvedTabNavigations = null;
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("non_competitive_bottom_nav_config_v2");
            if (StringUtils.isEmpty(json)) return;
            Type mapType = new TypeToken<Map<String, BottomNavSubscriptionConfig>>() {
            }.getType();
            Map<String, BottomNavSubscriptionConfig> configMap = new Gson().fromJson(json, mapType);

            BottomNavSubscriptionConfig subscriptionConfig = configMap.get(userSubscriptionType);
            if (subscriptionConfig == null)
                subscriptionConfig = configMap.get(SubscriptionType.BASIC.name());
            if (subscriptionConfig == null || subscriptionConfig.getTabs() == null) return;

            nonCompetitiveTabConfigs = subscriptionConfig.getTabs();
            resolvedTabNavigations = new ArrayList<>();

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            for (BottomNavTabConfig tabConfig : nonCompetitiveTabConfigs) {
                resolvedTabNavigations.add(resolveTabNavigation(tabConfig, batchCode));
            }
        } catch (Exception e) {
            Log.e(TAG, "loadNonCompetitiveTabConfig: " + e.getMessage());
        }
    }

    /**
     * Resolves the navigation for a single tab.
     * If the tab has a {@code remoteConfigKey}, fetches that key and returns the entry for
     * {@code batchCode} (or DEFAULT).  Falls back to {@code tabConfig.getDefaultNavigation()}.
     */
    private BottomNavTabNavigation resolveTabNavigation(BottomNavTabConfig tabConfig, String batchCode) {
        String remoteConfigKey = tabConfig.getRemoteConfigKey();
        if (StringUtils.isNotEmpty(remoteConfigKey)) {
            try {
                String json = MyApplication.getFirebaseRemoteConfig().getString(remoteConfigKey);
                if (StringUtils.isNotEmpty(json)) {
                    Type navMapType = new TypeToken<Map<String, BottomNavTabNavigation>>() {
                    }.getType();
                    Map<String, BottomNavTabNavigation> navMap = new Gson().fromJson(json, navMapType);
                    BottomNavTabNavigation nav = StringUtils.isNotEmpty(batchCode) ? navMap.get(batchCode) : null;
                    if (nav == null) nav = navMap.get("DEFAULT");
                    if (nav != null) return nav;
                }
            } catch (Exception e) {
                Log.e(TAG, "resolveTabNavigation[" + remoteConfigKey + "]: " + e.getMessage());
            }
        }
        return tabConfig.getDefaultNavigation();
    }

    // ─────────────────────────────────────────────
    // Private — competitive nav config (batch-code driven)
    // ─────────────────────────────────────────────

    /**
     * Loads {@code competitive_bottom_nav_config} and resolves the entry for the
     * current batchCode into {@link #competitiveNavConfig}.
     */
    private void loadCompetitiveNavConfig() {
        competitiveNavConfig = null;
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("competitive_bottom_nav_config");
            if (StringUtils.isEmpty(json)) return;
            Type mapType = new TypeToken<Map<String, CompetitiveBatchNavConfig>>() {
            }.getType();
            Map<String, CompetitiveBatchNavConfig> configMap = new Gson().fromJson(json, mapType);
            if (configMap != null) competitiveNavConfig = configMap.get(batchCode);
        } catch (Exception e) {
            Log.e(TAG, "loadCompetitiveNavConfig: " + e.getMessage());
        }
    }

    /**
     * Returns the CENTER tab config, or {@code null} if config is unavailable.
     */
    private CompetitiveBottomNavTab getCompetitiveCenterTab() {
        if (competitiveNavConfig == null || competitiveNavConfig.getTabs() == null) return null;
        for (CompetitiveBottomNavTab tab : competitiveNavConfig.getTabs()) {
            if ("CENTER".equals(tab.getTabId())) return tab;
        }
        return null;
    }

    /**
     * Applies label and icon to all non-CENTER tabs from {@link #competitiveNavConfig}.
     * CENTER tab label/icon are handled separately in
     * {@link #applySubscribedUI()} / {@link #applyUnsubscribedUI()}.
     */
    private void applyCompetitiveTabLabels() {
        if (competitiveNavConfig == null || competitiveNavConfig.getTabs() == null) return;
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (StringUtils.isEmpty(lang)) lang = "en";
        List<CompetitiveBottomNavTab> tabs = competitiveNavConfig.getTabs();
        int menuSize = bottomNav.getMenu().size();
        for (int i = 0; i < Math.min(tabs.size(), menuSize); i++) {
            CompetitiveBottomNavTab tab = tabs.get(i);
            if (tab == null || "CENTER".equals(tab.getTabId())) continue;
            if (tab.getLabel() != null) {
                String label = tab.getLabel().get(lang);
                if (StringUtils.isEmpty(label)) label = tab.getLabel().get("en");
                if (StringUtils.isNotEmpty(label)) bottomNav.getMenu().getItem(i).setTitle(label);
            }
            BottomNavTabType tabType = BottomNavTabType.fromString(tab.getTabType());
            if (tabType != null) bottomNav.getMenu().getItem(i).setIcon(tabType.getDrawableRes());
        }
    }

    /**
     * Returns the display label for the tab at {@code tabIndex}, preferring the label from
     * the resolved per-batchCode navigation over the master-config label.
     */
    private String getResolvedLabel(int tabIndex, String lang) {
        // 1. Per-batchCode label from the referenced remote config key
        if (resolvedTabNavigations != null && tabIndex < resolvedTabNavigations.size()) {
            BottomNavTabNavigation nav = resolvedTabNavigations.get(tabIndex);
            if (nav != null && nav.getLabel() != null) {
                String label = nav.getLabel().get(lang);
                if (StringUtils.isNotEmpty(label)) return label;
                label = nav.getLabel().get("en");
                if (StringUtils.isNotEmpty(label)) return label;
            }
        }
        // 2. Default label from master config
        if (nonCompetitiveTabConfigs != null && tabIndex < nonCompetitiveTabConfigs.size()) {
            BottomNavTabConfig tabConfig = nonCompetitiveTabConfigs.get(tabIndex);
            if (tabConfig != null && tabConfig.getLabel() != null) {
                String label = tabConfig.getLabel().get(lang);
                if (StringUtils.isNotEmpty(label)) return label;
                String enLabel = tabConfig.getLabel().get("en");
                return enLabel != null ? enLabel : "";
            }
        }
        return "";
    }

    /**
     * Applies labels, icons, and badge state to all 5 tabs for non-competitive users.
     * Falls back to {@link #applyUnsubscribedUI()} when config is unavailable.
     */
    private void applyNonCompetitiveUI() {
        clearCustomCenterTab();
        chatMenuItemId = -1;
        if (nonCompetitiveTabConfigs == null) {
            applyUnsubscribedUI();
            return;
        }

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (StringUtils.isEmpty(lang)) lang = "en";

        int menuSize = bottomNav.getMenu().size();
        for (int i = 0; i < Math.min(nonCompetitiveTabConfigs.size(), menuSize); i++) {
            BottomNavTabConfig tabConfig = nonCompetitiveTabConfigs.get(i);
            if (tabConfig == null) continue;

            // Apply resolved label
            String labelText = getResolvedLabel(i, lang);
            if (StringUtils.isNotEmpty(labelText)) {
                bottomNav.getMenu().getItem(i).setTitle(labelText);
            }

            // Apply icon from tabType
            BottomNavTabType tabType = BottomNavTabType.fromString(tabConfig.getTabType());
            if (tabType != null) {
                bottomNav.getMenu().getItem(i).setIcon(tabType.getDrawableRes());
            }

            // Chat tab — red badge with white text
            if (tabType == BottomNavTabType.CHAT) {
                chatMenuItemId = bottomNav.getMenu().getItem(i).getItemId();
//                applyChatBadge(chatMenuItemId, "20+");
                continue;
            }

            // ── Center tab: local-drawable overlay when resolved tabType demands it ──
            // (e.g. REFER_NOTES or SUPER_COIN — set per-batchCode via remoteConfigKey)
            if (i == 2) {
                BottomNavTabNavigation resolvedNav = (resolvedTabNavigations != null
                        && resolvedTabNavigations.size() > 2)
                        ? resolvedTabNavigations.get(2) : null;
                BottomNavTabType resolvedType = resolvedNav != null
                        ? BottomNavTabType.fromString(resolvedNav.getTabType()) : null;
                if (resolvedType == BottomNavTabType.REFER_NOTES
                        || resolvedType == BottomNavTabType.SUPER_COIN) {
                    // Badge drawn by setupCenterTabLocalDrawable (not BadgeDrawable — z-order fix)
                    bottomNav.removeBadge(R.id.item_analytics);
                    Map<String, String> badgeMap = tabConfig.getBadge();
                    String badgeText = badgeMap != null
                            ? (badgeMap.containsKey(lang)
                            ? badgeMap.get(lang)
                            : badgeMap.getOrDefault(ArivihanLanguageEnum.ENGLISH.getCode(), ""))
                            : "";
                    int badgeBgColor = parseBadgeColor(tabConfig.getBadgeColor(),
                            android.graphics.Color.parseColor("#4BB543"));
                    int badgeTextColor = parseBadgeColor(tabConfig.getBadgeTextColor(),
                            ContextCompat.getColor(activity, android.R.color.white));
                    setupCenterTabLocalDrawable(resolvedType.getDrawableRes(),
                            getCenterTabIconUrl(resolvedNav),
                            badgeText, badgeBgColor, badgeTextColor);
                    continue;
                }
            }

            // Badge and custom overlay — driven by action, independent of tabType
            String action = tabConfig.getAction() != null ? tabConfig.getAction() : "";
            if ("SUBSCRIPTION".equals(action)) {
                Map<String, String> badge = tabConfig.getBadge();
                String badgeText = (badge != null)
                        ? (badge.containsKey(lang) ? badge.get(lang) : badge.getOrDefault(ArivihanLanguageEnum.ENGLISH.getCode(), activity.getString(R.string.New)))
                        : activity.getString(R.string.New);
                BadgeDrawable badgeDrawable = bottomNav.getOrCreateBadge(bottomNav.getMenu().getItem(i).getItemId());
                badgeDrawable.setVisible(true);
                badgeDrawable.setText(badgeText);
                badgeDrawable.setBackgroundColor(parseBadgeColor(tabConfig.getBadgeColor(),
                        ContextCompat.getColor(activity, R.color.streak_yellow)));
                badgeDrawable.setBadgeTextColor(parseBadgeColor(tabConfig.getBadgeTextColor(),
                        ContextCompat.getColor(activity, R.color.black)));
                if (i == 2) {
                    String centerImageUrl = getCenterTabImageUrl();
                    if (centerImageUrl != null) setupCustomCenterTab(centerImageUrl);
                }
            } else {
                bottomNav.removeBadge(bottomNav.getMenu().getItem(i).getItemId());
            }
        }
    }

    /**
     * Handles a tab click for non-competitive users using the pre-resolved tab config.
     * Dispatches based on {@link BottomNavTabConfig#getAction()}.
     */
    private void handleTabNavigate(int tabIndex) {
        Log.d(TAG, "handleTabNavigate: called with tabIndex=" + tabIndex);

        if (nonCompetitiveTabConfigs == null || tabIndex >= nonCompetitiveTabConfigs.size()) {
            Log.w(TAG, "handleTabNavigate: aborting — nonCompetitiveTabConfigs is " +
                    (nonCompetitiveTabConfigs == null ? "null" : "size=" + nonCompetitiveTabConfigs.size() + ", tabIndex=" + tabIndex));
            return;
        }

        BottomNavTabConfig tabConfig = nonCompetitiveTabConfigs.get(tabIndex);
        if (tabConfig == null) {
            Log.w(TAG, "handleTabNavigate: aborting — tabConfig is null at tabIndex=" + tabIndex);
            return;
        }

        String action = tabConfig.getAction() != null ? tabConfig.getAction() : "NAVIGATE";
        Log.d(TAG, "handleTabNavigate: action=" + action);

        switch (action) {
            case "HOME":
                Log.d(TAG, "handleTabNavigate: navigating to Dashboard");
                navigateToDashboard(null);
                break;
            case "CHAT":
                Log.d(TAG, "handleTabNavigate: navigating to VirtualChat");
                navigateToVirtualChat();
                break;
            case "SUBSCRIPTION":
                Log.d(TAG, "handleTabNavigate: navigating to Subscription");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.board_subscription_bottom_icon_clicked.getActionName());
                Intent subIntent = SubscriptionNavigationHelper.getSubscriptionPageIntent(activity, RedirectionSource.BOTTOM_NAV);
                activity.startActivity(subIntent);
                break;
            case "REELS":
                Log.d(TAG, "handleTabNavigate: navigating to Reels");
                activity.startActivity(new Intent(activity, ReelsActivity.class));
                break;
            case "PROFILE":
                Log.d(TAG, "handleTabNavigate: navigating to Profile");
                navigateToProfile(null);
                break;
            case "NAVIGATE":
            default:
                Log.d(TAG, "handleTabNavigate: resolving nav — resolvedTabNavigations=" +
                        (resolvedTabNavigations != null ? "size=" + resolvedTabNavigations.size() : "null"));

                BottomNavTabNavigation nav = (resolvedTabNavigations != null && tabIndex < resolvedTabNavigations.size())
                        ? resolvedTabNavigations.get(tabIndex)
                        : tabConfig.getDefaultNavigation();

                if (nav == null) {
                    Log.w(TAG, "handleTabNavigate: nav is null for tabIndex=" + tabIndex + " — no navigation performed");
                    break;
                }

                Log.d(TAG, "handleTabNavigate: nav resolved — navigationType=" + nav.getNavigationType()
                        + ", className=" + nav.getClassName()
                        + ", params=" + nav.getParams());

                if (StringUtils.isEmpty(nav.getNavigationType())) {
                    Log.w(TAG, "handleTabNavigate: navigationType is empty — no navigation performed");
                    break;
                }

                Log.d(TAG, "handleTabNavigate: calling navigateFromActivityConfig");
                CommonMethods.navigateFromActivityConfig(activity, nav.getNavigationType(), nav.getClassName(), nav.getParams());
                break;
        }
    }

    private void navigateToVirtualChat() {
        Log.d(TAG, "navigateToVirtualChat: called");
        boolean hasSubscription = SubscriptionType.hasActiveSubscription();
        Log.d(TAG, "navigateToVirtualChat: hasActiveSubscription=" + hasSubscription);
        if (hasSubscription) {
            Log.d(TAG, "navigateToVirtualChat: user has active subscription, navigating to virtual chat");
            CommonNavigator.navigateToVirtualChat(activity);
        } else {
            Log.w(TAG, "navigateToVirtualChat: no active subscription, redirecting to subscription page with popupType=" + SubscriptionPopupType.CHAT.name());
            Intent chatSubIntent = SubscriptionNavigationHelper.getSubscriptionPageIntent(activity, SubscriptionPopupType.CHAT.name(), RedirectionSource.VIRTUAL_CHAT);
            activity.startActivity(chatSubIntent);
        }
    }


    // ─────────────────────────────────────────────
    // Private — UI configuration (analytics tab icon/badge/title — competitive / legacy)
    // ─────────────────────────────────────────────

    private void applySubscribedUI() {
        clearCustomCenterTab();
        applyCompetitiveTabLabels();

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (StringUtils.isEmpty(lang)) lang = "en";

        CompetitiveBottomNavTab centerTab = getCompetitiveCenterTab();

        // Icon — prefer subscribedTabType when set, fall back to tabType, then default
        int iconRes = R.drawable.bottom_nav_competitive_subscription_icon_selection;
        if (centerTab != null) {
            String iconTypeName = StringUtils.isNotEmpty(centerTab.getSubscribedTabType())
                    ? centerTab.getSubscribedTabType()
                    : centerTab.getTabType();
            BottomNavTabType tabType = BottomNavTabType.fromString(iconTypeName);
            if (tabType != null) iconRes = tabType.getDrawableRes();
        }
        bottomNav.getMenu().getItem(2).setIcon(iconRes);

        // Label — from config label, fallback to string resource
        String label = activity.getString(R.string.lectures);
        if (centerTab != null && centerTab.getLabel() != null) {
            String l = centerTab.getLabel().get(lang);
            if (StringUtils.isEmpty(l)) l = centerTab.getLabel().get("en");
            if (StringUtils.isNotEmpty(l)) label = l;
        }
        bottomNav.getMenu().getItem(2).setTitle(label);
        bottomNav.removeBadge(R.id.item_analytics);
    }

    private void applyUnsubscribedUI() {
        if (MyApplication.isCompetitiveProfile()) {
            applyCompetitiveTabLabels();

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            CompetitiveBottomNavTab centerTab = getCompetitiveCenterTab();

            // Icon — from tabType in config, fallback to default competitive icon
            int iconRes = R.drawable.bottom_nav_competitive_subscription_icon_selection;
            if (centerTab != null) {
                BottomNavTabType tabType = BottomNavTabType.fromString(centerTab.getTabType());
                if (tabType != null) iconRes = tabType.getDrawableRes();
            }
            bottomNav.getMenu().getItem(2).setIcon(iconRes);

            // Unsubscribed label — from unsubscribedLabel in config, fallback to string resource
            String centerLabel = activity.getString(R.string.dr_batch);
            if (centerTab != null && centerTab.getUnsubscribedLabel() != null) {
                String l = centerTab.getUnsubscribedLabel().get(lang);
                if (StringUtils.isEmpty(l)) l = centerTab.getUnsubscribedLabel().get("en");
                if (StringUtils.isNotEmpty(l)) centerLabel = l;
            }

            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.item_analytics);
            badge.setVisible(true);
            badge.setText(activity.getString(R.string.New));
            badge.setBackgroundColor(ContextCompat.getColor(activity, R.color.streak_yellow));
            badge.setBadgeTextColor(ContextCompat.getColor(activity, R.color.black));
            bottomNav.getMenu().getItem(2).setTitle(centerLabel);
        } else {
            String badgeText = getUnnatiBadgeText();
            String defaultName = activity.getString(R.string.Unnati);

            bottomNav.getMenu().getItem(2).setIcon(R.drawable.bottom_nav_trophy_icon_selection);

            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.item_analytics);
            badge.setVisible(true);
            badge.setText(activity.getString(R.string.New));
            badge.setBackgroundColor(ContextCompat.getColor(activity, R.color.streak_yellow));
            badge.setBadgeTextColor(ContextCompat.getColor(activity, R.color.black));
            bottomNav.getMenu().getItem(2).setTitle(
                    StringUtils.isNotEmpty(badgeText) ? badgeText : defaultName);

            String centerImageUrl = getCenterTabImageUrl();
            if (centerImageUrl != null) setupCustomCenterTab(centerImageUrl);
        }
    }

    private String getUnnatiBadgeText() {
        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig()
                    .getString("home_screen_subscription_icon_text_v2");

            if (StringUtils.isEmpty(jsonString)) return "";

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if (StringUtils.isEmpty(batchCode)) return "";

            JSONObject root = new JSONObject(jsonString);
            return root.optString(batchCode, "").trim();

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    // ─────────────────────────────────────────────
    // Private — click listener setup
    // ─────────────────────────────────────────────

    private void setupListener() {
        bottomNav.setOnItemSelectedListener(item -> {
            String clickedName = item.getTitle().toString();
            logNavClick(clickedName);
            Log.d(TAG, "tab clicked: " + clickedName);

            if (isNavigating) return false;
            isNavigating = true;

            boolean isNavSelected = false;

            int itemId = item.getItemId();
            if (itemId == R.id.item_explore) {
                handleExploreTab();
            } else if (itemId == R.id.item_learn) {
                isNavSelected = true;
                handleLearnTab();
            } else if (itemId == R.id.item_reel) {
                handleReelTab();
            } else if (itemId == R.id.item_home) {
                isNavSelected = true;
                handleHomeTab();
            } else if (itemId == R.id.item_analytics) {
                handleAnalyticsTab();
            } else if (itemId == R.id.item_profile) {
                isNavSelected = true;
                handleProfileTab();
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> isNavigating = false, 500);
            return isNavSelected;
        });
    }

    // ─────────────────────────────────────────────
    // Private — tab click handlers
    // ─────────────────────────────────────────────

    private void handleExploreTab() {
        newUser = false;
        delegate.destroyHowTo();
        delegate.removeHowTo();
        // item_explore only exists in the non-competitive menu; drive entirely from config
        if (nonCompetitiveTabConfigs != null) {
            handleTabNavigate(1);
        }
    }

    private void handleLearnTab() {
        newUser = false;
        delegate.destroyHowTo();
        delegate.removeHowTo();
        navigateToLearn(null);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.clicked_learn_bottom_navigation.getActionName());
    }

    private void handleReelTab() {
        newUser = false;
        delegate.destroyHowTo();
        activity.startActivity(new Intent(activity, ReelsActivity.class));
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.clicked_reels_bottom_navigation.getActionName());
        delegate.removeHowTo();
    }

    private void handleHomeTab() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("NewUser", newUser);
        bundle.putBoolean("is_user_onboarded", isUserOnboarded);
        MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), true);
        navigateToDashboard(bundle);
        MyApplication.logFirebaseBundleCreation(MyApplication.isCompetitiveProfile()
                ? FirebaseEvents.clicked_competitive_home_bottom_navigation.getActionName()
                : FirebaseEvents.clicked_home_bottom_navigation.getActionName());
    }

    private void handleAnalyticsTab() {
        Log.d(TAG, "handleAnalyticsTab: competitiveBatch=" + MyApplication.isCompetitiveProfile()
                + " subscriptionType=" + userSubscriptionType);
        delegate.removeHowTo();

        if (!MyApplication.isCompetitiveProfile()) {
            // Non-competitive: entirely driven by remote config
            delegate.destroyHowTo();
            if (nonCompetitiveTabConfigs != null) {
                newUser = false;
                handleTabNavigate(2);
            } else {
                // Config unavailable — fallback: open subscription screen
                Intent fallbackIntent = SubscriptionNavigationHelper.getSubscriptionPageIntent(activity, RedirectionSource.BOTTOM_ANALYTICS_TAB);
                activity.startActivity(fallbackIntent);
            }
            return;
        }

        // Competitive batch: subscription check via BASIC / PLUS / PRO
        if (SubscriptionType.hasActiveSubscription()) {
            newUser = false;
            delegate.destroyHowTo();
            CompetitiveBottomNavTab centerTab = getCompetitiveCenterTab();
            if (centerTab != null && centerTab.getSubscribedNavigation() != null) {
                BottomNavTabNavigation nav = centerTab.getSubscribedNavigation();
                CommonMethods.navigateFromActivityConfig(
                        activity, nav.getNavigationType(), nav.getClassName(), nav.getParams());
            } else {
                // fallback — legacy launch using examId / examName stored by HomeScreen
                Intent intent = new Intent(activity, HalfYearlyActivity.class);
                intent.putExtra("examId", examId);
                intent.putExtra("examName", examName);
                activity.startActivity(intent);
            }
        } else {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.neet_subscription_bottom_nav_click.getActionName());
            delegate.destroyHowTo();
            Intent compIntent = SubscriptionNavigationHelper.getSubscriptionPageIntent(activity, RedirectionSource.NEET_BOTTOM_NAV);
            activity.startActivity(compIntent);
        }
    }

    private void handleProfileTab() {
        newUser = false;
        navigateToProfile(null);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_profile_bottom_navigation.getActionName());
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PROFILE_SCREEN.name(), AnalyticsEvent.PROFILE_SCREEN.name());
        delegate.removeHowTo();
    }

    // ─────────────────────────────────────────────
    // Private — fragment navigation helpers
    // ─────────────────────────────────────────────

    public void navigateToDashboard(Bundle bundle) {
        if (bundle == null) bundle = new Bundle();
        if (!MyApplication.isCompetitiveProfile()) {
            MainDashboard fragment = new MainDashboard();
            fragment.setArguments(bundle);
            delegate.setFragment(fragment);
        } else {
            CompetitiveHomeScreenFragment fragment = new CompetitiveHomeScreenFragment();
            fragment.setArguments(bundle);
            delegate.setFragment(fragment);
        }
    }

    private void navigateToProfile(Bundle bundle) {
        if (bundle == null) bundle = new Bundle();
        bundle.putBoolean(ProfileFragment.bundleTodoVisibility, delegate.isTodoVisible());
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);
        delegate.setFragment(fragment);
    }

    private void navigateToLearn(Bundle bundle) {
        if (bundle == null) bundle = new Bundle();
        CompetitiveLearnFragment fragment = new CompetitiveLearnFragment();
        fragment.setArguments(bundle);
        delegate.setFragment(fragment);
    }

    // ─────────────────────────────────────────────
    // Private — remote-config navigation
    // ─────────────────────────────────────────────

    private String getNavigationConfig() {
        return MyApplication.getFirebaseRemoteConfig().getString("bottom_navigation_batch_config");
    }

    private boolean handleNavigationFromConfig(String remoteJson, String language, String navKey) {
        try {
            JSONObject jsonObject = new JSONObject(remoteJson);
            if (!jsonObject.has(language)) return false;

            JSONObject langObject = jsonObject.getJSONObject(language);
            if (!langObject.has(navKey)) return false;

            JSONObject navObject = langObject.getJSONObject(navKey);
            NavigationConfig config = new Gson().fromJson(navObject.toString(), NavigationConfig.class);
            CommonMethods.navigateFromActivityConfig(activity, config);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openHalfYearly() {
        delegate.destroyHowTo();
        Intent intent = new Intent(activity, HalfYearlyActivity.class);
        intent.putExtra("examId", examId);
        intent.putExtra("examName", examName);
        activity.startActivity(intent);
    }

    // ─────────────────────────────────────────────
    // Private — utilities
    // ─────────────────────────────────────────────

    private void applyChatBadge(int menuItemId, String text) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setVisible(true);
        badge.setText(text);
        badge.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
        badge.setBadgeTextColor(ContextCompat.getColor(activity, android.R.color.white));
    }

    /**
     * Returns {@code true} only when {@code text} is non-null, non-empty, and represents
     * a positive number.  Handles null, "", "0", and any negative value → hide badge.
     */
    private boolean shouldShowChatBadge(String text) {
        if (StringUtils.isEmpty(text)) return false;
        try {
            return Long.parseLong(text.trim()) > 0;
        } catch (NumberFormatException e) {
            // Non-numeric text (e.g. "20+") — show it as-is
            return true;
        }
    }


    private void checkAndSetChecked(int menuItemId) {
        MenuItem menuItem = bottomNav.getMenu().findItem(menuItemId);
        if (menuItem != null) menuItem.setChecked(true);
    }

    private void logNavClick(String tab) {
        Bundle bundle = new Bundle();
        bundle.putString("tab", tab);
        MyApplication.logFirebaseEventOffMainThread("BottomNavigation_Clicked", bundle);
    }

    private void logMenuState() {
        android.view.Menu menu = bottomNav.getMenu();
        Log.d(TAG, "Total menu items: " + menu.size());
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Log.d(TAG, "BEFORE - Item[" + i + "]: ID=" + item.getItemId()
                    + ", Title=" + item.getTitle()
                    + ", ResourceName=" + activity.getResources().getResourceEntryName(item.getItemId()));
        }
    }

    // ─────────────────────────────────────────────
    // Custom center-tab overlay (non-subscribed standard nav)
    // ─────────────────────────────────────────────

    /**
     * Places a local drawable resource as the center-tab icon, with an optional text badge
     * drawn on top (correct z-order — badge is always above the image).
     *
     * <p>Uses a small {@link android.widget.FrameLayout} container so the badge can be
     * positioned at {@code Gravity.TOP|END} relative to the image without fighting the
     * {@code BadgeDrawable} z-ordering issue.
     *
     * @param drawableRes   local drawable resource used as placeholder / fallback
     *                      (e.g. {@code R.drawable.img_refer_notes})
     * @param iconUrl       remote icon URL from {@link #getCenterTabIconUrl}; when empty the
     *                      {@code drawableRes} is shown directly
     * @param badgeText     badge label; pass empty/null to omit the badge
     * @param badgeBgColor  badge background colour (ARGB int)
     * @param badgeTextColor badge text colour (ARGB int)
     */
    private void setupCenterTabLocalDrawable(int drawableRes,
                                             String iconUrl,
                                             String badgeText,
                                             int badgeBgColor,
                                             int badgeTextColor) {
        try {
            ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
            if (menuView == null || menuView.getChildCount() < 3) return;

            ViewGroup centerItem = (ViewGroup) menuView.getChildAt(2);

            // Hide the native icon so our overlay is the only visible icon
            View nativeIcon = centerItem.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (nativeIcon != null) nativeIcon.setVisibility(View.INVISIBLE);

            // Allow views to overflow beyond nav bar bounds if needed
            bottomNav.setClipChildren(false);
            bottomNav.setClipToPadding(false);
            menuView.setClipChildren(false);
            menuView.setClipToPadding(false);
            centerItem.setClipChildren(false);
            centerItem.setClipToPadding(false);
            View root = activity.findViewById(android.R.id.content);
            if (root instanceof ViewGroup) {
                ((ViewGroup) root).setClipChildren(false);
                ((ViewGroup) root).setClipToPadding(false);
            }

            int imageSize = dpToPx(32);

            // ── Container holds image + badge at correct z-order ─────────────
            centerTabContainer = new android.widget.FrameLayout(activity);

            centerTabImage = new ImageView(activity);
            centerTabImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (StringUtils.isNotEmpty(iconUrl)) {
                // Bundled drawable stays as placeholder + error fallback so the tab is never empty
                Glide.with(activity)
                        .load(iconUrl)
                        .placeholder(drawableRes)
                        .error(drawableRes)
                        .into(centerTabImage);
            } else {
                centerTabImage.setImageResource(drawableRes);
            }

            FrameLayout.LayoutParams imgParams =
                    new FrameLayout.LayoutParams(imageSize, imageSize);
            imgParams.gravity = Gravity.CENTER;
            centerTabContainer.addView(centerTabImage, imgParams);

            // Badge — added AFTER image so it draws on top of it
            if (StringUtils.isNotEmpty(badgeText)) {
                centerTabBadge = new android.widget.TextView(activity);
                centerTabBadge.setText(badgeText);
                centerTabBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                centerTabBadge.setTypeface(centerTabBadge.getTypeface(),
                        android.graphics.Typeface.BOLD);
                centerTabBadge.setTextColor(badgeTextColor);
                centerTabBadge.setPadding(dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));

                android.graphics.drawable.GradientDrawable badgeBg =
                        new android.graphics.drawable.GradientDrawable();
                badgeBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                badgeBg.setCornerRadius(dpToPx(2));
                badgeBg.setColor(badgeBgColor);
                centerTabBadge.setBackground(badgeBg);

                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                badgeParams.gravity = Gravity.TOP | Gravity.END;
                badgeParams.rightMargin = -dpToPx(4); // bleed slightly outside the right edge
                badgeParams.topMargin = dpToPx(3);    // small offset below the container top
                centerTabContainer.addView(centerTabBadge, badgeParams);
            }

            // Container is slightly larger than image to give badge room to bleed out
            int containerSize = imageSize + dpToPx(14);
            FrameLayout.LayoutParams containerParams =
                    new FrameLayout.LayoutParams(containerSize, containerSize);
            containerParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            containerParams.topMargin = dpToPx(1); // aligns with native icon area
            centerItem.addView(centerTabContainer, containerParams);

        } catch (Exception e) {
            Log.e(TAG, "setupCenterTabLocalDrawable failed: " + e.getMessage());
        }
    }

    /**
     * Parses {@code hex} as an ARGB colour. Returns {@code fallback} when the string is
     * blank or not a valid hex colour (e.g. when the field is absent from remote config).
     */
    private int parseBadgeColor(String hex, int fallback) {
        if (StringUtils.isNotEmpty(hex)) {
            try {
                return android.graphics.Color.parseColor(hex);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return fallback;
    }

    /**
     * Returns the {@code center_icon_url} of the resolved {@code board_subscribed_center_tab_config}
     * entry, or {@code ""} when it is absent — in which case the caller falls back to
     * {@link BottomNavTabType#getDrawableRes()}.
     */
    private String getCenterTabIconUrl(BottomNavTabNavigation tabNavigation) {
        return (tabNavigation != null && StringUtils.isNotEmpty(tabNavigation.getCenterIconUrl()))
                ? tabNavigation.getCenterIconUrl()
                : "";
    }

    /**
     * Returns the image URL to use for the custom center tab overlay, or {@code null} if the
     * current batchCode is excluded and the regular native icon should be shown instead.
     * <p>
     * Reads {@code bottom_nav_center_icon_config} from Firebase Remote Config:
     * <pre>
     * {
     *   "imageUrl": "https://...",          // URL of the center icon image
     *   "excludedBatchCodes": ["B1","B2"]   // batch codes that show the regular native icon
     * }
     * </pre>
     * Returns an empty string (use drawable fallback) when the key is missing or imageUrl is empty.
     */
    private String getCenterTabImageUrl() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig()
                    .getString("bottom_nav_center_icon_config");
            if (StringUtils.isEmpty(json)) return "";

            org.json.JSONObject obj = new org.json.JSONObject(json);
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());

            // Check excluded batchCodes
            if (StringUtils.isNotEmpty(batchCode) && obj.has("excludedBatchCodes")) {
                org.json.JSONArray excluded = obj.getJSONArray("excludedBatchCodes");
                for (int i = 0; i < excluded.length(); i++) {
                    if (batchCode.equals(excluded.getString(i))) {
                        return null; // use regular native icon for this batchCode
                    }
                }
            }

            return obj.optString("imageUrl", "");
        } catch (Exception e) {
            Log.e(TAG, "getCenterTabImageUrl: " + e.getMessage());
            return "";
        }
    }

    private void setupCustomCenterTab(String imageUrl) {
        try {
            // Guard — only run once; skip if already set up
            if (centerTabImage != null) return;

            ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
            if (menuView == null || menuView.getChildCount() < 3) return;

            ViewGroup centerItem = (ViewGroup) menuView.getChildAt(2);

            // ── 1. Hide the native icon ──────────────────────────────────────
            View nativeIcon = centerItem.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (nativeIcon != null) nativeIcon.setVisibility(View.INVISIBLE);

            // ── 2. Allow views to overflow upward beyond the nav bar bounds ──
            bottomNav.setClipChildren(false);
            bottomNav.setClipToPadding(false);
            menuView.setClipChildren(false);
            menuView.setClipToPadding(false);
            centerItem.setClipChildren(false);
            centerItem.setClipToPadding(false);
            // Also disable clipping on the activity's root so the image clears the nav bar
            View root = activity.findViewById(android.R.id.content);
            if (root instanceof ViewGroup) {
                ((ViewGroup) root).setClipChildren(false);
                ((ViewGroup) root).setClipToPadding(false);
            }

            // ── 3. Custom image (larger than the standard 21 dp icons) ───────
            centerTabImage = new ImageView(activity);
            if (StringUtils.isNotEmpty(imageUrl)) {
                Glide.with(activity).load(imageUrl).into(centerTabImage);
            } else {
                centerTabImage.setImageResource(R.drawable.img_bottom_nav_batch_51);
            }
            centerTabImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

            int imageSize = dpToPx(64);          // bigger than other 21 dp icons
            int imageRiseAboveBar = dpToPx(10);  // how far it floats above the nav bar top edge

            FrameLayout.LayoutParams imageParams =
                    new FrameLayout.LayoutParams(imageSize, imageSize);
            imageParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            imageParams.topMargin = -imageRiseAboveBar; // negative → extends upward
            centerItem.addView(centerTabImage, imageParams);

            // ── 4. Lottie animation on top of the image ───────────────────────
            centerTabLottie = new LottieAnimationView(activity);
            centerTabLottie.setAnimation(R.raw.anim_lottie_bottom_nav_fire_cracker); // ← swap animation here
            centerTabLottie.setRepeatCount(LottieDrawable.INFINITE);
            centerTabLottie.setRepeatMode(LottieDrawable.RESTART);

            int lottieSize = dpToPx(56); // larger than image to give it room to animate
            FrameLayout.LayoutParams lottieParams =
                    new FrameLayout.LayoutParams(lottieSize, lottieSize);
            lottieParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            lottieParams.topMargin = -(imageRiseAboveBar + dpToPx(-4)); // sit above the image
            centerItem.addView(centerTabLottie, lottieParams);

            centerTabLottie.playAnimation();

        } catch (Exception e) {
            Log.e(TAG, "setupCustomCenterTab failed: " + e.getMessage());
        }
    }

    /**
     * Removes the custom center-tab overlay and restores the native icon.
     * Called when the user subscribes (transitions to subscribed UI).
     */
    private void clearCustomCenterTab() {
        try {
            if (centerTabImage == null && centerTabLottie == null && centerTabContainer == null) return;

            ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
            if (menuView == null || menuView.getChildCount() < 3) return;

            ViewGroup centerItem = (ViewGroup) menuView.getChildAt(2);

            // Stop and remove Lottie
            if (centerTabLottie != null) {
                centerTabLottie.cancelAnimation();
                centerItem.removeView(centerTabLottie);
                centerTabLottie = null;
            }

            // Remove standalone image (setupCustomCenterTab path)
            if (centerTabImage != null) {
                centerItem.removeView(centerTabImage);
                centerTabImage = null;
            }

            // Remove container + badge (setupCenterTabLocalDrawable path)
            if (centerTabContainer != null) {
                centerItem.removeView(centerTabContainer);
                centerTabContainer = null;
                centerTabBadge = null;
            }

            // Restore native icon
            View nativeIcon = centerItem.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (nativeIcon != null) nativeIcon.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "clearCustomCenterTab failed: " + e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                activity.getResources().getDisplayMetrics());
    }
}
