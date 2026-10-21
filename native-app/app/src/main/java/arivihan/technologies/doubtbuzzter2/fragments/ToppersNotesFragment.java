package arivihan.technologies.doubtbuzzter2.fragments;

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
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersNotesAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.enums.batch.notes.BatchNotesScreenType;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.batch.note.ToppersNotesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ToppersNotesFragment extends Fragment implements ToppersNotesAdapter.OnNotesClickListener {

    private static final String TAG = ToppersNotesFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private ToppersNotesAdapter toppersNotesAdapter;
    private ArrayList<ToppersNotesModel> list;
    private LinearLayout noResultFoundLayout;
    private Dialog lockDialog;
    MaterialCardView subscriptionLayout;
    PlayStoreDialog playStoreDialog;
    private Map<String, Object> activityParamsMap;
    private TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    LottieAnimationView lockAnimation;
    private String lastClickedNotesType;
    private String categoryId;


    public ToppersNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", list);
    }

    public static ToppersNotesFragment newInstance(ArrayList<ToppersNotesModel> list, String categoryId) {
        ToppersNotesFragment fragment = new ToppersNotesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", list);
        args.putString("categoryId", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            list = getArguments().getParcelableArrayList("list");
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toppers_notes, container, false);
        recyclerView = view.findViewById(R.id.toppers_notes_rv);
        noResultFoundLayout = view.findViewById(R.id.no_result_found_layout);

        if (CollectionUtils.isEmpty(list)) {
            noResultFoundLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noResultFoundLayout.setVisibility(View.GONE);
            toppersNotesAdapter = new ToppersNotesAdapter(list, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(toppersNotesAdapter);
        }
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
            Object source = StringUtils.isNotEmpty(categoryId) ? categoryId : RedirectionSource.TOPPERS_NOTES_JOIN;
            Log.d(TAG, "initializeLockDialog: Navigating to subscription page | source: " + source + " | categoryId: " + categoryId);
            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntentWithSource(requireActivity(), source);

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
            if (toppersNotesAdapter != null) {
                toppersNotesAdapter.notifyDataSetChanged();
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
        subscriptionLayout.setOnClickListener(view -> handleSubscriptionClick());
    }

    private void handleSubscriptionClick() {
        Log.d(TAG, "handleSubscriptionClick---locked notes clicked: | categoryId: " + categoryId);
        String popupType = SubscriptionPopupType.NOTES.name();

        if (StringUtils.isNotEmpty(lastClickedNotesType)) {
            try {
                // Try to use the specific note type if it's defined in SubscriptionPopupType
                popupType = SubscriptionPopupType.valueOf(lastClickedNotesType.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                // Fallback logic for types that might be mapped differently
                if (BatchNotesScreenType.PREVIOUS_YEAR_PAPER.name().equalsIgnoreCase(lastClickedNotesType)
                        || BatchNotesScreenType.QUARTERLY_NOTES.name().equalsIgnoreCase(lastClickedNotesType)
                        || BatchNotesScreenType.SAMPLE_PAPER.name().equalsIgnoreCase(lastClickedNotesType)
                        || BatchNotesScreenType.HALF_YEARLY_NOTES.name().equalsIgnoreCase(lastClickedNotesType)) {
                    popupType = SubscriptionPopupType.PYQ.name();
                }
            }
        }

        Object source = StringUtils.isNotEmpty(categoryId) ? categoryId : RedirectionSource.TOPPERS_NOTES_JOIN;
        Log.d(TAG, "handleSubscriptionClick: Navigating to subscription page | source: " + source + " | categoryId: " + categoryId);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntentWithSource(requireActivity(), source);
        intent.putExtra("navigatedFromScreen", popupType);
        if (activityParamsMap != null) {
            activityParamsMap.forEach((k, v) -> {
                if (v instanceof Serializable) {
                    intent.putExtra(k, (Serializable) v);
                }
            });
        }
        lockDialog.dismiss();
        startActivity(intent);
    }

    private void closeDialog() {
        if (lockDialog != null && lockDialog.isShowing()) {
            lockDialog.dismiss();
        }
    }



    public void showLockDialog() {
        if (lockDialog != null && !lockDialog.isShowing()) {
            lockDialog.show();
        }
    }

    @Override
    public void onNotesClick(String chapterTitle, String url,String notesType, String subjectName, boolean isLocked) {
        try {
            if (isLocked) {
                if (lockDialog != null && !lockDialog.isShowing()) {
                    lastClickedNotesType = notesType;
                    handleSubscriptionClick();
                }
                return;
            }
            PdfUtils.openPdfViewer(requireContext(), url,
                    chapterTitle, notesType,subjectName,false, "", "");
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
                    ToppersNotesModel model = (ToppersNotesModel) object;
                    Log.d(TAG, "onNoteDownloadClick:" + model.toString());

                    NoteEntity noteEntity = new NoteEntity(
                            model.getId(),
                            model.getSubjectId(),
                            model.getSubjectName(),
                            model.getChapterTitle(),
                            model.getNotesBy(),
                            model.getNotesUrl(),
                            model.isLocked(),
                            model.getPageCount(), 0, "pdf",
                            NoteEntity.STATUS_PENDING, 0, null,
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                    );
                    noteEntity.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                    noteEntity.setNoteType(model.getNotesType());
                    PdfUtils.startPdfDownload(requireContext(), noteEntity);

                    params.put("id", model.getId());
                    params.put("subjectName", model.getSubjectName());
                    params.put("subjectId", model.getSubjectId());
                    params.put("chapterTitle", model.getChapterTitle());
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

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (lockDialog != null && lockDialog.isShowing()) {
            lockDialog.dismiss();
        }
        if (playStoreDialog != null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
    }

}