package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.ADD_RECENT_SEARCH;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SEARCH_RESULT;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.SeeAllAdapter;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.SpeechRecognitionHelper;
import arivihan.technologies.doubtbuzzter2.helper.SpeechRecognitionListenerHelper;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.model.SearchResultResponse;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SeeAllActivity extends AppCompatActivity implements SeeAllAdapter.SeeAllItemClickListener {

    RecyclerView recyclerView;
    SeeAllAdapter seeAllAdapter;
    TextView noResult;
    TextView title;
    private static final String TAG = "SeeAllActivity";
    ArrayList<SearchResult> list = new ArrayList<>();
    ArrayList<SearchResultResponse> searchResult = new ArrayList<>();
    String searchQuery = "";
    String type;
    Gson gson;
    EditText editText;
    LinearLayout divider, noResultFoundLayout;
    ShimmerFrameLayout shimmerFrameLayout;

    ImageView globalVoiceSearch;
    int page = 0;
    Handler searchHandler;
  PlayStoreDialog playStoreDialog;
    String mTitle;
    private boolean noMoreData;
    private boolean isFirstLoad = true;
    private boolean dataLoaded=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchHandler != null)
                    searchHandler.removeCallbacksAndMessages(null);
                finish();
            }
        });

        recyclerView = findViewById(R.id.rv_see_all);
        noResult = findViewById(R.id.no_result);
        editText = findViewById(R.id.search_text);
        editText.setFilters(MyEmojiFilter.getFilter());
        title = findViewById(R.id.title);
        divider = findViewById(R.id.divider);
        globalVoiceSearch = findViewById(R.id.global_voice_search);
        shimmerFrameLayout = findViewById(R.id.search_shimmer);
        noResultFoundLayout = findViewById(R.id.no_result_found_layout);
        type = getIntent().getStringExtra("type");
        searchQuery = getIntent().getStringExtra("queryText");
        mTitle = getIntent().getStringExtra("title");
        Log.d(TAG, "onCreate: "+mTitle+"type-"+type);
        title.setText(mTitle);
        editText.setText(searchQuery);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        seeAllAdapter = new SeeAllAdapter(this, list, this);
        recyclerView.setAdapter(seeAllAdapter);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        gson = new Gson();
        shimmerFrameLayout.startShimmer();
        page = 0;
        getGlobalSearchResult(true);
        onEditorActionListener();

        editText.setSelection(0);
        editText.requestFocus();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (searchHandler != null)
                    searchHandler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                editText.setSelection(s.length());
                searchHandler = new Handler();
                searchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyVolleySingleton.getInstance(SeeAllActivity.this).getRequestQueue().cancelAll(TAG);
                        shimmerFrameLayout.startShimmer();
                        shimmerFrameLayout.setVisibility(View.VISIBLE);
                        noResultFoundLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        noMoreData = false;
                        dataLoaded=false;
                        page = 0;
                        getGlobalSearchResult(true);
                    }
                }, 1500);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        globalVoiceSearch.setOnClickListener(v -> {
            SpeechRecognitionHelper speechRecognitionHelper = new SpeechRecognitionHelper(this);
            speechRecognitionHelper.initializeSpeechRecognition(editText);
            speechRecognitionHelper.setVoiceSearchDialog();
            speechRecognitionHelper.startListening();
        });

    }


    private void onEditorActionListener() {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            searchQuery = editText.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !searchQuery.isEmpty()) {
                MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
                noMoreData = false;
                dataLoaded=false;
                page = 0;
                addRecentSearch(searchQuery);
                getGlobalSearchResult(true);
                return true;
            }
            return false;
        });
    }

    private void getGlobalSearchResult(boolean clear) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", searchQuery);
        map.put("type", type);
        map.put("hiEnSupported", true);
        map.put("page", page);
        MyJsonArrayRequest recentSearchRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SEARCH_RESULT, map), null,
                response -> {
                    Log.d("Global", response.toString());
                    TypeToken<ArrayList<SearchResultResponse>> token = new TypeToken<ArrayList<SearchResultResponse>>() {
                    };

                    try {
                        searchResult = gson.fromJson(response.toString(), token.getType());

                        Log.d(TAG, "getGlobalSearchResult: "+searchResult);
                        Log.d(TAG,"clearing the previous data"+clear);

                        if (dataLoaded && CollectionUtils.isEmpty(searchResult)){
                            return;
                        }
                        if (!clear && CollectionUtils.isEmpty(searchResult.get(0).getData()) && !searchResult.get(0).isLast() && !isFirstLoad) {
                            Toast.makeText(getApplicationContext(), getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
                            noMoreData = true;
                        }
                        if (searchResult.get(0).isLast())
                            noMoreData = true;

                        if (clear) {
                            list.clear();
                        }
                        int oldSize = list.size();
                        list.addAll(searchResult.get(0).getData());

                        if (clear) {
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            if (!list.isEmpty()) {
                                noResultFoundLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                dataLoaded=true;
                                seeAllAdapter.notifyDataSetChanged();
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                noResultFoundLayout.setVisibility(View.VISIBLE);
                                Log.d(TAG, "getGlobalSearchResult:noResultFoundLayout visible "+list.size());
                            }
                        } else {
                            seeAllAdapter.notifyItemRangeInserted(oldSize, searchResult.get(0).getData().size());

                        }
                    } catch (Exception e) {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        recyclerView.setVisibility(View.GONE);
                        noResultFoundLayout.setVisibility(View.VISIBLE);
                        Log.e(TAG, "getGlobalSearchResult Visible: ", e);
                    }
                }, new VolleyGlobalErrorListener(SeeAllActivity.this) {
            @Override
            public void postError(VolleyError error) {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                recyclerView.setVisibility(View.GONE);
                noResultFoundLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "getGlobalSearchResult:noResultFoundLayout visible ");
                Log.d("Global", error.toString());
            }
        });
        recentSearchRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(recentSearchRequest);
    }

    @Override
    public void onSeeAllItemClicked(String type, String title) {
        try {
            if (type.equals("QUICK_NOTE")) {
                Intent intent = new Intent(this, ViewNotesActivity.class);
                intent.putExtra("id", title);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), "");
                startActivity(intent);
            } else
                openPdf(title);
        }catch (Exception e){
            Log.d("onSeeAllItemClicked",e.getMessage());
        }
    }

    @Override
    public void loadData() {
        if (!noMoreData) {
            page++;
            if (page == 1)
                isFirstLoad = true;
            else
                isFirstLoad = false;
            getGlobalSearchResult(false);
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

    public void openPdf(String url) {
        try {
            PdfUtils.openPdfViewer(this, url, "","previous.year.paper","", false, "", "");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null)
            searchHandler.removeCallbacksAndMessages(null);

        if (playStoreDialog != null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
    }
}