package arivihan.technologies.doubtbuzzter2.enums;

import java.io.Serializable;

/**
 * RedirectionSource enum used for tracking the source of navigation to subscription screens.
 */
public enum RedirectionSource implements Serializable {
    HOME("HomeScreen"),
    HOME_SCREEN_JOIN_NOW("maindashboard_join_now_btn"),
    CHAPTER_LIST_JOIN("chapter_list_join_click"),
    MICRO_LECTURE_JOIN("micro_lecture_join_click"),
    DOUBT_CHAT("doubt_chat_screen"),
    DOUBT_CHAT_JOIN("doubt_chat_join_click"),
    PROFILE_JOIN("profile_subscription_join_click"),
    LOCK_POPUP_JOIN("subscription_lock_popup_join_click"),
    LIMIT_EXCEED("limit_exceed_popup"),
    CHAT_SUPPORT("chat_support_sctivity"),
    MY_SUBSCRIPTIONS_JOIN("my_subscriptions_join_click"),
    DEEPLINK("deeplink"),
    TEST_SERIES("test_series_list_activity"),
    TEST_SERIES_JOIN("test_series_join_click"),
    MOCK_TEST_JOIN("mock_test_join_click"),
    TOPPERS_NOTES_JOIN("toppers_notes_join_click"),
    TOPPERS_KE_NOTES_JOIN("toppers_ke_notes_join_click"),
    VIDEO_PLAYER("video_player_activity"),
    BATCH_OVERVIEW("batch_overview_join_click"),
    JOIN_51_CLICK("join_51_click"),
    UNKNOWN("unknown"),
    NEET_HOME_JOIN_NOW("competitive_hs_fragment_join_now_btn"),
    BOTTOM_NAV("bottom_navigation_tab"),
    HOME_LOCKED_POPUP("home_locked_popup"),
    CHAPTER_LIST_LOCKED_POPUP("chapter_list_locked_popup"),
    MICRO_LECTURE_LOCKED_POPUP("micro_lecture_locked_popup"),
    ASK_DOUBT_LOCKED_POPUP("ask_doubt_locked_popup"),
    OFFLINE_DOWNLOADS_LOCKED_POPUP("offline_downloads_locked_popup"),
    RENEW_REMINDER("renew_reminder_popup"),
    REFERRAL_REDIRECT("referral_redirect"),
    TIMETABLE_LOCKED_POPUP("timetable_locked_popup"),
    LIVE_LOCKED_POPUP("live_locked_popup"),
    CAROUSEL("hs_carousel_click"),
    ASAT("asat_activity"),
    VIRTUAL_CHAT("virtual_chat_activity"),
    BOTTOM_ANALYTICS_TAB("analytics_tab"),
    NEET_BOTTOM_NAV("neet_bottom_nav"),
    FLASH_CARD("flash_card"),
    NOTIFICATION("notification"),
    WEBVIEW("webview"),
    NEET_HOME_SCREEN("neet_home_screen_fragment"),
    WEBVIEW_BY_URL_INTERFACE("webview_by_url_interface"),
    WEBVIEW_DOUBT_CHAT("webview_doubt_chat"),
    WEBVIEW_NEET_WEAK_TOPIC("webview_neet_weak_topic"),
    MICROLECTURE_PRACTISE_WEBVIEW_INTERFACE("microlecture_practise_webview_interface"),
    ;

    private final String firebaseName;

    RedirectionSource(String firebaseName) {
        this.firebaseName = firebaseName;
    }

    public String getFirebaseName() {
        return firebaseName;
    }

    /**
     * Returns the formatted string value to be sent in analytics.
     * Format: REDIRECTED_FROM_(SOURCE_NAME)
     *
     * @return formatted redirection source string
     */
    public String getFormattedValue() {
        return "REDIRECTED_FROM_" + this.name();
    }
}
