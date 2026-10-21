package arivihan.technologies.doubtbuzzter2.fragments.sample_paper;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersNotesAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ViewSamplePaperAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SamplePapersFragment extends Fragment implements ToppersNotesAdapter.OnNotesClickListener {

    private static final String TAG = SamplePapersFragment.class.getSimpleName();

    private ArrayList<SamplePaperModel> list;

    private RecyclerView recyclerView;
    private ViewSamplePaperAdapter adapter;
    private LinearLayout noResultFoundLayout;
    private Dialog lockDialog;
    MaterialCardView subscriptionLayout;
    PlayStoreDialog playStoreDialog;
    private Map<String, Object> activityParamsMap;
    private TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    LottieAnimationView lockAnimation;
    NotesViewModel notesViewModel;

    public SamplePapersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", list);
    }

    public static SamplePapersFragment newInstance(ArrayList<SamplePaperModel> list) {
        SamplePapersFragment fragment = new SamplePapersFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                list = getArguments().getParcelableArrayList("list");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sample_papers, container, false);
        recyclerView = view.findViewById(R.id.toppers_notes_rv);
        noResultFoundLayout = view.findViewById(R.id.no_result_found_layout);
        Log.d(TAG, "Note List: " + ((list != null) ? list.toString() : ""));
        if (CollectionUtils.isEmpty(list)) {
            noResultFoundLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noResultFoundLayout.setVisibility(View.GONE);
            adapter = new ViewSamplePaperAdapter(list, this, requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }
        notesViewModel = MyApplication.getInstance().getNotesViewModel();

        initializeLockDialog();
        MyApplication.getSubscriptionStatus(requireActivity(), new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                updateDialogContent(dto);
            }

            @Override
            public void onError(String errorMessage) {
                updateDialogContent(null);
            }
        });

        return view;
    }

    private void initializeLockDialog() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        lockDialog = new Dialog(requireContext());
        lockDialog.setContentView(R.layout.custom_dialog_lock_box);
        lockDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lockDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        lockDialog.setCancelable(false);

        subscriptionLayout = lockDialog.findViewById(R.id.subscriptionLayout);
        lockDialogText = lockDialog.findViewById(R.id.header);
        lockDialogBtnText = lockDialog.findViewById(R.id.subs);
        lockAnimation = lockDialog.findViewById(R.id.dialog_animation);
        lockDialogTitle = lockDialog.findViewById(R.id.dialog_title);

        subscriptionLayout.setOnClickListener(view -> {
            lockDialog.dismiss();

            Intent intent = getTargetIntent(null);
            if (activityParamsMap != null) {
                activityParamsMap.forEach((k, v) -> {
                    if (v instanceof Serializable) {
                        intent.putExtra(k, (Serializable) v);
                    }
                });
            }
            startActivity(intent);
        });

        lockDialog.findViewById(R.id.cancel).setOnClickListener(v -> lockDialog.dismiss());
    }

    public void updateAdapterList() {
        try {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateDialogContent(SubscriptionCheckDto dto) {

        if (dto != null) {
            if (dto.getDialogText() != null) lockDialogText.setText(dto.getDialogText());
            if (dto.getButtonText() != null) lockDialogBtnText.setText(dto.getButtonText());
            activityParamsMap = dto.getActivityParams();
            if (StringUtils.isNotEmpty(dto.getDialogTitle())) {
                lockDialogTitle.setVisibility(View.VISIBLE);
                lockDialogTitle.setText(dto.getDialogTitle());
            }
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                lockAnimation.setAnimation(R.raw.alert);
            }
        }
        subscriptionLayout.setOnClickListener(view -> handleSubscriptionClick(dto));
    }

    private void handleSubscriptionClick(SubscriptionCheckDto dto) {
        closeDialog();
        Intent intent = null;

        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                intent = new Intent(requireActivity(), ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("SubscriptionClick", "Class not found: " + dto.getClassName(), e);
            }
        }

        if (intent == null) {
//            intent = new Intent(requireActivity(), CampaignSubscriptionActivity.class)
//                    .putExtra("plan_level", "COURSE")
//                    .putExtra("fromUnlockText", true);
            intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireActivity(), RedirectionSource.CHAPTER_LIST_JOIN);
        }

        if (activityParamsMap != null) {
            Intent finalIntent = intent;
            activityParamsMap.forEach((key, value) -> {
                if (value instanceof Serializable) {
                    finalIntent.putExtra(key, (Serializable) value);
                }
            });
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                MyApplication.saveData(PreferenceKeys.NAVIGATE_TO_HOME_SCREEN.getKey(), true);
            }
        }
        startActivity(intent);
    }

    private Intent getTargetIntent(SubscriptionCheckDto dto) {
        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                return new Intent(getActivity(), ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("LockDialog", "Class not found: " + dto.getClassName(), e);
            }
        }
//        return new Intent(getActivity(), CampaignSubscriptionActivity.class);
//                .putExtra("plan_level", "COURSE");
        return SubscriptionNavigationHelper.getSubscriptionPageIntent(getActivity(), RedirectionSource.CHAPTER_LIST_JOIN);
    }

    private void closeDialog() {
        if (lockDialog != null && lockDialog.isShowing()) {
            lockDialog.dismiss();
        }
    }

    private void updateModelAndNotify(SamplePaperModel model, NoteEntity entity) {
//        model.setNotesUrl(entity.getNotesUrl());
//        model.setLocked(entity.isLocked());
        model.setDownloadComplete(entity.getStatus().equals(NoteEntity.STATUS_COMPLETED));
        model.setProgress(entity.getProgress());
//        model.setPageCount(entity.getPageCount());
        model.setStatus(entity.getStatus());

        if (adapter != null) {
            int position = adapter.getList().indexOf(model);
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onNotesClick(String title,String url,String notesType,String subjectName, boolean isLocked) {
        try {
            if (isLocked) {
                if (lockDialog != null && !lockDialog.isShowing()) {
                    lockDialog.show();
                }
                return;
            }
            Log.d(TAG, "Note Clicked: " + title + ", " + url);
            PdfUtils.openPdfViewer(requireContext(), url,
                    title,notesType,subjectName, false, "", "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onNoteDownloadClick(int position, String url, boolean isLocked, Object object) {
        try {
            if (CommonMethods.isOnline(requireContext())) {

                Map<String, String> params = new HashMap<>();
                params.put("page", TAG);

                if (StringUtils.isNotEmpty(url)) {
                    SamplePaperModel model = (SamplePaperModel) object;
                    Log.d(TAG, "Download Note: " + model.toString());
                    NoteEntity noteEntity = new NoteEntity(
                            model.getId(),
                            model.getSubject(),
                            model.getSubject(),
                            model.getTitle(),
                            model.getNotesType(),
                            model.getDownloadLink(),
                            model.isLocked(),
                            0, 0, "pdf", NoteEntity.STATUS_PENDING, 0, null,
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
                    noteEntity.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                    noteEntity.setNoteType(model.getNotesType());
                    notesViewModel.insert(noteEntity);
                    PdfUtils.startPdfDownload(requireContext(), noteEntity);

                    notesViewModel.getLiveNotesById(model.getId())
                            .observe(this, entity -> {
                                if (entity != null) {
                                    updateModelAndNotify(model, entity);
                                }
                            });

                    params.put("id", model.getId());
                    params.put("subjectName", model.getSubject());
                    params.put("subjectId", model.getSubject());
                    params.put("chapterTitle", model.getTitle());
                    params.put("noteType", model.getNotesType());
                    params.put("isLocked", ""+model.isLocked());
                    params.put("pageCount", ""+model.getPageCount());
                    params.put("size", ""+model.getFileSize());

                }

                params.put("notesUrl", url);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.note_download_click.getActionName(), params);

            } else {
                CommonMethods.showNegativeSnackbar(requireView(), getString(R.string.NoInternetConnection), 3000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onNoteStatusClick(int position, Object object) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}