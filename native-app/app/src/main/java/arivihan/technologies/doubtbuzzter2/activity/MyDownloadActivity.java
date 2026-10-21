package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_QANS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_DOWNLOAD;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_LECTURE_DATA;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VALIDATE_ANSWER;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.offline.MyDownloadVideoAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.CancelDownloadBottomSheet;
import arivihan.technologies.doubtbuzzter2.fragments.offline.MyDownloadLecturesFragment;
import arivihan.technologies.doubtbuzzter2.fragments.offline.MyDownloadNotesFragment;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.manager.DemoUtil;
import arivihan.technologies.doubtbuzzter2.manager.DownloadTracker;
import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.OfflineDownloadEntity;
import arivihan.technologies.doubtbuzzter2.viewModel.MediaViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MyDownloadActivity extends BaseActivity {

    private final String TAG = MyDownloadActivity.class.getSimpleName();
    public static final String bundleIsNoteSelected = "noteSelection";
    private LinearLayout back, noResultsLayout, noDownloadFound;
    //    private RecyclerView recyclerView;
//    private MyDownloadVideoAdapter adapter;
    private Dialog optionDialog, lectureLockedPopUp;
    private TextView title, activityTitle;
    private SearchView searchView;
    private boolean hasSearchText;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private MyDownloadLecturesFragment lecturesFragment;
    private MyDownloadNotesFragment notesFragment;
    private boolean isNoteFragmentSelected = false;
//    private List<MediaEntity> itemList;
//    private int currentPos;
//    private MediaViewModel mediaViewModel;
//    private CardView watchLectureButton;
//    private CardView retryDownloadButton;
//    private LinearLayout progressLayout;
//    private LinearLayout contentLayout;
//    private List<MicroLectureQuestionsAnsDto> questionsAnsList = new ArrayList<>();
//    private List<MicroLectureQuestionAnsResponseDto> questionsAnsResponseList = new ArrayList<>();
//    private Gson gson;
//    private ImageView cancel;
//    private CancelDownloadBottomSheet cancelDownloadBottomSheet;
//    private LiveData<MediaEntity> cancelDownloadLiveData;
//    private String queryText="";
//    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_my_download);
        initializeViews();
//        itemList = new ArrayList<>();
//        adapter = new MyDownloadVideoAdapter(this, itemList, this, noResultsLayout);
//        recyclerView.setAdapter(adapter);
//        setUpViewModel();
//        initializeOptionDialog();
        isNoteFragmentSelected = getIntent().getBooleanExtra(bundleIsNoteSelected, false);
        setSearchView();
        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        String[] tabTitles = new String[]{getString(R.string.lectures), getString(R.string.notes)};
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        lecturesFragment =
                (MyDownloadLecturesFragment) viewPagerAdapter.getFragment(0);

        notesFragment =
                (MyDownloadNotesFragment) viewPagerAdapter.getFragment(1);

        setSelectedTab(isNoteFragmentSelected);

    }

//    private void setUpViewModel() {
//        mediaViewModel = MyApplication.getInstance().getMediaViewModel();
//        mediaViewModel.getAllMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
//                .observe(this, this::setRecyclerViewData);
//        mediaViewModel.getAllDownloadedMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
//                        DownloadStatus.DOWNLOAD_COMPLETED.name())
//                .observe(this, list -> {
//                    if (!list.isEmpty()) {
//                        syncDownloadedData(list);
//                    }
//                });
//    }

//    public void syncDownloadedData(List<DownloadMedia> list) {
//        Log.d("Downloadissue", "syncDownloadedData call : " + (count++));
//
//        if (!CommonMethods.isOnline(this)) return;
//
//        try {
//            JSONArray jsonBody = new JSONArray(new Gson().toJson(list));
//
//            MyJsonArrayRequest request = new MyJsonArrayRequest(
//                    Request.Method.POST,
//                    AV_SYNC_DOWNLOADED_LECTURE_DATA,
//                    jsonBody,
//                    response -> {
//                        try {
//                            JSONArray responseArray = new JSONArray(response.toString());
//                            for (int i = 0; i < responseArray.length(); i++) {
//                                JSONObject item = responseArray.getJSONObject(i);
//                                String microLectureId = item.getString("microLectureId");
//                                boolean lockedFromApi = item.getBoolean("locked");
//
//                                mediaViewModel.getMediaByLectureId(microLectureId).observe(this, new Observer<MediaEntity>() {
//                                    @Override
//                                    public void onChanged(MediaEntity currentMedia) {
//                                        if (currentMedia != null) {
//                                            boolean currentLocked = currentMedia.isLocked();
//                                            if (currentLocked != lockedFromApi) {
//                                                mediaViewModel.updateByLockedStatusByLectureId(microLectureId, lockedFromApi);
//                                                Log.d(TAG, "Updated locked status for: " + microLectureId);
//                                            }
//                                        }
//                                        mediaViewModel.getMediaByLectureId(microLectureId).removeObserver(this);
//                                    }
//                                });
//                            }
//                        } catch (JSONException e) {
//                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
//                        }
//                    },
//                    error -> Log.e(TAG, "Sync error: " + error.getMessage())
//            );
//            request.setTag("SYNC_API");
//            MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request);
//        } catch (Exception e) {
//            Log.e(TAG, "Request creation error: " + e.getMessage());
//        }
//    }

    private void setSelectedTab(boolean isNoteFragmentSelected) {
        try {
            viewPager.setCurrentItem(isNoteFragmentSelected ? 1 : 0, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeViews() {
        activityTitle = findViewById(R.id.activity_title);
        back = findViewById(R.id.back_button_layout);
        noResultsLayout = findViewById(R.id.no_result_found_layout);
        noDownloadFound = findViewById(R.id.no_downloaded_videos_layout);
        ;
//        recyclerView = findViewById(R.id.my_download_recyclerview);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

//    @OptIn(markerClass = UnstableApi.class)
//    private void initializeOptionDialog() {
//        optionDialog = new Dialog(this);
//        optionDialog.setContentView(R.layout.my_download_options_dialog);
//        optionDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        optionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        optionDialog.setCancelable(true);
//
//        contentLayout = optionDialog.findViewById(R.id.content_layout);
//        progressLayout = optionDialog.findViewById(R.id.progress_layout);
//
//        CardView deleteLectureButton = optionDialog.findViewById(R.id.delete_lecture);
//        watchLectureButton = optionDialog.findViewById(R.id.watch_lecture);
//        retryDownloadButton = optionDialog.findViewById(R.id.retry_download);
//        deleteLectureButton.setOnClickListener(v -> {
//            clearSpecificCache(itemList.get(currentPos).getThumbnailUrl());
//            mediaViewModel.deleteByUrl(itemList.get(currentPos).getUrl());
//            mediaViewModel.deleteMicroLectureEntry(itemList.get(currentPos).getMicroLectureId());
//            DemoUtil.getDownloadManager(this).removeDownload(itemList.get(currentPos).getUrl());
//            itemList.remove(currentPos);
//            adapter.notifyDataSetChanged();
//            optionDialog.dismiss();
//        });
//
//        watchLectureButton.setOnClickListener(v -> {
//            MediaEntity mediaEntity = itemList.get(currentPos);
//            if(!mediaEntity.isLocked()) {
//                Intent intent = new Intent(this, OfflinePlayerActivity.class);
//                intent.putExtra("mediaEntity", mediaEntity);
//                startActivity(intent);
//                optionDialog.dismiss();
//            }else{
//                optionDialog.dismiss();
//                showLockedDialog();
//            }
//        });
//
//        retryDownloadButton.setOnClickListener(view -> {
//            contentLayout.setVisibility(View.GONE);
//            cancel.setVisibility(View.GONE);
//            progressLayout.setVisibility(View.VISIBLE);
//            getInteractiveQuestion(itemList.get(currentPos));
//        });
//
//        cancel = optionDialog.findViewById(R.id.cancel);
//        title = optionDialog.findViewById(R.id.micorlecture_name);
//
//        cancel.setOnClickListener(v -> optionDialog.dismiss());
//    }
//
//    public void clearSpecificCache(String imageUrl) {
//        Glide.with(this)
//                .load(imageUrl)
//                .signature(new ObjectKey(System.currentTimeMillis()))
//                .submit();
//    }
//
//    public void getInteractiveQuestion(MediaEntity mediaEntity) {
//        gson = new Gson();
//        final Map<String, Object> map = new HashMap<>();
//
//        map.put("microLectureId", mediaEntity.getMicroLectureId());
//        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(GET_MICROLECTURE_QANS, map), null,
//                response -> {
//                    TypeToken<List<MicroLectureQuestionsAnsDto>> token = new TypeToken<>() {
//                    };
//                    questionsAnsList = gson.fromJson(response.toString(),
//                            token.getType());
//                    if (!CollectionUtils.isEmpty(questionsAnsList))
//                        validateInteractiveAnswerAPI(0, mediaEntity);
//                    else {
//                        saveDownloadData(mediaEntity);
//                    }
//                }, new VolleyGlobalErrorListener(MyDownloadActivity.this) {
//            @Override
//            public void postError(VolleyError error) {
//                saveDownloadData(mediaEntity);
//                Log.e(TAG, "error in api getInteractiveQuestion");
//            }
//        });
//        myJsonObjectRequest.setTag(TAG);
//        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
//    }
//
//    public void validateInteractiveAnswerAPI(int pos, MediaEntity mediaEntity) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("microLectureQId", questionsAnsList.get(pos).getId());
//        map.put("userAnswer", "");
//
//        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(VALIDATE_ANSWER, map), null,
//                response -> {
//                    TypeToken<MicroLectureQuestionAnsResponseDto> token = new TypeToken<>() {
//                    };
//                    MicroLectureQuestionAnsResponseDto dto = gson.fromJson(response.toString(), token.getType());
//                    questionsAnsResponseList.add(dto);
//                    if (pos + 1 < questionsAnsList.size()) {
//                        validateInteractiveAnswerAPI(pos + 1, mediaEntity);
//                    } else {
//                        saveDownloadData(mediaEntity);
//                    }
//
//                }, new VolleyGlobalErrorListener(MyDownloadActivity.this) {
//            @Override
//            public void postError(VolleyError error) {
//                saveDownloadData(mediaEntity);
//                Log.d("InteractiveScenario", "postError " + error.getMessage());
//            }
//        });
//        myJsonObjectRequest.setTag(TAG);
//        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
//    }
//
//    public void saveDownloadData(MediaEntity mediaEntity) {
//        try {
//            OfflineDownloadEntity offlineDownloadEntity = OfflineDownloadEntity.builder()
//                    .userId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
//                    .startTime(System.currentTimeMillis())
//                    .microLectureId(mediaEntity.getMicroLectureId())
//                    .url(mediaEntity.getUrl())
//                    .duration(mediaEntity.getEndPos() - mediaEntity.getStartPos())
//                    .build();
//            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
//                    SAVE_DOWNLOAD, new JSONObject(gson.toJson(offlineDownloadEntity)),
//                    response -> {
//                        MyApplication.saveData(mediaEntity.getUrl(), offlineDownloadEntity.getStartTime());
//                        cacheImageWithCheck(mediaEntity);
//                        Log.d("saveDownloadData", "response ");
//                    }, new VolleyGlobalErrorListener(MyDownloadActivity.this) {
//                @Override
//                public void postError(VolleyError error) {
//                    cacheImageWithCheck(mediaEntity);
//                    Log.d("saveDownloadData", "postError " + error.getMessage());
//                }
//            });
//            myJsonObjectRequest.setTag(TAG);
//            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
//        } catch (JSONException e) {
//            cacheImageWithCheck(mediaEntity);
//            Log.d("saveDownloadData", "JSONException " + e.getMessage());
//        }
//    }
//
//    private void cacheImageWithCheck(MediaEntity mediaEntity) {
//        if (!mediaEntity.isThumbnailLoaded()) {
//            Glide.with(this)
//                    .load(mediaEntity.getThumbnailUrl())
//                    .listener(new RequestListener<>() {
//                        @Override
//                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            Log.e("GlideCache", "Image load failed: " + e.getMessage());
//                            mediaEntity.setThumbnailLoaded(false);
//                            completeDownload(mediaEntity);
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                            Log.d("GlideCache", "Image successfully loaded and cached");
//                            mediaEntity.setThumbnailLoaded(true);
//                            completeDownload(mediaEntity);
//                            return false;
//                        }
//                    })
//                    .submit();
//        } else {
//            completeDownload(mediaEntity);
//        }
//    }
//
//    @OptIn(markerClass = UnstableApi.class)
//    private void completeDownload(MediaEntity mediaEntity) {
//        optionDialog.dismiss();
//        mediaEntity.setQuestionsAnsList(!questionsAnsList.isEmpty() && !questionsAnsResponseList.isEmpty() ? questionsAnsList : null);
//        mediaEntity.setQuestionsAnsResponseList(!questionsAnsList.isEmpty() && !questionsAnsResponseList.isEmpty() ? questionsAnsResponseList : null);
//        mediaViewModel.insert(mediaEntity);
//        mediaViewModel.updateById(mediaEntity.getUrl(), DownloadStatus.DOWNLOAD_RESUMED.name());
//        MediaItem mediaItem = new MediaItem.Builder()
//                .setUri(Uri.parse(mediaEntity.getUrl()))
//                .setMediaId(mediaEntity.getUrl())
//                .setMediaMetadata(
//                        new MediaMetadata.Builder()
//                                .setTitle(mediaEntity.getMicroLectureTitle())
//                                .build()
//                )
//                .build();
//        DownloadTracker downloadTracker = DemoUtil.getDownloadTracker(this);
//        RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(this);
//        downloadTracker.toggleDownload(
//                mediaItem,
//                renderersFactory
//        );
//    }

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasSearchText) {
                hasSearchText = false;
                searchView.setQuery("", true);
            } else if (!hasFocus) {
                searchView.setIconified(true);
            }
        });


        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
//            if (!CollectionUtils.isEmpty(itemList)) {
//                searchView.setIconified(false);
//                searchView.setMaxWidth(Integer.MAX_VALUE);
//                activityTitle.setVisibility(View.GONE);
//            }
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.download_search_btn_clicked.getActionName());
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            activityTitle.setVisibility(View.GONE);
        });

        searchView.setOnCloseListener(() -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.download_search_close_btn_clicked.getActionName());
            activityTitle.setVisibility(View.VISIBLE);
//            queryText = "";
//            adapter.filter("");
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                queryText = query;
//                adapter.filter(query);
                setFiltersToFragments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
//                queryText = newText;
                hasSearchText = !query.isEmpty();
//                adapter.filter(newText);
                setFiltersToFragments(query);
                return true;
            }
        });
    }

    private void setFiltersToFragments(String query) {
        try {
            lecturesFragment.setFilter(query);
            notesFragment.setFilter(query);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setRecyclerViewData(List<MediaEntity> mediaList) {
//        itemList.clear();
//        itemList.addAll(mediaList);
//        Collections.reverse(itemList);
//        Log.d("setRecyclerViewData", itemList.toString());
//        adapter.notifyDataSetChanged();
//        adapter.filter(queryText);
//        if (CollectionUtils.isEmpty(itemList)) {
//            noDownloadFound.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//        } else {
//            noDownloadFound.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//        }
    }

//    @Override
//    public void showDialog(MediaEntity mediaEntity, int pos) {
//        currentPos = pos;
//        if (itemList.get(pos).getStatus().equals(DownloadStatus.DOWNLOAD_FAILED.name())) {
//            retryDownloadButton.setVisibility(View.VISIBLE);
//            watchLectureButton.setVisibility(View.GONE);
//        } else if (itemList.get(pos).getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
//            retryDownloadButton.setVisibility(View.GONE);
//            watchLectureButton.setVisibility(View.VISIBLE);
//        }
//        if (optionDialog != null) {
//            contentLayout.setVisibility(View.VISIBLE);
//            cancel.setVisibility(View.VISIBLE);
//            progressLayout.setVisibility(View.GONE);
//            optionDialog.show();
//            title.setText(mediaEntity.getMicroLectureTitle());
//        }
//    }
//
//    @OptIn(markerClass = UnstableApi.class)
//    @Override
//    public void onItemClick(MediaEntity mediaEntity, int pos, boolean locked) {
//        currentPos = pos;
//        if(!locked){
//        Intent intent = new Intent(this, OfflinePlayerActivity.class);
//        intent.putExtra("mediaEntity", mediaEntity);
//        Log.d(TAG, "onItemClick: "+mediaEntity);
//        startActivity(intent);
//        }else {
//            showLockedDialog();
//        }
//    }

    public void showLockedDialog() {
        if (lectureLockedPopUp == null) {
            lectureLockedPopUp = new Dialog(this);
            lectureLockedPopUp.setContentView(R.layout.custom_dialog_lock_box);
            lectureLockedPopUp.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            lectureLockedPopUp.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lectureLockedPopUp.setCancelable(false);
            TextView title = lectureLockedPopUp.findViewById(R.id.header);
            title.setText(R.string.plan_expire);
            MaterialCardView card = lectureLockedPopUp.findViewById(R.id.subscriptionLayout);
            card.setOnClickListener(v -> {
//                Intent intent;
//                intent = new Intent(this, CampaignSubscriptionActivity.class);
////                intent.putExtra("plan_level", "COURSE");
//                startActivity(intent);
                lectureLockedPopUp.dismiss();
                startActivity(SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.OFFLINE_DOWNLOADS_LOCKED_POPUP));
            });

            ImageView cut = lectureLockedPopUp.findViewById(R.id.cancel);
            cut.setOnClickListener(v -> lectureLockedPopUp.dismiss());
            lectureLockedPopUp.setOnDismissListener(dialog -> lectureLockedPopUp = null);
        }
        if (!lectureLockedPopUp.isShowing()) {
            lectureLockedPopUp.show();
        }
    }

//    @Override
//    public void openBottomSheet(MediaEntity mediaEntity, int pos) {
//        currentPos=pos;
//        if (cancelDownloadBottomSheet != null) {
//            cancelDownloadBottomSheet = null;
//        }
//
//        cancelDownloadBottomSheet = new CancelDownloadBottomSheet(this, this,
//                mediaEntity.getMicroLectureTitle(), mediaEntity.getProgress(),mediaEntity.getMicroLectureId());
//        cancelDownloadBottomSheet.setCancelable(true);
//        cancelDownloadBottomSheet.show(getSupportFragmentManager(), "");
//        observe(mediaEntity.getMicroLectureId());
//    }

//    private void observe(String microLectureId) {
//        if(cancelDownloadLiveData!=null)
//            cancelDownloadLiveData.removeObservers(this);
//        cancelDownloadLiveData = MyApplication.getInstance().getMediaViewModel().getMediaByLectureId(microLectureId);
//        cancelDownloadLiveData.observe(this, mediaEntity -> {
//            if(mediaEntity!=null) {
//                 if (mediaEntity.getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
//                    if (cancelDownloadBottomSheet != null) {
//                        cancelDownloadBottomSheet.dismiss();
//                        cancelDownloadBottomSheet = null;
//                    }
//                }else if (cancelDownloadBottomSheet != null && cancelDownloadBottomSheet.isVisible()) {
//                    cancelDownloadBottomSheet.updateProgress(mediaEntity.getProgress());
//                }
//            }
//        });
//    }

//    @OptIn(markerClass = UnstableApi.class)
//    @Override
//    public void onCancelClick(String microLectrueId) {
//            clearSpecificCache(itemList.get(currentPos).getThumbnailUrl());
//            mediaViewModel.deleteByUrl(itemList.get(currentPos).getUrl());
//            mediaViewModel.deleteMicroLectureEntry(itemList.get(currentPos).getMicroLectureId());
//            DemoUtil.getDownloadManager(this).removeDownload(itemList.get(currentPos).getUrl());
//            itemList.remove(currentPos);
//            adapter.notifyDataSetChanged();
//        if (cancelDownloadBottomSheet != null) {
//            cancelDownloadBottomSheet.dismiss();
//            cancelDownloadBottomSheet = null;
//        }
//
//    }


//    @Override
//    public void onCloseClick() {
//        if (cancelDownloadBottomSheet != null) {
//            cancelDownloadBottomSheet.dismiss();
//            cancelDownloadBottomSheet = null;
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getApplicationContext() != null) {
            MyVolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue().cancelAll("SYNC_API");
        }
        if (optionDialog != null && optionDialog.isShowing()) {
            optionDialog.dismiss();
        }
        if (lectureLockedPopUp != null && optionDialog.isShowing()) {
            optionDialog.dismiss();
        }
        lectureLockedPopUp = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (cancelDownloadBottomSheet != null) {
//            cancelDownloadBottomSheet.dismiss();
//            cancelDownloadBottomSheet=null;
//        }
    }




    public static class ViewPagerAdapter extends FragmentStateAdapter {

        private final Fragment[] fragments;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments = new Fragment[]{
                    new MyDownloadLecturesFragment(),
                    new MyDownloadNotesFragment()
            };
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
//            if (position == 1) {
//                return new MyDownloadNotesFragment();
//            }
//            return new MyDownloadLecturesFragment();
            return fragments[position];
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }

        public Fragment getFragment(int position) {
            return fragments[position];
        }
    }
}
