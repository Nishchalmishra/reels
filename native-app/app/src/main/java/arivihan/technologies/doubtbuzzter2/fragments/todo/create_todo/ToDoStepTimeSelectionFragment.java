package arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

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

public class ToDoStepTimeSelectionFragment extends Fragment implements TodoValidateListener {

    private static final String TAG = ToDoStepTimeSelectionFragment.class.getSimpleName();

    private ToDoTimeFragmentListener toDoTimeFragmentListener;

    private int stepCount, stepNumber;
    private NestedScrollView scroll_view;
    private TextView tv_sub_header, tv_header, tv_text1, tv_text2, tv_text3, tv_text4, tv_text5;
    private CardView cv_morning, cv_night;
    private ImageView iv_morning, iv_night;
    private TextView tv_morning, tv_night;
    private LinearLayout ll_morning_set_start_time, ll_morning_selected_start_time, ll_morning_set_end_time, ll_morning_selected_end_time;
    private LinearLayout ll_afternoon_set_start_time, ll_afternoon_selected_start_time, ll_afternoon_set_end_time, ll_afternoon_selected_end_time;
    private LinearLayout ll_evening_set_start_time, ll_evening_selected_start_time, ll_evening_set_end_time, ll_evening_selected_end_time;
    private LinearLayout ll_night_set_start_time, ll_night_selected_start_time, ll_night_set_end_time, ll_night_selected_end_time;
    private TextView tv_morning_selected_start_time, tv_morning_selected_end_time, tv_afternoon_selected_start_time, tv_afternoon_selected_end_time, tv_evening_selected_start_time, tv_evening_selected_end_time, tv_night_selected_start_time, tv_night_selected_end_time;
    private MiniVideoPlayer mini_video_player;
    public MiniVideoPlayerConfig miniVideoPlayerConfig;
    private TodoStepsConfig todoStepsConfig;
    private boolean isMorningModeSelected = false;

    public ToDoStepTimeSelectionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        toDoTimeFragmentListener = (ToDoStepTimeSelectionFragment.ToDoTimeFragmentListener) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stepNumber", stepNumber);
        outState.putInt("stepCount", stepCount);
        outState.putParcelable("miniVideo", miniVideoPlayerConfig);
    }

    public static ToDoStepTimeSelectionFragment newInstance(int stepNumber, int stepCount, MiniVideoPlayerConfig miniVideoPlayerConfig) {
        ToDoStepTimeSelectionFragment fragment = new ToDoStepTimeSelectionFragment();
        Bundle args = new Bundle();
        args.putInt("stepNumber", stepNumber);
        args.putInt("stepCount", stepCount);
        args.putParcelable("miniVideo", miniVideoPlayerConfig);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepNumber = getArguments().getInt("stepNumber");
            stepCount = getArguments().getInt("stepCount");
            miniVideoPlayerConfig = getArguments().getParcelable("miniVideo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_step_time_selection, container, false);

        scroll_view = view.findViewById(R.id.scroll_view);
        tv_sub_header = view.findViewById(R.id.tv_sub_header);
        tv_header = view.findViewById(R.id.tv_header);
        mini_video_player = view.findViewById(R.id.mini_video_player);

        tv_text1 = view.findViewById(R.id.tv_text1);
        tv_text2 = view.findViewById(R.id.tv_text2);
        tv_text3 = view.findViewById(R.id.tv_text3);
        tv_text4 = view.findViewById(R.id.tv_text4);
        tv_text5 = view.findViewById(R.id.tv_text5);

        cv_morning = view.findViewById(R.id.cv_morning);
        cv_night = view.findViewById(R.id.cv_night);
        iv_morning = view.findViewById(R.id.iv_morning);
        iv_night = view.findViewById(R.id.iv_night);
        tv_morning = view.findViewById(R.id.tv_morning);
        tv_night = view.findViewById(R.id.tv_night);

        ll_morning_set_start_time = view.findViewById(R.id.ll_morning_set_start_time);
        ll_morning_selected_start_time = view.findViewById(R.id.ll_morning_selected_start_time);
        ll_morning_set_end_time = view.findViewById(R.id.ll_morning_set_end_time);
        ll_morning_selected_end_time = view.findViewById(R.id.ll_morning_selected_end_time);

        ll_afternoon_set_start_time = view.findViewById(R.id.ll_afternoon_set_start_time);
        ll_afternoon_selected_start_time = view.findViewById(R.id.ll_afternoon_selected_start_time);
        ll_afternoon_set_end_time = view.findViewById(R.id.ll_afternoon_set_end_time);
        ll_afternoon_selected_end_time = view.findViewById(R.id.ll_afternoon_selected_end_time);

        ll_evening_set_start_time = view.findViewById(R.id.ll_evening_set_start_time);
        ll_evening_selected_start_time = view.findViewById(R.id.ll_evening_selected_start_time);
        ll_evening_set_end_time = view.findViewById(R.id.ll_evening_set_end_time);
        ll_evening_selected_end_time = view.findViewById(R.id.ll_evening_selected_end_time);

        ll_night_set_start_time = view.findViewById(R.id.ll_night_set_start_time);
        ll_night_selected_start_time = view.findViewById(R.id.ll_night_selected_start_time);
        ll_night_set_end_time = view.findViewById(R.id.ll_night_set_end_time);
        ll_night_selected_end_time = view.findViewById(R.id.ll_night_selected_end_time);

        tv_morning_selected_start_time = view.findViewById(R.id.tv_morning_selected_start_time);
        tv_morning_selected_end_time = view.findViewById(R.id.tv_morning_selected_end_time);
        tv_afternoon_selected_start_time = view.findViewById(R.id.tv_afternoon_selected_start_time);
        tv_afternoon_selected_end_time = view.findViewById(R.id.tv_afternoon_selected_end_time);
        tv_evening_selected_start_time = view.findViewById(R.id.tv_evening_selected_start_time);
        tv_evening_selected_end_time = view.findViewById(R.id.tv_evening_selected_end_time);
        tv_night_selected_start_time = view.findViewById(R.id.tv_night_selected_start_time);
        tv_night_selected_end_time = view.findViewById(R.id.tv_night_selected_end_time);

        setMiniVideoPlayer();
        setDynamicString();
        toggleStudyModeSelection(isMorningModeSelected);
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
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_time_selection_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Gson gson = new Gson();
            todoStepsConfig = gson.fromJson(jsonStr, TodoStepsConfig.class);
            tv_sub_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getSubHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getSubHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getSubHeaderText()) : todoStepsConfig.getSubHeaderText()) : getString(R.string.aur_behtar_timetable_ke_liye_humein_batayein)));
            tv_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getHeaderText().replace("{0}", String.format(stepNumber + "/" + stepCount))) : todoStepsConfig.getHeaderText()) : getString(R.string.step_1_to_create_time_table)));

            tv_text1.setText((todoStepsConfig!=null && StringUtils.isNotEmpty(todoStepsConfig.getModeText()) ? (CommonMethods.isValidJson(todoStepsConfig.getModeText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getModeText()) : todoStepsConfig.getModeText()) : getString(R.string.kab_padhna_pasand_hain)));
            tv_text2.setText((todoStepsConfig!=null && StringUtils.isNotEmpty(todoStepsConfig.getMorningTimeText()) ? (CommonMethods.isValidJson(todoStepsConfig.getMorningTimeText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getMorningTimeText()) : todoStepsConfig.getMorningTimeText()) : getString(R.string.ki_aap_morning_mein_kab_se_kab_tak_padhte_hein)));
            tv_text3.setText((todoStepsConfig!=null && StringUtils.isNotEmpty(todoStepsConfig.getAfternoonTimeText()) ? (CommonMethods.isValidJson(todoStepsConfig.getAfternoonTimeText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getAfternoonTimeText()) : todoStepsConfig.getAfternoonTimeText()) : getString(R.string.agar_aap_afternoon_mein_padte_hein)));
            tv_text4.setText((todoStepsConfig!=null && StringUtils.isNotEmpty(todoStepsConfig.getEveningTimeText()) ? (CommonMethods.isValidJson(todoStepsConfig.getEveningTimeText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getEveningTimeText()) : todoStepsConfig.getEveningTimeText()) : getString(R.string.agar_aap_evening_mein_padte_hein)));
            tv_text5.setText((todoStepsConfig!=null && StringUtils.isNotEmpty(todoStepsConfig.getNightTimeText()) ? (CommonMethods.isValidJson(todoStepsConfig.getNightTimeText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getNightTimeText()) : todoStepsConfig.getNightTimeText()) : getString(R.string.aap_night_me_kab_tak_padhte_ho)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void toggleStudyModeSelection(boolean isMorningMode) {

        cv_morning.setCardBackgroundColor(ContextCompat.getColor(requireContext(), isMorningMode?R.color.arivihan_secondary_color:R.color.color_transparent));
        tv_morning.setTextColor(ContextCompat.getColor(requireContext(), isMorningMode?R.color.white : R.color.arivihan_secondary_color));

        cv_night.setCardBackgroundColor(ContextCompat.getColor(requireContext(), isMorningMode?R.color.color_transparent:R.color.arivihan_secondary_color));
        tv_night.setTextColor(ContextCompat.getColor(requireContext(), isMorningMode?R.color.arivihan_secondary_color:R.color.white));

        ColorStateList morningTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), isMorningMode?R.color.white:R.color.arivihan_secondary_color));
        ImageViewCompat.setImageTintList(iv_morning, morningTint);

        ColorStateList nightTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), isMorningMode?R.color.arivihan_secondary_color:R.color.white));
        ImageViewCompat.setImageTintList(iv_morning, nightTint);

    }

    @Override
    public boolean validateTodoSelection() {
        return false;
    }

    public interface ToDoTimeFragmentListener {
        void onStudyModeChange(String selected);
        void onMorningTimeChange(String startTime, String endTime);
        void onAfternoonTimeChange(String startTime, String endTime);
        void onEveningTimeChange(String startTime, String endTime);
        void onNightTimeChange(String startTime, String endTime);
    }
}