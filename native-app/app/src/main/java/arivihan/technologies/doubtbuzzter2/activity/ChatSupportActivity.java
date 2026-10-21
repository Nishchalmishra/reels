package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.MyApplication.getPhoneNumber;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHAT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHAT_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_DOUBT_CHAT_HISTORY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_DOUBT_SAVE_RATING;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.SpeechRecognitionHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ChatRequestDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatLimitationCheckDto;
import arivihan.technologies.doubtbuzzter2.model.TutorSupport;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChatSupportActivity extends BaseActivity {


    private ImageView sendQuestionBtn, micBtn;
    private EditText questionField;
    private LinearLayout chatContainer;
    private LinearLayout chatResponseLoader;
    private String chatSessionId, mockTestQuestion, mockTestQuestionAnswer;
    private ScrollView chatScrollView;
    private int promptCount = 0;
    private RelativeLayout chatBox, doubtResolved;
    private boolean isRedirectedFromHistory = false;
    private boolean isMockTestDoubt = false;
    private Dialog feedbackDialog;
    ImageView back;
    private Gson gson;

    DoubtChatLimitationCheckDto chatLimitationCheckDto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String type = getIntent().getStringExtra("type");
                if (type != null && type.equals("new_chat")) {
                    feedbackDialog = new Dialog(ChatSupportActivity.this);
                    feedbackDialog.setContentView(R.layout.dialog_chat_support_feedback);
                    feedbackDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(ChatSupportActivity.this,
                            R.drawable.bachground_custom_dialogu_typeactivity));
                    feedbackDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ImageView closeBtn = feedbackDialog.findViewById(R.id.close_btn);
                    closeBtn.setOnClickListener(v -> {
                        feedbackDialog.dismiss();
                    });

                    CardView skipChatFeedback = feedbackDialog.findViewById(R.id.chat_feedback_skip);
                    skipChatFeedback.setOnClickListener(v -> {
                        finish();
                    });

                    RatingBar chatRating = feedbackDialog.findViewById(R.id.chat_rating);
                    chatRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            saveChatRating(getIntent().getStringExtra("doubtId"), rating + "");
                            finish();
                        }
                    });
                    feedbackDialog.show();

                } else {
                    finish();
                }
            }
        });

        gson = new Gson();
        sendQuestionBtn = findViewById(R.id.send_question_btn);
        questionField = findViewById(R.id.my_question);
        chatContainer = findViewById(R.id.chat_container);
        chatResponseLoader = findViewById(R.id.chat_response_loader);
        chatScrollView = findViewById(R.id.chat_scroll_view);
        chatBox = findViewById(R.id.chat_box);
        doubtResolved = findViewById(R.id.doubt_resolved);
        back = findViewById(R.id.back_arrow);
        micBtn = findViewById(R.id.open_mic_btn);


        chatSessionId = UUID.randomUUID().toString();
        Intent intent = getIntent();
        String chatType = intent.getStringExtra("type");

        isMockTestDoubt = intent.getBooleanExtra("isMockTestDoubt", false);

        if (isMockTestDoubt) {
            mockTestQuestion = intent.getStringExtra("question");
            mockTestQuestionAnswer = intent.getStringExtra("answer");
        }

        checkSubscription();

        micBtn.setOnClickListener(v -> {
            SpeechRecognitionHelper speechRecognitionHelper = new SpeechRecognitionHelper(this);
            speechRecognitionHelper.initializeSpeechRecognition(questionField);
            speechRecognitionHelper.setVoiceSearchDialog();
            speechRecognitionHelper.startListening();
        });


        if (chatType != null && chatType.equals("history")) {
            isRedirectedFromHistory = true;
        }

        back.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        if (isRedirectedFromHistory) {
            chatBox.setVisibility(View.GONE);
            doubtResolved.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) chatScrollView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.doubt_resolved);
            chatScrollView.setLayoutParams(layoutParams);
            getSessionChatHistory(getIntent().getStringExtra("doubtId"));
        } else {
            try {
                addReceiveChatBubble(getString(R.string.practice_question_bot_text).replace("{name}", MyApplication.getStringData(PreferenceKeys.NAME.getKey())), false);
            } catch (Exception e) {
            Log.d("onCreate: ", "Error initializing chat: " + e);
        }
            addReceiveChatBubble(getResources().getString(R.string.chat_init_text), true);
            getSessionChatHistory(getIntent().getStringExtra("doubtId"));

            sendQuestionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!MyApplication.isNetworkAvailable()) {
                        Toast.makeText(getApplicationContext(), R.string.NoInternetConnection, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (chatLimitationCheckDto == null) {
                        chatLimitationCheckDto = new DoubtChatLimitationCheckDto();
                        checkSubscription();
                    }

                    if ((!chatLimitationCheckDto.isSubscriptionStatus() && chatLimitationCheckDto.getChatSessionsCount() >= chatLimitationCheckDto.getSessionsLimitPerUser()) || (!chatLimitationCheckDto.isSubscriptionStatus() && promptCount >= chatLimitationCheckDto.getMessageLimitPerSession()) || (chatLimitationCheckDto.isSubscriptionStatus() && promptCount >= chatLimitationCheckDto.getMessageLimitPerSessionForSubscribedUser())) {
                        chatBox.removeAllViews();
                        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        relativeLayoutParams.setMargins(24, 8, 24, 8);
                        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        chatBox.setLayoutParams(relativeLayoutParams);

                        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
                        View contactWhatsapp = layoutInflater.inflate(R.layout.tutor_support_whatsapp_contact, null);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.END;
                        layoutParams.setMargins(0, 16, 0, 8);
                        contactWhatsapp.setLayoutParams(layoutParams);
                        if (chatLimitationCheckDto.isSubscriptionStatus()) {
                            CardView openWhatsappBtn = contactWhatsapp.findViewById(R.id.open_whatsapp_btn);
                            openWhatsappBtn.setOnClickListener(V -> {
                                openWhatsApp(getPhoneNumber(), questionField.getText().toString());
                            });

                        } else {
                            TextView subscriptionText = contactWhatsapp.findViewById(R.id.subscription_text);
                            subscriptionText.setText(R.string.chat_limit_in_session_exceeded);

                            CardView openWhatsappBtn = contactWhatsapp.findViewById(R.id.open_whatsapp_btn);
                            openWhatsappBtn.setVisibility(View.GONE);

                            CardView openSubscription = contactWhatsapp.findViewById(R.id.open_subscription);
                            openSubscription.setVisibility(View.VISIBLE);
                            openSubscription.setOnClickListener(V -> {
//                                 Intent intent = new Intent(ChatSupportActivity.this, CampaignSubscriptionActivity.class);
////                                 intent.putExtra("plan_level", "COURSE");
//                                 startActivity(intent);
                                SubscriptionNavigationHelper.navigateToSubscriptionPage(ChatSupportActivity.this, RedirectionSource.CHAT_SUPPORT);
                            });
                        }

                        if (chatContainer.findViewById(R.id.block_chat_and_redirect_to_whatsapp) == null) {
                            chatBox.addView(contactWhatsapp);
                            handleScollToBottom();
                        }


                    } else {

                        String requestPrompt = questionField.getText().toString();
                        if (requestPrompt.isEmpty()) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_prompt_request), Toast.LENGTH_SHORT).show();
                        } else {
                            addSendChatBubble(requestPrompt);
                            requestChat(requestPrompt);
                            questionField.setText("");
                            promptCount += 1;
                            handleScollToBottom();
                        }
                    }
                }
            });
        }

    }


    private void requestChat(String question) {
        chatResponseLoader.setVisibility(View.VISIBLE);


        ChatRequestDto chatRequestDto = ChatRequestDto.builder()
                .prompt(question)
                .chatSessionId(chatSessionId)
                .isMockTestDoubt(isMockTestDoubt)
                .doubtId(getIntent().getStringExtra("doubtId"))
                .build();

        if (isMockTestDoubt) {
            chatRequestDto.setMockQuestion(mockTestQuestion);
            chatRequestDto.setMockQuestionAnswer(mockTestQuestionAnswer);
        }


        MyJsonObjectRequest chatReq = null;
        try {
            chatReq = new MyJsonObjectRequest(Request.Method.POST,
                    TUTOR_SUPPORT_CHAT_V2,
                    new JSONObject(gson.toJson(chatRequestDto)),
                    response -> {
                        Log.i("CHATREQUEST", response.toString());

                        try {
                            addReceiveChatBubble(response.get("output").toString(), true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    new VolleyGlobalErrorListener(getApplicationContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.i("CHATREQUEST", error.toString());
                            addReceiveChatBubble("Oops! something went wrong. Please retry.", true);
                        }
                    });

            Volley.newRequestQueue(getApplicationContext()).add(chatReq).setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } catch (JSONException e) {
        }


    }

    private void getSessionChatHistory(String doubtId) {
        Map data = new HashMap();
        data.put("doubtId", doubtId);
        MyJsonArrayRequest chatReq = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TUTOR_SUPPORT_DOUBT_CHAT_HISTORY, data),
                null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<List<TutorSupport>> token = new TypeToken<List<TutorSupport>>() {
                    };
                    List<TutorSupport> chatHistory = gson.fromJson(response.toString(), token.getType());

                    for (TutorSupport chat : chatHistory) {
                        promptCount += 1;
                        if (chat.getChatRequest() != null) {
                            if (chat.getChatRequest().contains("with this reference")) {
                                addSendChatBubble(chat.getChatRequest().split("with this reference")[1]);
                            } else {
                                addSendChatBubble(chat.getChatRequest());
                            }
                        }
                        if (chat.getChatResponse() != null) {
                            addReceiveChatBubble(chat.getChatResponse(), true);
                        }
                    }

                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("CHATREQUEST", error.toString());
                    }
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(chatReq);
    }


    private void saveChatRating(String doubtId, String rating) {
        Map data = new HashMap();
        data.put("doubtId", doubtId);
        data.put("rating", rating);
        MyJsonObjectRequest chatReq = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TUTOR_SUPPORT_DOUBT_SAVE_RATING, data),
                null,
                response -> {
                    Toast.makeText(getApplicationContext(), "Thanks for your feedback.", Toast.LENGTH_SHORT).show();
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("CHATREQUEST", error.toString());
                    }
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(chatReq);
    }


    private void addReceiveChatBubble(String msg, boolean showMascot) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View receiveChatBubble = layoutInflater.inflate(R.layout.doubt_chat_receive_bubble, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.START;
        layoutParams.setMargins(0, 8, 0, 8);
        receiveChatBubble.setLayoutParams(layoutParams);
        TextView chat = receiveChatBubble.findViewById(R.id.chat_receive);
        chat.setVisibility(View.VISIBLE);
        chat.setText(convertToRichText(msg));

        if (!showMascot) {
            receiveChatBubble.findViewById(R.id.bot_avatar).setVisibility(View.INVISIBLE);
        }

        chatContainer.addView(receiveChatBubble);
        chatResponseLoader.setVisibility(View.GONE);
        handleScollToBottom();
    }

    private void addSendChatBubble(String msg) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View sentChatBubble = layoutInflater.inflate(R.layout.doubt_chat_send_bubble, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.END;
        layoutParams.setMargins(0, 8, 0, 8);
        sentChatBubble.setLayoutParams(layoutParams);

        TextView sentText = sentChatBubble.findViewById(R.id.sent_question_text);
        sentText.setText(msg);
        chatContainer.addView(sentChatBubble);
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


    private void openWhatsApp(String number, String message) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            sendIntent.putExtra("jid", number + "@s.whatsapp.net");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }

    }


    private void checkSubscription() {
        chatResponseLoader.setVisibility(View.VISIBLE);
        MyJsonObjectRequest checkSubs = new MyJsonObjectRequest(Request.Method.GET,
                TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION,
                null,
                response -> {
                    TypeToken<DoubtChatLimitationCheckDto> checkDtoTypeToken = new TypeToken<DoubtChatLimitationCheckDto>() {
                    };
                    Gson gson = new Gson();
                    chatLimitationCheckDto = gson.fromJson(response.toString(), checkDtoTypeToken.getType());
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("CHATREQUEST", error.toString());
//                        addReceiveChatBubble("Oops! something went wrong. Please retry.");
                    }
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(checkSubs);
    }

    private void handleScollToBottom() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chatScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 50);
    }
}