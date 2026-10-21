package arivihan.technologies.doubtbuzzter2.fragments.offline;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.adapter.offline.MyDownloadNoteAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.CancelDownloadBottomSheet;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineDownloadManager;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.manager.DemoUtil;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.model.offline.type.OfflineNoteConfigModel;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MyDownloadSubjectNoteFragment extends Fragment implements MyDownloadNoteAdapter.OnNoteClickListener, CancelDownloadBottomSheet.CancelDownloadBottomSheetListener {

    private static final String TAG = MyDownloadSubjectNoteFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private MyDownloadNoteAdapter adapter;
    private LinearLayout noResultsLayout, noDownloadFound;
    private List<NoteEntity> itemList = new ArrayList<>();
    private TextView title;
    private Dialog optionDialog, lectureLockedPopUp;
    private int currentPos;
    private NotesViewModel notesViewModel;
    private CardView watchLectureButton;
    private CardView retryDownloadButton;
    private LinearLayout progressLayout;
    private LinearLayout contentLayout;
    private ImageView cancel;
    private CancelDownloadBottomSheet cancelDownloadBottomSheet;
    private Gson gson;
    private LiveData<NoteEntity> cancelDownloadLiveData;
    private int count = 0;
    private String queryText = "";
    private String subjectName;

    public MyDownloadSubjectNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("subjectName", subjectName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                subjectName = getArguments().getString("subjectName");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static MyDownloadSubjectNoteFragment newInstance(String subjectName) {
        MyDownloadSubjectNoteFragment fragment = new MyDownloadSubjectNoteFragment();
        Bundle args = new Bundle();
        args.putSerializable("subjectName", subjectName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_download_subject_note, container, false);
        noResultsLayout = view.findViewById(R.id.no_result_found_layout);
        noDownloadFound = view.findViewById(R.id.no_downloaded_videos_layout);
        recyclerView = view.findViewById(R.id.my_download_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        OfflineNoteConfigModel offlineNoteConfigModel = MyApplication.getOfflineNoteConfig(OfflineDownloadManager.PREF_OFFLINE_DOWNLOAD_TAG);
        itemList = new ArrayList<>();
        adapter = new MyDownloadNoteAdapter(requireContext(), itemList, offlineNoteConfigModel, this, noResultsLayout);
        recyclerView.setAdapter(adapter);
        setUpViewModel();
        initializeOptionDialog();

        Log.d(TAG, "OfflineNote Config: " + ((offlineNoteConfigModel != null) ? offlineNoteConfigModel.toString() : ""));
        return view;
    }

    private void setUpViewModel() {
        notesViewModel = MyApplication.getInstance().getNotesViewModel();
//        setRecyclerViewData();
        notesViewModel.getAllNotesBySubjectName(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()), subjectName)
                .observe(getViewLifecycleOwner(), this::setRecyclerViewData);
//        notesViewModel.getAllDownloadedNote(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
//                        DownloadStatus.DOWNLOAD_COMPLETED.name())
//                .observe(getViewLifecycleOwner(), list -> {
//                    if (!list.isEmpty()) {
//                        syncDownloadedData(list);
//                    }
//                });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeOptionDialog() {
        optionDialog = new Dialog(requireContext());
        optionDialog.setContentView(R.layout.dialog_download_note_options);
        optionDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        optionDialog.setCancelable(true);

        contentLayout = optionDialog.findViewById(R.id.content_layout);
        progressLayout = optionDialog.findViewById(R.id.progress_layout);

        CardView deleteLectureButton = optionDialog.findViewById(R.id.delete_lecture);
        watchLectureButton = optionDialog.findViewById(R.id.watch_lecture);
        retryDownloadButton = optionDialog.findViewById(R.id.retry_download);
        deleteLectureButton.setOnClickListener(v -> {
            notesViewModel.deleteByLocalPath(itemList.get(currentPos).getLocalPath());
            notesViewModel.deleteById(itemList.get(currentPos).getId());
            DemoUtil.getDownloadManager(requireContext()).removeDownload(itemList.get(currentPos).getId());
            itemList.remove(currentPos);
            adapter.notifyDataSetChanged();
            optionDialog.dismiss();
        });

        watchLectureButton.setOnClickListener(v -> {
            NoteEntity noteEntity = itemList.get(currentPos);
            if (!noteEntity.isLocked()) {
                openOfflinePdf(noteEntity);
                optionDialog.dismiss();
            } else {
                optionDialog.dismiss();
                showLockedDialog();
            }
        });

        retryDownloadButton.setOnClickListener(view -> {
            contentLayout.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
//            progressLayout.setVisibility(View.VISIBLE);
//            getInteractiveQuestion(itemList.get(currentPos));
            if (CommonMethods.isOnline(requireContext())) {
                PdfUtils.startPdfDownload(requireContext(), itemList.get(currentPos));
            } else {
                CommonMethods.showNegativeSnackbar(requireView(), getString(R.string.NoInternetConnection), 2000);
            }
        });

        cancel = optionDialog.findViewById(R.id.cancel);
        title = optionDialog.findViewById(R.id.micorlecture_name);

        cancel.setOnClickListener(v -> optionDialog.dismiss());
    }

    private void setRecyclerViewData(List<NoteEntity> noteList) {
        Log.d(TAG, "Note list: " + ((noteList != null) ? noteList.toString() : ""));
        itemList.clear();
        itemList.addAll(noteList);
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

//    public void syncDownloadedData(List<NoteDto> list) {
//        Log.d("Downloadissue", "syncDownloadedData call : " + (count++));
//
//        if (!CommonMethods.isOnline(requireContext())) return;
//
//        try {
//            JSONArray jsonBody = new JSONArray(new Gson().toJson(list));
//
//            MyJsonArrayRequest request = new MyJsonArrayRequest(
//                    Request.Method.POST,
//                    SYNC_DOWNLOADED_NOTE_DATA,
//                    jsonBody,
//                    response -> {
//                        try {
//                            JSONArray responseArray = new JSONArray(response.toString());
//                            for (int i = 0; i < responseArray.length(); i++) {
//                                JSONObject item = responseArray.getJSONObject(i);
//                                String id = item.getString("id");
//                                boolean lockedFromApi = item.getBoolean("locked");
//
//                                notesViewModel.getLiveNotesById(id).observe(getViewLifecycleOwner(), new Observer<NoteEntity>() {
//                                    @Override
//                                    public void onChanged(NoteEntity entity) {
//                                        if (entity != null) {
//                                            boolean currentLocked = entity.isLocked();
//                                            if (currentLocked != lockedFromApi) {
//                                                notesViewModel.updateLockStatus(id, lockedFromApi);
//                                                Log.d(TAG, "Updated locked status for: " + id);
//                                            }
//                                        }
//                                        notesViewModel.getLiveNotesById(id).removeObserver(this);
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
//            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
//        } catch (Exception e) {
//            Log.e(TAG, "Request creation error: " + e.getMessage());
//        }
//    }

    public void showLockedDialog() {

        try {
//            Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.OFFLINE_DOWNLOADS_LOCKED_POPUP);

            SubscriptionLockPopupDto subscriptionLockPopupDto =
                    CampaignSubscriptionActivity.getSubscriptionLockPopupDto("offline_notes_"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

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
//            title.setText(R.string.apka_plan_expire_ho_gaya_hai_note_unload_karein);
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

    private void observe(String id) {
        if (cancelDownloadLiveData != null)
            cancelDownloadLiveData.removeObservers(this);
        cancelDownloadLiveData = MyApplication.getInstance().getNotesViewModel().getLiveNotesById(id);
        cancelDownloadLiveData.observe(this, noteEntity -> {
            if (noteEntity != null) {
                if (noteEntity.getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                    if (cancelDownloadBottomSheet != null) {
                        cancelDownloadBottomSheet.dismiss();
                        cancelDownloadBottomSheet = null;
                    }
                } else if (cancelDownloadBottomSheet != null && cancelDownloadBottomSheet.isVisible()) {
                    cancelDownloadBottomSheet.updateProgress(noteEntity.getProgress());
                }
            }
        });
    }

    public List<NoteEntity> getNoteList() {
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

    private void openOfflinePdf(NoteEntity noteEntity) {
        try {
            notesViewModel.selectNote(noteEntity.getId());
            PdfUtils.openPdfViewer(requireContext(), noteEntity.getLocalPath(), noteEntity.getChapterTitle(),noteEntity.getNoteType(),noteEntity.getSubjectName(),true, noteEntity.getNotesUrl(), "");

            Map<String, String> params = new HashMap<>();
            params.put("page", TAG);
            params.put("id", noteEntity.getId());
            params.put("subjectName", noteEntity.getSubjectName());
            params.put("subjectId", noteEntity.getSubjectId());
            params.put("chapterTitle", noteEntity.getChapterTitle());
            params.put("noteType", noteEntity.getNoteType());
            params.put("isLocked", "" + noteEntity.isLocked());
            params.put("pageCount", "" + noteEntity.getPageCount());
            params.put("size", "" + noteEntity.getFileSize());
            params.put("notesUrl", noteEntity.getNotesUrl());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.note_offline_click.getActionName(), params);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showDialog(NoteEntity noteEntity, int pos) {
        currentPos = pos;
        if (itemList.get(pos).getStatus().equals(NoteEntity.STATUS_FAILED)) {
            retryDownloadButton.setVisibility(View.VISIBLE);
            watchLectureButton.setVisibility(View.GONE);
        } else if (itemList.get(pos).getStatus().equals(NoteEntity.STATUS_COMPLETED)) {
            retryDownloadButton.setVisibility(View.GONE);
            watchLectureButton.setVisibility(View.VISIBLE);
        }
        if (optionDialog != null) {
            contentLayout.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            optionDialog.show();
            title.setText(noteEntity.getChapterTitle());
        }
    }

    @Override
    public void onItemClick(NoteEntity noteEntity, int pos, boolean locked) {
        currentPos = pos;
        if (!locked) {
            openOfflinePdf(noteEntity);
        } else {
            showLockedDialog();
        }
    }

    @Override
    public void openBottomSheet(NoteEntity noteEntity, int pos) {
        currentPos = pos;
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet = null;
        }

        cancelDownloadBottomSheet = new CancelDownloadBottomSheet(requireContext(), this,
                noteEntity.getChapterTitle(), noteEntity.getProgress(), noteEntity.getId());
        cancelDownloadBottomSheet.setCancelable(true);
        cancelDownloadBottomSheet.show(getParentFragmentManager(), "");
        observe(noteEntity.getId());
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCancelClick(String microLectrueId) {
        int pos = -1;
        for (int i = 0; i < itemList.size(); i++) {
            if (microLectrueId.equals(itemList.get(i).getId())) {
                pos = i;
                break;
            }
        }
        if (pos == -1) return;
        notesViewModel.deleteByLocalPath(itemList.get(pos).getLocalPath());
        notesViewModel.deleteById(itemList.get(pos).getId());
        DemoUtil.getDownloadManager(requireContext()).removeDownload(itemList.get(pos).getId());
        itemList.remove(pos);
        adapter.notifyDataSetChanged();
        if (cancelDownloadBottomSheet != null) {
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
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet.dismissAllowingStateLoss();
            cancelDownloadBottomSheet = null;
        }
    }

}