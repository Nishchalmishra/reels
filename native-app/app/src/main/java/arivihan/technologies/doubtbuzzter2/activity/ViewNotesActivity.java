package arivihan.technologies.doubtbuzzter2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.model.QuickNotesContents;
import arivihan.technologies.doubtbuzzter2.model.QuickNotesModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import lombok.SneakyThrows;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ViewNotesActivity extends LocaleActivity implements StoriesProgressView.StoriesListener{

    String id = "";
    String Type = "";
    RelativeLayout ParentLayout;

    private String[] resources;

    long pressTime = 0L;
    long limit = 500L;
    URL url;
    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private int counter = 0;
    private int listLength;
    private View zoomPart;

    QuickNotesModel quickNotesModel = new QuickNotesModel();
    List<QuickNotesContents> list = new ArrayList<>();
    Gson gson;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    storiesProgressView.setVisibility(View.GONE);
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    storiesProgressView.setVisibility(View.VISIBLE);
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    private void setName() {
        try {
            FirebaseCrashlytics crashlytics = MyApplication.getFirebaseInstance();
            String name = getIntent().getStringExtra("Name");

            crashlytics.setCustomKey("notesName", name);


        } catch (Exception e) {
            Log.d("Error", "Error");

        }
    }


    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);

        id = getIntent().getStringExtra("id");
        Type = getIntent().getStringExtra(PreferenceKeys.TYPE.getKey());

        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        storiesProgressView.setStoriesListener(this);
        if(id!=null) {
            getNotesImages(id);
        }
        else {
            try {
                resources = getIntent().getStringArrayExtra("images");
                listLength = resources.length;
                storiesProgressView.setStoriesCount(resources.length);
                if (!Type.equals("Manually")) {
                    storiesProgressView.setStoryDuration(5000L);
                } else {
                    storiesProgressView.setStoryDuration(Long.MAX_VALUE);
                }

                storiesProgressView.startStories();
                Glide.with(getApplicationContext()).load(resources[counter]).into(image);
            } catch (Exception ignored) {}
        }

        CommonMethods.showLongPositiveSnackbar(ParentLayout,"Click left and Right side for changing Page");

        View reverse = findViewById(R.id.reverse);
        zoomPart = findViewById(R.id.zoomPart);

        reverse.setOnClickListener(v -> storiesProgressView.reverse());

        zoomPart.setOnTouchListener(onTouchListener);

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);
        setName();

        gson = new Gson();
    }

    void getNotesImages(String id){
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UrlRoutes.GET_NOTES,map), null,
                 response -> {
                    TypeToken<QuickNotesModel> token = new TypeToken<QuickNotesModel>() {};
                    quickNotesModel = gson.fromJson(response.toString(),token.getType());
                    list = quickNotesModel.getQuickNotesContents();
                    listLength =list.size();
                     storiesProgressView.setStoriesCount(list.size());
                     if (!Type.equals("Manually")) {
                         storiesProgressView.setStoryDuration(5000L);
                     }
                     else {
                         storiesProgressView.setStoryDuration(Long.MAX_VALUE);
                     }
                     storiesProgressView.startStories();
                    Glide.with(getApplicationContext()).load(list.get(counter).getContentUrl()).into(image);
                }, error -> {

                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myJsonObjectRequest);
    }

    @SneakyThrows
    @Override
    public void onNext() {
        if(counter + 1 >= listLength) return;
        counter++;
        try {
            if(!list.isEmpty())
                Glide.with(getApplicationContext()).load(list.get(counter).getContentUrl()).into(image);
            else
                Glide.with(getApplicationContext()).load(resources[counter]).into(image);
        } catch (Exception e){

        }
    }

    @SneakyThrows
    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        counter--;
        try {
            if(!list.isEmpty())
              Glide.with(getApplicationContext()).load(list.get(counter).getContentUrl()).into(image);
            else
              Glide.with(getApplicationContext()).load(resources[counter]).into(image);
        } catch (Exception e){

        }
    }

    @Override
    public void onComplete() {
        Intent i = new Intent(ViewNotesActivity.this, QuickNotesActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();

    }
}
