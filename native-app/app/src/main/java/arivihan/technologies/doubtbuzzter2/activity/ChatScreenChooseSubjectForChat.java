package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_HISTORY;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.DOUBT_CHAT;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CategorizePreviousDoubtListAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.DoubtCategoryObject;
import arivihan.technologies.doubtbuzzter2.model.ArivihanSession;
import arivihan.technologies.doubtbuzzter2.model.ArivihanSessionHistoryData;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.WebChatRecord;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChatScreenChooseSubjectForChat extends BaseActivity implements
        SubjectSelectionAdapter.SubjectClickListener {

    LinearLayout newChatBtn;
    TextView chatUserName,disclaimer_dialog_title;
    ImageView hamburgIcon;
    DrawerLayout drawerLayout;
    CharSequence formattedText = null;
    Dialog instantGuruDisclaimerDialog;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView previousDoubtRecyclerView;
    Gson gson = new Gson();
    RecyclerView subjectSelectionRecylerView;
    List<SubjectResponse> subjectModels;
    SubjectSelectionAdapter subjectSelectionAdapter;
    RelativeLayout drawerSubscriptionLayout;
    boolean allowBack;
    private HomeScreenViewModel homeScreenViewModel;
    private final String TAG = ChatScreenChooseSubjectForChat.class.getSimpleName();
    private ArrayList<DoubtCategoryObject> lists = new ArrayList<>();
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private CardView how_to_card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen_choose_subject_for_chat);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                } else if (howToVideoView != null
                        && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE
                        && howToVideoView.expanded) {
                    howToVideoView.resize();
                }
                finish();
            }
        });

        MyApplication myapp = (MyApplication) getApplication();
        homeScreenViewModel = myapp.getHomeScreenViewModel();
        allowBack = getIntent().getBooleanExtra("allowBack", false);
        LinearLayout instantGuruDisclaimerLayout = findViewById(R.id.instant_guru_disclaimer_layout);
        TextView instantGuruDisclaimerText = findViewById(R.id.instant_guru_disclaimer_text);
        if(MyApplication.getFirebaseRemoteConfig().getBoolean("show_instant_guru_new_disclaimer")) {
            instantGuruDisclaimerLayout.setVisibility(View.VISIBLE);

            String disclaimerText = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.disclaimer_text) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            SpannableString spannableString = new SpannableString(disclaimerText);

            spannableString.setSpan(new UnderlineSpan(), 0, disclaimerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            instantGuruDisclaimerText.setText(spannableString);
        }
        how_to_card = findViewById(R.id.how_to_card);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_card_doubt") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos") )
            how_to_card.setVisibility(View.GONE);
        how_to_video_view = findViewById(R.id.how_to_video_view);
        hamburgIcon = findViewById(R.id.hamburg_icon);
        newChatBtn = findViewById(R.id.new_chat_btn);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerSubscriptionLayout = findViewById(R.id.subscription_layout);

        previousDoubtRecyclerView = findViewById(R.id.previous_doubts_recycler_view);
        chatUserName = findViewById(R.id.chat_user_name);


        subjectModels = new ArrayList<>();
        subjectSelectionRecylerView = findViewById(R.id.doubt_chat_recycler_view);
        subjectSelectionRecylerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        subjectSelectionAdapter = new SubjectSelectionAdapter(this, subjectModels, this);
        subjectSelectionRecylerView.setAdapter(subjectSelectionAdapter);
        getListOfPreviousDoubts();
        getSubscribedSubjects();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerToggle.syncState();

        chatUserName.setText(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));


        hamburgIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        instantGuruDisclaimerLayout.setOnClickListener(v->{
            initializeInstantGuruDisclaimerDialog();
        });

        if (getIntent().getBooleanExtra("subscriptionActive", false)) {
            drawerSubscriptionLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) drawerSubscriptionLayout.getLayoutParams();
            layoutParams.height = 0;
            drawerSubscriptionLayout.setLayoutParams(layoutParams);
        }


        findViewById(R.id.chat_history_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, DoubtHistoryActivity.class);
            destroyHowTo();
            startActivity(intent);
        });

        findViewById(R.id.subscribe_now_btn).setOnClickListener(v -> {
//            Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
//            intent.putExtra("plan_level", "COURSE");
            destroyHowTo();
//            startActivity(intent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.DOUBT_CHAT_JOIN);

        });

        how_to_card.setOnClickListener(view -> {
            logFirebaseEvent();
            howToVideoView = HowToVideoView.getInstance(this);
            if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                howToVideoView.setView(this, how_to_video_view);
            }
            howToVideoView.setType(DOUBT_CHAT, true);
            howToVideoView.setButtonText(getString(R.string.Ask_Now));
        });

        int count = MyApplication.getIntData(DOUBT_CHAT.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        Log.d("debugcount", "getHowToVideoType: " + count + " type: " + DOUBT_CHAT.name());
        if (count < 0) count = 0;
        if (count <= 1) {
            howToVideoView = HowToVideoView.getInstance(this);

            if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                howToVideoView.setView(this, how_to_video_view);
            }

            if (howToVideoView.videoView.getVisibility() != View.VISIBLE) {
                howToVideoView.setType(DOUBT_CHAT, false); // collapsed
                howToVideoView.setButtonText(getString(R.string.Ask_Now));
                MyApplication.saveData(DOUBT_CHAT.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count + 1);
            }
        }
    }

    private void logFirebaseEvent(){
        Map<String,String> params = new HashMap<>();
        params.put("action", "doubt_how_to_card_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.doubt_how_to_card_click.getActionName(), params);
    }

    private void initializeInstantGuruDisclaimerDialog() {
        instantGuruDisclaimerDialog = new Dialog(this);
        instantGuruDisclaimerDialog.setContentView(R.layout.instant_guru_disclaimer_dialog);
        instantGuruDisclaimerDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        instantGuruDisclaimerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        instantGuruDisclaimerDialog.setCancelable(false);

         CardView closeDialog=instantGuruDisclaimerDialog.findViewById(R.id.dialog_close);
         TextView disclaimerButtonText=instantGuruDisclaimerDialog.findViewById(R.id.subs);

         disclaimer_dialog_title=instantGuruDisclaimerDialog.findViewById(R.id.disclaimer_dialog_title);
         disclaimer_dialog_title.setText(MyApplication.getFirebaseRemoteConfig()
                 .getString(getString(R.string.disclaimer_dialog_title) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));

         disclaimerButtonText.setText(MyApplication.getFirebaseRemoteConfig()
                 .getString(getString(R.string.disclaimer_button_text) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));

         TextView dialogMessages = instantGuruDisclaimerDialog.findViewById(R.id.disclaimer_message);

        String disclaimerDialogMessage = MyApplication.getFirebaseRemoteConfig()
                .getString(getString(R.string.disclaimer_dialog_message) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedText = Html.fromHtml(disclaimerDialogMessage, Html.FROM_HTML_MODE_LEGACY);
        }else {
            formattedText = Html.fromHtml(disclaimerDialogMessage);
        }
        dialogMessages.setText(formattedText);
        instantGuruDisclaimerDialog.show();
         closeDialog.setOnClickListener(v->{
             if(instantGuruDisclaimerDialog!=null) {
                 instantGuruDisclaimerDialog.dismiss();
             }
         });
    }

    private void divideInCategory(List<WebChatRecord> list) {

        Map<String, ArrayList<WebChatRecord>> map = new HashMap<>();
        lists.clear();

        for (WebChatRecord record : list) {

            String type = record.getHistoryCategory();

            if (!map.containsKey(type)) {
                map.put(type, new ArrayList<>());
            }

            map.get(type).add(record);
        }

        for (Map.Entry<String, ArrayList<WebChatRecord>> entry : map.entrySet()) {
            lists.add(new DoubtCategoryObject(entry.getKey(), entry.getValue()));
        }

        Collections.reverse(lists);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getSubscribedSubjects() {
        subjectModels.clear();
        homeScreenViewModel.getSubjectList().observe(this, this::updateSubject);
    }

    private void updateSubject(List<SubjectResponse> subjectModels) {

        Log.d(TAG, "updateSubject: " + subjectModels);
        if (subjectModels != null) {
            this.subjectModels.clear();
            this.subjectModels.addAll(subjectModels);
            subjectSelectionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(howToVideoView!=null && howToVideoView.videoView!=null) {
            howToVideoView.resumePlayer();
        }
        getSubscribedSubjects();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(howToVideoView!=null && howToVideoView.videoView!=null)
            howToVideoView.pausePlayer();
    }

    private void getListOfPreviousDoubts() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("subjectId", "");
        map.put("sortOrder", false);
        map.put("query", "");

        MyJsonObjectRequest feedbackRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_CHAT_HISTORY, map), null,
                response -> {
                    Log.d(TAG, "Response received: " + response);
                    TypeToken<ArivihanSessionHistoryData> token = new TypeToken<ArivihanSessionHistoryData>() {
                    };
                    ArivihanSessionHistoryData historyData = gson.fromJson(response.toString(), token.getType());
                    List<WebChatRecord> records = new ArrayList<>();
                    if (historyData != null && historyData.getList() != null) {
                        for (ArivihanSession session : historyData.getList()) {
                            records.add(session.toWebChatRecord());
                        }
                    }
                    divideInCategory(records);
                    previousDoubtRecyclerView.setNestedScrollingEnabled(false);
                    previousDoubtRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    CategorizePreviousDoubtListAdapter previousDoubtListAdapter = new CategorizePreviousDoubtListAdapter(getApplicationContext(), lists, true);
                    previousDoubtRecyclerView.setAdapter(previousDoubtListAdapter);
                    if (records.isEmpty()) {
                        TextView textView = findViewById(R.id.previous_chat_text);
                        textView.setText(getString(R.string.no_chat_history));
                        findViewById(R.id.chat_history_btn).setVisibility(View.GONE);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        feedbackRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(feedbackRequest);
    }

    @Override
    public void subjectClicked(SubjectResponse subjectModel) {
        Map<String, String> params = new HashMap<>();
        params.put("subject", subjectModel.getSubjectCode());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.ask_doubt_subject_clicked.getActionName(), params);
        destroyHowTo();
        CommonNavigator.navigateToDoubtChatWithSubject(this, subjectModel.getSubjectCode(), subjectModel.id, subjectModel.subjectName, allowBack);
        finish();
    }

    private void destroyHowTo(){
        how_to_video_view.setVisibility(View.GONE);
        if(howToVideoView!=null)
            howToVideoView.remove();
    }

    @Override
    public void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        try {
            homeScreenViewModel.getSubjectList().removeObservers(this);
        } catch (Exception e) {

        }
        if(instantGuruDisclaimerDialog!=null && instantGuruDisclaimerDialog.isShowing()){
            instantGuruDisclaimerDialog.dismiss();
            instantGuruDisclaimerDialog=null;
        }

        super.onDestroy();
    }

}