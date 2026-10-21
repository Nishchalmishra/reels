package arivihan.technologies.doubtbuzzter2;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.adapter.ReelsViewAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.fragments.ReelsFragment;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.model.ReelInfoDto;
import arivihan.technologies.doubtbuzzter2.model.ReelsApiResult;
import arivihan.technologies.doubtbuzzter2.model.ReelsDto;
import arivihan.technologies.doubtbuzzter2.model.ReelsType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REELS_GET;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REELS_UPDATE_LIKES;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REELS_UPDATE_VIEWS;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: ReelsActivity Copyright 29/5/21 3:09 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/5/21 3:09 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ReelsActivity extends BaseActivity
        implements MyReelsListener, EdgeToEdgeHelper.SelfManagedInsets {

    private ViewPager2 viewPager2;
    private FloatingActionButton fab;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ReelsViewAdapter reelsViewAdapter;
    private String uuid;
    private List<ReelsDto> reelsDtoList = new ArrayList<>();
    private boolean isFirst = true;
    private final String TAG = ReelsActivity.class.getSimpleName();
    private int sessionViewCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.REELS_DEMO) { DemoHome.open(this); finish(); return; }
        setContentView(R.layout.activity_reels);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.reel_bck_btn_click.getActionName());
                finish();
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uuid = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

            if (StringUtils.isEmpty(uuid)) {
                uuid = "default";
            }
        }
        viewPager2 = findViewById(R.id.view_pager_reel);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setOffscreenPageLimit(4);
        fab = findViewById(R.id.fab);
        applyReelsInsets();
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.REELS_SCREEN.name(), "SCREEN");

        reelsDtoList = new ArrayList<>();

        String videoUrl = getIntent().getStringExtra("video_uri");
        String viralVideoUrl = getIntent().getStringExtra("viral_video_url");
        Uri data = getIntent().getData();

//        String videoUrl;

        String contentUrl = null;

        if (StringUtils.isNotEmpty(videoUrl)) {
            contentUrl = videoUrl;
        } else if (StringUtils.isNotEmpty(viralVideoUrl)) {
            contentUrl = viralVideoUrl;
        }

        if (StringUtils.isEmpty(contentUrl) && data != null) {
            String deepLinkVideoUrl = data.getQueryParameter("video_url");
            if (StringUtils.isNotEmpty(deepLinkVideoUrl)) {
                contentUrl = deepLinkVideoUrl;
                viralVideoUrl = deepLinkVideoUrl; // Treat deep link as viral
            }
        }

        if (StringUtils.isNotEmpty(contentUrl)) {
            ReelsDto reelsDto = new ReelsDto();
            reelsDto.setContentUrl(contentUrl);
            reelsDto.setReelsType(ReelsType.VIDEO);
            reelsDto.setAspectRatio("1");
            reelsDto.setHideAction(true);
            reelsDtoList.add(reelsDto);
            if (contentUrl.equals(viralVideoUrl)) {
                getNextReelsData();
            }
        } else {
            getNextReelsData();
        }


        reelsViewAdapter = new ReelsViewAdapter(this, reelsDtoList, this);
        viewPager2.setAdapter(reelsViewAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.d("scroll page position", String.valueOf(position));
                if (position == reelsDtoList.size() - 2) {
                    getNextReelsData();
                }
                super.onPageSelected(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("scroll position", String.valueOf(state));

            }
        });
        fab.setOnClickListener(view -> {
            goToHomeScreen();
        });

        updateFabVisibility();
        ViewCompat.requestApplyInsets(findViewById(R.id.reels_root));
    }

    /** Keep the pager full-screen while lifting the floating home button above the nav bar. */
    private void applyReelsInsets() {
        View root = findViewById(R.id.reels_root);
        if (root == null || fab == null) return;

        ViewGroup.MarginLayoutParams baseParams =
                (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        final int baseBottomMargin = baseParams.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safeArea = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            int bottomInset = EdgeToEdgeHelper.bottomInset(insets, safeArea);
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            params.bottomMargin = baseBottomMargin + bottomInset;
            fab.setLayoutParams(params);

            // The pager and each reel consume the insets they need for their own controls.
            return insets;
        });
    }

    private void updateFabVisibility() {
        if (reelsDtoList.size() <= 1) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void goToHomeScreen() {
        try {
            Intent intent = new Intent(this, HomeScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(HomeScreen.EXTRA_OPEN_HOME_TAB, true);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to return to HomeScreen", ex);
            getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void getNextReelsData() {
        String lastReelsId = MyApplication.getStringData(PreferenceKeys.LAST_REELS_ID.getKey());
        if (null != lastReelsId && !lastReelsId.isEmpty()) {
            Log.d("nextReelsId", lastReelsId);
        }
        if (StringUtils.isNotEmpty(lastReelsId) && lastReelsId.equals("null")) {
            lastReelsId = null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("lastId", lastReelsId);
        map.put("size", 3);
        map.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        Log.d(TAG,"Next Reel Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(REELS_GET, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<ReelsApiResult> token = new TypeToken<ReelsApiResult>() {
                    };
                    ReelsApiResult reelsApiResult = gson.fromJson(response.toString(), token.getType());

                    Log.i("REELS_DATA", reelsApiResult.toString());

                    List<ReelInfoDto> data = reelsApiResult.getData();
                    if (data == null || data.isEmpty() || reelsApiResult.getLastId() == null || StringUtils.isEmpty(reelsApiResult.getLastId()) || reelsApiResult.getLastId().equals("null")) {

//                    if (StringUtils.isEmpty(reelsApiResult.getLastId())) {
                        MyApplication.saveData(PreferenceKeys.LAST_REELS_ID.getKey(), "null");
                        if (isFirst) {
                            getNextReelsData();
                        }
                    } else {
                        Log.d("nextReelsIdSave", reelsApiResult.getLastId());
                        MyApplication.saveData(PreferenceKeys.LAST_REELS_ID.getKey(), reelsApiResult.getLastId());
                    }
                    updateDataInView(reelsApiResult.getData());
                    isFirst = false;
                }, new VolleyGlobalErrorListener(ReelsActivity.this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(fab, getString(R.string.please_try_again));
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void updateDataInView(List<ReelInfoDto> datas) {

        for (ReelInfoDto data : datas) {
            ReelsDto reelsDto = new ReelsDto();
            reelsDto.setContentUrl(data.getReelsUrl());
            reelsDto.setReelsType(data.getReelsType());
            reelsDto.setId(data.getId());
            reelsDto.setLiked(data.isLiked());
            reelsDto.setName(data.getName());
            reelsDto.setActualLikes(data.getLikes());
            reelsDto.setActualViews(data.getViews());
            reelsDto.setAspectRatio(String.valueOf(data.getAspectRatio()));
            reelsDtoList.add(reelsDto);

        }
        reelsViewAdapter.notifyDataSetChanged();

        updateFabVisibility();

    }

    @Override
    public void setLike(boolean liked, long likes, int position) {
        if (position < 0 || position >= reelsDtoList.size()) {
            Log.e(TAG, "Invalid position in setViews: " + position);
            return;
        }
        ReelsDto reelsDto = reelsDtoList.get(position);
        reelsDto.setLiked(liked);
        long actualLikes = reelsDto.getActualLikes();
        if (liked)
            reelsDto.setActualLikes(actualLikes + 1);
        else if (actualLikes > 1)
            reelsDto.setActualLikes(actualLikes - 1);
        reelsViewAdapter.notifyDataSetChanged();
        Map<String, Object> map = new HashMap<>();
        map.put("reelsId", reelsDtoList.get(position).getId());
        map.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        map.put("liked", liked);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(REELS_UPDATE_LIKES, map), null,
                response -> {

                }, new VolleyGlobalErrorListener(ReelsActivity.this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setViews(int views, String reelsDtoName, int position) {
        if (position < 0 || position >= reelsDtoList.size()) {
            Log.e(TAG, "Invalid position in setViews: " + position);
            return;
        }
        sessionViewCount++;

        Map<String, String> Params = new HashMap<>();
        Params.put("reel_id", reelsDtoList.get(position).getId());
        Params.put("reel_name", reelsDtoName != null ? reelsDtoName : "unknown");
        Params.put("position", String.valueOf(position));
        Params.put("session_total_count", String.valueOf(sessionViewCount));

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.reels_viewed.getActionName(), Params);

        reelsDtoList.get(position).setActualViews(reelsDtoList.get(position).getActualViews());
        reelsViewAdapter.notifyDataSetChanged();
        Map<String, Object> map = new HashMap<>();
        map.put("reelsId", reelsDtoList.get(position).getId());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(REELS_UPDATE_VIEWS, map), null,
                response -> {

                }, new VolleyGlobalErrorListener(ReelsActivity.this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    @Override
    public void setWatched(String reelsDtoName) {

    }

    @Override
    public void setAspectRatio(double aspectRatio, String reelsDtoName) {

    }


    @Override
    protected void onDestroy() {
        if (MyVolleySingleton.getInstance(this) != null) {
            MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        }
        super.onDestroy();
    }


}
