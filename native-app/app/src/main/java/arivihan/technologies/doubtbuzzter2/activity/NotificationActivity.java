package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.FETCH_NOTIFICATION;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.NotificationAdapter;
import arivihan.technologies.doubtbuzzter2.model.NotificationModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class NotificationActivity extends BaseActivity {

    LinearLayout back;
    TextView title;
    RecyclerView recyclerView;
    NotificationAdapter adapter;
    LottieAnimationView lottieAnimationView;
    List<NotificationModel> model = new ArrayList<>();

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        back = findViewById(R.id.back_arrow);
        title = findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.notification));
        recyclerView = findViewById(R.id.recyclerview);
        lottieAnimationView = findViewById(R.id.lotti_notification);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL ,false));
        fetchNotification();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

    }

    public void fetchNotification() {
        Log.d( "fetchNotification","start");
        try {
            Log.d( "fetchNotification","try");
            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    FETCH_NOTIFICATION, null,
                    response -> {
                        Log.d( "fetchNotification",response.toString());
                        TypeToken<List<NotificationModel>> token = new TypeToken<List<NotificationModel>>() {};
                        model = gson.fromJson(String.valueOf(response), token.getType());
                        adapter = new NotificationAdapter(model,this);
                        recyclerView.setAdapter(adapter);

                        if(model.size() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            lottieAnimationView.setVisibility(View.VISIBLE);
                        }else
                            recyclerView.setVisibility(View.VISIBLE);
                        Log.d( "fetchNotification",model.toString());
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.d( "fetchNotification",error.toString());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
        } catch (Exception e) {
            Log.d( "fetchNotification",e.toString());
        }
    }


}