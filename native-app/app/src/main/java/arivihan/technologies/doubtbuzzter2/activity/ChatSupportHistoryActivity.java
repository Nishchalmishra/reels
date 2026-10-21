package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHAT_HISTORY;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChatSupportHistoryListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.TutorSupport;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class ChatSupportHistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerview_chat_support_history);
        shimmer = findViewById(R.id.chat_support_history_shimmer);

        getChatHistory();

    }


    private void getChatHistory() {
        MyJsonArrayRequest chatReq = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TUTOR_SUPPORT_CHAT_HISTORY, new HashMap<>()),
                null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<List<TutorSupport>> token = new TypeToken<List<TutorSupport>>() {
                    };
                    List<TutorSupport> chatHistory = gson.fromJson(response.toString(), token.getType());

                    ChatSupportHistoryListAdapter supportHistoryListAdapter = new ChatSupportHistoryListAdapter(chatHistory, getApplicationContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(supportHistoryListAdapter);
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("CHATREQUEST", error.toString());
                    }
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(chatReq);
    }
}