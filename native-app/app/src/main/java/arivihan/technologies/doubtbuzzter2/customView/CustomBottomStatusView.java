package arivihan.technologies.doubtbuzzter2.customView;

import static arivihan.technologies.doubtbuzzter2.MyApplication.getASATStatusDatabaseReference;
import static arivihan.technologies.doubtbuzzter2.MyApplication.getDoubtStatusDatabaseReference;
import static arivihan.technologies.doubtbuzzter2.MyApplication.getPendingSubscriptionDatabaseReference;
import static arivihan.technologies.doubtbuzzter2.MyApplication.getStringData;
import static arivihan.technologies.doubtbuzzter2.MyApplication.getTestStatusDatabaseReference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MockTestAnalysisActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionCongatulationsActivity;
import arivihan.technologies.doubtbuzzter2.activity.TestSeriesListActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.adapter.HomeStatusAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.PendingSubscriptionStatus;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.DoubtStatusDto;
import arivihan.technologies.doubtbuzzter2.model.PendingSubscriptionEntry;
import arivihan.technologies.doubtbuzzter2.model.RescheduledLectureStatusDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionStatus;
import arivihan.technologies.doubtbuzzter2.model.TestEvaluationStatusEnum;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatus;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TopperNoteDownloadedStatusDto;
import arivihan.technologies.doubtbuzzter2.model.UserTestEvaluationStautsObject;
import arivihan.technologies.doubtbuzzter2.model.asat.UserAsatFirebaseData;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.firebase.rdb.NeetWeakTopicRdbModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CustomBottomStatusView implements HomeStatusAdapter.ClickListener {
    private final View bottomSheet;
    private boolean isBottomSheetVisible = false;
    private final ArrayList<Object> objects = new ArrayList<>();
    private CountDownTimer countDownTimerAll;
    private final ArrayList<DoubtStatusDto> doubtList = new ArrayList<>();
    private final ArrayList<PendingSubscriptionEntry> pendingSubscriptionEntries = new ArrayList<>();
    private final ArrayList<UserAsatFirebaseData> asatList = new ArrayList<>();
    private final ArrayList<NeetWeakTopicRdbModel> neetWeakTopicList = new ArrayList<>();
    private final ArrayList<RescheduledLectureStatusDto> rescheduledLectureList = new ArrayList<>(); // ← NEW
    private final ConstraintLayout home_screen_all_status_layout;
    private final CardView close_status_card_layout;
    private final RecyclerView all_status_card_rv;
    private final CardView all_status_card;
    private final HomeStatusAdapter homeStatusAdapter;
    private final Context context;
    private final CardView home_screen_status_background_card;
    private final CardView more_text_card;
    private final TextView more_text;
    private ValueEventListener valueEventListenerDoubt;
    private ValueEventListener valueEventListenerTest;
    private ValueEventListener valueEventListenerSubscription;
    private ValueEventListener valueEventListenerASAT;
    private ValueEventListener valueEventListenerNeetWeakTopic;
    private ValueEventListener valueEventListenerRescheduledLecture; // ← NEW
    private CustomBottomStatusListener customBottomStatusListener;
    private boolean isShowAnimating = false;
    private final Handler pendingUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable pendingUpdateRunnable = this::handleCollapsedView;

    private final ArrayList<TopperNoteDownloadedStatusDto> topperNotesList = new ArrayList<>();
    private ValueEventListener valueEventListenerTopperNotes;

    private final ArrayList<TestGenerationStatusDto> testGenerationList = new ArrayList<>();
    private ValueEventListener valueEventListenerTestGeneration;
    private com.google.firebase.database.DatabaseReference testGenerationRef;

    public CustomBottomStatusView(View bottomSheet, Context context, CustomBottomStatusListener customBottomStatusListener) {
        this.bottomSheet = bottomSheet;
        this.context = context;
        this.customBottomStatusListener = customBottomStatusListener;

        home_screen_all_status_layout = this.bottomSheet.findViewById(R.id.home_screen_all_status_layout);
        home_screen_status_background_card = this.bottomSheet.findViewById(R.id.home_screen_status_background_card);
        more_text_card = this.bottomSheet.findViewById(R.id.more_text_card);
        more_text = this.bottomSheet.findViewById(R.id.more_text);

        close_status_card_layout = this.bottomSheet.findViewById(R.id.close_status_card_layout);
        all_status_card_rv = this.bottomSheet.findViewById(R.id.all_status_card_rv);
        all_status_card = this.bottomSheet.findViewById(R.id.all_status_card);
        homeStatusAdapter = new HomeStatusAdapter(objects, context, this);
        all_status_card_rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        all_status_card_rv.setAdapter(homeStatusAdapter);

        // The underlying Views survive across fragment onPause/onResume even though this
        // wrapper is torn down and rebuilt each time (see MainDashboard/CompetitiveHomeScreenFragment
        // onPause/onResume), so any expanded state left over from before navigating away must be
        // reset here — otherwise it stays visually expanded (or the "+N more" chip mispositioned)
        // after returning from a card click.
        close_status_card_layout.setVisibility(View.GONE);
        home_screen_all_status_layout.setVisibility(View.GONE);
        more_text_card.setVisibility(View.GONE);
        home_screen_status_background_card.setVisibility(View.GONE);
        all_status_card.setBackground(null);
        all_status_card.setCardBackgroundColor(Color.parseColor("#00000000"));
        collapseBottomSheet();

        checkTestAndDoubtEvaluation();
    }

    public void toggle() {
        hide();
        home_screen_all_status_layout.setVisibility(View.GONE);
        all_status_card.setVisibility(View.GONE);
    }

    public void show() {
        if (isBottomSheetVisible || isShowAnimating) return;

        isShowAnimating = true;
        bottomSheet.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, bottomSheet.getHeight(), 0);
        animate.setDuration(300);
        animate.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation a) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
            @Override public void onAnimationEnd(android.view.animation.Animation a) { isShowAnimating = false; }
        });
        bottomSheet.startAnimation(animate);
        isBottomSheetVisible = true;
    }

    public void hide() {
        if (!isBottomSheetVisible && bottomSheet.getVisibility() == View.INVISIBLE) return;

        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, bottomSheet.getHeight());
        animate.setDuration(300);
        bottomSheet.startAnimation(animate);

        bottomSheet.postDelayed(() -> bottomSheet.setVisibility(View.INVISIBLE), 300);
        isBottomSheetVisible = false;
        isShowAnimating = false;
    }

    public boolean isVisible() {
        return isBottomSheetVisible;
    }

    private void checkTestAndDoubtEvaluation() {

        valueEventListenerASAT = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        if (snapshot.getValue() instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                            UserAsatFirebaseData userAsatFirebaseData = MyApplication.parseAsatResult(dataMap);
                            String currentLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                            if (userAsatFirebaseData.getLanguage() != null && currentLang != null
                                    && userAsatFirebaseData.getLanguage().contains(currentLang)) {
                                asatList.clear();
                                asatList.add(0, userAsatFirebaseData);
                            }
                            updateObjectList();
                        }
                    } catch (JSONException ignored) {
                    }
                } else {
                    asatList.clear();
                    updateObjectList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        getASATStatusDatabaseReference().addValueEventListener(valueEventListenerASAT);

        valueEventListenerDoubt = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        if (snapshot.getValue() instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                            List<DoubtStatusDto> allDoubts = MyApplication.parseFirebaseResponseDoubt(dataMap);
                            doubtList.clear();
                            String currentLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                            for (DoubtStatusDto dto : allDoubts) {
                                if (dto.getLanguage() != null && currentLang != null
                                        && dto.getLanguage().contains(currentLang)) {
                                    doubtList.add(dto);
                                }
                            }
                            if (!doubtList.isEmpty()) {
                                updateObjectList();
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                } else {
                    doubtList.clear();
                    updateObjectList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        getDoubtStatusDatabaseReference().addValueEventListener(valueEventListenerDoubt);

        valueEventListenerTest = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        if (snapshot.getValue() instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                            if (!CollectionUtils.isEmpty(objects)) {
                                if (objects.get(0) instanceof UserTestEvaluationStautsObject)
                                    objects.remove(0);
                            }
                            objects.add(0, MyApplication.parseFirebaseResponseTest(dataMap));
                            pendingUpdateHandler.removeCallbacks(pendingUpdateRunnable);
                            pendingUpdateHandler.postDelayed(pendingUpdateRunnable, 80);
                        }
                    } catch (JSONException ignored) {
                    }
                } else {
                    if (!CollectionUtils.isEmpty(objects) && objects.get(0) instanceof UserTestEvaluationStautsObject) {
                        objects.remove(0);
                    }
                    pendingUpdateHandler.removeCallbacks(pendingUpdateRunnable);
                    pendingUpdateHandler.postDelayed(pendingUpdateRunnable, 80);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        getTestStatusDatabaseReference().addValueEventListener(valueEventListenerTest);

        valueEventListenerSubscription = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        if (snapshot.getValue() instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                            List<PendingSubscriptionEntry> pendingSubscriptionsList =
                                    MyApplication.parseFirebaseResponsePendingSubscriptionEntries(dataMap);
                            List<PendingSubscriptionEntry> filteredSubscriptions = new ArrayList<>();
                            String currentLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

                            for (PendingSubscriptionEntry entry : pendingSubscriptionsList) {
                                if (PendingSubscriptionStatus.ACTIVE.name().equalsIgnoreCase(entry.getStatus())
                                        && entry.getLanguage() != null && currentLang != null
                                        && entry.getLanguage().contains(currentLang)) {

                                    MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
                                    MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey());
                                    MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey());

                                    filteredSubscriptions.add(entry);
                                }
                            }
                            pendingSubscriptionEntries.clear();
                            pendingSubscriptionEntries.addAll(filteredSubscriptions);
                            if (!filteredSubscriptions.isEmpty() && customBottomStatusListener != null) {
                                customBottomStatusListener.updateSubscriptionActivation(true);
                            }
                            updateObjectList();
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    pendingSubscriptionEntries.clear();
                    updateObjectList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        getPendingSubscriptionDatabaseReference().addValueEventListener(valueEventListenerSubscription);

        valueEventListenerTopperNotes = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        if (snapshot.getValue() instanceof Map) {
                            Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                            TopperNoteDownloadedStatusDto dto = MyApplication.parseToppersNotesResult(dataMap);
                            topperNotesList.clear();
                            topperNotesList.add(dto);
                            updateObjectList();
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    topperNotesList.clear();
                    updateObjectList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        MyApplication.getToppersNotesStatusDatabaseReference().addValueEventListener(valueEventListenerTopperNotes);

        // ── Rescheduled Lecture listener ──────────────────────────────────────── NEW
        valueEventListenerRescheduledLecture = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("rescheduled_debug", "onDataChange: exists=" + snapshot.exists()
                        + " childCount=" + snapshot.getChildrenCount() + " key=" + snapshot.getKey());
                if (snapshot.exists()) {
                    try {
                        rescheduledLectureList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Object raw = child.getValue();
                            Log.d("rescheduled_debug", "child key=" + child.getKey()
                                    + " type=" + (raw == null ? "null" : raw.getClass().getSimpleName())
                                    + " value=" + raw);
                            if (raw instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> dataMap = (Map<String, Object>) raw;
                                RescheduledLectureStatusDto dto = MyApplication.parseRescheduledLectureResult(dataMap);
                                // Use push key as lectureId if not set in the data
                                if (org.apache.commons.lang3.StringUtils.isEmpty(dto.getChapterId())) {
                                    dto.setChapterId(child.getKey());
                                }
                                Log.d("rescheduled_debug", "parsed dto: title=" + dto.getTitle()
                                        + " subTitle=" + dto.getSubTitle()
                                        + " lectureId=" + dto.getChapterId()
                                        + " newStartTime=" + dto.getNewStartTime());
                                if (MyApplication.isRescheduledLectureViewed(dto.getChapterId())) {
                                    Log.d("rescheduled_debug", "lectureId=" + dto.getChapterId() + " already viewed — skipping");
                                    continue;
                                }
                                rescheduledLectureList.add(dto);
                            }
                        }
                        Log.d("rescheduled_debug", "rescheduledLectureList.size=" + rescheduledLectureList.size()
                                + " → calling updateObjectList");
                        updateObjectList();
                    } catch (Exception e) {
                        Log.e("rescheduled_debug", "Exception during parse/update: " + e.getMessage(), e);
                    }
                } else {
                    Log.d("rescheduled_debug", "snapshot does not exist — clearing list");
                    rescheduledLectureList.clear();
                    updateObjectList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("rescheduled_debug", "Firebase listener cancelled: " + error.getMessage());
            }
        };
        MyApplication.getRescheduledLectureStatusDatabaseReference()
                .addValueEventListener(valueEventListenerRescheduledLecture);

        valueEventListenerTestGeneration = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                testGenerationList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object raw = child.getValue();
                        if (raw instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dataMap = (Map<String, Object>) raw;
                            TestGenerationStatusDto dto =
                                    MyApplication.parseTestGenerationResult(child.getKey(), dataMap);
                            if (TestGenerationStatus.IN_PROGRESS.equals(dto.getStatus())
                                    || TestGenerationStatus.COMPLETED_UNSEEN.equals(dto.getStatus())) {
                                testGenerationList.add(dto);
                            }
                        }
                    }
                }
                updateObjectList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        testGenerationRef = MyApplication.getTestGenerationParentDatabaseReference();
        testGenerationRef.addValueEventListener(valueEventListenerTestGeneration);
    }

    private void updateObjectList() {
        if (!CollectionUtils.isEmpty(objects)) {
            if (objects.get(0) instanceof UserTestEvaluationStautsObject userTestEvaluationStautsObject) {
                // First item is a test object — preserve it at index 0, rebuild the rest
                objects.clear();
                if (!CollectionUtils.isEmpty(testGenerationList))
                    objects.addAll(testGenerationList);
                if (!CollectionUtils.isEmpty(doubtList))
                    objects.addAll(doubtList);
                if (!CollectionUtils.isEmpty(pendingSubscriptionEntries))
                    objects.addAll(pendingSubscriptionEntries);
                if (!CollectionUtils.isEmpty(topperNotesList))
                    objects.addAll(topperNotesList);
                if (!CollectionUtils.isEmpty(rescheduledLectureList)
                        && SubscriptionType.hasActiveSubscription())
                    objects.addAll(rescheduledLectureList);
                objects.add(0, userTestEvaluationStautsObject);
                if (!CollectionUtils.isEmpty(asatList))
                    objects.addAll(0, asatList);
            } else {
                objects.clear();
                if (!CollectionUtils.isEmpty(asatList))
                    objects.addAll(asatList);
                if (!CollectionUtils.isEmpty(testGenerationList))
                    objects.addAll(testGenerationList);
                if (!CollectionUtils.isEmpty(doubtList))
                    objects.addAll(doubtList);
                if (!CollectionUtils.isEmpty(pendingSubscriptionEntries))
                    objects.addAll(pendingSubscriptionEntries);
                if (!CollectionUtils.isEmpty(topperNotesList))
                    objects.addAll(topperNotesList);
                if (!CollectionUtils.isEmpty(rescheduledLectureList)
                        && SubscriptionType.hasActiveSubscription())
                    objects.addAll(rescheduledLectureList);
                if (!CollectionUtils.isEmpty(neetWeakTopicList))
                    objects.addAll(neetWeakTopicList);
            }
        } else {
            if (!CollectionUtils.isEmpty(neetWeakTopicList))
                objects.addAll(neetWeakTopicList);
            if (!CollectionUtils.isEmpty(topperNotesList))
                objects.addAll(topperNotesList);
            if (!CollectionUtils.isEmpty(rescheduledLectureList)
                    && SubscriptionType.hasActiveSubscription())
                objects.addAll(rescheduledLectureList);
            if (!CollectionUtils.isEmpty(asatList))
                objects.addAll(asatList);
            if (!CollectionUtils.isEmpty(testGenerationList))
                objects.addAll(testGenerationList);
            if (!CollectionUtils.isEmpty(doubtList))
                objects.addAll(doubtList);
            if (!CollectionUtils.isEmpty(pendingSubscriptionEntries))
                objects.addAll(pendingSubscriptionEntries);
        }
        Log.d("userData", "updateObjectList: " + objects.toString());
        pendingUpdateHandler.removeCallbacks(pendingUpdateRunnable);
        pendingUpdateHandler.postDelayed(pendingUpdateRunnable, 80);
    }

    private void handleCollapsedView() {
        if (!CollectionUtils.isEmpty(objects)) {
            all_status_card.setVisibility(View.VISIBLE);
            if (objects.size() > 1) {
                home_screen_status_background_card.setVisibility(View.VISIBLE);
                more_text_card.setVisibility(View.VISIBLE);
                more_text.setText("+" + (objects.size() - 1));
            } else {
                home_screen_status_background_card.setVisibility(View.GONE);
                homeStatusAdapter.setCollapsed(true);
                all_status_card.setBackground(null);
                all_status_card.setCardBackgroundColor(Color.parseColor("#00000000"));
                collapseBottomSheet();
                close_status_card_layout.setVisibility(View.GONE);
                more_text_card.setVisibility(View.GONE);
            }
            homeStatusAdapter.notifyDataSetChanged();
            more_text_card.setOnClickListener(view -> {
                more_text_card.setVisibility(View.GONE);
                close_status_card_layout.setVisibility(View.VISIBLE);
                home_screen_all_status_layout.setVisibility(View.VISIBLE);
                homeStatusAdapter.setCollapsed(false);
                all_status_card.setBackground(context.getDrawable(R.drawable.bottomcontact));
                all_status_card.setCardBackgroundColor(context.getResources().getColor(R.color.white));
                home_screen_status_background_card.setVisibility(View.GONE);
                expandRecyclerView();
                expandBottomSheet();
            });
            close_status_card_layout.setOnClickListener(view -> collapseExpanded());
            show();
        } else {
            home_screen_status_background_card.setVisibility(View.GONE);
            all_status_card.setVisibility(View.GONE);
            homeStatusAdapter.notifyDataSetChanged();
            home_screen_all_status_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemDeleted() {
        if (objects.isEmpty()) {
            home_screen_all_status_layout.setVisibility(View.GONE);
            close_status_card_layout.setVisibility(View.GONE);
            home_screen_status_background_card.setVisibility(View.GONE);
            more_text_card.setVisibility(View.GONE);
            all_status_card.setVisibility(View.GONE);
            collapseBottomSheet();
            hide();
        } else if (close_status_card_layout.getVisibility() == View.VISIBLE) {
            // expanded view
            if (objects.size() == 1) collapseExpanded();
            // else: adapter's notifyItemRemoved already updated the list in place
        } else {
            // collapsed view — update "+N more" chip and re-render to surface the new last item
            if (objects.size() > 1) {
                more_text.setText("+" + (objects.size() - 1));
                home_screen_status_background_card.setVisibility(View.VISIBLE);
                more_text_card.setVisibility(View.VISIBLE);
            } else {
                home_screen_status_background_card.setVisibility(View.GONE);
                more_text_card.setVisibility(View.GONE);
            }
            homeStatusAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(Object object) {
        if (object instanceof UserAsatFirebaseData) {
            Intent intent = new Intent(context, ASATActivity.class);
            intent.putExtra(ASATActivity.ASAT_OPEN_RESULT, true);
            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, RedirectionSource.ASAT);
            context.startActivity(intent);
            deleteAsatEntry();
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_bottom_popup_click.getActionName());

        } else if (object instanceof TopperNoteDownloadedStatusDto topperNoteDownloadedStatusDto) {
            try {
                Gson gson = new Gson();
                String activityParamsJson = topperNoteDownloadedStatusDto.getActivityParams();
                if (activityParamsJson == null || activityParamsJson.trim().isEmpty()) {
                    Log.e("onItemClick", "activityParams is null or empty");
                    return;
                }
                HashMap<String, Object> paramsMap = gson.fromJson(activityParamsJson, HashMap.class);
                if (paramsMap == null) {
                    Log.e("onItemClick", "Parsed paramsMap is null");
                    return;
                }
                Object activityParamsObj = paramsMap.get("activityParams");
                if (activityParamsObj != null && !(activityParamsObj instanceof String)) {
                    paramsMap.put("activityParams", gson.toJson(activityParamsObj));
                }
                String className = topperNoteDownloadedStatusDto.getClassName();
                if (className == null || className.isEmpty()) {
                    Log.e("onItemClick", "className is null or empty");
                    return;
                }
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_toppers_notes_card_view_btn_click.getActionName());
                MyApplication.openActivity(context, className, paramsMap);
                MyApplication.getToppersNotesStatusDatabaseReference().removeValue();
            } catch (Exception e) {
                Log.e("onItemClick", "Crash while handling TopperNoteDownloadedStatusDto", e);
            }

        } else if (object instanceof RescheduledLectureStatusDto lecture) {
            try {
                CommonNavigator.navigateToChapterListByBatchActivity(context,lecture.getSubjectId(), "d", lecture.getSubjectCode(), "CustomBottomStatusView", "lectures", null,lecture.getChapterId());
                deleteRescheduledLectureEntry(lecture.getChapterId());
            } catch (Exception e) {
                Log.e("onItemClick", "Crash while handling RescheduledLectureStatusDto", e);
            }

        } else if (object instanceof DoubtStatusDto doubtStatusDto) {
            CommonNavigator.navigateToDoubtChatSession(context, doubtStatusDto.getSessionId(), false);

        } else if (object instanceof UserTestEvaluationStautsObject userTestEvaluationStautsObject) {
            if (userTestEvaluationStautsObject.getTestEvaluationStatusEnum().equals(TestEvaluationStatusEnum.REVIEW_PENDING)) {
                deleteTestEntry();
                Intent intent = new Intent(context, MockTestAnalysisActivity.class);
                intent.putExtra("testSeriesId", userTestEvaluationStautsObject.getTestId());
                intent.putExtra(PreferenceKeys.BOARD.getKey(), true);
                intent.putExtra("Id", userTestEvaluationStautsObject.getTestId());
                intent.putExtra("testSeriesListId", userTestEvaluationStautsObject.getListId());
                intent.putExtra("testSeriesName", userTestEvaluationStautsObject.getListName());
                MyApplication.saveTestSeriesQuestions("questions", null);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, TestSeriesListActivity.class);
                intent.putExtra("Id", userTestEvaluationStautsObject.getListId());
                intent.putExtra("title", userTestEvaluationStautsObject.getListName());
                intent.putExtra("filterStatus", "");
                intent.putExtra("filterSubject", "");
                intent.putExtra("testSeriesId", userTestEvaluationStautsObject.getTestId());
                context.startActivity(intent);
            }

        } else if (object instanceof PendingSubscriptionEntry pendingSubscriptionEntry) {
            if (pendingSubscriptionEntry.getStatus().equals(SubscriptionStatus.ACTIVE.name())) {
                deletePendingEntry(pendingSubscriptionEntry.getPlanId());
                Intent intent = new Intent(context, SubscriptionCongatulationsActivity.class);
                intent.putExtra("planId", pendingSubscriptionEntry.getPlanId());
                context.startActivity(intent);
            }
        } else if (object instanceof TestGenerationStatusDto dto) {
            if (TestGenerationStatus.COMPLETED_UNSEEN.equals(dto.getStatus())) {
                Map<String, Object> update = new HashMap<>();
                update.put("status", TestGenerationStatus.COMPLETED_SEEN.name());
                MyApplication.getTestGenerationDatabaseReference(dto.getRequestId()).updateChildren(update);

                Map<String, Object> params = new HashMap<>();
                params.put("url", CommonMethods.getTestGeneratorUrl());
                params.put("testId", dto.getRequestId());
                params.put("action", "startTest");
                if (StringUtils.isNotEmpty(dto.getTestName())) params.put("testName", dto.getTestName());
                params.put("appendUserId", true);
                params.put("appendUserName", true);
                params.put("appendBoard", true);
                params.put("appendToken", true);
                params.put("appendClassId", true);
                params.put("appendCourseId", true);
                params.put("appendUserGender", true);
                params.put("appendUserImageUrl", true);
                MyApplication.openActivity(context,
                        "arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity",
                        params);
            }
        }
    }

    private void deleteTestEntry() {
        MyApplication.getTestStatusDatabaseReference().removeValue();
    }

    private void deletePendingEntry(String planId) {
        MyApplication.getPendingSubscriptionDatabaseReference(planId).removeValue();
    }

    private void deleteAsatEntry() {
        MyApplication.getASATStatusDatabaseReference().removeValue();
    }

    private void expandRecyclerView() {
        int childCount = all_status_card_rv.getChildCount();
        for (int i = childCount - 2; i >= 0; i--) {
            View child = all_status_card_rv.getChildAt(i);
            child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredHeight = child.getMeasuredHeight();
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            child.setLayoutParams(layoutParams);
            child.setTranslationY(measuredHeight);
            child.setVisibility(View.VISIBLE);
            child.animate()
                    .translationY(0)
                    .setDuration(150)
                    .setStartDelay((childCount - i - 1) * 20L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void collapseRecyclerView() {
        int childCount = all_status_card_rv.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {
            View child = all_status_card_rv.getChildAt(i);
            child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int originalHeight = child.getMeasuredHeight();
            ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
            animator.setDuration(200);
            animator.setStartDelay(i * 20L);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(valueAnimator -> {
                int animatedHeight = (int) valueAnimator.getAnimatedValue();
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                layoutParams.height = animatedHeight;
                child.setLayoutParams(layoutParams);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                    layoutParams.height = 0;
                    layoutParams.width = 0;
                    child.setLayoutParams(layoutParams);
                    child.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }

    public void expandBottomSheet() {
        ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomSheet.setLayoutParams(params);
        bottomSheet.setClickable(true);
        bottomSheet.setOnClickListener(v -> collapseExpanded());
    }

    public void collapseBottomSheet() {
        ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        bottomSheet.setLayoutParams(params);
        bottomSheet.setOnClickListener(null);
        bottomSheet.setClickable(false);
    }

    private void collapseExpanded() {
        close_status_card_layout.setVisibility(View.GONE);
        home_screen_all_status_layout.setVisibility(View.GONE);
        if (objects.size() > 1) {
            home_screen_status_background_card.setVisibility(View.VISIBLE);
            more_text_card.setVisibility(View.VISIBLE);
            more_text.setText("+" + (objects.size() - 1));
        } else {
            home_screen_status_background_card.setVisibility(View.GONE);
            more_text_card.setVisibility(View.GONE);
        }
        homeStatusAdapter.setCollapsed(true);
        collapseRecyclerView();
        collapseBottomSheet();
        all_status_card.setBackground(null);
        all_status_card.setCardBackgroundColor(Color.parseColor("#00000000"));
    }

    public void dismissListener() {
        homeStatusAdapter.stopTimer();
        if (valueEventListenerDoubt != null)
            getDoubtStatusDatabaseReference().removeEventListener(valueEventListenerDoubt);
        if (valueEventListenerTest != null)
            getTestStatusDatabaseReference().removeEventListener(valueEventListenerTest);
        if (valueEventListenerSubscription != null)
            getPendingSubscriptionDatabaseReference().removeEventListener(valueEventListenerSubscription);
        if (valueEventListenerASAT != null)
            getASATStatusDatabaseReference().removeEventListener(valueEventListenerASAT);
        if (valueEventListenerTopperNotes != null)
            MyApplication.getToppersNotesStatusDatabaseReference().removeEventListener(valueEventListenerTopperNotes);
        if (valueEventListenerRescheduledLecture != null)
            MyApplication.getRescheduledLectureStatusDatabaseReference()
                    .removeEventListener(valueEventListenerRescheduledLecture);
        if (valueEventListenerTestGeneration != null && testGenerationRef != null)
            testGenerationRef.removeEventListener(valueEventListenerTestGeneration);
        pendingUpdateHandler.removeCallbacks(pendingUpdateRunnable);
    }

    public interface CustomBottomStatusListener {
        void updateSubscriptionActivation(boolean isActive);
    }

    private void deleteRescheduledLectureEntry(String lectureId) {
        Log.d("delete", "deleteRescheduledLectureEntry: "+lectureId);
        MyApplication.markRescheduledLectureViewed(lectureId);
    }
}