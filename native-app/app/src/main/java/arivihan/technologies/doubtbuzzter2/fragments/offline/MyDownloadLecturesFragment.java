package arivihan.technologies.doubtbuzzter2.fragments.offline;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_QANS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_DOWNLOAD;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_LECTURE_DATA;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VALIDATE_ANSWER;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.OfflinePlayerActivity;
import arivihan.technologies.doubtbuzzter2.adapter.offline.MyDownloadVideoAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.CancelDownloadBottomSheet;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.manager.DemoUtil;
import arivihan.technologies.doubtbuzzter2.manager.DownloadTracker;
import arivihan.technologies.doubtbuzzter2.database.helper.MediaMapper;
import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.DownloadedLectureResponse;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.SyncDownloadRequest;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.OfflineDownloadEntity;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.viewModel.MediaViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MyDownloadLecturesFragment extends Fragment implements MyDownloadVideoAdapter.DialogCallback,
        CancelDownloadBottomSheet.CancelDownloadBottomSheetListener {

    private static final String TAG = MyDownloadLecturesFragment.class.getSimpleName();
    private static final long MIN_CLICK_INTERVAL = 600; // milliseconds
    private long lastClickTime = 0;
    private RecyclerView recyclerView;
    private MyDownloadVideoAdapter adapter;
    private LinearLayout noResultsLayout, noDownloadFound;
    private List<MediaEntity> itemList;
    private TextView title;
    private Dialog optionDialog, lectureLockedPopUp;
    private int currentPos;
    private MediaViewModel mediaViewModel;
    private CardView watchLectureButton;
    private CardView retryDownloadButton;
    private LinearLayout progressLayout;
    private LinearLayout contentLayout;
    private ImageView cancel;
    private CancelDownloadBottomSheet cancelDownloadBottomSheet;
    private List<MicroLectureQuestionsAnsDto> questionsAnsList = new ArrayList<>();
    private List<MicroLectureQuestionAnsResponseDto> questionsAnsResponseList = new ArrayList<>();
    private Gson gson;
    private LiveData<MediaEntity> cancelDownloadLiveData;
    private int count = 0;
    private String queryText = "";

    public MyDownloadLecturesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_download_lectures, container, false);
        noResultsLayout = view.findViewById(R.id.no_result_found_layout);
        noDownloadFound = view.findViewById(R.id.no_downloaded_videos_layout);
        ;
        recyclerView = view.findViewById(R.id.my_download_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        itemList = new ArrayList<>();
        adapter = new MyDownloadVideoAdapter(requireContext(), itemList, this, noResultsLayout);
        recyclerView.setAdapter(adapter);
        setUpViewModel();
        initializeOptionDialog();

        return view;
    }

    private void setUpViewModel() {
        mediaViewModel = MyApplication.getInstance().getMediaViewModel();
        mediaViewModel.getAllMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .observe(getViewLifecycleOwner(), this::setRecyclerViewData);
        mediaViewModel.getAllDownloadedMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
                        DownloadStatus.DOWNLOAD_COMPLETED.name())
                .observe(getViewLifecycleOwner(), list -> {
                    if (!list.isEmpty()) {
                        syncDownloadedData(list);
                    }
                });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void deleteItem(String microlectureId) {
        try {
            int position = -1;
            for(int i=0;i<itemList.size();i++) {
                MediaEntity mediaEntity = itemList.get(i);
                if(mediaEntity.getMicroLectureId().equals(microlectureId)) {
                    position = i;
                }
            }

            if(position!=-1) {
                clearSpecificCache(itemList.get(position).getThumbnailUrl());
                mediaViewModel.deleteByUrl(itemList.get(position).getUrl());
                mediaViewModel.deleteMicroLectureEntry(itemList.get(position).getMicroLectureId());
                DemoUtil.getDownloadManager(requireContext()).removeDownload(itemList.get(position).getUrl());
                itemList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeOptionDialog() {
        optionDialog = new Dialog(requireContext());
        optionDialog.setContentView(R.layout.my_download_options_dialog);
        optionDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        optionDialog.setCancelable(true);

        contentLayout = optionDialog.findViewById(R.id.content_layout);
        progressLayout = optionDialog.findViewById(R.id.progress_layout);

        CardView deleteLectureButton = optionDialog.findViewById(R.id.delete_lecture);
        watchLectureButton = optionDialog.findViewById(R.id.watch_lecture);
        retryDownloadButton = optionDialog.findViewById(R.id.retry_download);
        deleteLectureButton.setOnClickListener(v -> {
            clearSpecificCache(itemList.get(currentPos).getThumbnailUrl());
            mediaViewModel.deleteByUrl(itemList.get(currentPos).getUrl());
            mediaViewModel.deleteMicroLectureEntry(itemList.get(currentPos).getMicroLectureId());
            DemoUtil.getDownloadManager(requireContext()).removeDownload(itemList.get(currentPos).getUrl());
            itemList.remove(currentPos);
            adapter.notifyDataSetChanged();
            optionDialog.dismiss();
        });

        watchLectureButton.setOnClickListener(v -> {
            MediaEntity mediaEntity = itemList.get(currentPos);
            if (!mediaEntity.isLocked()) {
                openOfflineLecture(mediaEntity);
                optionDialog.dismiss();
            } else {
                optionDialog.dismiss();
                showLockedDialog();
            }
        });

        retryDownloadButton.setOnClickListener(view -> {
            contentLayout.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            getInteractiveQuestion(itemList.get(currentPos));
        });

        cancel = optionDialog.findViewById(R.id.cancel);
        title = optionDialog.findViewById(R.id.micorlecture_name);

        cancel.setOnClickListener(v -> optionDialog.dismiss());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openOfflineLecture(MediaEntity mediaEntity) {
        try {

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
                lastClickTime = currentTime;

                Intent intent = new Intent(requireContext(), OfflinePlayerActivity.class);
                intent.putExtra("mediaEntity", mediaEntity);
                startActivity(intent);

                Map<String, String> params = new HashMap<>();
                params.put("page", TAG);
                params.put("id", mediaEntity.getId());
                params.put("url", mediaEntity.getUrl());
                params.put("title", mediaEntity.getTitle());
                params.put("chapterTitle", mediaEntity.getChapterTitle());
                params.put("chapterId", mediaEntity.getChapterId());
                params.put("microLectureTitle", mediaEntity.getMicroLectureTitle());
                params.put("microLectureId", mediaEntity.getMicroLectureId());
                params.put("duration", mediaEntity.getDuration());
                params.put("size", mediaEntity.getSize());
                params.put("progress", String.valueOf(mediaEntity.getProgress()));
                params.put("language", mediaEntity.getLanguage());
                params.put("status", mediaEntity.getStatus());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.lecture_offline_click.getActionName(), params);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearSpecificCache(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .submit();
    }

    public void getInteractiveQuestion(MediaEntity mediaEntity) {
        gson = new Gson();
        final Map<String, Object> map = new HashMap<>();

        map.put("microLectureId", mediaEntity.getMicroLectureId());
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_MICROLECTURE_QANS, map), null,
                response -> {
                    TypeToken<List<MicroLectureQuestionsAnsDto>> token = new TypeToken<>() {
                    };
                    questionsAnsList = gson.fromJson(response.toString(),
                            token.getType());
                    if (!CollectionUtils.isEmpty(questionsAnsList))
                        validateInteractiveAnswerAPI(0, mediaEntity);
                    else {
                        saveDownloadData(mediaEntity);
                    }
                }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                saveDownloadData(mediaEntity);
                Log.e(TAG, "error in api getInteractiveQuestion");
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    public void validateInteractiveAnswerAPI(int pos, MediaEntity mediaEntity) {
        Map<String, Object> map = new HashMap<>();
        map.put("microLectureQId", questionsAnsList.get(pos).getId());
        map.put("userAnswer", "");

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(VALIDATE_ANSWER, map), null,
                response -> {
                    TypeToken<MicroLectureQuestionAnsResponseDto> token = new TypeToken<>() {
                    };
                    MicroLectureQuestionAnsResponseDto dto = gson.fromJson(response.toString(), token.getType());
                    questionsAnsResponseList.add(dto);
                    if (pos + 1 < questionsAnsList.size()) {
                        validateInteractiveAnswerAPI(pos + 1, mediaEntity);
                    } else {
                        saveDownloadData(mediaEntity);
                    }

                }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                saveDownloadData(mediaEntity);
                Log.d("InteractiveScenario", "postError " + error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    public void saveDownloadData(MediaEntity mediaEntity) {
        try {
            OfflineDownloadEntity offlineDownloadEntity = OfflineDownloadEntity.builder()
                    .userId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                    .startTime(System.currentTimeMillis())
                    .microLectureId(mediaEntity.getMicroLectureId())
                    .url(mediaEntity.getUrl())
                    .duration(mediaEntity.getEndPos() - mediaEntity.getStartPos())
                    .build();
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    SAVE_DOWNLOAD, new JSONObject(gson.toJson(offlineDownloadEntity)),
                    response -> {
                        MyApplication.saveData(mediaEntity.getUrl(), offlineDownloadEntity.getStartTime());
                        cacheImageWithCheck(mediaEntity);
                        Log.d("saveDownloadData", "response ");
                    }, new VolleyGlobalErrorListener(getContext()) {
                @Override
                public void postError(VolleyError error) {
                    cacheImageWithCheck(mediaEntity);
                    Log.d("saveDownloadData", "postError " + error.getMessage());
                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            cacheImageWithCheck(mediaEntity);
            Log.d("saveDownloadData", "JSONException " + e.getMessage());
        }
    }

    private void cacheImageWithCheck(MediaEntity mediaEntity) {
        if (!mediaEntity.isThumbnailLoaded()) {
            Glide.with(this)
                    .load(mediaEntity.getThumbnailUrl())
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideCache", "Image load failed: " + e.getMessage());
                            mediaEntity.setThumbnailLoaded(false);
                            completeDownload(mediaEntity);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("GlideCache", "Image successfully loaded and cached");
                            mediaEntity.setThumbnailLoaded(true);
                            completeDownload(mediaEntity);
                            return false;
                        }
                    })
                    .submit();
        } else {
            completeDownload(mediaEntity);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void completeDownload(MediaEntity mediaEntity) {
        optionDialog.dismiss();
        mediaEntity.setQuestionsAnsList(!questionsAnsList.isEmpty() && !questionsAnsResponseList.isEmpty() ? questionsAnsList : null);
        mediaEntity.setQuestionsAnsResponseList(!questionsAnsList.isEmpty() && !questionsAnsResponseList.isEmpty() ? questionsAnsResponseList : null);
        mediaViewModel.insert(mediaEntity);
        mediaViewModel.updateById(mediaEntity.getUrl(), DownloadStatus.DOWNLOAD_RESUMED.name());
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(mediaEntity.getUrl()))
                .setMediaId(mediaEntity.getUrl())
                .setMediaMetadata(
                        new MediaMetadata.Builder()
                                .setTitle(mediaEntity.getMicroLectureTitle())
                                .build()
                )
                .build();
        DownloadTracker downloadTracker = DemoUtil.getDownloadTracker(requireContext());
        RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(requireContext());
        downloadTracker.toggleDownload(
                mediaItem,
                renderersFactory
        );
    }

    public void syncDownloadedData(List<DownloadMedia> list) {
        Log.d(TAG, "syncDownloadedData: call #" + (count++) + ", count=" + list.size());

        if (!CommonMethods.isOnline(requireContext())) return;

        try {
            List<SyncDownloadRequest> requestBody = MediaMapper.fromDownloadMediaList(list);
            JSONArray jsonBody = new JSONArray(new Gson().toJson(requestBody));

            Log.d(TAG, "syncDownloadedData: REQUEST → " + AV_SYNC_DOWNLOADED_LECTURE_DATA + ", body=" + jsonBody);

            MyJsonArrayRequest request = new MyJsonArrayRequest(
                    Request.Method.POST,
                    AV_SYNC_DOWNLOADED_LECTURE_DATA,
                    jsonBody,
                    response -> {
                        Log.d(TAG, "syncDownloadedData: RESPONSE → " + response.toString());
                        TypeToken<ApiWrappedResponse<List<DownloadedLectureResponse>>> token =
                                new TypeToken<ApiWrappedResponse<List<DownloadedLectureResponse>>>() {};
                        ApiWrappedResponse<List<DownloadedLectureResponse>> wrapped =
                                new Gson().fromJson(response.toString(), token.getType());

                        if (wrapped == null || !wrapped.isSuccess() || wrapped.getData() == null) {
                            Log.w(TAG, "syncDownloadedData: invalid or unsuccessful response");
                            return;
                        }

                        for (DownloadedLectureResponse item : wrapped.getData()) {
                            String microLectureId = item.getMicroLectureId();
                            boolean lockedFromApi = item.isLocked();

                            if (!item.isDeleted()) {
                                mediaViewModel.getMediaByLectureId(microLectureId).observe(getViewLifecycleOwner(), new Observer<MediaEntity>() {
                                    @Override
                                    public void onChanged(MediaEntity currentMedia) {
                                        if (currentMedia != null && currentMedia.isLocked() != lockedFromApi) {
                                            mediaViewModel.updateByLockedStatusByLectureId(microLectureId, lockedFromApi);
                                            Log.d(TAG, "syncDownloadedData: updated lock for=" + microLectureId + " → " + lockedFromApi);
                                        }
                                        mediaViewModel.getMediaByLectureId(microLectureId).removeObserver(this);
                                    }
                                });
                            } else {
                                deleteItem(microLectureId);
                            }
                        }
                    },
                    error -> Log.e(TAG, "syncDownloadedData: ERROR → " + error.getMessage(), error)
            );
            request.setTag("SYNC_API");
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "syncDownloadedData: request creation error: " + e.getMessage());
        }
    }

    private void setRecyclerViewData(List<MediaEntity> mediaList) {
        itemList.clear();
        itemList.addAll(mediaList);
        Collections.reverse(itemList);
        Log.d("setRecyclerViewData", itemList.toString());
        adapter.notifyDataSetChanged();
        adapter.filter(queryText);
        if (CollectionUtils.isEmpty(itemList)) {
            noDownloadFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noDownloadFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public List<MediaEntity> getLectureList() {
        return itemList;
    }

    public void setFilter(String filter) {
        try {
            queryText = filter;
            adapter.filter(filter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showDialog(MediaEntity mediaEntity, int pos) {
        currentPos = pos;
        if (itemList.get(pos).getStatus().equals(DownloadStatus.DOWNLOAD_FAILED.name())) {
            retryDownloadButton.setVisibility(View.VISIBLE);
            watchLectureButton.setVisibility(View.GONE);
        } else if (itemList.get(pos).getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
            retryDownloadButton.setVisibility(View.GONE);
            watchLectureButton.setVisibility(View.VISIBLE);
        }
        if (optionDialog != null) {
            contentLayout.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            optionDialog.show();
            title.setText(mediaEntity.getMicroLectureTitle());
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onItemClick(MediaEntity mediaEntity, int pos, boolean locked) {
        currentPos = pos;
        if (!locked) {
            openOfflineLecture(mediaEntity);
        } else {
            showLockedDialog();
        }
    }

    public void showLockedDialog() {
        try {
//            Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.OFFLINE_DOWNLOADS_LOCKED_POPUP);

            SubscriptionLockPopupDto subscriptionLockPopupDto =
                    CampaignSubscriptionActivity.getSubscriptionLockPopupDto("offline_lecture_"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            if (subscriptionLockPopupDto != null) {
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        if (lectureLockedPopUp == null) {
//            lectureLockedPopUp = new Dialog(requireContext());
//            lectureLockedPopUp.setContentView(R.layout.custom_dialog_lock_box);
//            lectureLockedPopUp.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.bachground_custom_dialogu_typeactivity));
//            lectureLockedPopUp.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            lectureLockedPopUp.setCancelable(false);
//            TextView title = lectureLockedPopUp.findViewById(R.id.header);
//            title.setText(R.string.plan_expire);
//            MaterialCardView card = lectureLockedPopUp.findViewById(R.id.subscriptionLayout);
//            card.setOnClickListener(v -> {
//                Intent intent;
//                intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
////                intent.putExtra("plan_level", "COURSE");
//                startActivity(intent);
//                lectureLockedPopUp.dismiss();
//            });
//
//            ImageView cut = lectureLockedPopUp.findViewById(R.id.cancel);
//            cut.setOnClickListener(v -> lectureLockedPopUp.dismiss());
//            lectureLockedPopUp.setOnDismissListener(dialog -> lectureLockedPopUp = null);
//        }
//        if (!lectureLockedPopUp.isShowing()) {
//            lectureLockedPopUp.show();
//        }

    }

    @Override
    public void openBottomSheet(MediaEntity mediaEntity, int pos) {
        currentPos = pos;
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet = null;
        }

        cancelDownloadBottomSheet = new CancelDownloadBottomSheet(requireContext(), this,
                mediaEntity.getMicroLectureTitle(), mediaEntity.getProgress(), mediaEntity.getMicroLectureId());
        cancelDownloadBottomSheet.setCancelable(true);
        cancelDownloadBottomSheet.show(getParentFragmentManager(), "");
        observe(mediaEntity.getMicroLectureId());
    }

    private void observe(String microLectureId) {
        if (cancelDownloadLiveData != null)
            cancelDownloadLiveData.removeObservers(this);
        cancelDownloadLiveData = MyApplication.getInstance().getMediaViewModel().getMediaByLectureId(microLectureId);
        cancelDownloadLiveData.observe(this, mediaEntity -> {
            if (mediaEntity != null) {
                if (mediaEntity.getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                    if (cancelDownloadBottomSheet != null) {
                        cancelDownloadBottomSheet.dismiss();
                        cancelDownloadBottomSheet = null;
                    }
                } else if (cancelDownloadBottomSheet != null && cancelDownloadBottomSheet.isVisible()) {
                    cancelDownloadBottomSheet.updateProgress(mediaEntity.getProgress());
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCancelClick(String microLectrueId) {
        if (CollectionUtils.isEmpty(itemList)) {
            Log.w("CancelClick", "Item list is empty, nothing to cancel");
            return;
        }
        if (currentPos < 0 || currentPos >= itemList.size()) {
            Log.e("CancelClick", "Invalid position: " + currentPos);
            return;
        }
        clearSpecificCache(itemList.get(currentPos).getThumbnailUrl());
        mediaViewModel.deleteByUrl(itemList.get(currentPos).getUrl());
        mediaViewModel.deleteMicroLectureEntry(itemList.get(currentPos).getMicroLectureId());
        DemoUtil.getDownloadManager(requireContext()).removeDownload(itemList.get(currentPos).getUrl());
        itemList.remove(currentPos);
        adapter.notifyDataSetChanged();
        if (cancelDownloadBottomSheet != null
                && cancelDownloadBottomSheet.isAdded()) {
            cancelDownloadBottomSheet.dismiss();
            cancelDownloadBottomSheet = null;
        }

    }

    @Override
    public void onCloseClick() {
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet.dismiss();
            cancelDownloadBottomSheet = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext();
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll("SYNC_API");
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        if (optionDialog != null && optionDialog.isShowing()) {
            optionDialog.dismiss();
        }
        if (lectureLockedPopUp != null && optionDialog.isShowing()) {
            optionDialog.dismiss();
        }
        lectureLockedPopUp = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cancelDownloadBottomSheet != null
                && cancelDownloadBottomSheet.isAdded()) {
            cancelDownloadBottomSheet.dismissAllowingStateLoss();
            cancelDownloadBottomSheet = null;
        }
    }


//    @Override
//    private void onPause() {
//        super.onPause();
//        if (cancelDownloadBottomSheet != null) {
//            cancelDownloadBottomSheet.dismiss();
//            cancelDownloadBottomSheet=null;
//        }
//    }

}