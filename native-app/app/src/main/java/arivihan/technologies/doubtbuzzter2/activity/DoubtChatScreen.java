package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.MyApplication.getPhoneNumber;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_FEEDBACK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_HISTORY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_PREVIOUS_DOUBTS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_RESUME;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_REQUEST_VIDEO_SOLUTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.IMAGE_UPLOAD;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.OPTION_CLICK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_DOUBT_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_UNWATCHED_VIDEO;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DoubtChatDisambiguationAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.PreviousDoubtListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.GlobalDialogs;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.SpeechRecognitionHelper;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentHeightRecyclerView;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.DoubtChatActivityActions;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.model.DoubtBotResponse;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatConstants;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatLimitationCheckDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtSolutionStatus;
import arivihan.technologies.doubtbuzzter2.model.DoubtUserRequest;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.OptionResponse;
import arivihan.technologies.doubtbuzzter2.model.WebChatMessageDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class DoubtChatScreen extends BaseActivity implements DoubtChatActivityActions {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    ImageView hamburgIcon;
    EditText promptInput;
    TextView chatUserName;
    ImageView micBtn, cameraBtn, sendPromptBtn;
    RelativeLayout chatInputContainer, drawerSubscriptionLayout;
    LinearLayout chatContainer, botChatLoader;
    ScrollView chatScrollView;
    Gson gson = new Gson();
    List<File> imagesToUpload = new ArrayList<>();
    String chatSessionId;
    RecyclerView previousDoubtRecyclerView;
    boolean isWaitingForResponse, isDoubtSolvedVideoSeen = true, isDoubtImageUploaded = false;
    Integer indexOfDisambiguationItemClicked = null;
    DoubtBotResponse recentDoubtResponse, recentNonOptionDoubtResponse;
    private Uri imageuri;
    boolean firstDoubt;
    Boolean isImageFirstDoubt = true;
    boolean giveOption, isSubscribedUser;
    String previousMessage, firstMessage;
    Snackbar snackbar;
    private Timer doubtSolutionReadyTimer;
    private CardView chatScrollBottomBtn;

    private ShimmerFrameLayout chatLoadShimmer;

    private List<String> nonClickableOptionsList = new ArrayList<>();
    boolean pseudoLive, isStepWiseSolution;
    private static final long BACK_PRESS_THRESHOLD = 2000;
    private long backPressedTime;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_CODE = 118;

    Dialog image_pick_dialog;

    DoubtChatLimitationCheckDto chatLimitationCheckDto;
    int chatCountInSession = 0;
    boolean maxChatSessionLimitExceeded = false;
    boolean isVideoFeedback = false;

    String doubtTypeOptionResponseId, doubtTypeOptionSelected;

    boolean allowBack;
    boolean update;
    String status;
    boolean hasVideoOrImage, hasOptions, hasReceivedSolutionResponseOptionType;
    String doubtText;
    private Handler handler;
    private Runnable runnable;

    private Handler refreshScreenOnDoubtNotificationReceiveHanlder;
    private Runnable refreshScreenOnDoubtNotificationReceiveRunnable;

    private String dialogWebViewContent;

    private List<WebView> responseWebviews = new ArrayList<>();
    NetworkStatusLiveData networkStatusLiveData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt_chat_screen);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(DoubtChatScreen.this, HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                if (allowBack) {
                    Intent back = new Intent();
                    back.putExtra("update", update);
                    setResult(2, back);
                    finish();
                } else if (isDoubtSolvedVideoSeen) {
                    startActivity(intent);
                } else {
                    if (backPressedTime + BACK_PRESS_THRESHOLD > System.currentTimeMillis()) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "You haven't seen solved doubt video yet.", Toast.LENGTH_SHORT).show();
                    }
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });

        hamburgIcon = findViewById(R.id.hamburg_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        chatContainer = findViewById(R.id.chat_container);
        chatScrollView = findViewById(R.id.chat_scollview);
        promptInput = findViewById(R.id.prompt_input);
        promptInput.setFilters(MyEmojiFilter.getFilter());
        micBtn = findViewById(R.id.open_mic_btn);
        cameraBtn = findViewById(R.id.take_photo_btn);
        sendPromptBtn = findViewById(R.id.send_prompt_btn);
        botChatLoader = findViewById(R.id.bot_chat_loader);
        previousDoubtRecyclerView = findViewById(R.id.previous_doubts_recycler_view);
        chatUserName = findViewById(R.id.chat_user_name);
        chatScrollBottomBtn = findViewById(R.id.chat_scroll_down_btn);
        chatLoadShimmer = findViewById(R.id.chat_load_shimmer);
        chatInputContainer = findViewById(R.id.chat_input_container);
        drawerSubscriptionLayout = findViewById(R.id.subscription_layout);
        allowBack = getIntent().getBooleanExtra("allowBack", false);
        status = getIntent().getStringExtra("status");
        doubtText = getIntent().getStringExtra("doubt_text");
        pseudoLive = getIntent().getBooleanExtra("pseudoLive", false);
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> chatScrollView.fullScroll(View.FOCUS_DOWN);
        if (pseudoLive) {
            findViewById(R.id.new_chat_btn).setVisibility(View.GONE);
        }

        if (getIntent().getBooleanExtra("isMockTestDoubt", false)) {
            findViewById(R.id.new_chat_btn).setVisibility(View.GONE);
        }

        findViewById(R.id.new_chat_btn).setOnClickListener((v) -> {
            if (chatLimitationCheckDto != null) {
                if (chatLimitationCheckDto.isSubscriptionStatus() || chatLimitationCheckDto.getChatSessionsCount() < chatLimitationCheckDto.getSessionsLimitPerUser()) {
                    Intent intent = new Intent(this, ChatScreenChooseSubjectForChat.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Dialog dialog2 = new Dialog(this);
                    dialog2.setContentView(R.layout.custom_dialog_lock_box);
                    dialog2.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                    dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog2.setCancelable(false);
                    TextView dialogText = dialog2.findViewById(R.id.header);
                    dialogText.setText(getResources().getString(R.string.chat_limit_reached_popup_text));
                    MaterialCardView subscriptionLayout = dialog2.findViewById(R.id.subscriptionLayout);

                    subscriptionLayout.setOnClickListener(vw -> {
                        dialog2.cancel();
                        dialog2.dismiss();
//                        Intent intent = new Intent(getApplicationContext(), CampaignSubscriptionActivity.class);
//                        intent.putExtra("plan_level", "COURSE");
//                        startActivity(intent);
                        SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.DOUBT_CHAT_JOIN);
                    });

                    ImageView cut2 = dialog2.findViewById(R.id.cancel);
                    cut2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog2.dismiss();
                        }
                    });

                    dialog2.show();
                }

            }
        });


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerToggle.syncState();

        findViewById(R.id.chat_history_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, DoubtChatHistoryActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.subscribe_now_btn).setOnClickListener(v -> {
//            Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
//            intent.putExtra("plan_level", "COURSE");
//            startActivity(intent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.DOUBT_CHAT_JOIN);
        });

        chatUserName.setText(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));

        hamburgIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        sendPromptBtn.setOnClickListener(v -> {
            if ((!isSubscribedUser && maxChatSessionLimitExceeded) ||
                    (chatLimitationCheckDto != null && !isSubscribedUser &&
                            chatCountInSession >= chatLimitationCheckDto.getMessageLimitPerSession()) ||
                    (chatLimitationCheckDto != null && isSubscribedUser &&
                            chatCountInSession >= chatLimitationCheckDto.getMessageLimitPerSessionForSubscribedUser()) && !isStepWiseSolution) {
                redirectIfChatLimitExceed();
                return;
            }

            if (isWaitingForResponse) {
                Toast.makeText(this, R.string.waiting_for_response, Toast.LENGTH_SHORT).show();
                return;
            }

            String promptText = promptInput.getText().toString();
            if (!MyApplication.isNetworkAvailable()) {
                Toast.makeText(this, R.string.NoInternetConnection, Toast.LENGTH_SHORT).show();
                return;
            }
            if (promptText.isEmpty()) {
                Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_SHORT).show();
            } else if (promptText.split(" ").length < 3 && !isStepWiseSolution) {
                Toast.makeText(this, getString(R.string.please_describe_more), Toast.LENGTH_SHORT).show();
            } else if (promptText.split(" ").length > 200) {
                Toast.makeText(this, "Maximum 200 words allowed.", Toast.LENGTH_SHORT).show();
            } else {
                if (indexOfDisambiguationItemClicked != null && indexOfDisambiguationItemClicked == 1) {
                    promptInput.setText("");
                    requestNewVideo(doubtTypeOptionResponseId, chatSessionId, promptText, false);
                    indexOfDisambiguationItemClicked = null;
                    isWaitingForResponse = true;
                } else {

                    if (firstMessage == null) {
                        firstMessage = promptText;
                    }

                    if (firstDoubt) {
                        hasReceivedSolutionResponseOptionType = true;
                        giveOption = true;
                        doubtChatRequest(firstDoubt, firstMessage, DoubtChatConstants.DoubtRequestType.HTML, "");
                        firstDoubt = false;
                        giveOption = false;

                    } else {
                        doubtChatRequest(false, promptText, DoubtChatConstants.DoubtRequestType.HTML, "");
                    }
                }
            }
        });

        if (getIntent().getBooleanExtra("isMockTestDoubt", false)) {
            cameraBtn.setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) micBtn.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            micBtn.setLayoutParams(layoutParams);
        }

        cameraBtn.setOnClickListener(v -> {
            if (isDoubtImageUploaded || !firstDoubt) {
                Toast.makeText(this, R.string.to_upload_an_image_please_click_on_new_chat, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!MyApplication.isNetworkAvailable()) {
                Toast.makeText(this, "Oops! No Network", Toast.LENGTH_SHORT).show();
                return;
            }

            image_pick_dialog.show();
        });

        micBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                SpeechRecognitionHelper speechRecognitionHelper = new SpeechRecognitionHelper(this);
                speechRecognitionHelper.initializeSpeechRecognition(promptInput);
                speechRecognitionHelper.setVoiceSearchDialog();
                speechRecognitionHelper.startListening();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showPermissionExtraInfoDialog(Manifest.permission.RECORD_AUDIO);
                } else {
                    requestPermissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO});
                }
            }
        });

        getListOfPreviousDoubts();
        checkSubscription();

        Uri deepLink = getIntent().getData();

        String notificationChatSessionId = null;
        if (deepLink != null) {
            notificationChatSessionId = deepLink.getQueryParameter("chatSessionId");
        }


        if (getIntent().getStringExtra("chatSessionId") == null && notificationChatSessionId == null) {
            firstDoubt = true;
            doubtChatRequest(true, null, DoubtChatConstants.DoubtRequestType.HTML, "");
        } else {
            if (notificationChatSessionId != null) {
                chatSessionId = notificationChatSessionId;
                resumeChat(notificationChatSessionId);
            } else {
                String sessionId = getIntent().getStringExtra("chatSessionId");
                chatSessionId = sessionId;
                resumeChat(sessionId);
            }
        }

        doubtSolutionReadyTimer = new Timer();
        doubtSolutionReadyTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handleDoubtSolutionReady();
            }
        }, 0, 200);

        chatScrollBottomBtn.setOnClickListener(v -> {
            handleScrollToBottom();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            chatScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if ((chatScrollView.getChildAt(0).getHeight() - chatScrollView.getHeight()) / 2 > scrollY) {
                    chatScrollBottomBtn.setVisibility(View.VISIBLE);
                } else {
                    chatScrollBottomBtn.setVisibility(View.GONE);
                }
            });
        }

        setPickerDialog();


        refreshScreenOnDoubtNotificationReceiveHanlder = new Handler();
        refreshScreenOnDoubtNotificationReceiveRunnable = new Runnable() {
            @Override
            public void run() {
                if (MyApplication.getBoolData(PreferenceKeys.REFRESH_DOUBT_CHAT_SCREEN.getKey())) {
                    resumeChat(chatSessionId);
                    MyApplication.removeString(PreferenceKeys.REFRESH_DOUBT_CHAT_SCREEN.getKey());
                    refreshScreenOnDoubtNotificationReceiveHanlder.removeCallbacks(refreshScreenOnDoubtNotificationReceiveRunnable);
                }

                refreshScreenOnDoubtNotificationReceiveHanlder.postDelayed(refreshScreenOnDoubtNotificationReceiveRunnable, 500);
            }
        };

        refreshScreenOnDoubtNotificationReceiveRunnable.run();

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(drawerLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(drawerLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }

    private void redirectIfChatLimitExceed() {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View contactWhatsapp = layoutInflater.inflate(R.layout.tutor_support_whatsapp_contact, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.END;
        layoutParams.setMargins(0, 8, 0, 8);

        contactWhatsapp.setLayoutParams(layoutParams);

        if (isSubscribedUser) {
            CardView openWhatsappBtn = contactWhatsapp.findViewById(R.id.open_whatsapp_btn);
            openWhatsappBtn.setOnClickListener(V -> {
                openWhatsApp(getPhoneNumber(), promptInput.getText().toString());
            });

        } else {
            TextView subscriptionText = contactWhatsapp.findViewById(R.id.subscription_text);
            subscriptionText.setText(R.string.chat_limit_in_session_exceeded);

            CardView openWhatsappBtn = contactWhatsapp.findViewById(R.id.open_whatsapp_btn);
            openWhatsappBtn.setVisibility(View.GONE);

            CardView openSubscription = contactWhatsapp.findViewById(R.id.open_subscription);
            openSubscription.setVisibility(View.VISIBLE);
            openSubscription.setOnClickListener(V -> {
//                Intent intent = new Intent(DoubtChatScreen.this, CampaignSubscriptionActivity.class);
//                intent.putExtra("plan_level", "COURSE");
//                startActivity(intent);
                SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.DOUBT_CHAT);
            });
        }

        if (chatContainer.findViewById(R.id.block_chat_and_redirect_to_whatsapp) == null) {
            chatInputContainer.removeAllViews();
            chatInputContainer.setMinimumHeight(300);
            RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.setMargins(24, 8, 24, 8);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            chatInputContainer.setLayoutParams(relativeLayoutParams);
            chatInputContainer.addView(contactWhatsapp);
            handleScrollToBottom();
        }
    }

    public void showPermissionExtraInfoDialog(String permission) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("This app needs you to allow this permission in order to function.Will you allow it");
        alertDialogBuilder.setPositiveButton("Yes",
                (arg0, arg1) -> requestPermissionLauncher.launch(new String[]{permission}));

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void openWhatsApp(String number, String message) {
        try {
            String encodedMessage = Uri.encode(message);
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + number + "&text=" + encodedMessage);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.setPackage("com.whatsapp");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }

    }


    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {

        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            boolean isGranted = entry.getValue();

            if (permission.equals(Manifest.permission.CAMERA) && isGranted) {
                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            }

            if (permission.equals(Manifest.permission.RECORD_AUDIO) && isGranted) {
                SpeechRecognitionHelper speechRecognitionHelper = new SpeechRecognitionHelper(this);
                speechRecognitionHelper.initializeSpeechRecognition(promptInput);
                speechRecognitionHelper.setVoiceSearchDialog();
                speechRecognitionHelper.startListening();
            }

        }

    });

    public void setPickerDialog() {
        image_pick_dialog = new Dialog(this);
        image_pick_dialog.setContentView(R.layout.action_popup_layout);
        image_pick_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        image_pick_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.setCancelable(false);
//        image_pick_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        image_pick_dialog.findViewById(R.id.close_btn).setOnClickListener(view -> image_pick_dialog.cancel());

        TextView title = image_pick_dialog.findViewById(R.id.title);
        title.setText(getString(R.string.select_images));
        TextView subtitle = image_pick_dialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);

        CardView galleryBtn = image_pick_dialog.findViewById(R.id.btn1);
        CardView cameraBtn = image_pick_dialog.findViewById(R.id.btn2);
        TextView galleryBtnText = image_pick_dialog.findViewById(R.id.btn1_text);
        galleryBtnText.setText(getString(R.string.choose_from_gallery));
        TextView cameraBtnText = image_pick_dialog.findViewById(R.id.btn2_text);
        cameraBtnText.setText(getString(R.string.click_a_photo));

        LottieAnimationView lottieAnimationView = image_pick_dialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        ImageView selectionImage = image_pick_dialog.findViewById(R.id.selection_image);
        selectionImage.setVisibility(View.VISIBLE);

        galleryBtn.setOnClickListener(view -> {
            openGallery();
            image_pick_dialog.cancel();
        });
        cameraBtn.setOnClickListener(view -> {
            image_pick_dialog.cancel();

            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showPermissionExtraInfoDialog(Manifest.permission.CAMERA);
                } else {
                    requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                }
            }

        });
    }


    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CODE);
    }


    private void handleDoubtSolutionReady() {
        if (MyApplication.getStringData(PreferenceKeys.IS_DOUBT_SOLUTION_READY.getKey()) != null &&
                MyApplication.getStringData(PreferenceKeys.IS_DOUBT_SOLUTION_READY.getKey()).equals("yes")) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (chatSessionId == null) {
                        resumeChat(MyApplication.getStringData(PreferenceKeys.DOUBT_NOTIFICATION_DOUBT_ID.getKey()));
                        try {
                            MyApplication.removeString(PreferenceKeys.IS_DOUBT_SOLUTION_READY.getKey());
                            MyApplication.removeString(PreferenceKeys.DOUBT_NOTIFICATION_TITLE.getKey());
                            MyApplication.removeString(PreferenceKeys.DOUBT_NOTIFICATION_VIDEO_LINK.getKey());
                            MyApplication.removeString(PreferenceKeys.DOUBT_NOTIFICATION_DOUBT_ID.getKey());
                        } catch (Exception e) {
                            Log.i("SHAREDPREFERENCE", "CANNOT DELETE ITEM");
                        }

                    } else {
                        resumeChat(chatSessionId);
                    }
                }
            });


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    0);
        }
    }


    private void doubtChatRequest(boolean firstRequest, String text, DoubtChatConstants.DoubtRequestType requestType, String url) {

        Log.i("CHATSESSIONID", "SESSIONID :: " + chatSessionId);

        isWaitingForResponse = true;
        if (chatLoadShimmer.getVisibility() != View.VISIBLE) {
            botChatLoader.setVisibility(View.VISIBLE);
        }
        DoubtUserRequest userRequest = new DoubtUserRequest();
        userRequest.setRequestType(requestType);
        userRequest.setSelectedSubjectName(getIntent().getStringExtra("subject"));
        userRequest.setGiveOption(giveOption);
        userRequest.setDoubtImageUrl(url);

        if (getIntent().getBooleanExtra("isMockTestDoubt", false)) {
            userRequest.setMockTestDoubt(true);
            userRequest.setQuestion(getIntent().getStringExtra("question"));
            userRequest.setAnswer(getIntent().getStringExtra("answer"));
        }

        Log.i("CHATSESSIONID", "SESSIONID :: " + userRequest.toString());


        if (giveOption) {
            previousMessage = text;
        }
        giveOption = false;
        userRequest.setFirstDoubt(firstRequest);
        if (requestType != DoubtChatConstants.DoubtRequestType.IMAGE && requestType != DoubtChatConstants.DoubtRequestType.IMAGE_HTML) {
            userRequest.setChatSessionId(chatSessionId);
            userRequest.setUserQuery(text);
            if (chatSessionId != null) {
                addSendChatBubble(text, null, getIstTime(new Date()));
            }
        } else {
            userRequest.setChatSessionId(chatSessionId);
            userRequest.setUserQuery(text);
        }

        promptInput.setText("");

        Log.d("userRequest", userRequest.toString());
        Log.d("userRequestFirst", String.valueOf(firstRequest));

        Map<String, String> params = new HashMap<>();
        params.put("Question_Text", text);
        params.put("Doubt_request_type", requestType.name());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Send_prompt.getActionName(), params);

        MyJsonObjectRequest doubtRequest = null;
        try {
            doubtRequest = new MyJsonObjectRequest(Request.Method.POST,
                    DOUBT_CHAT, new JSONObject(gson.toJson(userRequest)),
                    response -> {
                        TypeToken<Map<String, List<DoubtBotResponse>>> token = new TypeToken<Map<String, List<DoubtBotResponse>>>() {
                        };
                        Map<String, List<DoubtBotResponse>> mapRes = gson.fromJson(response.toString(), token.getType());

                        List<DoubtBotResponse> botResponse = mapRes.get("data");

                        Log.i("CHATRESPONSE", "RESPONSE :: " + botResponse.toString());

                        isImageFirstDoubt = false;

                        Log.i("CHATRESPONSE", "RESPONSE :: " + response.toString());
                        isVideoFeedback = false;

                        int index = 0;

                        for (DoubtBotResponse res : botResponse) {
                            if (res.getChatSesssionId() != null) {
                                chatSessionId = res.getChatSesssionId();
                            }
                            if (res.getResponseType() == DoubtChatConstants.DoubtResponseType.TEXT_OPTION) {
                                recentDoubtResponse = res;
                            }

                            if (res.isNeedFeedback()) {
                                recentNonOptionDoubtResponse = res;
                            }

                            if (res.isStepWiseSolution()) {
                                isStepWiseSolution = true;
                            }

//                            if (res.getResponseType() == DoubtChatConstants.DoubtResponseType.VIDEO && !res.getSeen()) {
//                                isDoubtSolvedVideoSeen = false;
//                            }

                            giveOption = res.isGiveOption();
                            if ((botResponse.size() > 1 && index == botResponse.size() - 1 && !res.isShowBotAvatar()) || botResponse.size() == 1) {
                                res.setShowBotAvatar(true);
                            }
                            if (StringUtils.isNotEmpty(res.getBotResponse())) {
                                addReceiveChatBubble(res, true);
                            }
                            index++;


                            isWaitingForResponse = res.isWaitingForResponse();
                        }

                        if (!CollectionUtils.isEmpty(botResponse)) {

                            DoubtBotResponse lastResponse = botResponse.get(botResponse.size() - 1);
                            if (lastResponse.isMarkSeen()) {
                                status = DoubtSolutionStatus.SOLVED_SEEN.name();
                                updateDoubtStatus();
                            }
                        }

                        botChatLoader.setVisibility(View.GONE);
                        chatLoadShimmer.setVisibility(View.GONE);
//                        indexOfDisambiguationItemClicked = null;

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    botChatLoader.setVisibility(View.GONE);
                    isWaitingForResponse = false;
                    Log.e("CHATRESPONSE", "postError : " + error.getMessage());
                    addReceiveChatBubble(DoubtBotResponse.builder().showBotAvatar(true).responseType(DoubtChatConstants.DoubtResponseType.TEXT).botResponse(getString(R.string.technical_issue_please_try_again)).build(), true);
                }
            });
        } catch (Exception e) {
            Log.e("Error", "JSONException : " + e.getMessage());
        }

        doubtRequest.setTag("DOUBT_CHAT_SCREEN");
        Volley.newRequestQueue(getApplicationContext()).add(doubtRequest).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static String getIstTime(Date createdAt) {
        Format formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
        return formatter.format(createdAt);
    }

    public static int px2dp(Resources resource, float px) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                px,
                resource.getDisplayMetrics()
        );
    }

    private void addReceiveChatBubble(DoubtBotResponse response, boolean clickable) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View receiveChatBubble = layoutInflater.inflate(R.layout.doubt_chat_receive_bubble, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.START;
        layoutParams.setMargins(0, 8, 0, 8);
        receiveChatBubble.setLayoutParams(layoutParams);


        ImageView botAvatar = receiveChatBubble.findViewById(R.id.bot_avatar);
        if (response.isShowBotAvatar()) {
            botAvatar.setVisibility(View.VISIBLE);
        } else {
            botAvatar.setVisibility(View.INVISIBLE);
        }

        if (response.getResponseType() == DoubtChatConstants.DoubtResponseType.HTML) {
            Log.i("HTMLRESPONSES", response.getBotResponse());


            WebView webView = receiveChatBubble.findViewById(R.id.chat_receive_webview);
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            String htmlContent = response.getBotResponse().replace("#", "");

            dialogWebViewContent = htmlContent;
            webView.loadData(htmlContent, "text/html", "UTF-8");

//            Log.i("HTML_LENGTH", "length :: " + htmlContent.length());

            if (htmlContent.length() > 1300) {
                ViewGroup.LayoutParams params = webView.getLayoutParams();
                params.height = 1400;
                webView.setLayoutParams(params);

                TextView showFullAnwer = receiveChatBubble.findViewById(R.id.show_full_answer_text);
                showFullAnwer.setVisibility(View.VISIBLE);
                showFullAnwer.setOnClickListener(cl -> {
                    Dialog webviewDialog = new Dialog(DoubtChatScreen.this);
                    webviewDialog.setContentView(R.layout.dialog_chat_webview_content);
                    webviewDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                    webviewDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                  webviewDialog.setCancelable(false);
                    webviewDialog.show();

                    WebView dialogWebview = webviewDialog.findViewById(R.id.dialog_chat_receive_webview);

                    dialogWebview.getSettings().setJavaScriptEnabled(true);
                    dialogWebview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    dialogWebview.loadData(htmlContent, "text/html", "UTF-8");

                    ImageView closeBtn = webviewDialog.findViewById(R.id.close_dialog_button);
                    closeBtn.setOnClickListener(icl -> {
                        webviewDialog.dismiss();
                    });

                });
            } else {

            }


//            webView.setWebViewClient(new WebViewClient() {
//                @Override
//                public void onPageFinished(WebView view, String url) {
//                    super.onPageFinished(view, url);
//
//                    webView.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                                @Override
//                                public void onGlobalLayout() {
//                                    webView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                                    int contentHeight = (int) Math.ceil(webView.getContentHeight() * webView.getScale());
//                                    ViewGroup.LayoutParams params = webView.getLayoutParams();
//                                    if(contentHeight > 200){
//                                        params.height = 200;
//                                        webView.setLayoutParams(params);
//
//                                        TextView showFullAnwer = receiveChatBubble.findViewById(R.id.show_full_answer_text);
//                                        showFullAnwer.setVisibility(View.VISIBLE);
//                                        showFullAnwer.setOnClickListener(cl ->{
//                                            Dialog webviewDialog = new Dialog(DoubtChatScreen.this);
//                                            webviewDialog.setContentView(R.layout.dialog_chat_webview_content);
//                                            webviewDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
//                                            webviewDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////                                            webviewDialog.setCancelable(false);
//                                            webviewDialog.show();
//
//                                           WebView dialogWebview =  webviewDialog.findViewById(R.id.dialog_chat_receive_webview);
//
//                                            dialogWebview.getSettings().setJavaScriptEnabled(true);
//                                            dialogWebview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//                                            dialogWebview.loadData(htmlContent, "text/html", "UTF-8");
//                                        });
//
//                                    }else{
//                                        params.height = contentHeight;
//                                        webView.setLayoutParams(params);
//                                    }
//                                }
//                            });
//                        }
//                    }, 100);
//                }
//            });

//            webView.setWebChromeClient(new WebChromeClient() {
//                @Override
//                public void onProgressChanged(WebView view, int newProgress) {
//                    super.onProgressChanged(view, newProgress);
//                    Log.d("PROGRESS_WEBVIEW", "onProgressChanged: " + newProgress);
//                    if (newProgress == 100) {
//                        webView.evaluateJavascript(
//                                "(function() { return window.innerHeight; })();",
//                                new ValueCallback<String>() {
//                                    @Override
//                                    public void onReceiveValue(String height) {
//                                        Log.d("PROGRESS_WEBVIEW", "height: " + height);
//                                        try {
//                                            int contentHeightPx = Integer.parseInt(height.replace("\"", ""));
//                                            webView.post(() -> {
//                                                ViewGroup.LayoutParams params = webView.getLayoutParams();
//                                                params.height = px2dp(getResources(), contentHeightPx);
//                                                webView.setLayoutParams(params);
//                                            });
//                                        } catch (NumberFormatException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                    }
//                }
//            });

            responseWebviews.add(webView);
        }

        if (response.getResponseType() == DoubtChatConstants.DoubtResponseType.TEXT ||
                response.getResponseType() == DoubtChatConstants.DoubtResponseType.TEXT_OPTION) {
            TextView chat = receiveChatBubble.findViewById(R.id.chat_receive);
            chat.setVisibility(View.VISIBLE);
            if (response.getBotResponse() != null) {
                chat.setText(convertToRichText(response.getBotResponse()));
            }
            chat.post(new Runnable() {
                @Override
                public void run() {
                    int textHeight = chat.getLineCount() * chat.getLineHeight();
                    chat.setHeight(textHeight);
                }
            });


        }


        if (response.getResponseType() == DoubtChatConstants.DoubtResponseType.VIDEO) {
            TextView subtitle = receiveChatBubble.findViewById(R.id.subtitle);
            ImageView doubtVideoSolution = receiveChatBubble.findViewById(R.id.doubt_video_solution);
            doubtVideoSolution.setVisibility(View.VISIBLE);
            doubtVideoSolution.setOnClickListener(v -> {
                if (response.getDoubtId() != null) {
                    updateUnwatched(response.getDoubtId());
                }
                status = DoubtSolutionStatus.SOLVED_SEEN.name();
                updateDoubtStatus();
                Log.i("VDIEORESPONSE::", "RESPONSE :: " + response.toString());
                Log.i("VDIEORESPONSEOPTION::", "RESPONSE :: " + response.getOptionResponse().get(0).getEndPosition());
                Log.i("VDIEORESPONSEOPTION::", "RESPONSE :: " + response.getOptionResponse().get(0).getContentUrl());
                Log.i("VDIEORESPONSEOPTION::", "RESPONSE :: " + response.getOptionResponse().get(0).getStartPosition());

                Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
                intent.putExtra("video_uri", response.getOptionResponse().get(0).getContentUrl());
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), response.getOptionResponse().get(0).getStartPosition());
                intent.putExtra("video_ending", response.getOptionResponse().get(0).getEndPosition());
                intent.putExtra("doubt", "yes");
                intent.putExtra("response_id", response.getResponseId());

                intent.putExtra("chat_session_id", chatSessionId);

                startActivityForResult(intent, 501);
            });

            CardView videoSeenTeal = receiveChatBubble.findViewById(R.id.video_seen_teal);
            if (response.getSeen() == null || response.getSeen()) {
                videoSeenTeal.setVisibility(View.GONE);
            } else {
                videoSeenTeal.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(response.getBotResponse())) {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(convertToRichText(response.getBotResponse()));
            }
        }

        if (response.getResponseType() == DoubtChatConstants.DoubtResponseType.IMAGE) {
            TextView subtitle = receiveChatBubble.findViewById(R.id.subtitle);
            ImageView doubtVideoSolution = receiveChatBubble.findViewById(R.id.doubt_video_solution);
            doubtVideoSolution.setVisibility(View.VISIBLE);
            Glide.with(this).load(response.getOptionResponse().get(0).getContentUrl()).into(doubtVideoSolution);

            doubtVideoSolution.setOnClickListener(v -> {
                status = DoubtSolutionStatus.SOLVED_SEEN.name();
                updateDoubtStatus();
                Intent intent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                ArrayList<String> urls = new ArrayList<>();
                for (OptionResponse optionResponse : response.getOptionResponse()) {
                    urls.add(optionResponse.getContentUrl());
                }
                if (!CollectionUtils.isEmpty(urls)) {
                    intent.putStringArrayListExtra("imageUrls", urls);
                    startActivity(intent);
                }
            });
            if (StringUtils.isNotEmpty(response.getBotResponse())) {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(convertToRichText(response.getBotResponse()));
            }
        }

        if (response.getResponseType() == DoubtChatConstants.DoubtResponseType.TEXT_OPTION) {
            WrapContentHeightRecyclerView responseList = receiveChatBubble.findViewById(R.id.response_list);
            if (!DoubtSolutionStatus.SOLVED_SEEN.name().equals(status))
                status = DoubtSolutionStatus.IN_PROGRESS.name();
            responseList.setVisibility(View.VISIBLE);
            DoubtChatDisambiguationAdapter disambiguationAdapter = new DoubtChatDisambiguationAdapter(response.getOptionResponse(), this, clickable);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
            responseList.setLayoutManager(linearLayoutManager);
            responseList.setAdapter(disambiguationAdapter);

            recentDoubtResponse = response;
            isVideoFeedback = true;

            hasReceivedSolutionResponseOptionType = true;

        }

        LinearLayout feedbackLayout = receiveChatBubble.findViewById(R.id.feedback_layout);

        if (response.isNeedFeedback()) {
            feedbackLayout.setVisibility(View.VISIBLE);

            ImageView thumbsUpBtn = receiveChatBubble.findViewById(R.id.thumbs_up_btn);

            if (response.getThumbsUp() != null && response.getThumbsUp()) {
                thumbsUpBtn.setImageResource(R.drawable.icon_thumbs_up_teal);
            }


            thumbsUpBtn.setOnClickListener(v -> {
                Log.i("RESPONSEACTIONS", "THUMBSUP :: " + "TEXT_OPTOIN");
                if (response.getThumbsUp() == null) {
                    Log.i("RESPONSEACTIONS", "THUMBSUP :: " + "TEXT_OPTION_CLICKED");
                    response.setThumbsUp(true);
                    thumbsUpBtn.setImageResource(R.drawable.icon_thumbs_up_teal);
                    handleChatFeedback(response.getResponseId(), true, false);
                }
            });

            ImageView thumbsDownBtn = receiveChatBubble.findViewById(R.id.thumbs_down_btn);

            if (response.getThumbsUp() != null && !response.getThumbsUp()) {
                thumbsDownBtn.setImageResource(R.drawable.icon_thumbs_down_teal);
            }

            thumbsDownBtn.setOnClickListener(v -> {
                if (response.getThumbsUp() == null) {
                    response.setThumbsUp(false);
                    thumbsDownBtn.setImageResource(R.drawable.icon_thumbs_down_teal);

                    botChatLoader.setVisibility(View.VISIBLE);
                    handleChatFeedback(response.getResponseId(), false, false);

                }
            });
        }

        chatContainer.addView(receiveChatBubble);

        for (int i = 0; i < chatContainer.getChildCount(); i++) {
            View childView = chatContainer.getChildAt(i);
            if (childView != null && childView.findViewById(R.id.feedback_layout) != null && i < (chatContainer.getChildCount() - 1)) {
                childView.findViewById(R.id.feedback_layout).setVisibility(View.GONE);
            }
        }

        handleScrollToBottom();
    }

    public SpannableStringBuilder convertToRichText(String text) {
        String[] list = text.split("<b>");
        SpannableStringBuilder finalResponse = new SpannableStringBuilder();
        int startingIndex = 0;
        int endingIndex = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].contains("(bold)")) {
                String add = list[i].replace("(bold)", "");
                finalResponse.append(add);
                endingIndex = finalResponse.length();
                finalResponse.setSpan(new StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0);
            } else {
                finalResponse.append(list[i]);
                startingIndex = finalResponse.length();
            }
        }

        return finalResponse;
    }

    public void updateUnwatched(String videoId) {
        try {
            MyApplication instance = MyApplication.getInstance();
            Map<String, Object> map = new HashMap<>();
            map.put("id", videoId);
            map.put("sessionId", chatSessionId);

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(UPDATE_UNWATCHED_VIDEO, map), null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ApiResponse> token = new TypeToken<ApiResponse>() {
                            };
                            ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                            if (apiResponse.isSuccess()) {

                                Set<String> unwatchedSet = MyApplication.getStringSet(PreferenceKeys.UNWATCHED_SET.getKey());

                                if (!CollectionUtils.isEmpty(unwatchedSet)) {
                                    unwatchedSet.remove(videoId);
                                    MyApplication.saveStringSet(PreferenceKeys.UNWATCHED_SET.getKey(), unwatchedSet);

                                    if (CollectionUtils.isEmpty(unwatchedSet)) {
                                        MyApplication.saveData(PreferenceKeys.UNWATCHED.getKey(), false);
                                    }
                                }

                            }

                        } catch (Exception e) {

                        }
                    }, new VolleyGlobalErrorListener(instance) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            myJsonObjectRequest.setTag("DOUBT_CHAT_SCREEN");
            MyVolleySingleton.getInstance(instance).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {

        }
    }

    private void addSendChatBubble(String msg, String imageUri, String time) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View sentChatBubble = layoutInflater.inflate(R.layout.doubt_chat_send_bubble, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.END;
        layoutParams.setMargins(260, 8, 0, 8);
        sentChatBubble.setLayoutParams(layoutParams);

        TextView sentText = sentChatBubble.findViewById(R.id.sent_question_text);
        if (imageUri == null) {
            sentText.setText(msg);
        } else {
            sentText.setVisibility(View.GONE);
            ImageView imgView = sentChatBubble.findViewById(R.id.sent_question_image);
            imgView.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(imgView);
            imgView.setOnClickListener(v -> {
                GlobalDialogs.fullScreenImageViewDialog(this, imageUri);
            });

            isDoubtImageUploaded = true;
        }

        chatContainer.addView(sentChatBubble);
        handleScrollToBottom();

        chatCountInSession += 1;
    }

    private void handleScrollToBottom() {
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, 50);
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        if (refreshScreenOnDoubtNotificationReceiveHanlder != null && refreshScreenOnDoubtNotificationReceiveRunnable != null) {
            refreshScreenOnDoubtNotificationReceiveHanlder.removeCallbacks(refreshScreenOnDoubtNotificationReceiveRunnable);
        }
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("DOUBT_CHAT_SCREEN");
        super.onDestroy();
    }

    private void handleChatFeedback(String responseId, boolean thumbsUp, boolean getDoubtResponseOption) {
        botChatLoader.setVisibility(View.VISIBLE);
        handleScrollToBottom();

        Map<String, Object> params = new HashMap<>();
        params.put("responseId", responseId);
        params.put("chatSessionId", chatSessionId);
        params.put("thumbsUp", thumbsUp);
        params.put("videoFeedback", getDoubtResponseOption);
        params.put("selectedSubjectName", getIntent().getStringExtra("subject"));

        MyJsonObjectRequest feedbackRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_CHAT_FEEDBACK, params), null,
                response -> {
                    TypeToken<Map<String, List<DoubtBotResponse>>> token = new TypeToken<Map<String, List<DoubtBotResponse>>>() {
                    };
                    Map<String, List<DoubtBotResponse>> mapRes = gson.fromJson(response.toString(), token.getType());
                    List<DoubtBotResponse> botResponses = mapRes.get("data");

                    for (DoubtBotResponse botResponse : botResponses) {
                        if (botResponse.getResponseType() == DoubtChatConstants.DoubtResponseType.TEXT_OPTION) {
                            giveOption = true;
                            addReceiveChatBubble(botResponse, true);
                        } else {
                            addReceiveChatBubble(botResponse, false);
                        }
                    }

                    botChatLoader.setVisibility(View.GONE);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                botChatLoader.setVisibility(View.GONE);
            }
        });

        feedbackRequest.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(this).addToRequestQueue(feedbackRequest);
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
                    Log.i("BOTRESPONSE", "PREVDOUBTS :: " + response.toString());

                    TypeToken<DoubtChatHistoryData> token = new TypeToken<DoubtChatHistoryData>() {
                    };
                    DoubtChatHistoryData doubtChatHistoryData = gson.fromJson(response.toString(), token.getType());

                    previousDoubtRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    PreviousDoubtListAdapter previousDoubtListAdapter = new PreviousDoubtListAdapter(getApplicationContext(), doubtChatHistoryData, true);
                    previousDoubtRecyclerView.setAdapter(previousDoubtListAdapter);

                    if (doubtChatHistoryData.getList().isEmpty()) {
                        TextView textView = findViewById(R.id.previous_chat_text);
                        textView.setText(getString(R.string.no_chat_history));
                        CardView chatHistoryBtn = findViewById(R.id.chat_history_btn);
                        chatHistoryBtn.setVisibility(View.GONE);
                    } else if (doubtChatHistoryData.getList().size() >= 5) {
//                        maxChatSessionLimitExceeded = true;
                        CardView chatHistoryBtn = findViewById(R.id.chat_history_btn);
                        chatHistoryBtn.bringToFront();
                        chatHistoryBtn.setVisibility(View.VISIBLE);
                        chatHistoryBtn.setOnClickListener(v -> {
                            startActivity(new Intent(DoubtChatScreen.this, DoubtHistoryActivity.class));
                        });
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
            }
        });

        feedbackRequest.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(this).addToRequestQueue(feedbackRequest);
    }

    private void resumeChat(String chatSessionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("chatSessionId", chatSessionId);
        Log.i("RESUMECHATLIST", "USERID ::" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        Log.i("RESUMECHATLIST", "SESSION::" + chatSessionId);

        MyJsonArrayRequest feedbackRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_CHAT_RESUME, params), null,
                response -> {
                    Log.i("RESUMECHATLIST", "CHATLIST::" + response.toString());
                    TypeToken<List<WebChatMessageDto>> token = new TypeToken<List<WebChatMessageDto>>() {
                    };
                    List<WebChatMessageDto> doubtBotResponseList = gson.fromJson(response.toString(), token.getType());

                    botChatLoader.setVisibility(View.GONE);

                    if (isDestroyed() || isFinishing()) {
                        return;
                    }

                    int chatCount = 0;
                    for (WebChatMessageDto botResponse : doubtBotResponseList) {
                        if ((botResponse.getRequestType() == DoubtChatConstants.DoubtRequestType.IMAGE || botResponse.getRequestType() == DoubtChatConstants.DoubtRequestType.IMAGE_HTML) && botResponse.getUserQuery() != null) {
                            addSendChatBubble(null, botResponse.getUserQuery(), botResponse.getTime());
                        } else if (botResponse.getUserQuery() != null && !botResponse.getUserQuery().isEmpty() && StringUtils.isNotEmpty(botResponse.getUserQuery())) {
                            addSendChatBubble(botResponse.getUserQuery(), null, botResponse.getTime());
                        }

                        if (StringUtils.isNotEmpty(botResponse.getBotResponse()) ||
                                !CollectionUtils.isEmpty(botResponse.getOptionResponse())) {
                            if (botResponse.getOptionResponse() != null && chatCount == doubtBotResponseList.size() - 1) {
                                recentDoubtResponse = transformWebResponseDtoToBotResponse(botResponse);
                                addReceiveChatBubble(transformWebResponseDtoToBotResponse(botResponse), true);
                            } else {
                                addReceiveChatBubble(transformWebResponseDtoToBotResponse(botResponse), false);
                            }
                            indexOfDisambiguationItemClicked = null;
                        }
                        chatCount++;

                        if (botResponse.isStepWiseSolution()) {
                            isStepWiseSolution = true;
                        }

                        isWaitingForResponse = botResponse.isWaitingForResponse();
                    }

                    if (!CollectionUtils.isEmpty(doubtBotResponseList)) {
                        WebChatMessageDto webChatMessageDto = doubtBotResponseList.get(doubtBotResponseList.size() - 1);
                        if (!webChatMessageDto.isMarkSeen() && !isWaitingForResponse && webChatMessageDto.getResponseType() != DoubtChatConstants.DoubtResponseType.VIDEO) {
                            status = DoubtSolutionStatus.SOLVED_SEEN.name();
                            updateDoubtStatus();
                        }
                    }

//                    isWaitingForResponse = false;
                    chatLoadShimmer.setVisibility(View.GONE);


                    if (StringUtils.isNotEmpty(doubtText) && pseudoLive) {
                        firstDoubt = doubtBotResponseList.isEmpty();
                        botChatLoader.setVisibility(View.VISIBLE);

                        if (getIntent().getBooleanExtra("videoRequest", false)) {
                            requestNewVideo("", chatSessionId, doubtText, true);
//                            doubtChatRequest(firstDoubt, doubtText, DoubtChatConstants.DoubtRequestType.TEXT, "");
                        } else {

                            doubtChatRequest(firstDoubt, doubtText, DoubtChatConstants.DoubtRequestType.HTML, "");
                        }
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                chatLoadShimmer.setVisibility(View.GONE);
                if (StringUtils.isNotEmpty(doubtText) && pseudoLive) {
                    botChatLoader.setVisibility(View.VISIBLE);
                    if (getIntent().getBooleanExtra("videoRequest", false)) {
                        requestNewVideo("", chatSessionId, doubtText, true);
                    } else {
                        doubtChatRequest(true, doubtText, DoubtChatConstants.DoubtRequestType.HTML, "");
                    }
                } else {
                    botChatLoader.setVisibility(View.GONE);
                }
            }
        });

        feedbackRequest.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(this).addToRequestQueue(feedbackRequest);
    }

    private DoubtBotResponse transformWebResponseDtoToBotResponse(WebChatMessageDto messageDto) {
        return DoubtBotResponse.builder()
                .responseId(messageDto.getResponseId())
                .responseType(messageDto.getResponseType())
                .botResponse(messageDto.getBotResponse())
                .chatQuotaExhausted(messageDto.isChatQuotaExhausted())
                .needFeedback(messageDto.isNeedFeedback())
                .responseId(messageDto.getResponseId())
                .time(messageDto.getTime())
                .optionResponse(messageDto.getOptionResponse())
                .seen(messageDto.getSeen())
                .doubtId(messageDto.getDoubtId())
                .showBotAvatar(messageDto.isShowAvatar())
                .build();
    }


    public void checkCameraPermission(String permission, int requestCode) {

        if (getApplicationContext() == null) {
            return;
        }
        String filename = "photo";
        File storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(filename, ".jpg", storageDirectory);
//                currentPhotoPath = imageFile.getAbsolutePath();

            imageuri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);

            startActivityForResult(intent, 19);

        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 501) {
            if (data == null) {
                return;
            }
            if (data.getBooleanExtra("disliked", false)) {
                if (!nonClickableOptionsList.contains(data.getStringExtra("response_id"))) {
                    handleChatFeedback(data.getStringExtra("response_id"), false, false);
                }
                nonClickableOptionsList.add(data.getStringExtra("response_id"));
            } else {
//                String[] solvedMessages = {getResources().getString(R.string.we_hope_your_doubt_is_resolved),
//                        getResources().getString(R.string.we_hope_you_got_the_solution_of_your_doubt),
//                        getResources().getString(R.string.we_hope_this_solves_your_doubt)};
//                Random random = new Random();
//                addReceiveChatBubble(
//                        DoubtBotResponse.builder()
//                                .responseType(DoubtChatConstants.DoubtResponseType.TEXT)
//                                .needFeedback(false)
//                                .showBotAvatar(true)
//                                .botResponse(solvedMessages[random.nextInt(solvedMessages.length)]).build(),
//                        false
//                );
            }
        }

        if (requestCode == 118 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageuri = data.getData();
                Log.i("IMAGEURI", "IMAG UIR :: " + imageuri);
                CropImage.activity(imageuri)
                        .start(this);
            }
        }


        if (requestCode == 19 && resultCode == RESULT_OK) {
            CropImage.activity(imageuri)
                    .start(this);
        }

        Log.d("CROPPINGINDEX :: ", "RESULTCODE: " + resultCode);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Log.d("CROPPINGINDEX :: ", "INDEX ON OK: ");

                try {
                    if (result == null) {
                        Log.d("CROPPINGINDEX :: ", "NULL RESULT: ");
                        return;
                    }
                    Uri uri = result.getUri();
                    File file = new File(uri.getPath());
                    float lengthmb = (float) (file.length() / 1024) / 1024;

                    if (lengthmb <= 15) {
                        Log.d("IMAGEURI", "FILE URI CROP :: " + uri);
//                            list.add(uri);
                        imagesToUpload.add(file);
//
//                        Glide.with(this).load(uri).into()

//                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
//                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
//                        int minutes = calendar.get(Calendar.MINUTE);
//                        int amPm = calendar.get(Calendar.AM_PM);
//                        hours = (hours == 0) ? 12 : hours;
//                        String period = (amPm == Calendar.AM) ? "AM" : "PM";
//                        String formattedTime = hours+":"+ minutes+":"+period;
//
//                        LocalTime currentTime = LocalTime.now();
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
//                        addSendChatBubble(null, uri.toString(), currentTime.format(formatter));

                        addSendChatBubble(null, uri.toString(), null);
                        doubtChatRequestWithImage();
                    } else {
                        Toast.makeText(getApplicationContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                    }

                } catch (Exception error) {
                    Log.e("resultUri", error.getMessage(), error);
                }
            }
        }

    }


    private void doubtChatRequestWithImage() {
        botChatLoader.setVisibility(View.VISIBLE);


        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST, IMAGE_UPLOAD, response -> {
            try {
                TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                };
                Gson gson = new Gson();
                ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, "UTF-8"), token.getType());
                Log.d("ImageRequest", "RESPONSE URL : " + extractionResponse.getMessage());

                doubtChatRequest(isImageFirstDoubt, extractionResponse.getMessage(), DoubtChatConstants.DoubtRequestType.IMAGE_HTML, extractionResponse.getMessage());
            } catch (Exception e) {
                Log.e("Error in subscription", e.getMessage(), e);

            }

        }, error -> {
            Log.d("MULTIPARTRES", "RESPONSE : " + error.toString());
            botChatLoader.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {

                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                for (int i = 0; i < imagesToUpload.size(); i++) {
                    File file = imagesToUpload.get(i);
                    try {
                        byte[] fileData = CommonMethods.readFileToByteArray(file);
                        String fileName = file.getName();
                        String contentType = CommonMethods.getMimeType(fileName);

                        if (i == 0) {
                            params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return params;
            }
        };


        multipartRequest.setTag("DOUBT_CHAT_SCREEN");
        Volley.newRequestQueue(getApplicationContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDisambiguationItemClick(int index, boolean isSomethingElseOption) {
        hasOptions = false;
        indexOfDisambiguationItemClicked = index;

//        handleOptionClick(recentDoubtResponse.getResponseId(),
//                recentDoubtResponse.getOptionResponse().get(index).getTitle());
        if (!isWaitingForResponse) {
            handleOptionClick(index, recentDoubtResponse.getResponseId(),
                    recentDoubtResponse.getOptionResponse().get(index).getTitle(), isSomethingElseOption);
        }
    }


    private void handleOptionClick(int index, String responseId, String selectedOption, boolean isSomethingElseOption) {

        if (nonClickableOptionsList.contains(responseId)) {
            return;
        }


        if (isSomethingElseOption) {
            giveOption = true;
            doubtChatRequest(false, firstMessage, DoubtChatConstants.DoubtRequestType.HTML, "");
            return;
        }

        botChatLoader.setVisibility(View.VISIBLE);
        handleScrollToBottom();
        if (selectedOption.contains(getString(R.string.step_wise_solution))) {
            Bundle clickBtnBundle = new Bundle();
            clickBtnBundle.putString("selectedOption", "step_wise_solution");
            if (StringUtils.isNotEmpty(doubtText))
                clickBtnBundle.putString("doubtText", doubtText);
            MyApplication.logFirebaseEvent("doubt_response_type", clickBtnBundle);
            addReceiveChatBubble(DoubtBotResponse.builder()
                            .responseType(DoubtChatConstants.DoubtResponseType.TEXT)
                            .botResponse(getString(R.string.please_wait_writing_your_solution))
                            .showBotAvatar(true)
                            .build(),
                    false);
        }

        handleOptionClickRequest(responseId, selectedOption, true);
    }

    private void handleOptionClickRequest(String responseId, String selectedOption, boolean requestPromptDetail) {
        isWaitingForResponse = true;
        Map<String, Object> params = new HashMap<>();
        params.put("chatMessageId", responseId);
        params.put("chatSessionId", chatSessionId);
        params.put("selectedOption", selectedOption);
        params.put("userDoubtText", previousMessage);
        params.put("requestPromptDetail", requestPromptDetail);
        params.put("requestType", DoubtChatConstants.DoubtRequestType.HTML);
        String subject = getIntent().getStringExtra("subject");
        if (StringUtils.isEmpty(subject)) {
            subject = "Physics";
        }
        params.put("selectedSubjectName", subject);

        if (requestPromptDetail) {
            doubtTypeOptionResponseId = responseId;
            doubtTypeOptionSelected = selectedOption;
        } else {
            doubtTypeOptionResponseId = null;
            doubtTypeOptionSelected = null;
        }

        botChatLoader.setVisibility(View.VISIBLE);


        MyJsonObjectRequest optionClickResume = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(OPTION_CLICK, params), null,
                response -> {
//                    TypeToken<DoubtBotResponse> token = new TypeToken<DoubtBotResponse>() {};
//                    DoubtBotResponse botResponse = gson.fromJson(response.toString(), token.getType());
                    nonClickableOptionsList.add(responseId);

                    TypeToken<Map<String, List<DoubtBotResponse>>> token = new TypeToken<Map<String, List<DoubtBotResponse>>>() {
                    };
                    Map<String, List<DoubtBotResponse>> mapRes = gson.fromJson(response.toString(), token.getType());

                    List<DoubtBotResponse> botResponses = mapRes.get("data");

                    Log.i("CHATRESPONSE", "RES :: " + botResponses.toString());

                    for (DoubtBotResponse botResponse : botResponses) {
                        if (botResponse.getResponseType() == DoubtChatConstants.DoubtResponseType.VIDEO) {
                            isVideoFeedback = true;
                        }

                        if (botResponse.isStepWiseSolution()) {
                            isStepWiseSolution = true;
                        }

                        addReceiveChatBubble(botResponse, false);
                    }

                    botChatLoader.setVisibility(View.GONE);
                    isWaitingForResponse = false;
//                    botResponse.setShowBotAvatar(true);

//                    indexOfDisambiguationItemClicked = null;
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                botChatLoader.setVisibility(View.GONE);
                Log.i("DOUBTCHATERROR", error.toString());
                isWaitingForResponse = false;
                addReceiveChatBubble(DoubtBotResponse.builder().showBotAvatar(true).responseType(DoubtChatConstants.DoubtResponseType.TEXT).botResponse(getString(R.string.technical_issue_please_try_again)).build(), true);
            }
        });

        optionClickResume.setTag("DOUBT_CHAT_SCREEN");
        Volley.newRequestQueue(getApplicationContext()).add(optionClickResume).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void requestNewVideo(String chatMessageId, String chatSessionId, String message, boolean createNewSessionRecord) {
        addSendChatBubble(message, null, null);
        Map<String, Object> params = new HashMap<>();
        params.put("chatMessageId", chatMessageId);
        params.put("chatSessionId", chatSessionId);
        params.put("doubt", message);
        params.put("createNewSessionRecord", createNewSessionRecord);


        String subject = getIntent().getStringExtra("subject");
        if (StringUtils.isEmpty(subject)) {
            subject = "Physics";
        }
        params.put("subject", subject);


        botChatLoader.setVisibility(View.VISIBLE);


        MyJsonArrayRequest optionClickResume = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_REQUEST_VIDEO_SOLUTION, params), null,
                response -> {
                    TypeToken<List<DoubtBotResponse>> token = new TypeToken<List<DoubtBotResponse>>() {
                    };
                    List<DoubtBotResponse> botResponses = gson.fromJson(response.toString(), token.getType());

                    Log.i("VIDEO_RESPONSE", "RES ::" + botResponses);

                    for (DoubtBotResponse botResponse : botResponses) {
                        addReceiveChatBubble(botResponse, false);
                    }

                    botChatLoader.setVisibility(View.GONE);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                botChatLoader.setVisibility(View.GONE);
                Log.i("VIDEO_RESPONSE", "RES ::" + error.getMessage());
            }
        });

        optionClickResume.setTag("DOUBT_CHAT_SCREEN");
        Volley.newRequestQueue(getApplicationContext()).add(optionClickResume).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void checkSubscription() {
        MyJsonObjectRequest checkSubs = new MyJsonObjectRequest(Request.Method.GET,
                TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION,
                null,
                response -> {
                    TypeToken<DoubtChatLimitationCheckDto> checkDtoTypeToken = new TypeToken<DoubtChatLimitationCheckDto>() {
                    };
                    chatLimitationCheckDto = gson.fromJson(response.toString(), checkDtoTypeToken.getType());
                    isSubscribedUser = chatLimitationCheckDto.isSubscriptionStatus();
                    if (isSubscribedUser) {
                        drawerSubscriptionLayout.setVisibility(View.INVISIBLE);
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) drawerSubscriptionLayout.getLayoutParams();
                        layoutParams.height = 0;
                        drawerSubscriptionLayout.setLayoutParams(layoutParams);
                    }
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("CHATREQUEST", error.toString());
                    }
                });

        checkSubs.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(checkSubs);
    }

    private void updateDoubtStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("chatSessionId", chatSessionId);
        map.put("status", status);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UPDATE_DOUBT_STATUS, map),
                null,
                response -> {
                    update = true;
                    Log.i(PreferenceKeys.RESPONSE.getKey(), response.toString());
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i(PreferenceKeys.RESPONSE.getKey(), error.toString());
                    }
                });

        myJsonObjectRequest.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest);
    }


}
