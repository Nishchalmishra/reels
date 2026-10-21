package arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.video_player.MiniVideoPlayer;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.listener.todo.TodoValidateListener;
import arivihan.technologies.doubtbuzzter2.model.player.MiniPlayerPosition;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoStepsConfig;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToDoStepExamSelectionFragment extends Fragment implements TodoValidateListener {

    private static final String TAG = ToDoStepExamSelectionFragment.class.getSimpleName();
    private ConstraintLayout date_layout;
    private TextView tv_date_text;
    private ScrollView scroll_view;
    private Handler handler;
    private Runnable runnable;
    private ToDoExamFragmentListener toDoExamFragmentListener;
    private String startDate;
    private String endDate;
    private int stepCount, stepNumber;
    private String examDate;
    private boolean hideHeader;
    private TextView tv_sub_header, tv_header, tv_exam_date_text;
    private DatePicker date_picker;
    private CardView cv_date_picker;
    private MiniVideoPlayer mini_video_player;
    public MiniVideoPlayerConfig miniVideoPlayerConfig;
    private TodoStepsConfig todoStepsConfig;

    public ToDoStepExamSelectionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        toDoExamFragmentListener = (ToDoExamFragmentListener) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("startDate", startDate);
        outState.putString("endDate", endDate);
        outState.putInt("stepNumber", stepNumber);
        outState.putInt("stepCount", stepCount);
        outState.putString("examDate", examDate);
        outState.putBoolean("hideHeader", hideHeader);
        outState.putParcelable("miniVideo", miniVideoPlayerConfig);
    }

    public static ToDoStepExamSelectionFragment newInstance(String startDate, String endDate, int stepNumber, int stepCount, String examDate, boolean hideHeader, MiniVideoPlayerConfig miniVideoPlayerConfig) {
        ToDoStepExamSelectionFragment fragment = new ToDoStepExamSelectionFragment();
        Bundle args = new Bundle();
        args.putString("startDate", startDate);
        args.putString("endDate", endDate);
        args.putInt("stepNumber", stepNumber);
        args.putInt("stepCount", stepCount);
        args.putString("examDate", examDate);
        args.putBoolean("hideHeader", hideHeader);
        args.putParcelable("miniVideo", miniVideoPlayerConfig);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startDate = getArguments().getString("startDate");
            endDate = getArguments().getString("endDate");
            stepNumber = getArguments().getInt("stepNumber");
            stepCount = getArguments().getInt("stepCount");
            examDate = getArguments().getString("examDate");
            hideHeader = getArguments().getBoolean("hideHeader");
            miniVideoPlayerConfig = getArguments().getParcelable("miniVideo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_step_exam_selection, container, false);

        tv_sub_header = view.findViewById(R.id.tv_sub_header);
        tv_header = view.findViewById(R.id.tv_header);
        tv_exam_date_text = view.findViewById(R.id.tv_exam_date_text);
        scroll_view = view.findViewById(R.id.scroll_view);
        date_layout = view.findViewById(R.id.date_layout);
        tv_date_text = view.findViewById(R.id.tv_date_text);
        date_picker = view.findViewById(R.id.date_picker);
        cv_date_picker = view.findViewById(R.id.cv_date_picker);
        mini_video_player = view.findViewById(R.id.mini_video_player);

        date_layout.setOnClickListener(v -> {
            if (cv_date_picker.getVisibility() == View.GONE) {
                expandView(cv_date_picker);
                startHandler();
            } else {
                collapseView(cv_date_picker);
            }
        });

        setMiniVideoPlayer();
        setDynamicString();
        styleDatePickerHeader(date_picker, ContextCompat.getColor(requireContext(), R.color.black));
        setupNativeDatePicker(date_picker, startDate, endDate, examDate);
//        setStartDate();
//        setEndDate();
        return view;
    }

    private void setMiniVideoPlayer() {
        try {
            if (mini_video_player == null)
                return;

            if (miniVideoPlayerConfig != null && StringUtils.isNotEmpty(miniVideoPlayerConfig.getVideoUrl())) {

                ViewGroup.LayoutParams layoutParams = MiniVideoPlayer.getPositionedLayoutParams((ViewGroup) mini_video_player.getParent(),
                        miniVideoPlayerConfig.getGravity() != null ? miniVideoPlayerConfig.getGravity() : MiniPlayerPosition.BOTTOM_END,
                        CommonMethods.dpToPx(requireContext(), miniVideoPlayerConfig.getMarginStartDp()),
                        CommonMethods.dpToPx(requireContext(), miniVideoPlayerConfig.getMarginTopDp()),
                        CommonMethods.dpToPx(requireContext(), miniVideoPlayerConfig.getMarginEndDp()),
                        CommonMethods.dpToPx(requireContext(), miniVideoPlayerConfig.getMarginBottomDp()));

                mini_video_player.setLayoutParams(layoutParams);

                mini_video_player.playVideo(miniVideoPlayerConfig, TAG);
                mini_video_player.setVisibility(View.VISIBLE);

            } else {
                mini_video_player.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setDynamicString() {
        try {
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_exam_selection_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Gson gson = new Gson();
            todoStepsConfig = gson.fromJson(jsonStr, TodoStepsConfig.class);
            tv_sub_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getSubHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getSubHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getSubHeaderText()) : todoStepsConfig.getSubHeaderText()) : getString(R.string.hamare_sath_quaterly_ki_taiyari_ke_liye_exam_date_batayein)));
            tv_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getHeaderText().replace("{0}", String.format(stepNumber + "/" + stepCount))) : todoStepsConfig.getHeaderText()) : getString(R.string.step_1_to_create_time_table)));
            tv_exam_date_text.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getBodyText()) ? (CommonMethods.isValidJson(todoStepsConfig.getBodyText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getBodyText()) : todoStepsConfig.getBodyText()) : getString(R.string.set_your_quarterly_exam_date)));

            tv_header.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
            tv_date_text.setText(StringUtils.isNotEmpty(examDate) ? examDate : "--/--/----");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupNativeDatePicker(
            DatePicker datePicker,
            String startDateStr,
            String endDateStr,
            String defaultDateStr
    ) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

//            Date parsedStartDate = sdf.parse(startDateStr);
//            Date endDate = sdf.parse(endDateStr);
            Date defaultDate;

            Calendar calendar = Calendar.getInstance();
            Date today = calendar.getTime();

            // start date is not before today
            // Parse or fallback start date
            Date parsedStartDate = null;
            if (!StringUtils.isBlank(startDateStr)) {
                parsedStartDate = sdf.parse(startDateStr);
            }
            Date startDate = (parsedStartDate == null || parsedStartDate.before(today)) ? today : parsedStartDate;

            Date endDate;
            if (!StringUtils.isBlank(endDateStr)) {
                endDate = sdf.parse(endDateStr);
            } else {
                Calendar endCal = Calendar.getInstance();
                endCal.add(Calendar.DAY_OF_YEAR, 15);
                endDate = endCal.getTime();
            }

            if (!StringUtils.isBlank(defaultDateStr)) {
                defaultDate = sdf.parse(defaultDateStr);
            } else {
                defaultDate = startDate;
            }

            examDate = sdf.format(defaultDate);
            tv_date_text.setText(examDate);
            toDoExamFragmentListener.onExamDateChange(examDate);

            // limit range
            datePicker.setMinDate(startDate.getTime());
            datePicker.setMaxDate(endDate.getTime());

            // default date
            calendar.setTime(defaultDate);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePicker.init(year, month, day, (view, selectedYear, selectedMonth, selectedDay) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(selectedYear, selectedMonth, selectedDay);
                examDate = sdf.format(selectedCal.getTime());
                toDoExamFragmentListener.onExamDateChange(examDate);
                tv_date_text.setText(examDate);
            });

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void expandView(View view) {
        view.setVisibility(View.VISIBLE);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        ValueAnimator animator = ValueAnimator.ofInt(0, view.getMeasuredHeight());
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    // Method to collapse the view
    private void collapseView(View view) {
        int initialHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
    }

    private void startHandler() {
        handler = new Handler();
        runnable = () -> scroll_view.fullScroll(View.FOCUS_DOWN);
        handler.postDelayed(runnable, 300);
    }

    private String format(int year, int month, int day) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return dateFormat.format(selectedCalendar.getTime());
    }

    private Calendar convertStringToCalendar(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = dateFormat.parse(dateString);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private void styleDatePickerHeader(DatePicker datePicker, int color) {
        try {
            ViewGroup root = (ViewGroup) datePicker.getChildAt(0);
            if (root != null) {
                List<TextView> headerTexts = new ArrayList<>();
                findHeaderTextViews(root, headerTexts);

                for (TextView headerText : headerTexts) {
//                    String text = headerText.getText().toString();
//                    if (isHeaderText(text)) {
//                        headerText.setTextColor(color);
//                    }
                    headerText.setTextColor(color);
                }
            }
        } catch (Exception e) {
            Log.e("DatePickerHeader", "Error styling header: " + e.getMessage());
        }
    }

    private void findHeaderTextViews(ViewGroup viewGroup, List<TextView> outList) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                outList.add((TextView) child);
            } else if (child instanceof ViewGroup) {
                findHeaderTextViews((ViewGroup) child, outList);
            }
        }
    }

    private boolean isHeaderText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        text = text
                .replaceAll("[\\p{Punct}॰।]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Year
        if (text.matches("^\\d{4}$")) return true;

        // Date
        if (text.matches("^[A-Za-z]{3,9},?\\s+\\d{1,2}\\s?[A-Za-z]{3,9}?$")) return true;
        return text.matches("^[A-Za-z]{3,9},?\\s+[A-Za-z]{3,9}\\s+\\d{1,2}$");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mini_video_player != null) {
            mini_video_player.resumeVideo();
        }
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onPause();
        if (mini_video_player != null) {
            mini_video_player.pauseVideo();
        }
    }

    @Override
    public boolean validateTodoSelection() {
        return true;
    }

    public interface ToDoExamFragmentListener {
        void onExamDateChange(String date);
    }
}