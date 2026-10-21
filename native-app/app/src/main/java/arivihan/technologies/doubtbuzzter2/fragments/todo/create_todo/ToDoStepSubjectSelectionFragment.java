package arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ToDoSubjectSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.video_player.MiniVideoPlayer;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.listener.todo.TodoValidateListener;
import arivihan.technologies.doubtbuzzter2.model.player.MiniPlayerPosition;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;
import arivihan.technologies.doubtbuzzter2.model.ToDoSubjectSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoStepsConfig;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToDoStepSubjectSelectionFragment extends Fragment implements ToDoSubjectSelectionAdapter.OnSelectionClickListener, TodoValidateListener {

    private static final String TAG = ToDoStepSubjectSelectionFragment.class.getSimpleName();
    private LottieAnimationView step_animation;
    private ConstraintLayout parent_layout;
    public ArrayList<ToDoSubjectSelectionModel> subjectModelList = new ArrayList<>();
    public MiniVideoPlayerConfig miniVideoPlayerConfig;
    private ToDoSubjectSelectionAdapter toDoSubjectSelectionAdapter;
    private RecyclerView subject_selection_rv;
    private LinearLayout step_number_layout, top_layout, ll_content;
    private MiniVideoPlayer mini_video_player;

    private TextView tv_header, tv_sub_header, tv_body;
    private Handler animHandler;
    private Runnable animRunnable;
    private OneFragmentListener oneFragmentListener;
    private TodoStepsConfig todoStepsConfig;

    public ToDoStepSubjectSelectionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        oneFragmentListener = (OneFragmentListener) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("subjectModelList", subjectModelList);
        outState.putParcelable("miniVideo", miniVideoPlayerConfig);
    }

    public static ToDoStepSubjectSelectionFragment newInstance(ArrayList<ToDoSubjectSelectionModel> subjectModelList, MiniVideoPlayerConfig miniVideoPlayerConfig) {
        ToDoStepSubjectSelectionFragment fragment = new ToDoStepSubjectSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("subjectModelList", subjectModelList);
        args.putParcelable("miniVideo", miniVideoPlayerConfig);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectModelList = getArguments().getParcelableArrayList("subjectModelList");
            miniVideoPlayerConfig = getArguments().getParcelable("miniVideo");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_step_subject_selection, container, false);
        parent_layout = view.findViewById(R.id.parent_layout);
        top_layout = view.findViewById(R.id.top_layout);
        ll_content = view.findViewById(R.id.ll_content);
        tv_header = view.findViewById(R.id.tv_header);
        tv_sub_header = view.findViewById(R.id.tv_sub_header);
        tv_body = view.findViewById(R.id.tv_body);
        step_number_layout = view.findViewById(R.id.step_number_layout);
        subject_selection_rv = view.findViewById(R.id.subject_selection_rv);
        step_animation = view.findViewById(R.id.step_animation);
        mini_video_player = view.findViewById(R.id.mini_video_player);
        step_animation.playAnimation();
        step_animation.loop(true);

        setMiniVideoPlayer();
        setDynamicString();
        initializeSubjectSelectionRecycler();
        startAnimation();
        startAnimationHandler();

        return view;
    }

    private void setMiniVideoPlayer() {
        try {
//            String videoUrl = "https://d3dkidc6rv57u9.cloudfront.net/Reels/how_to_home_video_English/App+Guidance+Final+Export.m3u8";
            if(mini_video_player==null)
                return;

            if(miniVideoPlayerConfig!=null && StringUtils.isNotEmpty(miniVideoPlayerConfig.getVideoUrl())) {

                ViewGroup.LayoutParams layoutParams = MiniVideoPlayer.getPositionedLayoutParams((ViewGroup)mini_video_player.getParent(),
                        miniVideoPlayerConfig.getGravity()!=null?miniVideoPlayerConfig.getGravity():MiniPlayerPosition.BOTTOM_END,
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
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_subject_selection_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Gson gson = new Gson();
            todoStepsConfig = gson.fromJson(jsonStr, TodoStepsConfig.class);
            tv_sub_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getSubHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getSubHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getSubHeaderText()) : todoStepsConfig.getSubHeaderText()) : getString(R.string.aap_hamse_jo_subject_padna_chahte_he_unhe_tick_kare)));
            tv_body.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getBodyText()) ? (CommonMethods.isValidJson(todoStepsConfig.getBodyText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getBodyText()) : todoStepsConfig.getBodyText()) : getString(R.string.jo_subject_nahi_chaiye_unka_tick_hataye)));
            tv_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getHeaderText().replace("{0}", "" + 1)) : todoStepsConfig.getHeaderText()) : getString(R.string.step_1_to_create_time_table)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        step_number_layout.startAnimation(fadeInFromRight);
        tv_sub_header.startAnimation(fadeInFromRight);
        step_animation.startAnimation(fadeInFromRight);
        tv_body.startAnimation(fadeInFromRight);
    }

    private void startAnimationHandler() {
        animHandler = new Handler();
        animRunnable = this::startRecyclerAnimation;
        animHandler.postDelayed(animRunnable, 200);
    }

    private void startRecyclerAnimation() {
        if (getContext() != null && isAdded()) {
            final Animation fadeInFromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_in);
            subject_selection_rv.startAnimation(fadeInFromBottom);
        }
    }

    private void initializeSubjectSelectionRecycler() {
        try {
            toDoSubjectSelectionAdapter = new ToDoSubjectSelectionAdapter(requireActivity(), subjectModelList, this);
            subject_selection_rv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            subject_selection_rv.setAdapter(toDoSubjectSelectionAdapter);

        } catch (Exception ignored) {
        }
    }

    private int getSelectedSubjectCount() {
        int count = 0;
        for (ToDoSubjectSelectionModel toDoSubjectSelectionModel : subjectModelList) {
            if (toDoSubjectSelectionModel.isSelected())
                count++;
        }
        return count;
    }

    @Override
    public void onSelectionChange(int position, boolean selected) {
        ToDoSubjectSelectionModel subjectModel = subjectModelList.get(position);
        if (getSelectedSubjectCount() > 1 || !selected) {
            // condition select at least one subject
            subjectModel.setSelected(!subjectModel.isSelected());
            subjectModelList.set(position, subjectModel);
            toDoSubjectSelectionAdapter.notifyItemChanged(position);
            oneFragmentListener.onSubjectListChange(subjectModelList);
        } else {
            CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.please_ek_subject_select_karna_zaroori_he), 3000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        step_animation.playAnimation();
        step_animation.loop(true);
        if (mini_video_player != null) {
            mini_video_player.resumeVideo();
        }
    }

    @Override
    public void onPause() {
        if (animHandler != null) {
            animHandler.removeCallbacksAndMessages(null);
        }
        step_animation.cancelAnimation();
        super.onPause();

        if (mini_video_player != null) {
            mini_video_player.pauseVideo();
        }
    }

    @Override
    public boolean validateTodoSelection() {
        return getSelectedSubjectCount()>0;
    }

    public interface OneFragmentListener {
        void onSubjectListChange(ArrayList<ToDoSubjectSelectionModel> subjectModelList);
    }
}