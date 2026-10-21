package arivihan.technologies.doubtbuzzter2.activity.competitive.topic;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;

public class AdaptiveTopicListActivity extends BaseActivity {

    private static final String TAG = AdaptiveTopicListActivity.class.getSimpleName();

    // UI Variables


    // Data Variables
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_topic_list);

//        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        Uri data = getIntent().getData();

        if (data != null) {
            // From deep link
            id = data.getQueryParameter("id");
        } else {
            // From intent extras
            id = getIntent().getStringExtra("id");
        }

    }
}