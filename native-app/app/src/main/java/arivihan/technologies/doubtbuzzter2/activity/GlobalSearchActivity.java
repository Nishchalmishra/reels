package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.ADD_RECENT_SEARCH;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DOUBT_MICROLECTURE_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DOUBT_RESULT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_RECENT_SEARCHES;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SEARCH_RESULT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SEARCH_SUGGESTIONS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIBED_SUBJECT;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.OnBackPressedCallback;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.GlobalSearchAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.GlobalSearchSectionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.GlobalSearchVideoAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.RecentSearchAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SearchSuggestionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.customView.ContactUsBottomSheetFragment;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.SpeechRecognitionListenerHelper;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.model.SearchResultResponse;
import arivihan.technologies.doubtbuzzter2.model.SectionActionType;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class GlobalSearchActivity extends BaseActivity implements GlobalSearchAdapter.GlobalSearchInterface,
        RecentSearchAdapter.RecentSearchOnClickListener,
        SearchSuggestionAdapter.SuggestionOnClickListener,
        GlobalSearchSectionAdapter.GlobalSearchSectionInterface,
        GlobalSearchVideoAdapter.GlobalSearchVideoInterface,
        SortSubjectNameAnalyticAdapter.OnSubjectSortClick,
        SubjectNameAdapter.OnSubjectClick {
    private static final String TAG = GlobalSearchActivity.class.getSimpleName();
    private static final String SEARCH_SUGGESTION_API = "GlobalSearchActivity";
    private final int RecordAudioRequestCode = 0;

    ImageView globalVoiceSearch;
    TextSwitcher globalTextSwitcher;
    LinearLayout textSwitcherLayout;
    EditText globalSearchText;
    TextView searchTextStart;
    int i = 0;
    Handler handler;
    Runnable runnable;

    ArrayList<String> searchOptionList;
    ArrayList<String> searchSuggestionList;
    RecyclerView suggestionRecyclerView;
    SearchSuggestionAdapter suggestionAdapter;

    LinearLayout divider_2;
    TextView recentSearchTitle;
    ArrayList<String> recentSearchList;
    RecentSearchAdapter recentSearchAdapter;
    RecyclerView recentSearchRecycler;
    ArrayList<SearchResultResponse> list;
    RecyclerView globalSearchRecycler;
    GlobalSearchAdapter globalSearchAdapter;
    Gson gson;
    ArivihanCustomDialog voiceSearchDialog;
    LottieAnimationView lottieAnimationView;

    SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    TextView txtSpeechInput;
    boolean isResultObtained;
    private AudioManager mAudioManager;
    ArrayList<SearchResult> doubtList;
    String newSearchQuery = "";
    boolean isDoubtResultReady = false;
    int mPosition;

    ShimmerFrameLayout recentSearchShimmer;
    String queryText;
    long recognizerStartTime = 0;
    FloatingActionButton voiceFab;
    int recognitionStartFrequency = 0;
    private TextView searchTextEnd;

    private Handler suggestApiCallHandler, globalSearchApiCallHandler;

    FloatingActionButton fab_numerical, fab_conceptual;
    TextView conceptualQuestionText, numericalQuestionText;
    Dialog selectSubject;
    Animation toBottom, fromBottom;
    private MicroLectureContentDto microLectureContentDto;
    private ProgressDialog loading;
    public int dataLoaded = 0;
    private List<SubjectResponse> sub = new ArrayList<>();
    LinearLayout ParentLayout;
    boolean clicked = false;
    LinearLayout noResultLayout;
    LottieAnimationView noResultAnim;
   PlayStoreDialog playStoreDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_search);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (globalSearchApiCallHandler != null)
                    globalSearchApiCallHandler.removeCallbacksAndMessages(null);
                if (suggestApiCallHandler != null)
                    suggestApiCallHandler.removeCallbacksAndMessages(null);

                if (clicked) {
                    clicked = false;
                    setVisibility();
                } else if (globalSearchText.getText().length() > 0) {
                    globalSearchText.setText("");
                } else {
                    finish();
                }
            }
        });

        noResultAnim = findViewById(R.id.no_result_anim);
        noResultLayout = findViewById(R.id.no_result_layout);
        ParentLayout = findViewById(R.id.Parent_Layout);
        numericalQuestionText = findViewById(R.id.numerical_question_text);
        conceptualQuestionText = findViewById(R.id.conceptual_question_text);
        fab_numerical = findViewById(R.id.numerical_question);
        fab_conceptual = findViewById(R.id.conceptual_question);
        divider_2 = findViewById(R.id.divider_2);
        recentSearchTitle = findViewById(R.id.recent_search_title);
        globalVoiceSearch = findViewById(R.id.global_voice_search);
        globalTextSwitcher = findViewById(R.id.global_text_switcher);
        globalSearchText = findViewById(R.id.global_search_text);
        globalSearchText.setFilters(MyEmojiFilter.getFilter());
        searchTextStart = findViewById(R.id.txt_search_start);
        searchTextEnd = findViewById(R.id.txt_search_end);
        textSwitcherLayout = findViewById(R.id.text_switcher_layout);
        suggestionRecyclerView = findViewById(R.id.rv_search_suggestions);
        recentSearchRecycler = findViewById(R.id.rv_recent_searches);
        globalSearchRecycler = findViewById(R.id.rv_global_search);
        recentSearchShimmer = findViewById(R.id.recent_search_shimmer);
        recentSearchShimmer.startShimmer();
        setGlobalSearchLayout();

        selectSubject = new Dialog(this);
        selectSubject.setContentView(R.layout.custom_dialogue_subjectselection);
        selectSubject.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        selectSubject.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        toBottom = AnimationUtils.loadAnimation(this, R.anim.tobottom);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);


        ParentLayout.setOnClickListener(v -> {
            clicked = !clicked;
            setVisibility();
        });

        loading = new ProgressDialog(this);
        loading.setCancelable(true);
        loading.setMessage(getString(R.string.loading));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        globalVoiceSearch.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(GlobalSearchActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                askPermission();
            } else {
                startListening();
            }
        });


        String globalSearchStartText = getText("global_search_start_text");
        String globalSearchSwitchText = getText("global_search_switch_text");
        String globalSearchEndText = getText("global_search_end_text");

        searchOptionList = new ArrayList<>();

        if (StringUtils.isNotEmpty(globalSearchSwitchText)) {
            String[] responses = globalSearchSwitchText.split(",");
            searchOptionList.addAll(Arrays.asList(responses));
        } else {
            searchOptionList.add("Notes");
            searchOptionList.add("Classes");
            searchOptionList.add("Test Series");
        }

        searchTextStart.setText(globalSearchStartText);
        searchTextEnd.setText(globalSearchEndText);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVoiceSearchDialog();
        initializeSpeechRecognition();
        startSearchOptionsHandler();
        gson = new Gson();
        setRecentSearchList();
        getRecentSearches();
        getGlobalSearchResult();

    }

    private void showSubjectDropdown() {
        ArrayList<String> subjects = new ArrayList<>();
        ArrayList<String> subjectIds = new ArrayList<>();

        for (SubjectResponse subjectModel : sub) {
            if (subjectModel.isToShowInConceptual()) {
                subjects.add(subjectModel.getSubjectName());
                subjectIds.add(subjectModel.getSubjectCode());
            }
        }

        SortSubjectNameAnalyticAdapter subjectAdapter = new SortSubjectNameAnalyticAdapter(subjects, subjectIds,
                this, subjects.size());

        RecyclerView recyclerView = selectSubject.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(subjectAdapter);
        selectSubject.show();
    }

    private void setVisibility() {
        if (clicked) {
            ParentLayout.setBackgroundColor(getResources().getColor(R.color.black));
            ParentLayout.setVisibility(View.VISIBLE);
            fab_numerical.setVisibility(View.VISIBLE);
            fab_conceptual.setVisibility(View.VISIBLE);
            numericalQuestionText.setVisibility(View.VISIBLE);
            conceptualQuestionText.setVisibility(View.VISIBLE);
        } else {
            ParentLayout.setVisibility(View.GONE);
            fab_conceptual.setVisibility(View.GONE);
            fab_numerical.setVisibility(View.GONE);
            numericalQuestionText.setVisibility(View.GONE);
            conceptualQuestionText.setVisibility(View.GONE);
        }
    }

    private String getText(String key) {
        String text;
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String appendText = "_" + lang;
        if ("en".equalsIgnoreCase(lang)) {
            appendText = "";
        }
        Log.d(TAG, "Remote config value for appendText " + appendText);

        text = MyApplication.getFirebaseRemoteConfig().getString(key + appendText);
        Log.d(TAG, "Remote config value for key 1 " + key + " text = " + text);
        if (!"".equalsIgnoreCase(text) && StringUtils.isEmpty(text)) {
            text = MyApplication.getFirebaseRemoteConfig().getString(key);
            Log.d(TAG, "Remote config value for key 2 " + key + " text = " + text);
        }
        return text;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_please_approve), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.permission_denied_please_approve), Toast.LENGTH_SHORT).show();
        }
    }

    private void startListening() {
        isResultObtained = false;
        voiceSearchDialog.show();
        voiceFab.setVisibility(View.GONE);
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();
        txtSpeechInput.setText(getString(R.string.listening));
        recognizerStartTime = System.currentTimeMillis();
        recognitionStartFrequency = 1;
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RecordAudioRequestCode);
        }
    }

    private void initializeSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (lang == null) {
            lang = "";
        }

        if ("hi-rIN".equals(lang) || "hi".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        } else if ("mr".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN");
        } else if ("gu".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "gu-IN");
        } else if ("bn".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN");
        } else if ("te".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN");
        } else if ("kn".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "kn-IN");
        } else if ("ta".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN");
        } else if ("ml".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ml-IN");
        } else if ("pa".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pa-Guru-IN");
        } else {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        }

        speechRecognizer.setRecognitionListener(new SpeechRecognitionListenerHelper() {
            @Override
            public void onReadyForSpeechCallback(Bundle params) {
                Log.d(TAG, "onReadyForSpeechCallback");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
                txtSpeechInput.setText("");
                super.onBeginningOfSpeech();
            }

            @Override
            public void onEndOfSpeech() {

                super.onEndOfSpeech();
                Log.d(TAG, "onEndOfSpeech before is result obtained");

            }

            @Override
            public void onResultsCallback(Bundle params) {
                Log.d(TAG, "onResultsCallback");

                isResultObtained = true;
                speechRecognizer.stopListening();
                ArrayList<String> result = params.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                txtSpeechInput.setText(result.get(0));
                Log.d(TAG, "onResultsCallback result = " + Arrays.toString(new ArrayList[]{result}));


                voiceSearchDialog.dismiss();
                lottieAnimationView.pauseAnimation();

                globalSearchText.setText(result.get(0));
                globalSearchText.setSelection(result.get(0).length());


            }

            @Override
            public void onError(int error) {
                Log.d(TAG, "onError == " + error);
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Log.d(TAG, "onError == Again starting " + error);

                    restartVoice("onError");
                }
            }
        });
    }

    private void restartVoice(String type) {
        txtSpeechInput.setText("");
        speechRecognizer.cancel();
        long currentTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "difference " + type + " = " + (currentTimeMillis - recognizerStartTime));
        Log.d(TAG, "Frequency " + recognitionStartFrequency);

        if (currentTimeMillis < recognizerStartTime + 6000 && recognitionStartFrequency < 2) {
            Log.d(TAG, "Restarting voice recognition");
            recognitionStartFrequency++;
            isResultObtained = false;
            lottieAnimationView.setVisibility(View.VISIBLE);
            voiceFab.setVisibility(View.GONE);
            lottieAnimationView.playAnimation();
            txtSpeechInput.setText(getString(R.string.listening));
            recognizerStartTime = System.currentTimeMillis();
            speechRecognizer.startListening(speechRecognizerIntent);
        } else {
            Log.d(TAG, "Stopping voice recognition");

            txtSpeechInput.setText(getString(R.string.global_search_unclear_voice_error));
            lottieAnimationView.setVisibility(View.GONE);
            voiceFab.setVisibility(View.VISIBLE);
        }
    }

    private void muteSound() {
        try {
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void setVoiceSearchDialog() {
        voiceSearchDialog = new ArivihanCustomDialog(this);
        voiceSearchDialog.setContentView(R.layout.voice_search_animation_layout);
        voiceSearchDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        voiceSearchDialog.getWindow().setGravity(Gravity.BOTTOM);
        voiceSearchDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EdgeToEdgeHelper.applyToDialog(voiceSearchDialog);
        lottieAnimationView = voiceSearchDialog.findViewById(R.id.voice_search_animation);
        voiceFab = voiceSearchDialog.findViewById(R.id.fab_voice);

        voiceFab.setOnClickListener((v) -> {
            startListening();
        });
        voiceSearchDialog.findViewById(R.id.close).setOnClickListener(v -> {
            voiceSearchDialog.dismiss();
            speechRecognizer.stopListening();
            lottieAnimationView.pauseAnimation();
        });
        txtSpeechInput = voiceSearchDialog.findViewById(R.id.txt_speech_input);
        voiceSearchDialog.setOnDismissListener(dialog -> {
            speechRecognizer.stopListening();
            txtSpeechInput.setText("");
        });
    }

    private void getSuggestions(String text) {
        Log.d("SearchCode", "GET SUGGESTION RESULT CALLED");

        searchSuggestionList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("text", text);
        MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SEARCH_SUGGESTIONS, params), null,
                response -> {
                    TypeToken<ArrayList<String>> token = new TypeToken<>() {
                    };
                    searchSuggestionList = gson.fromJson(response.toString(), token.getType());
                    Log.d("SearchCode", "Size " + searchSuggestionList.size());

                    if (searchSuggestionList.size() > 0) {
                        noResultLayout.setVisibility(View.GONE);
                        divider_2.setVisibility(View.VISIBLE);
                        suggestionRecyclerView.setVisibility(View.VISIBLE);
                        recentSearchRecycler.setVisibility(View.GONE);
                        recentSearchTitle.setVisibility(View.GONE);
                    } else {
                        recentSearchRecycler.setVisibility(View.GONE);
                        recentSearchTitle.setVisibility(View.GONE);
                        divider_2.setVisibility(View.GONE);
                    }
                    setSearchSuggestionList();
                }, error -> {
            Log.d("SearchCode", "Search Suggestion error=  " + error.toString());
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
    }

    private void setSearchSuggestionList() {
        suggestionAdapter = new SearchSuggestionAdapter(this, searchSuggestionList, this);
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        suggestionRecyclerView.setAdapter(suggestionAdapter);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        if (globalSearchApiCallHandler != null) {
            globalSearchApiCallHandler.removeCallbacksAndMessages(null);
        }
        if (playStoreDialog!= null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("DOUBT_REQUEST");
        super.onDestroy();
    }

    void startSearchOptionsHandler() {
        handler = new Handler();
        runnable = this::changeText;
        runnable.run();
        onGlobalSearchTextChange();
        onEditorActionListener();
    }

    private void onEditorActionListener() {
        globalSearchText.setOnEditorActionListener((v, actionId, event) -> {
            newSearchQuery = globalSearchText.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !newSearchQuery.isEmpty()) {
                Log.d(TAG, "setOnEditorActionListener");
                suggestionRecyclerView.setVisibility(View.GONE);
                recentSearchRecycler.setVisibility(View.GONE);
                recentSearchTitle.setVisibility(View.GONE);
                if (globalSearchApiCallHandler != null)
                    globalSearchApiCallHandler.removeCallbacksAndMessages(null);
                getSuggestions(newSearchQuery);
                getGlobalSearchResult();
                addRecentSearch(newSearchQuery);
                return true;
            }
            return false;
        });
    }

    void onGlobalSearchTextChange() {
        globalSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                globalSearchText.requestFocus();
                if (globalSearchApiCallHandler != null) {
                    globalSearchApiCallHandler.removeCallbacksAndMessages(null);
                }
                if (suggestApiCallHandler != null) {
                    suggestApiCallHandler.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
                onTextChange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged");
            }
        });
    }

    private void onTextChange(String s) {
        queryText = s;
        newSearchQuery = s;

        if (s.length() == 0) {
            globalTextSwitcher.setVisibility(View.VISIBLE);
            searchTextStart.setVisibility(View.VISIBLE);
            searchTextEnd.setVisibility(View.VISIBLE);
            suggestionRecyclerView.setVisibility(View.GONE);
            getRecentSearches();
            getGlobalSearchResult();
        } else {
            if (s.length() >= 3) {

                suggestApiCallHandler = new Handler();
                suggestApiCallHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSuggestions(queryText);
                    }
                }, 600);

                globalSearchApiCallHandler = new Handler();
                globalSearchApiCallHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getGlobalSearchResult();
                    }
                }, 1500);
            }

            globalTextSwitcher.setVisibility(View.GONE);
            searchTextStart.setVisibility(View.GONE);
            searchTextEnd.setVisibility(View.GONE);
        }
    }

    private void changeText() {
        if (i == searchOptionList.size()) i = 0;
        int position = i++;
        globalTextSwitcher.setText(searchOptionList.get(position));
        handler.postDelayed(runnable, 3200);
    }

    private void getRecentSearches() {
        Log.d("recentSearches", "RECENET RESULT CALLED");

        MyJsonArrayRequest recentSearchRequest = new MyJsonArrayRequest(Request.Method.GET,
                GET_RECENT_SEARCHES, null,
                response -> {
                    Log.d("Global", response.toString());
                    TypeToken<ArrayList<String>> token = new TypeToken<ArrayList<String>>() {
                    };
                    recentSearchList.clear();
                    recentSearchList.addAll(gson.fromJson(response.toString(), token.getType()));
                    if (recentSearchList.size() == 0) {
                        recentSearchShimmer.stopShimmer();
                        recentSearchShimmer.setVisibility(View.GONE);
                        recentSearchTitle.setVisibility(View.GONE);
                        recentSearchRecycler.setVisibility(View.GONE);
                        divider_2.setVisibility(View.GONE);
                    } else {
                        recentSearchShimmer.stopShimmer();
                        recentSearchShimmer.setVisibility(View.GONE);
                        divider_2.setVisibility(View.VISIBLE);
                        recentSearchTitle.setVisibility(View.VISIBLE);
                        recentSearchRecycler.setVisibility(View.VISIBLE);
                        recentSearchAdapter.notifyDataSetChanged();
                    }
                    Log.d("recentSearches", recentSearchList.toString());
                }, new VolleyGlobalErrorListener(GlobalSearchActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("recentSearches", error.toString());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(recentSearchRequest);
    }

    private void setRecentSearchList() {
        recentSearchList = new ArrayList<>();
        recentSearchRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recentSearchAdapter = new RecentSearchAdapter(this, recentSearchList, this);
        recentSearchRecycler.setAdapter(recentSearchAdapter);
    }

    private void getGlobalSearchResult() {
        Log.d("SearchCode", "GLOBALSERAC RESULT CALLED");
        Map<String, Object> map = new HashMap<>();
        map.put("text", newSearchQuery);
        map.put("hiEnSupported", true);
        Log.d("SearchQuery", newSearchQuery);
        MyJsonArrayRequest recentSearchRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SEARCH_RESULT, map), null,
                response -> {
                    Log.d("Global", response.toString());
                    TypeToken<ArrayList<SearchResultResponse>> token = new TypeToken<ArrayList<SearchResultResponse>>() {
                    };
                    ArrayList<SearchResultResponse> searchResultList;
                    searchResultList = gson.fromJson(response.toString(), token.getType());
                    list.clear();
                    list.addAll(searchResultList);
                    if (searchResultList.isEmpty()) {
                        noResultAnim.playAnimation();
                        noResultLayout.setVisibility(View.VISIBLE);
                    } else {
                        noResultAnim.pauseAnimation();
                        noResultLayout.setVisibility(View.GONE);
                    }
                    globalSearchRecycler.setVisibility(View.VISIBLE);

                    globalSearchAdapter = new GlobalSearchAdapter(this, list, this, this, this);
                    globalSearchRecycler.setAdapter(globalSearchAdapter);
                    callDoubtApi(list);
                }, new VolleyGlobalErrorListener(GlobalSearchActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("SearchCode", error.toString());
            }
        });
        recentSearchRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(recentSearchRequest);
    }

    private void callDoubtApi(ArrayList<SearchResultResponse> list) {
        int position = -1;
        if (!CollectionUtils.isEmpty(list)) {
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getType().equalsIgnoreCase("DOUBT")) {
                    position = j;
                    break;
                }
            }
        }
        if (position >= 0) {
            fetchDoubt(position, list.get(position));
        }
    }

    private void setGlobalSearchLayout() {
        list = new ArrayList<>();
        globalSearchRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        globalSearchAdapter = new GlobalSearchAdapter(this, list, this);
        globalSearchRecycler.setAdapter(globalSearchAdapter);
    }

    private void getDoubtPosition() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType().equalsIgnoreCase("Doubt")) {
                mPosition = i;
                break;
            }
        }

    }

    @Override
    public void fetchDoubt(int position, SearchResultResponse searchResultResponse) {
        newSearchQuery = globalSearchText.getText().toString();
        doubtList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("userQuestion", newSearchQuery);
        Log.d("Doubt", "Fetch doubt started");
        Map<String, String> params = new HashMap<>();
        params.put("Search_text", newSearchQuery);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.global_search_text.getActionName(), params);

        MyJsonArrayRequest recentSearchRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_DOUBT_RESULT, map), null,
                response -> {
                    Log.d("Doubt", response.toString());
                    TypeToken<ArrayList<SearchResult>> token = new TypeToken<>() {
                    };
                    doubtList = gson.fromJson(response.toString(), token.getType());
                    getDoubtPosition();
                    list.get(mPosition).setData(doubtList);
                    list.get(mPosition).setTitle(searchResultResponse.getTitle());
                    list.get(mPosition).setType("");
                    isDoubtResultReady = true;
                    updateAdapter(mPosition);
                }, new VolleyGlobalErrorListener(GlobalSearchActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("Doubt", error.toString());
                list.get(position).setData(new ArrayList<>());
                list.get(position).setTitle("");
                list.get(position).setType("");
                isDoubtResultReady = true;
                updateAdapter(position);
            }
        });
        recentSearchRequest.setTag("DOUBT_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(recentSearchRequest);
    }

    @Override
    public void onSeeAllClick(int position) {
        Intent intent = new Intent(this, SeeAllActivity.class);
        intent.putExtra("type", list.get(position).getType());
        intent.putExtra("queryText", queryText);
        intent.putExtra("title", list.get(position).getTitle());
        startActivity(intent);
    }

    void updateAdapter(int position) {
        if (isDoubtResultReady) {
            globalSearchAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onRecentSearchClick(String query) {
        Log.d(TAG, "onRecentSearchClick");
        addRecentSearch(query);
        globalSearchText.setText(query);
        globalSearchText.setSelection(query.length());
    }

    @Override
    public void onSuggestionClick(String query) {
        Log.d(TAG, "onSuggestionClick");
        addRecentSearch(query);
        globalSearchText.setText(query);
        globalSearchText.post(() -> globalSearchText.setSelection(query.length()));
    }

    @Override
    public void onItemClick(String headingType, String type, String title) {
        SectionActionType action = SectionActionType.valueOf(type);
        switch (action) {
            case CONTACT_US:
                showContactUsBottomSheet();
                break;
            case SAMPLE_PAPER:
                if (headingType.equals(type))
                    openPdf(title);
                else {
                    Intent testSeriesIntent = new Intent(this, SeeAllActivity.class);
                    testSeriesIntent.putExtra("type", type);
                    testSeriesIntent.putExtra("queryText", "");
                    testSeriesIntent.putExtra("title", title);
                    startActivity(testSeriesIntent);
                }
                break;
            case CHAPTER_NOTES:
                openPdf(title);
                break;
            case QUICK_NOTE:
                Intent intent = new Intent(this, ViewNotesActivity.class);
                intent.putExtra("id", title);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), "");
                startActivity(intent);
                break;
            case ASK_A_DOUBT:
                startActivity(new Intent(this, ChatScreenChooseSubjectForChat.class));
                break;
            default:
                Intent defaultIntent = new Intent(this, SeeAllActivity.class);
                defaultIntent.putExtra("type", type);
                defaultIntent.putExtra("queryText", "");
                defaultIntent.putExtra("title", title);
                startActivity(defaultIntent);
        }
    }

    public void openPdf(String url) {
        try {
            PdfUtils.openPdfViewer(this, url, "sample","previous.year.paper","", false, "", "");
//            Intent target = new Intent(Intent.ACTION_VIEW);
//            target.setDataAndType(Uri.parse(url), "application/pdf");
//            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            if (target.resolveActivity(this.getPackageManager()) != null) {
//                Intent intent = Intent.createChooser(target, "Open File");
//                startActivity(intent);
//            } else {
//                playStoreDialog = new PlayStoreDialog(this);
//                playStoreDialog.showPlayStoreDialog();
//            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
        }
    }


    private void showContactUsBottomSheet() {
        if (isFinishing() || isDestroyed())
        {
            return;
        }
        try {
            ContactUsBottomSheetFragment contactSheet = new ContactUsBottomSheetFragment();
            contactSheet.show(
                    getSupportFragmentManager(),
                    "profile_contact_bottom_sheet"
            );
        } catch (Exception e) {
            Log.e("ContactBottomSheet", "Failed to show bottom sheet", e);
        }
    }

    private void addRecentSearch(String searchText) {

        Map<String, Object> params = new HashMap<>();
        params.put("text", searchText);

        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(ADD_RECENT_SEARCH, params), null,
                response -> {
                }, error -> {
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void addToRecentSearch(String title) {
        addRecentSearch(title);
    }

    @Override
    public void onVideoItemClick(String title) {
        addRecentSearch(title);
    }

    @Override
    public void onsubjectSortClick(String id, String name, int position) {
        if (selectSubject != null) {
            selectSubject.dismiss();
            selectSubject.cancel();
        }
        if (microLectureContentDto != null) {
            startVideoActivity(id);
        } else {
            loading.show();
            getDoubtVideoContent(id);
        }
    }

    private void getDoubtVideoContent(String selectedSubject) {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_DOUBT_MICROLECTURE_CONTENT, map), null,
                response -> {
                    TypeToken<MicroLectureContentDto> token = new TypeToken<MicroLectureContentDto>() {
                    };
                    microLectureContentDto = gson.fromJson(response.toString(), token.getType());
                    if (StringUtils.isNotEmpty(selectedSubject)) {
                        startVideoActivity(selectedSubject);
                        loading.cancel();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {

                dataLoaded = 0;
                if (loading != null) {
                    loading.cancel();
                }
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void startVideoActivity(String id) {
        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("selectedChapter", microLectureContentDto.getChapterName());
        intent.putExtra(getString(R.string.selected_chpter_id), microLectureContentDto.getChapterId());
        intent.putExtra("position", microLectureContentDto.getDefaultStartTime());
        intent.putExtra("selectedSubject", id);
        intent.putExtra("shouldNotResume", false);
        intent.putExtra("doubtVideo", true);
        intent.putExtra("isAskDoubtActivity", true);
        intent.putExtra("askDoubtResumePosition", Long.valueOf(microLectureContentDto.getResumePosition()));
        intent.putExtra("resumePosition", Long.valueOf(microLectureContentDto.getDefaultStartTime()));
        startActivity(intent);
    }

    private void subjectExtraction() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("customSupported", true);
        map.put("personalisedSupported", true);
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        map.put("languageSupport", true);

        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SUBSCRIBED_SUBJECT, map), null,
                response -> {
                    TypeToken<List<SubjectResponse>> token = new TypeToken<List<SubjectResponse>>() {
                    };
                    List<SubjectResponse> list = gson.fromJson(response.toString(), token.getType());
                    sub.clear();
                    for (SubjectResponse model : list) {
                        sub.add(model);
                    }
                    loading.cancel();
                    showSubjectDropdown();

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                dataLoaded = 0;
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);


    }

    @Override
    public void onsubjectClick(int position) {
        SubjectResponse subjectModel = sub.get(position);
        String s = sub.get(position).getSubjectName();
        String icon = sub.get(position).getIconUrl();

        if (sub.get(position).isOnDemand() || s.equalsIgnoreCase("On-Demand Lecture")) {
            Intent intent = new Intent(this, CustomChapterListActivity.class);
            intent.putExtra("SubjectName", s);
            intent.putExtra("SubjectId", subjectModel.getId());
            intent.putExtra("Icon", icon);
            startActivity(intent);
        } else if (sub.get(position).isPersonalised() || s.equalsIgnoreCase("Personalised Lecture")) {
            Intent intent = new Intent(this, PersonalisedLecture.class);
            intent.putExtra("SubjectName", s);
            intent.putExtra("SubjectId", subjectModel.getId());
            intent.putExtra("Icon", icon);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ChapterListActivity.class);
            intent.putExtra("SubjectName", s);
            intent.putExtra("SubjectId", subjectModel.getId());
            intent.putExtra("Icon", icon);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (StringUtils.isEmpty(queryText))
            getRecentSearches();
    }
}