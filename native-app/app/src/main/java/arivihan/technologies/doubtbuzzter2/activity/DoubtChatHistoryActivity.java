package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_HISTORY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_PREVIOUS_DOUBTS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.PreviousDoubtListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class DoubtChatHistoryActivity extends BaseActivity {

    RecyclerView previousDoubtRecyclerView;
    ShimmerFrameLayout chatHistoryShimmer;

    ImageView searchIcon;

    TextView headerTitle;
    EditText searchTextField;

    Map<String, Map<String, String>> previousDoubts = new HashMap<>();

    Map<String, Map<String, String>> filteredDoubts = new HashMap<>();
    PreviousDoubtListAdapter previousDoubtListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt_chat_history);

        previousDoubtRecyclerView = findViewById(R.id.previous_doubts_recycler_view);
        chatHistoryShimmer = findViewById(R.id.chat_history_shimmer);
        searchIcon = findViewById(R.id.search_icon);
        headerTitle = findViewById(R.id.header_title);
        searchTextField = findViewById(R.id.search_text_field);

        findViewById(R.id.new_chat_btn).setOnClickListener((v) -> {
            Intent intent = new Intent(this, ChatScreenChooseSubjectForChat.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.activity_back_btn).setOnClickListener(v -> {
            finish();
        });

        searchIcon.setOnClickListener(v -> {
            if (searchTextField != null) {
                searchTextField.setVisibility(View.VISIBLE);
            }
            if (headerTitle != null) {
                headerTitle.setVisibility(View.GONE);
            }
        });


        searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredDoubts.clear();
                if (s.length() == 0) {
                    filteredDoubts.putAll(previousDoubts);
                } else {
                    for (String key : previousDoubts.keySet()) {
                        if (previousDoubts.get(key) != null && previousDoubts.get(key).get("title").toString().toLowerCase().contains(s.toString().trim())) {
                            filteredDoubts.put(key, previousDoubts.get(key));
                        }
                    }
                }
                if (previousDoubtListAdapter != null) {
                    previousDoubtListAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        getListOfPreviousDoubts();
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
                    Gson gson = new Gson();

                    TypeToken<DoubtChatHistoryData> token = new TypeToken<DoubtChatHistoryData>() {
                    };
                    DoubtChatHistoryData doubtChatHistoryData = gson.fromJson(response.toString(), token.getType());

                    if (chatHistoryShimmer != null) {
                        chatHistoryShimmer.setVisibility(View.GONE);
                    }
                    Log.i("BOTRESPONSE", "PREVDOUBTS :: " + previousDoubts.toString());

                    previousDoubtRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    previousDoubtListAdapter = new PreviousDoubtListAdapter(getApplicationContext(), doubtChatHistoryData, false);
                    previousDoubtRecyclerView.setAdapter(previousDoubtListAdapter);

                    if (doubtChatHistoryData.getList().isEmpty()) {
                        TextView textView = findViewById(R.id.previous_chat_text);
                        if (textView != null) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(getString(R.string.no_chat_history));
                        }
                        if (findViewById(R.id.chat_history_btn) != null) {
                            findViewById(R.id.chat_history_btn).setVisibility(View.GONE);
                        }
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(feedbackRequest);
    }
}