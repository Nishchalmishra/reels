package arivihan.technologies.doubtbuzzter2.helper;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ChapterListActivity;
import arivihan.technologies.doubtbuzzter2.activity.ChatScreenChooseSubjectForChat;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.helper.DoubtChatHost;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class WebViewDoubtChatInterface {
    Context mContext;
    DrawerLayout drawerLayout;
    WebView webView;

    DoubtChatHost doubtChatHost;

    private static final int CAMERA_PERMISSION_CODE = 100;

    public WebViewDoubtChatInterface(Context context, DrawerLayout drawerLayout, WebView webView) {
        this.mContext = context;
        this.drawerLayout = drawerLayout;
        this.webView = webView;
        doubtChatHost = (DoubtChatHost) context;
    }

    public WebViewDoubtChatInterface(Context context, WebView webView) {
        this.mContext = context;
        this.webView = webView;
        doubtChatHost = (DoubtChatHost) context;
    }

    public WebViewDoubtChatInterface(DoubtChatHost host, Context context, WebView webView) {
        this.mContext = context;
        this.webView = webView;
        this.doubtChatHost = host;
    }

    // Show a toast from the web page
    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void backToPreviousActivity() {
        doubtChatHost.backToPreviousActivity();
    }

    @JavascriptInterface
    public void openActivity(String className, String activityParams) {
        Map<String, Object> params = new HashMap<>();

        Log.i("OPEN_ACTICITY", "PARAMS :: " + activityParams);

        if (activityParams != null && !activityParams.equals("null")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                params = mapper.readValue(activityParams, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        params.put("source", RedirectionSource.WEBVIEW_DOUBT_CHAT.name());

        try {
            if (params.get(PreferenceKeys.BOARD_CODE.getKey()) == null) {
                params.put(PreferenceKeys.BOARD_CODE.getKey(), "MPBSE");
            }
            if (params.containsKey("finishActivity") && params.get("finishActivity") instanceof Boolean && (boolean) params.get("finishActivity")) {
                ((Activity) mContext).finish();
            }
        } catch (Exception ignored) {
        }

        ActivityLauncher.launchActivity(mContext, className, params);
    }

    @JavascriptInterface
    public void setFirebaseEvent(String eventName, String eventParams) {
        Map<String, String> params = new HashMap<>();
        if (eventParams != null && !eventParams.equals("null")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                params = mapper.readValue(eventParams, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        doubtChatHost.setFirebaseEvent(eventName, params);
    }

    @JavascriptInterface
    public void openPdf(String url, String title, String notesType, String subjectName) {
        try {
            PdfUtils.openPdfViewer(mContext, url, StringUtils.isNotEmpty(title) ? title : "", notesType, subjectName, false, "", "");
//            Intent target = new Intent(Intent.ACTION_VIEW);
//            target.setDataAndType(Uri.parse(url), "application/pdf");
//            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            Intent intent = Intent.createChooser(target, "Open File");
//            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    @JavascriptInterface
    public void shareOnWhatsapp(String message) {
        try {

            String encodedMessage = message.replace("\\n", "\n");
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, encodedMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp"); // Targets WhatsApp directly
            mContext.startActivity(sendIntent);
        } catch (Exception e) {
        }
    }


    @JavascriptInterface
    public void showSubscriptionDialog() {
        doubtChatHost.showDialogForSubscription();
    }


    @JavascriptInterface
    public void changeCourse() {
        Intent intent = new Intent(mContext, HomeScreen.class);
        intent.putExtra("redirected_from", "WebViewDoubtChatScreen");
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void shareContent(String title, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TITLE, title);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        mContext.startActivity(Intent.createChooser(shareIntent, ""));
    }

    @JavascriptInterface
    public void watchLectureNow() {
        if (mContext instanceof Activity activity) {
            String selectedSubject = activity.getIntent().getStringExtra("subjectName");
            Log.d("TESKJHLSJ", "watchLectureNow: " + selectedSubject);

            MyApplication myapp = (MyApplication) activity.getApplication();
            HomeScreenViewModel homeScreenViewModel = myapp.getHomeScreenViewModel();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<SubjectResponse> subjectModelList = homeScreenViewModel.getSubjectList().getValue();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && subjectModelList != null) {
                        subjectModelList.forEach(item -> {
                            if (item.getSubjectName().equalsIgnoreCase(selectedSubject)) {
                                Intent intent = new Intent(activity, ChapterListActivity.class);
                                intent.putExtra("SubjectName", selectedSubject);
                                intent.putExtra("SubjectId", item.getId());
                                intent.putExtra("Icon", item.getIconUrl());
                                intent.putExtra("SubjectCode", item.getSubjectCode());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                activity.startActivity(intent);
                            }
                        });
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void setChatSessionId(String chatSessionId) {
        doubtChatHost.setChatSessionId(chatSessionId);
        Log.i("SNAPSHOT_DATA", "CHAT_SESSION :: " + chatSessionId);

    }

    @JavascriptInterface
    public void openVideo(String videoUrl, Integer startPosition, Integer endPosition, String chatSessionId, String responseId) {
        Intent intent = new Intent(mContext, ExoPlayerActivity.class);
        intent.putExtra("video_uri", videoUrl);
        intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), startPosition);
        intent.putExtra("video_ending", endPosition);
        intent.putExtra("doubt", "yes");
        intent.putExtra("response_id", responseId);

        intent.putExtra("chat_session_id", chatSessionId);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void openDrawer() {
        Log.d("openDrawercalled", "openDrawer called");
        if (drawerLayout == null) return;
        drawerLayout.post(() -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    @JavascriptInterface
    public void openWhatsapp() {
        Log.d("openWhatsAPP", "openWhatsapp: ");
        try {
            String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.whatsapp");
            intent.setData(uri);
            mContext.startActivity(intent);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_whatsApp_from_doubt.getActionName());
        } catch (Exception e) {
            Toast.makeText(mContext, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }
    }


    @JavascriptInterface
    public void openNewChat() {
        Log.d("open new chat", "openNewChat: ");
        try {
            if (mContext instanceof Activity activity) {
                activity.finish();
                Intent intent = new Intent(activity, ChatScreenChooseSubjectForChat.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Unable to go back", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void openChapterList(String subjectName, String subjectId, String subject) {
        Log.d("open Chapter List", "openChapterList: ");
        try {
            if (mContext instanceof Activity activity) {
                activity.finish();
                Intent intent = new Intent(activity, ChapterListActivity.class);
                intent.putExtra("SubjectName", subjectName);
                intent.putExtra("SubjectId", subjectId);
                intent.putExtra("SubjectCode", subject);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Unable to go back", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void openMicInput() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                                == PackageManager.PERMISSION_GRANTED) {
                            SpeechRecognitionHelper speechRecognitionHelper = new SpeechRecognitionHelper(activity);
                            EditText editText = new EditText(mContext);
                            speechRecognitionHelper.initializeSpeechRecognition(editText);
                            speechRecognitionHelper.speechRecognizer.setRecognitionListener(new SpeechRecognitionListenerHelper() {
                                @Override
                                public void onError(int error) {
                                    super.onError(error);
                                    if (speechRecognitionHelper.voiceSearchDialog != null && speechRecognitionHelper.voiceSearchDialog.isShowing()) {
                                        try {
                                            speechRecognitionHelper.voiceSearchDialog.dismiss();
                                        } catch (IllegalArgumentException e) {
                                            Log.e("WebViewDoubtChat", "Dialog already detached", e);
                                        }
                                    }
                                }

                                @Override
                                public void onEndOfSpeech() {
                                    super.onEndOfSpeech();
                                    if (speechRecognitionHelper.voiceSearchDialog != null && speechRecognitionHelper.voiceSearchDialog.isShowing()) {
                                        try {
                                            speechRecognitionHelper.voiceSearchDialog.dismiss();
                                        } catch (IllegalArgumentException e) {
                                            Log.e("WebViewDoubtChat", "Dialog already detached", e);
                                        }
                                    }
                                }

                                @Override
                                public void onReadyForSpeechCallback(Bundle params) {

                                }

                                @Override
                                public void onResultsCallback(Bundle params) {
                                    speechRecognitionHelper.speechRecognizer.stopListening();
                                    if (speechRecognitionHelper.voiceSearchDialog != null && speechRecognitionHelper.voiceSearchDialog.isShowing()) {
                                        try {
                                            speechRecognitionHelper.voiceSearchDialog.dismiss();
                                        } catch (IllegalArgumentException e) {
                                            Log.e("WebViewDoubtChat", "Dialog already detached", e);
                                        }
                                    }
                                    if (speechRecognitionHelper.lottieAnimationView != null) {
                                        speechRecognitionHelper.lottieAnimationView.pauseAnimation();
                                    }
                                    ArrayList<String> result = params.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                                    if (result != null && !result.isEmpty()) {
                                        String javascript = "javascript:processMicInput('" + result.get(0).replace("'", "\\'") + "');";
                                        activity.runOnUiThread(() -> webView.evaluateJavascript(javascript, null));
                                    }
                                }

                            });
                            speechRecognitionHelper.setVoiceSearchDialog();
                            speechRecognitionHelper.startListening();
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                                showPermissionExtraInfoDialog(Manifest.permission.RECORD_AUDIO);
                            } else {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.RECORD_AUDIO},
                                        2);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Unable to go back", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void openImagePicker() {
        Dialog image_pick_dialog = new Dialog(mContext);
        image_pick_dialog.setContentView(R.layout.action_popup_layout);
        image_pick_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        image_pick_dialog.getWindow().setBackgroundDrawable(mContext.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.setCancelable(false);
//        image_pick_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        image_pick_dialog.findViewById(R.id.close_btn).setOnClickListener(view -> image_pick_dialog.cancel());

        TextView title = image_pick_dialog.findViewById(R.id.title);
        title.setText(mContext.getString(R.string.select_images));
        TextView subtitle = image_pick_dialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);

        CardView galleryBtn = image_pick_dialog.findViewById(R.id.btn1);
        CardView cameraBtn = image_pick_dialog.findViewById(R.id.btn2);
        TextView galleryBtnText = image_pick_dialog.findViewById(R.id.btn1_text);
        galleryBtnText.setText(mContext.getString(R.string.choose_from_gallery));
        TextView cameraBtnText = image_pick_dialog.findViewById(R.id.btn2_text);
        cameraBtnText.setText(mContext.getString(R.string.click_a_photo));

        LottieAnimationView lottieAnimationView = image_pick_dialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        ImageView selectionImage = image_pick_dialog.findViewById(R.id.selection_image);
        selectionImage.setVisibility(View.VISIBLE);

        galleryBtn.setOnClickListener(view -> {
            image_pick_dialog.cancel();
            doubtChatHost.launchGalleryPicker();
        });

        cameraBtn.setOnClickListener(view -> {
            image_pick_dialog.cancel();
            doubtChatHost.checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        });
        image_pick_dialog.show();
    }


    public void showPermissionExtraInfoDialog(String permission) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(R.string.allow_permission);

        alertDialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.yes), (arg0, arg1) -> {
            openAppSettings();
        });

        alertDialogBuilder.setNegativeButton(mContext.getResources().getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void getRemoteConfigValue(String key) {
        String value = MyApplication.getFirebaseRemoteConfig().getString(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callback = "javascript:fetchRemoteConfigValue('" + key + "','" + safeValue + "')";
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() ->
                    webView.evaluateJavascript(callback, null)
            );
        }
    }

}
