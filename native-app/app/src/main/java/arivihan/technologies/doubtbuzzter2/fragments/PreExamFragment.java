package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyHorizontalAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyContentType;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CRASH_COURSE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HALF_YEARLY_USER_TASK_UPDATE;

import org.apache.commons.lang3.StringUtils;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.crash_course.CrashCourseResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;

public class PreExamFragment extends Fragment implements HalfYearlyHorizontalAdapter.HalfYearlyHorizontalInterface {

    private static final String TAG = PreExamFragment.class.getSimpleName();
    private HalfYearlyHorizontalAdapter halfYearlyHorizontalAdapter;
    private Handler handler = new Handler();
    private Runnable runnable;
    private List<TodayClassItemResponse> itemList = new ArrayList<>();
    private CardView hourCountDownBackground, minuteCountDownBackground, secondCountDownBackground;
    private TextView hourCountdown, minuteCountdown, secondCountdown;
    RelativeLayout preExamLayout;
    RecyclerView recyclerView;
    static String EXAM_ID;
    String examId;
    private Handler countdownHandler = new Handler();
    private long remainingTimeInMillis = 43200000;
    TodayClassItemResponse clickedItem;
    PlayStoreDialog playStoreDialog;
    private static final int REQUEST_CODE = 1;
    TextView fragmentTitle;


    public PreExamFragment() {
    }

    public static PreExamFragment newInstance(String examId) {
        PreExamFragment fragment = new PreExamFragment();
        Bundle args = new Bundle();
        args.putString(EXAM_ID, examId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pre_exam, container, false);

        hourCountDownBackground = view.findViewById(R.id.hour_countdown_background);
        minuteCountDownBackground = view.findViewById(R.id.minute_countdown_background);
        secondCountDownBackground = view.findViewById(R.id.second_countdown_background);
        preExamLayout = view.findViewById(R.id.pre_exam_layout);
        preExamLayout.setVisibility(View.GONE);

        hourCountdown = view.findViewById(R.id.hour_countdown);
        minuteCountdown = view.findViewById(R.id.minute_countdown);
        secondCountdown = view.findViewById(R.id.second_countdown);
        recyclerView = view.findViewById(R.id.recyclerView);

        fragmentTitle = view.findViewById(R.id.title_text);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            examId = getArguments().getString(EXAM_ID, "");
        } else {
            return view;
        }
        getData();
        return view;
    }

    private void startCountdown() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateCountdown() {
        Calendar now = Calendar.getInstance();
        long currentTimeInMillis = now.getTimeInMillis();

        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, 24);
        targetTime.set(Calendar.MINUTE, 0);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        if (currentTimeInMillis > targetTime.getTimeInMillis()) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long targetTimeInMillis = targetTime.getTimeInMillis();
        long countdownMillis = targetTimeInMillis - currentTimeInMillis;

        long hours = TimeUnit.MILLISECONDS.toHours(countdownMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(countdownMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(countdownMillis) % 60;


        String textColor = "";
        String backgroundColor = "";

        if (hours < 6) {
            textColor = "#ED5588";
            backgroundColor = "#FBDDE7";
        } else if (hours < 12) {
            textColor = "#FABC4B";
            backgroundColor = "#FEF2DB";
        } else {
            textColor = "#28BC84";
            backgroundColor = "#D4F1E6";
        }

        hourCountdown.setTextColor(Color.parseColor(textColor));
        minuteCountdown.setTextColor(Color.parseColor(textColor));
        secondCountdown.setTextColor(Color.parseColor(textColor));

        hourCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        minuteCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        secondCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));

        hourCountdown.setText(String.format(Locale.ENGLISH, "%02dh", hours));
        minuteCountdown.setText(String.format(Locale.ENGLISH, "%02dm", minutes));
        secondCountdown.setText(String.format(Locale.ENGLISH, "%02ds", seconds));
    }

    private void getData() {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        int batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        int batchAddOnId = MyApplication.getIntData(PreferenceKeys.EXAM_ID.getKey());
        Log.d("examIdPreExam", "batchId=" + batchId + " batchAddOnId=" + batchAddOnId);
        map.put("batchId", batchId);
        map.put("batchAddOnId", batchAddOnId);
        MyJsonObjectRequest getDataRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(AV_CRASH_COURSE, map),
                null,
                response -> {
                    TypeToken<ApiWrappedResponse<CrashCourseResponse>> token = new TypeToken<ApiWrappedResponse<CrashCourseResponse>>() {};
                    ApiWrappedResponse<CrashCourseResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    CrashCourseResponse crashCourseResponse = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    Log.d(TAG, "getData: " + crashCourseResponse);
                    if (crashCourseResponse != null) {
                        onCrashCourseDataLoaded(crashCourseResponse);
                    }
                },
                new VolleyGlobalErrorListener(getContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        CommonMethods.showNegativeSnackbar(getView(), error.getMessage());
                    }
                }
        );
        getDataRequest.setTag(TAG);
        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(getDataRequest);
    }

    private void onCrashCourseDataLoaded(CrashCourseResponse response) {
        TodayClassResponse liveClasses = response.getTodayClasses();
        if (liveClasses != null && !CollectionUtils.isEmpty(liveClasses.getClasses())) {
            itemList.clear();
            if (StringUtils.isNotEmpty(liveClasses.getLayoutTitle())) {
                fragmentTitle.setText(liveClasses.getLayoutTitle());
            }
            itemList.addAll(liveClasses.getClasses());
            halfYearlyHorizontalAdapter = new HalfYearlyHorizontalAdapter(itemList, getActivity(), this);
            recyclerView.setAdapter(halfYearlyHorizontalAdapter);
            halfYearlyHorizontalAdapter.notifyDataSetChanged();
            preExamLayout.setVisibility(View.VISIBLE);
            startCountdown();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        countdownHandler.removeCallbacksAndMessages(null);
        if (getActivity() != null) {
            MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
        }
    }

    @Override
    public void onItemClick(TodayClassItemResponse item) {
        clickedItem = item;
        if (item.getContentType() == HalfYearlyContentType.VIDEO ||
                item.getContentType() == HalfYearlyContentType.MICROLECTURE_VIDEO) {
            playVideo(item);
        } else if (item.getContentType() == HalfYearlyContentType.NOTES) {
            if (StringUtils.isNotEmpty(item.getNotesPdfUrl())) {
                openPdf(item.getNotesPdfUrl(), "lecture.notes", item.getSubject(), item.getTitle());
            }
        }
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item) {
    }

    public void openPdf(String url,String notesType ,String subjectName,String title) {
        try {
            PdfUtils.openPdfViewer(requireContext(), url, title,notesType,subjectName,false, "", "");
//            Intent target = new Intent(Intent.ACTION_VIEW);
//            target.setDataAndType(Uri.parse(url), "application/pdf");
//            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            if (target.resolveActivity(requireActivity().getPackageManager()) != null) {
//                Intent intent = Intent.createChooser(target, "Open File");
//                startActivity(intent);
//            } else {
//                playStoreDialog = new PlayStoreDialog(getActivity());
//                playStoreDialog.showPlayStoreDialog();
//            }
        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    public static void updateStatus(Context context, String routineId,
                                    String routineStatus) {
        Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "api call");

        Map<String, Object> map = new HashMap<>();
        map.put("routineId", routineId);
        map.put("routineStatus", routineStatus);
        map.put("videoProgress", 0);
        MyJsonObjectRequest taskUpdateRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(HALF_YEARLY_USER_TASK_UPDATE, map), null,
                response -> {
                    Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "success");
                    MyApplication.saveData(PreferenceKeys.UPDATE_STATUS.getKey(), true);
                }, new VolleyGlobalErrorListener(context) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        taskUpdateRequest.setTag(TAG);
        MyVolleySingleton.getInstance(context).addToRequestQueue(taskUpdateRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Status update not applicable for new API (no routineId)
    }

    private void playVideo(TodayClassItemResponse item) {
        Intent intent = new Intent(requireActivity(), MicrolectureListActivity.class);
        intent.putExtra("chapterId", item.getChapterCode() != null ? item.getChapterCode() : item.getChapterId());
        intent.putExtra("chapterName", item.getTitle() != null ? item.getTitle() : item.getChapterName());
        intent.putExtra("hideTapTarget", true);
        intent.putExtra("microLectureId", item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
        intent.putExtra("openVideo", true);
        MyApplication.saveData(item.getTitle() + PreferenceKeys.TYPE.getKey(), 0);
        startActivity(intent);
    }
}
