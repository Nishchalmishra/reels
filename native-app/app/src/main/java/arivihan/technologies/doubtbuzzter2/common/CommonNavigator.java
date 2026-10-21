package arivihan.technologies.doubtbuzzter2.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.DoubtChatScreen;
import arivihan.technologies.doubtbuzzter2.activity.WebviewDoubtChatScreen;
import arivihan.technologies.doubtbuzzter2.activity.TestSeriesListActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.AdaptiveSubjectPracticeActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListByBatchActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListBySubjectsActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListByTypeActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.NameInputDto;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCard;

public class CommonNavigator {
    private static final String TAG = CommonNavigator.class.getSimpleName();


    /**
     * Navigate to TestSeriesListActivity with full control over optional params.
     *
     * @param hideFilterSection pass true to hide the filter UI (e.g. ATL tests)
     * @param clearTop          pass true to add FLAG_ACTIVITY_CLEAR_TOP (e.g. back-press from analysis screens)
     */
    public static void navigateToTestSeriesListActivity(Context context, String testId, String title,
                                                        String optionalTestSeriesId, boolean optionalIsHalfYearly,
                                                        String optionalTestSeriesType, boolean hideFilterSection,
                                                        boolean clearTop, String screenName) {
        try {
            Intent intent = new Intent(context, TestSeriesListActivity.class);
            if (StringUtils.isNotEmpty(title)) {
                intent.putExtra("title", title);
            }
            intent.putExtra("Id", testId);
            intent.putExtra("fromHalfYearly", optionalIsHalfYearly);
            intent.putExtra("testSeriesId", StringUtils.isNotEmpty(optionalTestSeriesId) ? optionalTestSeriesId : "");
            intent.putExtra("testSeriesType", optionalTestSeriesType);
            intent.putExtra("Screen", screenName);
            if (hideFilterSection) {
                intent.putExtra("hidefilterSection", true);
            }
            if (clearTop) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            context.startActivity(intent);

            Map<String, String> params = new HashMap<>();
            params.put("Id", testId);
            params.put("testSeriesId",
                    StringUtils.isNotEmpty(optionalTestSeriesId) ? optionalTestSeriesId : "");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.competitive_test_series_section_clicked.getActionName(), params);
        } catch (Exception ignored) {
        }
    }

    /**
     * Navigate to TestSeriesListActivity from a TestSeriesCard (new API / card-based flow).
     * Uses testCategoryId as the "Id" extra and logs the hs_Test_click Firebase event.
     */
    public static void navigateToTestSeriesListFromCard(Context context, TestSeriesCard card, String testSeriesType, String screenName) {
        try {
            String testCategoryId = card.getTestCategoryId() != null ? card.getTestCategoryId() : "";
            String name = card.getName() != null ? card.getName() : "";
            Intent intent = new Intent(context, TestSeriesListActivity.class);
            intent.putExtra("Id", testCategoryId);
            intent.putExtra("title", name);
            intent.putExtra("testSeriesType", testSeriesType);
            intent.putExtra("filterStatus", "");
            intent.putExtra("filterSubject", "");
            context.startActivity(intent);

            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.SELECTED_TEST_SERIES_TITLE.getKey(), name);
            params.put("TestSeriesId", testCategoryId);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_Test_click.getActionName(), params);
        } catch (Exception ignored) {
        }
    }

    public static void navigateToChapterListActivity(Context context, String title, String subjectId,
                                                     String subjectName, String subjectCode, String screenName, String boardCode, String batchName) {
        Intent intent = new Intent(context, ChapterListBySubjectsActivity.class);
        intent.putExtra("SubjectName", subjectName);
        intent.putExtra("SubjectId", subjectId);
        intent.putExtra("SubjectCode", subjectCode);
        intent.putExtra("Screen", screenName);
        intent.putExtra("boardCode", boardCode);
        intent.putExtra("batchName", batchName);
        intent.putExtra("title", title);
        Map<String, String> params = new HashMap<>();
        params.put("SubjectName", subjectName);
        params.put("SubjectId", subjectId);
        params.put("Screen", screenName);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subject_click.getActionName(), params);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_list_screen_view.getActionName(), params);
        context.startActivity(intent);
    }

    public static void navigateToChapterListByBatchActivity(Context context, String subjectId,
                                                     String subjectName, String subjectCode,
                                                            String screenName,
                                                            String title, String boardCode) {
        navigateToChapterListByBatchActivity(context, subjectId, subjectName, subjectCode, screenName, title, boardCode, null);
    }

    public static void navigateToChapterListByBatchActivity(Context context, String subjectId,
                                                     String subjectName, String subjectCode,
                                                            String screenName,
                                                            String title, String boardCode,
                                                            String scrollToChapterId) {
        boolean isCompetitive = MyApplication.isCompetitiveProfile();
        boolean isBasicSubscription = SubscriptionType.isBasicSubscription();
        boolean isOldTwelve2026 = MyApplication.isOldTwelve2026Profile();
        boolean hideProPlan = SubscriptionNavigationHelper.hideProPlanSubscription();

        Log.d("ChapterNavigation", "isCompetitiveProfile = " + isCompetitive);
        Log.d("ChapterNavigation", "isBasicSubscription = " + isBasicSubscription);
        Log.d("ChapterNavigation", "isOldTwelve2026Profile = " + isOldTwelve2026);
        Log.d("ChapterNavigation", "hideProPlanSubscription = " + hideProPlan);

        if (isCompetitive
                || isBasicSubscription
                || isOldTwelve2026
                || hideProPlan) {

            Log.d("ChapterNavigation", "Navigating to ChapterListByBatchActivity");

            Intent intent = new Intent(context, ChapterListByBatchActivity.class);
            intent.putExtra("SubjectName", subjectName);
            intent.putExtra("SubjectId", subjectId);
            intent.putExtra("SubjectCode", subjectCode);
            intent.putExtra("Screen", screenName);
            intent.putExtra("title", title);
            intent.putExtra("boardCode", boardCode);
            if (scrollToChapterId != null) intent.putExtra("scrollToChapterId", scrollToChapterId);

            Log.d(
                    "ChapterNavigation",
                    "SubjectName=" + subjectName
                            + ", SubjectId=" + subjectId
                            + ", SubjectCode=" + subjectCode
                            + ", Screen=" + screenName
                            + ", Title=" + title
                            + ", BoardCode=" + boardCode
                            + ", ChapterId=" + scrollToChapterId
            );
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, ChapterListSubscribedActivity.class);
            intent.putExtra("SubjectName", subjectName);
            intent.putExtra("SubjectId", subjectId);
            intent.putExtra("SubjectCode", subjectCode);
            intent.putExtra("Screen", screenName);
            intent.putExtra("title", title);
            intent.putExtra("boardCode", boardCode);
            if (scrollToChapterId != null) intent.putExtra("scrollToChapterId", scrollToChapterId);
            context.startActivity(intent);
        }
        Log.d(
                "ChapterNavigation",
                "SubjectName=" + subjectName
                        + ", SubjectId=" + subjectId
                        + ", SubjectCode=" + subjectCode
                        + ", Screen=" + screenName
                        + ", Title=" + title
                        + ", BoardCode=" + boardCode
                        + ", ChapterId=" + scrollToChapterId
        );

        Map<String, String> params = new HashMap<>();
        params.put("SubjectName", subjectName);
        params.put("SubjectId", subjectId);
        params.put("Screen", screenName);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subject_click.getActionName(), params);

    }

    public static void navigateToSubjectPractiseActivity(Context context) {
        Intent intent = new Intent(context, AdaptiveSubjectPracticeActivity.class);
        context.startActivity(intent);
    }

    public static void navigateToCompetitiveTopicListActivity(Context context, String chapterId) {
        Intent intent = new Intent(context, AdaptiveSubjectPracticeActivity.class);
        intent.putExtra("id", chapterId);
        context.startActivity(intent);
    }

    public static void navigateToDppSubjectActivity(Context context, String subjectId, String subjectName) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("subjectId", subjectId);
        params.put("title", subjectName);
        String url = CommonMethods.getPersonalizedPathUrl() +
                AdaptivePath.DPP.path();
        CommonNavigator.navigateToWebViewByUrlParams(context,
                url,
                params,
                true, true,
                false, false,
                false, false, null, true, true, false, false, null, TAG, null);
    }

    public static void openGoogleMeet(Context context, String meetLink) {
        if (StringUtils.isEmpty(meetLink)) {
            Toast.makeText(context, context.getString(R.string.meeting_link_not_available), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent meetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
            meetIntent.setPackage("com.google.android.apps.meetings");
            context.startActivity(meetIntent);
        } catch (Exception e) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
                context.startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(context, context.getString(R.string.unable_to_open_meeting_link), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void navigateToDppGuidelines(Context context, String dppBatchId, String subjectId, String topic) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("dppBatchId", dppBatchId);
        params.put("subjectId", subjectId);
        params.put("topic", topic);
        params.put("joinFrom", "list_join_now");
        params.put("zoomEnabled", true);
        params.put("source", "home_screen_todays_class");
        String url = CommonMethods.getPersonalizedPathUrl() +
                AdaptivePath.DPP_GUIDELINES.path();
        CommonNavigator.navigateToWebViewByUrlParams(context,
                url,
                params,
                true, true,
                false, false,
                false, false, null, true, true, false, false, null, TAG, null);
    }

    public static void navigateToDppPractice(Context context, String dppBatchId, String subjectId, String topicName) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("dppBatchId", dppBatchId);
        params.put("type", "MULTIPLE_CHOICE_QUESTION");
        params.put("topicName", topicName);
        params.put("subjectId", subjectId);
        String url = CommonMethods.getPersonalizedPathUrl() +
                AdaptivePath.DPP_PRACTICE.path();
        CommonNavigator.navigateToWebViewByUrlParams(context,
                url,
                params,
                true, true,
                false, false,
                false, false, null, true, true, false, false, null, TAG, null);
    }

    public static void navigateToChapterListByTypeActivity(Context context, String title, String type, String subjectId, String screenName) {
        try {
            Intent intent = new Intent(context, ChapterListByTypeActivity.class);
            intent.putExtra(ChapterListByTypeActivity.EXTRA_TITLE, title);
            intent.putExtra(ChapterListByTypeActivity.EXTRA_TYPE, type);
            intent.putExtra(ChapterListByTypeActivity.EXTRA_SUBJECT_ID, subjectId);
            intent.putExtra("Screen", screenName);
            context.startActivity(intent);

            Map<String, String> params = new HashMap<>();
            params.put("type", StringUtils.isNotEmpty(type) ? type : "");
            params.put("subjectId", StringUtils.isNotEmpty(subjectId) ? subjectId : "");
            params.put("Screen", StringUtils.isNotEmpty(screenName) ? screenName : "");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_list_screen_view.getActionName(), params);
        } catch (Exception ignored) {
        }
    }

    public static void navigateToChapterListBySubjectActivity(Context context, String title) {
        Intent intent = new Intent(context, ChapterListBySubjectsActivity.class);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    public static void navigateToWebViewByUrlParams(Context context, String url,
                                                    Map<String, Object> params, boolean appendUserId,
                                                    boolean appendToken, boolean appendClassId,
                                                    boolean appendCourseId, boolean appendBoard, boolean
                                                            isBackRestricted, String backMessage,
                                                    boolean appendUserName, boolean appendGender,
                                                    boolean allowOrientation,
                                                    boolean appendUserImageUrl,
                                                    Bundle savedBundle, String screenName, String miniVideoUrl) {
        Bundle extras = new Bundle();
        extras.putString("url", url);
        extras.putSerializable("params", (Serializable) params);
        extras.putBoolean("appendUserId", appendUserId);
        extras.putBoolean("appendToken", appendToken);
        extras.putBoolean("appendClassId", appendClassId);
        extras.putBoolean("appendCourseId", appendCourseId);
        extras.putBoolean("appendBoard", appendBoard);
        extras.putBoolean("isBackRestricted", isBackRestricted);
        extras.putString("onBackMessage", backMessage);
        extras.putBoolean("appendUserName", appendUserName);
        extras.putBoolean("appendUserGender", appendGender);
        extras.putBoolean("allowOrientation", allowOrientation);
        extras.putBoolean("appendUserImageUrl", appendUserImageUrl);
        extras.putString("miniVideoUrl", miniVideoUrl);
        if (savedBundle != null) {
            extras.putBundle("savedBundle", savedBundle);
        }

        boolean isNameEmpty = StringUtils.isEmpty(MyApplication.getStringData("name"));
        boolean isLiveScreen = "LIVE".equals(screenName);
        boolean isVirtualChatScreen = "VIRTUAL_CHAT".equals(screenName);

        Log.d("NavigationCheck", "Name Empty: " + isNameEmpty);
        Log.d("NavigationCheck", "Screen Name: " + screenName);
        Log.d("NavigationCheck", "isLiveScreen: " + isLiveScreen);
        Log.d("NavigationCheck", "isVirtualChatScreen: " + isVirtualChatScreen);

// Fixing condition with proper grouping
        if (isNameEmpty && (isLiveScreen || isVirtualChatScreen)) {

            Log.d("NavigationCheck", "Condition matched. Redirecting to NameInputActivity");

            String paramsJson = new Gson().toJson(params != null ? params : new HashMap<>());
            Log.d("NavigationCheck", "Params JSON: " + paramsJson);

            extras.putString("params", paramsJson);

            NameInputDto dto = NameInputDto.builder()
                    .nextScreenClass(WebViewByUrlActivity.class.getName())
                    .screen(screenName)
                    .extras(CommonMethods.getBundleParams(extras))
                    .build();

            Log.d("NavigationCheck", "DTO Created: " + dto);

            Intent intent = new Intent(context, NameInputActivity.class);
            intent.putExtra("activity_params", dto);

            Log.d("NavigationCheck", "Starting NameInputActivity");

            context.startActivity(intent);

        }
        else {
            Intent intent = new Intent(context, WebViewByUrlActivity.class);
            intent.putExtras(extras);
            Log.d(TAG, "navigateToWebViewByUrlParams: "+extras.toString());
            context.startActivity(intent);
        }
    }

    public static void navigateToLiveClassWebView(Context context, String chapterName, String subjectName, String subjectCode, String chapterId, String chapterCode, String chapterThumbnail, String facultyName, String fromScreen, Bundle savedBundle) {
        Bundle bundle = new Bundle();
        bundle.putString("chapterName", chapterName);
        bundle.putString("subjectCode", subjectCode);
        bundle.putString("subjectName", subjectName);
        bundle.putString("chapterId", chapterId);
        bundle.putString("chapterCode", chapterCode);
        bundle.putString("chapterThumbnail", CommonMethods.encodeUrl(chapterThumbnail));
        bundle.putString("facultyName", facultyName);
        Log.d(TAG, "navigateToLiveClassWebView:-bundle"+CommonMethods.getBundleParams(bundle));

        if(savedBundle!=null)
            CommonMethods.copyBundle(savedBundle, bundle);
        Map<String, Object> webViewParams = CommonMethods.getBundleParams(bundle);
        String url = CommonMethods.getPersonalizedPathUrl() +
                AdaptivePath.LIVE_CLASS.path();

        navigateToWebViewByUrlParams(context,
                url,
                webViewParams,
                true, true,
                false, false,
                false, false,
                null, true, true, true, true, savedBundle,"LIVE",null);

        Map<String, String> params = new HashMap<>(CommonMethods.convertToStringMap(CommonMethods.getBundleParams(bundle)));
        params.put("cardType", "live");
        params.put("source", fromScreen);
//        MyApplication.logFirebaseBundleCreation("todays_task_card_clicked", params);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.live_class_web_view.getActionName(), params);
    }

    public static void navigateToVirtualChat(Context context) {
        CommonNavigator.navigateToWebViewByUrlParams(context,
                CommonMethods.getPersonalizedPathUrl() + AdaptivePath.VIRTUAL_BATCH.path(),
                null,
                true,
                true,
                false,
                false,
                false,
                false,
                null,
                true,
                false,
                false,
                false,
                null,
                "VIRTUAL_CHAT",
                null
        );
        Map<String, String> params = new HashMap<>();
        params.put("source", "Home Screen");
        params.put("userId", MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        params.put("Board", MyApplication.getStringData(PreferenceKeys.BOARD.getKey()));
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_virtual_chat_bottom_navigation.getActionName(), params );
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.VIRTUAL_BATCH_SCREEN.name(), AnalyticsEvent.VIRTUAL_BATCH_SCREEN.name());
    }
//    public static void navigateToSubscriptionScreenV2(Context context) {
//        try {
//            Intent intent = new Intent(context, CampaignSubscriptionActivity.class);
//            context.startActivity(intent);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    /**
     * Open the doubt chat screen with a specific subject for a new doubt.
     * Used when the user selects a subject before asking a doubt.
     */
    public static void navigateToDoubtChatWithSubject(Context context, String subject, String subjectId, String subjectName, boolean allowBack) {
        boolean redirectToFullWebView = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_redirect_to_full_webview");
        Intent intent = new Intent(context, redirectToFullWebView ? WebviewDoubtChatScreen.class : DoubtChatScreen.class);
        intent.putExtra("subject", subject);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("subjectName", subjectName);
        intent.putExtra("allowBack", allowBack);
        intent.putExtra("openOldChat", false);
        context.startActivity(intent);
    }

    /**
     * Open an existing doubt chat session by session ID.
     * Used from previous-doubt lists and notification status views.
     */
    public static void navigateToDoubtChatSession(Context context, String chatSessionId, boolean allowBack) {
        boolean redirectToFullWebView = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_redirect_to_full_webview");
        Intent intent = new Intent(context, redirectToFullWebView ? WebviewDoubtChatScreen.class : DoubtChatScreen.class);
        intent.putExtra("chatSessionId", chatSessionId);
        intent.putExtra("allowBack", allowBack);
        intent.putExtra("openOldChat", false);
        if (!(context instanceof android.app.Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * Open a doubt chat session navigated from the doubt history screen.
     */
    public static void navigateToDoubtChatFromHistory(Context context, String chatSessionId, boolean allowBack, String type, String status, boolean navigatingFromMyDoubt) {
        boolean redirectToFullWebView = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_redirect_to_full_webview");
        Intent intent = new Intent(context, redirectToFullWebView ? WebviewDoubtChatScreen.class : DoubtChatScreen.class);
        intent.putExtra("chatSessionId", chatSessionId);
        intent.putExtra("allowBack", allowBack);
        intent.putExtra("type", type);
        intent.putExtra("status", status);
        intent.putExtra("navigatingFromMyDoubt", navigatingFromMyDoubt);
        intent.putExtra("openOldChat", false);
        context.startActivity(intent);
    }

    /**
     * Open the doubt chat screen for a mock-test question doubt.
     */
    public static void navigateToDoubtChatMockTest(Context context, String chatSessionId, String subject, String question, String answer) {
        boolean redirectToFullWebView = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_mocktest_redirect_to_full_webview");
        Intent intent = new Intent(context, redirectToFullWebView ? WebviewDoubtChatScreen.class : DoubtChatScreen.class);
        intent.putExtra("chatSessionId", chatSessionId);
        intent.putExtra("subject", subject);
        intent.putExtra("allowBack", true);
        intent.putExtra("isMockTestDoubt", true);
        intent.putExtra("question", question);
        intent.putExtra("answer", answer);
        intent.putExtra("openOldChat", false);
        context.startActivity(intent);
    }

    /**
     * Open the doubt chat screen with a pre-loaded AI response (e.g., from PDF Circle).
     */
    public static void navigateToDoubtChatWithResponse(Context context, String subject,
                                                       String userQuery, String response,
                                                       String title, String extractedText, String screenshotUrl,
                                                       boolean showResponseBubble, String origin,
                                                       String requestId, String pdfTitle, int pageNumber,
                                                       String pageContext) {
        Intent intent = new Intent(context, WebviewDoubtChatScreen.class);
        intent.putExtra("subject", subject);
        intent.putExtra("userQuery", userQuery);
        intent.putExtra("response", response);
        intent.putExtra("title", title);
        intent.putExtra("extractedText", extractedText);
        intent.putExtra("screenshotUrl", screenshotUrl);
        intent.putExtra("showResponseBubble", showResponseBubble);
        intent.putExtra("origin", origin);
        intent.putExtra("requestId", requestId);
        intent.putExtra("openOldChat", false);
        intent.putExtra("pdfTitle",pdfTitle);
        intent.putExtra("pageNumber",pageNumber);
        intent.putExtra("pageContext",pageContext);
        context.startActivity(intent);
        Log.d(TAG, "navigateToDoubtChatWithResponse: "+CommonMethods.getIntentParams(intent));
    }

    /**
     * Open the doubt chat screen in pseudo-live mode from the video player.
     */
    public static void navigateToDoubtChatPseudoLive(Context context, String chatSessionId, String subject, String doubtText, boolean videoRequest) {
        boolean redirectToFullWebView = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_mocktest_redirect_to_full_webview");
        Intent intent = new Intent(context, redirectToFullWebView ? WebviewDoubtChatScreen.class : DoubtChatScreen.class);
        intent.putExtra("chatSessionId", chatSessionId);
        intent.putExtra("pseudoLive", true);
        intent.putExtra("allowBack", true);
        intent.putExtra("subject", subject);
        intent.putExtra("doubt_text", doubtText);
        intent.putExtra("videoRequest", videoRequest);
        intent.putExtra("openOldChat", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Bring an existing WebviewDoubtChatScreen instance to the top (used for back-navigation from HomeScreen).
     */
    public static void navigateToDoubtChatClearTop(Context context) {
        Intent intent = new Intent(context, WebviewDoubtChatScreen.class);
        intent.putExtra("openOldChat", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    public static void navigateToDoubtChatFromCQNQ(
            Context context,
            String chatSessionId,
            String subject,
            String question,
            String studentAnswer,
            String idealAnswer,
            String analysisOfAnswer,
            String explanationOfQuestion,
            String obtainedMarks,
            String totalMarks,
            String questionType,
            String imageExplanationLatex,
            String imageQuestion,
            String imageAnswer,
            String chapterCode,
            String microlectureCode
    ) {

        Intent intent = new Intent(context, WebviewDoubtChatScreen.class);
        intent.putExtra("openDoubtChatForCqnq", true);
        intent.putExtra("subject", subject);
        intent.putExtra("allowBack", true);
        intent.putExtra("isMockTestDoubt", true);
        intent.putExtra("question", question);
        intent.putExtra("answer", studentAnswer); // student's submitted answer
        intent.putExtra("idealAnswer", idealAnswer);
        intent.putExtra("questionType", questionType);
        intent.putExtra("analysisOfAnswer", analysisOfAnswer); // from response (HTML string)
        intent.putExtra("explanationOfQuestion", explanationOfQuestion); // from response
        intent.putExtra("obtainedMarks", obtainedMarks); // response "marks"
        intent.putExtra("totalMarks", totalMarks); // response "totalMarks"
        intent.putExtra("chatSessionId", chatSessionId);
        intent.putExtra("openOldChat", false);

        intent.putExtra("imageExplanationLatex", imageExplanationLatex);
        intent.putExtra("imageQuestion", imageQuestion);
        intent.putExtra("imageAnswer", imageAnswer);
        intent.putExtra("chapterCode", chapterCode);
        intent.putExtra("microlectureCode", microlectureCode);
        Log.d(TAG, "navigateToDoubtChatFromCQNQ: "
                + CommonMethods.getIntentParams(intent));
        context.startActivity(intent);
    }




}
