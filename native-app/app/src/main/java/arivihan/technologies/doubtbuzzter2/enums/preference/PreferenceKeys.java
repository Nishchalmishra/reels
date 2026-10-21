package arivihan.technologies.doubtbuzzter2.enums.preference;

public enum PreferenceKeys {
    USER_ONBOARDING_LOGIN_PHONE_NUMBER("user_onboarding_login_phone_number"),
    USER_LOCATION_DENIAL_COUNT("user_location_denial_count"),
    ADAPTIVE_SELECTED_CHAPTER_ID("neet_adaptive_selected_chapter_id"),
    ADAPTIVE_SELECTED_TOPIC_ID("neet_adaptive_selected_topic_id"),
    ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE("neet_adaptive_chapter_completion_screen_visible"),
    USER_LOGGED_IN("user_logged_in"),
    NEET_WEAK_TOPIC_SELECTION_VIDEO_COUNT("neet_week_topic_selection_video_count"),
    COMPLETED_LECTURES("completed_lectures"),
    AUTHENTICATION_DONE("authentication_done"),
    PENDING_DEEP_LINK("pending_deep_link"),
    ALL_UTM_PARAMS("all_utm_params"),
    USER_NAME_ENTERED_DURING_ONBOARDING("user_name_entered_during_onboarding"),
    USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING("user_phone_number_entered_during_onboarding"),
    DO_NOT_DELETE_SELECTED_COURSE_MODEL("selected_course_model"),
    ONBOARDING_COURSE_SELECTION_LIST("onboarding_course_selection_list"),
    ONBOARDING_USER_STATE_V2("onboarding_user_state_v2"),
    USER_ID("userId"),
    ON_BOARDING_NUMBER("onboardingNumber"),
    LAUNCH_COUNT("launchCount"),
    APP_OPENED_EVENT_SENT("app_opened_event_sent"),
    APP_OPENED_EVENT_SENT_VERSION("app_opened_event_sent_version"),
    EXAM_SELECTED_PENDING("exam_selected_pending"),
    ON_BOARDING_COMPLETED("OnboardingCompleted"),
    NAME("name"),
    IS_NAME_UPDATED("isNameUpdated"),
    SELECTED_COURSE_ID("selected_course_id"),

    // Batch related changes
    BATCH_ID("batch_id"),
    BATCH_CODE("batch_code"),
    BATCH_NAME("batch_name"),
    BATCH_DISPLAY_NAME("batch_display_ame"),
    EXAM_CODE("exam_code"),
    EXAM_ID("exam_id"),
    STREAM_CODE("stream_code"),
    STREAM_ID("stream_id"),
    GRADE_CODE("grade_code"),
    GRADE_ID("grade_id"),
    YEAR_CODE("year_code"),
    YEAR_ID("year_id"),
    YEAR_NAME("year_name"),
    GRADE_NAME("grade_name"),
    STREAM_NAME("stream_name"),
//    EXAM_NAME("exam_name"),

    // User profile data
    USER_BATCH_LIST("user_batch_list"),
    IS_NEW_USER("isNewUser"),
    USER_ADDRESS("userAddress"),
    EXAM_TYPE("examType"),
    EXAM_NAME("examName"),
    DISPLAY_NAME("displayName"),
    COURSE_ID("courseId"),
    SECONDARY_COURSE_ID("secondaryCourseId"),
    SELECTED_COURSE_ID_LOCAL("selectedCourseId"),
    COACHING_ID("coachingId"),
    USER_EMAIL("userEmail"),
    COURSE_NAME("course"),
    CLASS_NAME("class"),
    BOARD_CODE("boardCode"),
    CLASS_ID("classId"),
    YOUR_POINTS("yourPoints"),
    FREE_DOUBT("freeDoubt"),
    IMAGE_URI("ImageUri"),
    REFER_CODE("referCode"),
    USER_BOARD("userBoard"),
    USER_BOARD_NAME("userBoardName"),
    ASKED_DOUBT("askedDoubt"),
    SHOW_MOCK_VIDEO("showMockVideo"),
    SYLLABUS_GENERATED("syllabusGenerated"),
    SUBSCRIBED("subscribed"),
    RAJASTHAN_BOARD_SELECTED("rajasthanBoardSelected"),
    MY_LANG("My_Lang"),
    MY_LANG_CAMEL("MyLang"),
    GO_TO_COACHING("goToCoaching"),
    GENDER("gender"),
    LAST_REELS_ID("last_reel_id"),
    CAMPAIGN_SUBSCRIBED("campaignSubscribed"),
    PHONE_NUMBER("number"),
    BOARD_SWITCH("boardSwitch"),
    BOARD("board"),
    IS_COMPETITIVE_PROFILE("isCompetitiveProfile"),
    PROFILE_ICON("profileIcon"),
    USER_SCORE("UserScore"),
    USER_SCORE_LOWER("userScore"),

    // Auth and tokens
    AUTH_TOKEN("authToken"),
    FCM_TOKEN("FIRBASETOKEN"),
    FCM_TOKEN_STORE_TIME("FCMTokenStoreTime"),
    FCM_NOTIFICATION_TOKEN("FCMNotificationToken"),
    FCM_NOTIFICATION_TOKEN_STORE_TIME("FCMNotificationTokenStoreTime"),
    DEVICE_TOKEN("deviceToken"),
    OTP("OTP"),

    // App state
    MICROLECTURE_DATA_RELOAD("MicrolectureDataReload"),
    HOME_DASHBOARD_DATA_RELOAD("HomeDashboardDataReload"),
    HOME_SCREEN_ON_CREATE_CALLED("Home_screen_on_create_called"),
    MAIN_DASHBOARD("MainDashboard"),
    IS_DOUBT_SOLUTION_READY("isDoubtSolutionReady"),
    IS_REVIEW_TOOL_TIP_VISIBLE("isReviewToolTipVisible"),
    IS_TODOLIST_READY("isTodolistReady"),
    PROGRESS_RELOAD("progressReload"),
    REFRESH("refresh"),
    REFRESH_DOUBT_CHAT_SCREEN("refreshDoubtChatScreen"),
    RELOAD_TEST_LIST_DATA("reloadTestListData"),
    RESET_LAUNCH_COUNT("resetLaunchCount"),
    UPDATE_STATUS("updateStatus"),
    CONNECTION_CONNECTED("connectionConnected"),
    CONNECTION_DISCONNECTED("connectionDisconnected"),
    NAVIGATE_TO_HOME_SCREEN("navigate_to_home_screen"),
    VIDEO_STARTING("video_starting"),

    // Onboarding and screen navigation
    NEW_ACTIVITY_ENABLED("NEW_ACTIVITY_ENABLED"),
    COMPETITIVE_HOME_SCREEN_FRAGMENT("CompetitiveHomeScreenFragment"),
    EXPLORE_ACTIVITY("exploreActivity"),
    EXPLORE_SCREEN_COMPLETED("exploreScreenCompleted"),
    EXPLORE_SCREEN_VISIT_COUNT("exploreScreenVisitCount"),
    SHOWN_AFTER_INTRO("shownAfterIntro"),
    EDIT_ROUTINE_COMPLETED("editRoutineCompleted"),

    // Location and class selection
    USER_LOCATION("user_location"),
    USER_LOCATION_SAVED("user_location_saved"),
    SELECTED_GRADE_ID("selected_grade_id"),
    SELECTED_GRADE_CODE("selected_grade_code"),

    // Notifications and doubt
    DOUBT_NOTIFICATION_DOUBT_ID("doubtNotificationDoubtId"),
    DOUBT_NOTIFICATION_TITLE("doubtNotificationTitle"),
    DOUBT_NOTIFICATION_VIDEO_LINK("doubtNotificationVideoLink"),
    DOUBT_CHAT_SUBSCRIBED("DOUBT_CHAT_SUBSCRIBED"),
    SUBSCRIBED_FIREBASE_NOTIFICATION("subscribed-firebase-notification"),

    // UI state flags
    SHOW_ASAT("showAsat"),
    SHOW_MATCH_THESE_DIALOG("showMatchTheseDialog"),
    SHOW_PRE_EXAM_LAYOUT("showPreExamLayout"),
    SHOW_PRE_EXAM_LAYOUT_PREFIX("showPreExamLayout_"),
    SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN("show_course_information_pop_up_on_home_screen"),
    SHOW_DOUBT_SCREEN_TOOLTIPS("show_doubt_screen_tooltips"),
    SHOW_DOUBT_SUGGESTION("show_doubt_suggestion"),
    HIDE_ASAT_NEW_BADGE("hideAsatNewBadge"),
    HIDE_EXAM_NEW_BADGE("hideExamNewBadge"),
    HIDE_LATE_POPUP("hideLatePopup"),
    HIDE_NCERT_SOLUTION_NEW_BADGE("hideNcertSolutionNewBadge"),
    HIDE_TOOLTIP_FOR_HOW_TO("hideTooltipForHowTo"),
    HIDE_MICROPHONE_POPUP("hide_microphone_popup"),
    HIDE_MINI_VIDEO_IN_ADAPTIVE_SCREEN("hide_mini_video_in_adaptive_screen"),
    HIDE_NOTIFICATION_POPUP("hide_notification_popup"),
    HOW_TO_VIDEO_VISIBLE("how_to_video_visible"),
    WEB_VIEW_TOUCH_DISABLED("webViewTouchDisabled"),
    TO_SHOW_NEW_BADGE_ATL("to_show_new_badge_atl"),
    SHOW_POP_UP("ShowPopUp"),
    COUNT_POP_UP("countPopUp"),
    POPUP_STORE_TIME("PopupStoreTime"),
    POPUP_ID("popupId"),
    POP_UP_INDEX("popUpIndex"),
    DIALOG_BANNER_IMAGE("dialogBannerImage"),
    DIALOG_BANNER_TEXT("dialogBannerText"),
    DIALOG_TIMEOUT("dialogTimeout"),

    // Pre-exam
    PRE_EXAM_ICON_URL("preExamIconUrl"),
    PRE_EXAM_ICON_URL_NON_BOARD("preExamIconUrlNonBoard"),
    PRE_EXAM_ID_BOARD("preExamIdBoard"),
    PRE_EXAM_ID_NON_BOARD("preExamIdNonBoard"),
    PRE_EXAM_NAME_BOARD("preExamNameBoard"),
    PRE_EXAM_NAME_NON_BOARD("preExamNameNonBoard"),
    EXAM_DATE_FILLED("examDateFilled"),

    LAST_SELECTED_CHECKED_TIME("last_selected_checked_time"),

    // Test series and scores
    SELECTED_TEST_SERIES_ID("selectedTestSeriesId"),
    SELECTED_TEST_SERIES_TITLE("selectedTestSeriesTitle"),
    TEST_SERIES_COURSE_TITLE("TestSeriesCourseTitle"),
    TOTAL_TESTS_COUNT("totalTestsCount"),
    CORRECT_PERCENTAGE("correctPercentage"),
    WRONG_PERCENTAGE("wrongPercentage"),
    ASAT_LAST_CALL_BACK_TIME("asat_last_call_back_time"),
    ASAT_TITLE("asat_title"),
    NEET_TOGGLE_STATE("neet_toggle_state"),

    // Media and microlecture
    MICROLECTURE_ID("microlectureId"),
    TO_SHOW_NOTES_VIDEO_COUNTER("toShowNotesVideoCounter"),
    UNWATCHED("unwatched"),
    UNWATCHED_SET("unwatchedSet"),
    USER_SELECTED_DOWNLOAD_QUALITY("user_selected_download_quality"),
    HEADER_IMAGE_URL("headerImageUrl"),

    // Competitive and personalisation
    COMPETITION_TEXT("COMPETITION_TEXT"),
    REDIRECTED_FROM_CONTINUE_LEARNING("REDIRECTED_FROM_CONTINUE_LEARNING"),
    PERSONALISED_TYPE("Personalised Type"),
    PERSONALISED_PATH_SUBJECT_TITLE("personalisedPathSubjectTitle"),
    SWITCHABLE_COURSE_INFO("switchable_course_info"),
    TARGET_COURSE_ID("target_course_id"),
    UNLOCK_SUBSCRIPTION_SCREEN_DATA("unlock_subscription_screen_data"),
    UNLOCK_SUBSCRIPTION_SCREEN_DATA_V2("unlock_subscription_screen_data_v2"),

    // Referral
    REFERRAL_CODE("referral_code"),
    REFERRAL_DATA_SET("referral_data_set"),
    REFERRAL_DETECTED("referral_detected"),
    REFERRAL_REDIRECTED_PENDING("referral_redirected_pending"),
    REFERRAL_SOURCE("referral_source"),
    REFER_POSTER_COUNT("refer_poster_count"),
    REFER_WIN_LAST_CALL_BACK_TIME("refer_win_last_call_back_time"),

    // Miscellaneous
    CAMERA_CAPTURE_URI("camera_capture_uri"),
    COUPON_ID("couponId"),
    DISCOUNT("discount"),
    OTHER_PAYMENT("other_payment"),
    PDF_PENCIL_EXPANDED_COUNTER("pdf_pencil_expanded_counter"),
    SUBJECT_SIZE("subjectSize"),
    SELECTED_TAB("selectedTab"),
    POSITION("Position"),
    RESPONSE("RESPONSE"),
    TYPE("Type"),
    TIME_TAKEN("TimeTaken"),
    TOPIC_DETAIL_ADAPTER("TopicDetailAdapter"),
    NUMBER_UPPER("Number"),
    ABCD("abcd"),
    LAST_DEVICE_INFO_TIME("lastDeviceInfoTime"),
    FEATURE_INFO_VIDEO_COUNT_PREFIX("feature_info_video_count_"),
    NEET_DIAGNOSTIC_WEBVIEW_SHOWN_PREFIX("neet_diagnostic_webview_shown_"),
    // Pinjra Tod Analysis home-screen card: glow border + pin-to-front, until
    // first tap / until the diagnostic test is attempted (see
    // CompetitiveHomeScreenFragment.applyPinjraTodCardOrdering()).
    PINJRATOD_CARD_TAPPED_PREFIX("pinjratod_card_tapped_"),
    // Set from the diagnostic webview itself via the JS bridge
    // (setDoNotDeletePreference, String-only) — must be read back with
    // getStringDataFromDoNotDeleteSharedPreference() + a "true" string
    // comparison, NOT getBooleanDataFromDoNotDeleteSharedPreference(), since a
    // value written as a String through the bridge fails SharedPreferences'
    // getBoolean() type check and silently returns the default.
    PINJRATOD_TEST_ATTEMPTED_PREFIX("pinjratod_test_attempted_"),
    POSTER_COUNT_PREFIX("poster_count_"),
    FEATURE_SECTION_ANIMATION_INDEX("feature_section_animation_index"),

    // Subscription type
    USER_SUBSCRIPTION_TYPE("user_subscription_type"),
    USER_SUBSCRIPTION_PLAN_ID("user_subscription_plan_id"),
    USER_SUBSCRIPTION_PROFILE_VIDEO_URL("user_subscription_profile_video_url"),

    // QR payment
    QR_CODE_IMAGE("qr_code_image"),
    QR_CODE_ID("QR_CODE_ID"),
    QR_CODE_IMAGE_TIMESTAMP("qr_code_image_timestamp"),
    QR_CODE_ORDER_ID("qr_code_order_id"),
    QR_CODE_SUBSCRIPTION_DTO("qr_code_subscription_dto"),

    // Firebase daily events
    USER_ACTIVE_DAY_LAST_LOGGED("user_active_day_last_logged"),

    // Subject optional flag
    OPTIONAL_SUBJECT_AVAILABLE("optional_subject_available"),

    //Carousal State Management
    HOME_CAROUSEL_POSITION("home_carousal_position"),
    USER_ACTIVE_DAYS("user_active_day"),

    // Streak tracking
    USER_STREAK_COUNT("user_streak_count"),
    USER_STREAK_START_DATE("user_streak_start_date"),

    // Refer and Earn
    REFER_AND_EARN_CART_SHOW_PREVIOUSLY("refer_and_earn_cart_show_previously"),
    REFER_AND_EARN_MINI_VIDEO_SHOW("refer_and_earn_mini_video_show"),
    REFER_AND_EARN_SHARE_TEXT("refer_and_earn_share_text"),
    MILESTONE_ANIMATION_SHOWN_PREFIX("milestone_animation_shown_"),
    MILESTONE_ANIMATION_LAST_SHOWN("milestone_animation_last_shown"),
    TOPPER_NOTES_SELECTED_TOPPER("topper_notes_selected_topper"),
    NEET_CLASS_SELECTION_PENDING("neet_class_selection_pending"),
    NOTIFICATION_DAILY_COUNT("notification_daily_count"),
    GENERIC_NOTIFICATION_DAILY_COUNT("generic_notification_daily_count");

    private final String key;
    PreferenceKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
