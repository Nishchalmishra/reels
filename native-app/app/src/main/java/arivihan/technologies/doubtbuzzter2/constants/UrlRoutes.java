package arivihan.technologies.doubtbuzzter2.constants;

public interface UrlRoutes {
    String GET_CHAPTER_LIST = "/secure/chapter/list";
    String GET_PERSONALISED_CHAPTER_LIST = "/secure/personalised/chapter/list";
    String GET_CHAPTER_OF_SUBJECT = "/secure/chapter/subject/{id}/list";
    String GET_CHAPTER_OF_SUBJECT_V2 = "/secure/chapter/v2/subject/{id}/list";
    String GET_CUSTOM_CHAPTER_OF_SUBJECT = "/secure/custom/subject/{id}/list";
    String CHAPTER_FETCH_BY_ID = "/secure/api/v1/chapters";
    String GET_MICROLECTURE_CONTENT = "/secure/chapter/microlecture/list";
    String GET_MICROLECTURE_CONTENT_BOARD = "/secure/chapter/board/microlecture/list";
    String GET_DOUBT_MICROLECTURE_CONTENT = "/secure/doubt/microlecture";
    String GET_MICROLECTURE_QANS = "/secure/chapter/microlecture/qans/list";
    String UPDATE_VIDEO_PROGRESS = "/secure/chapter/update/progress";
    String UPDATE_VIDEO_SELECTION = "/secure/chapter/update/video/selection";
    String GET_DOUBT_INFO = "/secure/pseudoLive/qans/doubt";
    String GET_CONTEXTUALIZED_DOUBT = "/secure/pseudoLive/doubt/context/gpt";
    String VALIDATE_ANSWER = "/secure/question/answer/check";
    String GET_MICROLECTURE_QUESTION_OPTIONS = "/secure/micro/lecture/v2/microlecture/download/qans";
    String SAVE_INTERACTION = "/secure/question/save/interaction";
    String SNACKBAR_LIST = "/secure/pseudoLive/snackbar/list";
    String BEHAVIOUR_LIST = "/secure/pseudoLive/behaviour/list";
    String BEHAVIOUR_LIST_NEW = "/secure/pseudoLive/behaviour/list/new";
    String GET_CLASSES = "/secure/onBoarding/classes";
    String CLASS_LIST = "/secure/onBoarding/classes";
    String SUBJECT_LIST = "/secure/onBoarding/subjects";
    String SUBJECT_GROUP_LIST = "/secure/onBoarding/v2/subject/group";
    String COURSES_LIST = "/secure/onBoarding/courses";
    String COURSES_LISTV2 = "/secure/onBoarding/coursesV2";
    String COURSES_LISTV3 = "/secure/onBoarding/coursesV3";

    String SECONDARY_COURSES_LIST = "/secure/onBoarding/secondary-courses";

    String LEVEL_lIST = "/secure/onBoarding/level";
    String SUBMIT_CUSTOM_CHAPTER_REQUEST = "/secure/custom/chapter";
    String REGISTER = "/secure/user/register";
    String REGISTER_V2 = "/secure/user/v2/register";
    String PUT_UPDATE = "/secure/api/v1/user/update";
    String GET_USER_BY_PHONE = "/secure/user/phone";

    String GET_TRUECALLER_USER_INFO = "/secure/user/truecaller-user-info";

    String GET_USER_BY_ID = "/secure/user";
    String GET_HIGHLIGHT_WEEK = "/secure/home/highlights/week";
    String GET_SAMPLE_PAPERS = "/secure/sample/paper/list";
    String GET_SAMPLE_PAPER_CONTENTS = "/secure/sample/paper/content";
    String GET_CLASS_PROGRESS = "/secure/home/class/progress";
    String GET_TEST_SERIES = "/secure/home/test/series";
    String AV_TEST_SERIES_CARDS = "/secure/api/v1/test-series-cards";
    String GET_SUBSCRIBED_SUBJECT = "/secure/home/subscribed/subjects";
    String GET_SUBSCRIBED_SUBJECT_V2 = "/secure/home/v2/subscribed/subjects";
    String GET_SUBSCRIBED_SUBJECT_HOME_SCREEN = "/secure/home/v2/subscribed/subjects/home/screen";
    String AV_SUBJECTS = "/secure/api/v1/subjects";
    String AV_SUBJECTS_V2 = "/secure/api/v2/subjects";
    String AV_OPTIONAL_SUBJECTS = "/secure/api/v1/subjects/optional";
    String AV_ADD_USER_OPTIONAL_SUBJECTS = "/secure/api/v1/users/additional-subjects";
    String HOME_SCREEN_CAROUSEL = "/secure/home/carousel";
    String HOME_SCREEN_BADGE_PROGRESS = "/secure/home/badge/progress";
    String GET_SUBJECT_PROGRESS = "/secure/home/student/progress";
    String AV_CHANGE_USER_LANGUAGE = "/secure/api/v1/user/language";
    String GET_GAME_STATISTICS = "/secure/game/user/statistics";
    String GET_GAME_QUESTION = "/secure/game/user/question";
    String START_GAME = "/secure/game";
    String POST_GAME_RESPONSE = "/secure/game/user/submit";
    String POST_MICROLECTURE_ANSWER = "/secure/question";
    String GET_USER_POINTS = "/secure/user/point";
    String GET_USER_FREE_DOUBT = "/secure/user/doubt";
    String SEND_FEEDBACK = "/secure/feedback";
    String SEND_UNSOLVED_DOUBT = "/secure/pseudoLive/unsolvedDoubt";
    String RESUBMIT_DOUBT = "/secure/doubt/doubt/resubmit";
    //Test series
    String TEST_SERIES = "/secure/testSeries";
    String TEST_SERIES_LIST = "/secure/testSeries/list/v2";
    String TEST_SERIES_v2 = "/secure/testSeries/v2";
    String TEST_SERIES_ASYNC_v2 = "/secure/testSeries/async/v2";
    String REATTEMPTTEST = "/secure/testSeries/reattempt";
    String TEST_SERIES_QUESTION = "/secure/testSeries/question";
    String TEST_SERIES_CAROUSEL = "/secure/testSeries/carousel";


    String TEST_REPORT_QUESTION = "/secure/testSeries/report/question";

    //reels
    String REELS_GET = "/secure/reels";
    String REELS_UPDATE_VIEWS = "/secure/reels/views";
    String REELS_UPDATE_LIKES = "/secure/reels/likes";
    String AV_REELS_GET = "/secure/api/v1/av-reels";
    String AV_REELS_UPDATE_VIEWS = "/secure/api/v1/av-reels/view";
    String AV_REELS_UPDATE_LIKES = "/secure/api/v1/av-reels/like";


    String PAYMENT_INFO_RAZORPAY = "/secure/payment/info/razorpay";
    String RAZORPAY_CREATE_QR_CODE = "/secure/razorpay/create-qr-code";
    String RAZORPAY_CHECK_QR_PAYMENT_STATUS = "/secure/razorpay/check-qr-code-payment-status";


    //Analytics
    String GET_USER_ALL_SUBJECT_PROGRESS = "/analytics/progress/user/subjects";
    String GET_USER_ONE_SUBJECT_PROGRESS = "/analytics/progress/user/chapters/subject";
    String GET_USER_TEST_ANALYSIS = "/analytics/progress/user/testseries";
    String GET_USER_DAILY_PROGRESS = "/analytics/progress/user/daily";
    String GET_USER_TEST_ANALYTICS_CHAPTER = "/analytics/progress/user/testseries/chapter";


    //QuickNotes
    String QUICK_NOTES = "/secure/quick/notes";
    String AV_TOPPER_NOTES = "/secure/api/v1/dashboard/notes";
    String AV_NOTES_BY_USER = "/secure/api/v1/dashboard/notes/by-user";
    String TOPPER_NOTES = "/secure/quick/notes/topper";
    String TOPPER_NOTES_CAROUSEL = "/secure/quick/notes/carousel";
    //Subscription
    String GET_SUBSCRIPTION = "/secure/subscription/plan";
    String GET_SUBSCRIPTION_V2 = "/secure/subscriptionV2/plan/";

    String V2_GET_SUBSCRIPTION = "/secure/api/v2/subscription/plan/";
    String BUY_SUBSCRIPTION_V2 = "/secure/subscriptionV2/plan/buy-plan";

    String V2_BUY_SUBSCRIPTION = "/secure/api/v2/subscription/plan/buy-plan";
    String GET_SUBSCRIPTION_PLAN_LEVELS = "/secure/subscription/plan/levels";
    String GET_COUPONS = "/secure/secure/coupon/user";
    String VERIFY_COUPON = "/secure/coupon/valid/";
    String USER_SUBSCRIPTION = "/secure/user/subscription";
    String BUY_NOW_PLAN_DETAILS = "/secure/api/v2/subscription/plan/buy-plan";
    String USER_SUBSCRIPTION_BENEFITS = "/secure/user/subscription/benefits";
    String USER_SUBSCRIPTION_TEST = "/secure/user/subscription/test";
    String INVOICE = "/secure/invoice";
    String USER_SUBSCRIPTION_DETAIL = "/secure/user/subscription/invoice";
    String USER_SUBSCRIPTION_CHECK_STATUS = "/secure/user/subscription/check-subscription";

    String SAVE_FCM_TOKEN = "/secure/fcm";
    String NEW_ACTIVITY_ENABLED = "/secure/pseudoLive/video/activity";
    String FEATURE_ENABLED = "/secure/feature";
    String CONFIGURATION = "/secure/feature/configuration";
    String TEST_SERIES_STARTED = "/secure/testSeries/start";
    //Competition
    String COMPETITION = "/secure/competition";
    String QUESTION = "/secure/competition/question";
    String COMPETITION_REGISTER = "/secure/competition/register";
    String COMPETITION_SUBMIT = "/secure/competition/submit";
    String HISTORY = "/secure/competition/history";
    String LEADER_BOARD = "/secure/competition/leaderboard";
    String REVIEW = "/secure/competition/review";
    String NOTIFICATION = "/secure/fcm/send";
    String GET_BOARD_LIST = "/secure/onBoarding/board";

    String GET_USER_BOARD_LIST = "/secure/home/user-board";
    String GET_TO_DO_LIST = "/secure/toDoList";

    String AV_GET_TODAY_CLASS = "/secure/api/v1/today-classes/home";
    String GET_TODAY_CLASS = "/secure/toDoList/today-classes/v2";
    String GET_TODAY_CLASS_BY_BOARD_CODE = "/secure/toDoList/live-classes";
    String GET_TODAY_CLASS_BY_BATCH_ADD_ON = "/secure/api/v1/today-classes";

    String GET_USER_TO_DO_LIST = "/secure/admin/toDoList";
    String GET_USER_DEFAULT_ORDER = "/secure/admin/toDoList/default/order";
    String UPDATE_USER_SYLLABUS_ORDER = "/secure/admin/toDoList/update/order";


    String GET_DEFAULT_ORDER = "/secure/toDoList/default/order";
    String UPDATE_SYLLABUS_ORDER = "/secure/toDoList/update/order";
    String USER_RANK_LIST = "/secure/ranking/fetch/test/rank";
    String OVERALL_USER_RANK_LIST = "/secure/ranking/overall/test/rank";
    String DOUBT_SOLUTION_FEEDBACK = "/secure/pseudoLive/solution/user/feedback";
    String UPDATE_UNWATCHED_VIDEO = "/secure/home/update/unwatched";
    String UNWATCHED_VIDEO = "/secure/home/unwatched/video";

    String TUTOR_SUPPORT_CHAT = "/secure/bot/chatrequest";
    String TUTOR_SUPPORT_CHAT_V2 = "/secure/bot/chatrequest/v2";
    String TUTOR_SUPPORT_CHAT_HISTORY = "/secure/bot/chat/history";
    String TUTOR_SUPPORT_SESSION_CHAT_HISTORY = "/secure/bot/session/chats";
    String TUTOR_SUPPORT_DOUBT_CHAT_HISTORY = "/secure/bot/doubt/chats";
    String TUTOR_SUPPORT_DOUBT_SAVE_RATING = "/secure/bot/doubt/rating";
    String TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION = "/secure/bot/doubt/check/subscription";
    String EXTRACT_TEXT_FROM_IMAGE_URI = "/secure/board/text/extract";
    String BOARD_ANSWER_VALIDATE = "/secure/board/answer/validate";

    String FETCH_NOTIFICATION = "/secure/fcm/fetch/notification";
    String GET_RECENT_SEARCHES = "/secure/global/search/default/recent";
    String GET_SEARCH_RESULT = "/secure/global/search/default/search";
    String GET_DOUBT_RESULT = "/secure/global/search/search/doubt";
    String GET_SEARCH_SUGGESTIONS = "/secure/global/search/default/autocomplete";
    String GET_NOTES = "/secure/quick/notes/id";
    String ADD_RECENT_SEARCH = "/secure/global/search/default/recent/add";
    String TEST_SERIES_USER_SCORE = "/secure/testSeries/user/score";
    String TEST_SERIES_SECTIONAL_SUMMARY = "/secure/testSeries/user/sectional/summary";
    String TEST_SERIES_CHAPTER_SUMMARY = "/secure/testSeries/user/chapter/summary";
    String TEST_SERIES_TOPIC_SUMMARY = "/secure/testSeries/user/topic/summary";

    //account
    String DELETE_YOUR_ACCOUNT = "/secure/user/delete";
    String DELETE_ACCOUNT_OTP_GENERATE = "/secure/api/v1/user/generate/otp";
    String DELETE_ACCOUNT_OTP_VALIDATE = "/secure/api/v1/user/validate/otp";
    String LOGOUT_YOUR_ACCOUNT = "/secure/user/logout";

    String TEST_SERIES_CHAPTER_ALL_SUMMARY = "/secure/testSeries/user/total/chapter/summary";
    String TEST_SERIES_TOPIC_ALL_SUMMARY = "/secure/testSeries/user/total/topic/summary";
    String TEST_SERIES_USER_SCORE_COMPARISON = "/secure/testSeries/user/comparison/score";

    String DOUBT_CHAT = "/secure/doubt/chat";
    String DOUBT_CHAT_FEEDBACK = "/secure/doubt/feedback/v2";
    String DOUBT_CHAT_PREVIOUS_DOUBTS = "/secure/doubt/list";
    String DOUBT_CHAT_HISTORY = "/secure/doubt/list/v3";
    String DOUBT_CHAT_RESUME = "/secure/doubt/resume";
    String OPTION_CLICK = "/secure/doubt/option/click/v4";
    String DOUBT_REQUEST_VIDEO_SOLUTION = "/secure/doubt/request-video";
    String IMAGE_UPLOAD = "/secure/doubt/upload/image";
    String SEND_EMAIL = "/secure/send/email/body";
    String MICRO_LECTURE_LIST = "/secure/micro/lecture/v2";
    String MICRO_LECTURE_BY_ID = "/secure/micro/lecture/v2/microlecture/by/id";
    String MICRO_LECTURE_PRACTICE_LIST_BY_TYPE = "/secure/micro/lecture/v2/qnaByType";
    String UPDATE_DOUBT_STATUS = "/secure/doubt/update/doubt/status";
    String UPDATE_MOCK_VIDEO_WATCH_COUNT = "/secure/user/update/mock/video/watch/count";
    String GET_MOCK_VIDEO_URL = "/secure/testSeries/mock/video/url";
    String PUBLISH_SCREEN_VISIT_EVENT = "/secure/onBoarding/screen/event";
    String GENERATE_OTP = "/secure/user/generate/otp";
    String SIGN_IN = "/secure/user/login";

    String FIB_PROBABLE_ANSWER_VALIDATE = "/secure/question/validate/fib";
    String HALF_YEARLY_USER_TASK_UPDATE = "/secure/pre-exam/update-routine-status";
    String HALF_YEARLY_TODAY_ROUTINE = "/secure/pre-exam/today-routine/exam";
    String HALF_YEARLY_POPUP = "/secure/pre-exam/popup";

    String AV_BOTTOM_SHEET_CONTENT = "/secure/api/v1/bottom-sheet-content";

    String HALF_YEARLY_CAROUSEL = "/secure/quick/notes/pre-exam/carousel";

    String SAVE_DOWNLOAD = "/secure/micro/lecture/v2/save/download";
    String UPDATE_DOWNLOAD = "/secure/micro/lecture/v2/update/download";
    String DELETE_YOUR_DOWNLOADED_LECTURE = "/secure/micro/lecture/v2/delete/download";
    String DELETE_YOUR_ROUTINE = "/secure/home/delete/routine";
    String SYNC_DOWNLOADED_LECTURE_DATA = "/secure/micro/lecture/v2/sync/download/new";
    String AV_SYNC_DOWNLOADED_LECTURE_DATA = "/secure/api/v1/dashboard/micro-lecture/sync/download";
    String SUBSCRIPTION_EXPIRATION_REMINDER_GET = "/secure/user/subscription/get-subscription-expiration-renewal-reminder";
    String SUBSCRIPTION_EXPIRATION_REMINDER_UPDATE = "/secure/user/subscription/update-subscription-expiration-renewal-reminder";
    String CHECK_PAYMENT_STATUS = "/secure/razorpay/check-plan-status";

    String GET_TODO_ORDER_STATE = "/secure/toDoList/get/user/state";
    String UPDATE_TODO_ORDER_STATE = "/secure/toDoList/update/user/state";
    String GET_TOPPERS_LIST = "/secure/home/champions-of-arivihan";
    String AV_DASHBOARD_VIDEO = "/secure/api/v1/dashboard/video";
    String AV_EXAM_UI_DETAILS = "/secure/api/v1/exams/ui-details";
    String MICRO_LECTURE_SELECTION = "/secure/micro/lecture/v2/update/type/selection";
    String CAMPAIGN_SUBSCRIPTION_DATA = "/secure/subscription/plan/campaign";

    String GET_USER_FCM_TOPICS_TO_SUBSCRIBE = "/secure/user/fcm-topics-to-subscribe-list";

    String GET_ASAT_PROCESS_INFO = "/secure/asat/get/process/info";
    String GET_ASAT_USER_INFO = "/secure/asat/get/user/info";
    String SAVE_ASAT_USER_INFO = "/secure/asat/save/user/info";
    String SAVE_ASAT_USER_MARKSHEET_INFO = "/secure/asat/save/user/marksheet/info";
    String GET_ACADEMIC_DETAILS = "/secure/api/v1/user/academic-details";
    String CALL_ME_BACK = "/call/me";
    String UPDATE_ASAT_STEP = "/secure/asat/update/result/step";

    String ASAT_CHECK_ATTEMPTED_WITH_ROLL_NO = "/secure/asat/check-attempt-with-rollno";

    String UPDATE_ONBOARDING_STATE = "/secure/onBoarding/save/onboarding/state";
    String GET_ONBOARDING_STATE = "/secure/onBoarding/get/onboarding/state";

    // New onboarding user-state APIs (v1)
    String SAVE_USER_ONBOARDING_STATE = "/secure/api/v1/user-state";
    String GET_USER_ONBOARDING_STATE = "/secure/api/v1/user-state";
    String DELETE_USER_ONBOARDING_STATE = "/secure/api/v1/user-state";
    String POST_FEEDBACK_LIKE = "/secure/like";
    String GET_FEEDBACK_COMMENTS = "/secure/get/comments";
    String GET_NESTED_FEEDBACK_COMMENTS = "/secure/get/nested/comments";
    String POST_FEEDBACK_COMMENT = "/secure/comment";
    String DELETE_FEEDBACK_COMMENT = "/secure/delete/comment";
    String GET_TODO_EXAM_DATE_CHECK = "/secure/toDoList/exam-date/check";
    String POST_TODO_EXAM_DATE_SUBMIT = "/secure/toDoList/exam-date/submit";
    String CHECK_ATL_TEST_STATUS = "/secure/testSeries/check-atl-given";
    String GET_SOLUTIONS_SCREEN = "/secure/testSeries/get-solution-screen-data";
    String CHECK_SUBSCRIPTION = "/secure/user/subscription-check";
    String USER_FEEDBACK_STATUS = "/secure/user/feedback/status";
    String USER_FEEDBACK_SUBMIT = "/secure/user/feedback/submit";
    String SYNC_DOWNLOADED_NOTE_DATA = "/secure/quick/notes/sync/download";
    String AV_SYNC_DOWNLOADED_NOTE_DATA = "/secure/api/v1/dashboard/notes/sync/download";
    String REGISTER_FOR_LIVE_SESSION = "/secure/user/register-live-session";
    String MARKED_VIDEO_COMPLETION = "/secure/user/mark-viewed-recorded-live-lecture";
    String PDF_CIRCLE_USAGE_COUNT = "/secure/pdf-circle/usage-count";

    String HOME_CALL_ME_BACK = "/home/call/me";
    String ML_PDF_CIRCLE_EXPLAIN = "/api/v1/explain/circle";
    String ML_PDF_CIRCLE_LOAD = "/api/v1/pdf/load";

    // Action event: used to send user action events to backend
    String ACTION_EVENT = "/secure/api/v2/user/action-event";
    String ACTION_EVENT_NOTIFICATION = "/api/v1/analytics/notification-event";

    String VIDEO_COMPLETION = "/secure/user/video-completion";

    // Competitive Section API ENDPOINT
    String COMPETITIVE_PRACTISE = "/secure/neet/practise";
    String COMPETITIVE_LEARN_CONTINUE_LEARNING = "/secure/neet/learn/continue-learning";
    String COMPETITIVE_LEARN_TEST_SERIES = "/secure/neet/learn/test-series";
    String COMPETITIVE_LEARN_ADAPTIVE_PRACTISE = "/secure/neet/learn/adaptive-practise";
    String COMPETITIVE_LEARN_SUBJECT_LIST = "/secure/neet/subject/list";
    String COMPETITIVE_LEARN_ONE_SHOT_LECTURE = "/secure/api/v1/neet/learn/one-shot-lectures";
    String COMPETITIVE_LEARN_DPP = "/secure/api/v1/practice-problem/learn-screen";

    String GET_COMPETITIVE_TOPPERS = "/secure/neet/toppers";
    String AV_HOME_SCREEN_CARDS = "/secure/api/v1/home-screen-cards";
    String GET_PERSONALIZED_PATH = "/secure/api/v1/neet/personalised-paths";

    String GET_PERSONALIZED_PATH_V2 = "/secure/api/v1/neet/personalised-paths";
    String GET_VIRAL_VIDEO_SECTION = "/secure/neet/viral-videos";

    String GET_COMPETITIVE_HOME_SCREEN_CAROUSEL = "/secure/neet/carousel";
    String GET_COMPETITIVE_ADAPTIVE_CHAPTER_LIST = "/secure/api/v1/neet/adaptive-chapter-lists";
    String TEST_SERIES_CATEGORY_WISE_USER_SCORE = "/secure/testSeries/category-wise/user/score";
    String TEST_SERIES_CATEGORY_WISE_USER_SECTIONAL_SUMMARY = "/secure/testSeries/category-wise/user/sectional/summary";
    String TEST_SERIES_CATEGORY_WISE_USER_TOPIC_SUMMARY = "/secure/testSeries/category-wise/user/topic/summary";
    String TEST_SERIES_CATEGORY_WISE_USER_COMPARISON_SCORE = "/secure/testSeries/category-wise/user/comparison/score";

    String GET_CHAPTER_USER_BATCH = "/secure/chapter/v2/batches";
    String AV_GET_CHAPTER_USER_BATCH = "/secure/api/v1/chapters/sections";
    String SAVE_USER_LOCATION = "/secure/user/upsert-location";

    String GET_ONE_SHOT_LECTURES = "/secure/api/v1/neet/one-shot-lectures";
    String POST_CHAPTER_PROGRESS = "/secure/user-personalised-learning-path/progress";
    String POST_CHAPTER_PROGRESS_V2 = "/secure/user-personalised-learning-path/v2/progress";
    String POST_CHAPTER_DEMO_PROGRESS = "/secure/user-personalised-learning-path/chapter/demo/progress";

    String CHECK_METRICS_USER = "/secure/user/check-metrics-users";
    String SWITCH_TO_TARGET_COURSE = "/secure/user/switch-class-course";

    // Batch Architecture
    String AV_REGISTER = "/secure/api/v1/register";
    String AV_GRADE_LIST = "/secure/api/v1/grade/levels";
    String AV_STREAM_LIST = "/secure/api/v1/stream";
    String AV_GET_USER_BY_PHONE = "/secure/api/v1/user";
    String AV_CAROUSEL = "/secure/api/v1/carousels";
    String AV_GET_CHAPTER_OF_SUBJECT = "/secure/api/v1/chapters";
    String AV_GET_CHAPTER_OF_SUBJECT_BATCH_ADD_ON = "/secure/api/v1/batch-add-on/chapters";
    String AV_TEST_SERIES_LIST = "/secure/api/v1/test-series";
    String AV_CRASH_COURSE = "/secure/api/v1/crash-courses";
    String GET_REWARDS_SCREEN_DATA = "/secure/rewards/screen";
    String GET_WEEKLY_TIMETABLE = "/secure/api/v1/timetable";
    String GET_HOME_SCREEN_UI_DATA = "/secure/home/course-ui-data";
    String GET_COMPETITIVE_FEATURE_SECTIONS = "/secure/neet/v2/badges";

    // Notification Token Management
    String TOKEN_REGISTER = "/api/v1/tokens/register";
    String TOKEN_ATTACH_USER = "/api/v1/tokens/attach-user";
    String TOKEN_DETACH_USER = "/api/v1/tokens/detach-user";
    String TOKEN_REFRESH = "/api/v1/tokens/refresh";

    //force update
    String FORCE_UPDATE_HIT_COUNT = "/secure/api/v1/app-update";

    //refer and earn
    String REFER_EARN_PINCODE_VALIDATION = "/secure/refer-win/pincode";
    String REFER_EARN_ADDRESS_LIST = "/secure/refer-win/address";
    String REFER_EARN_LEADERBOARD = "/secure/refer-win/leaderboard";
    String REFER_EARN_SCREEN = "/secure/refer-win/screen";
    String REFER_EARN_FRIENDS = "/secure/refer-win/friends";
    String REFER_EARN_REFERRALS = "/secure/refer-win/referrals";
    String REFER_EARN_GIFT_REVEAL = "/secure/refer-win/redeem/gift-reveal";
    String BOOK_ORDER_CART = "/secure/refer-win/cart";
    String BOOK_ORDER_CART_ITEMS = "/secure/refer-win/cart/items";
    String BOOK_ORDER_CATALOG = "/secure/refer-win/books/catalog";
    String BOOK_ORDER_CHECKOUT = "/secure/refer-win/checkout";
    String ORDER_CONFIRM = "/secure/refer-win/checkout/confirm";
    String REDEEM_ORDER = "/secure/refer-win/redeem/order";
    String ORDER_HISTORY = "/secure/refer-win/orders";


    String GET_OVERALL_CHAPTERS = "/secure/api/v1/batch-add-on/chapters";


    // Ask Doubt
    String DOUBT_SESSION_API = "/secure/doubt/sessions";

    // Analytics — full URL is built dynamically; see ScreenAnalyticsHelper.dispatch()
    String SCREEN_EVENT_PATH = "/api/v1/analytics/screen-event";

    // Profile image upload
    String PROFILE_IMAGE_UPLOAD = "/secure/api/v1/user/profile-image";

    // Direct video progress — retention tracking
    String DIRECT_VIDEO_PROGRESS = "/analytics/events/direct-video-progress";
    String SUPPORTED_LANGUAGES = "/secure/api/v1/supported/languages";
    String ACADEMIC_EXAM_LIST = "/secure/api/v1/exams";

}
