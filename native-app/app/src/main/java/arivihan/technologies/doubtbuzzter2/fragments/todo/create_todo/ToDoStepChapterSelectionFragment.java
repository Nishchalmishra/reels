package arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ToDoChapterSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.video_player.MiniVideoPlayer;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.listener.todo.TodoValidateListener;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;
import arivihan.technologies.doubtbuzzter2.model.player.MiniPlayerPosition;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoStepsConfig;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ToDoStepChapterSelectionFragment extends Fragment implements ToDoChapterSelectionAdapter.ToDoChapterSelectionListener, TodoValidateListener {

    private static final String TAG = ToDoStepChapterSelectionFragment.class.getSimpleName();
    private RelativeLayout rl_parent_layout;
    private RecyclerView rv_chapter_selection;
    private ToDoChapterSelectionAdapter toDoChapterSelectionAdapter;
    private ArrayList<UserSyllabusOrder> userSyllabusOrders = new ArrayList<>();
    private MiniVideoPlayerConfig miniVideoPlayerConfig;
    private String subject;
    private TextView tv_header, tv_sub_header, tv_body;
    private MiniVideoPlayer mini_video_player;
    private ThreeFragmentListener threeFragmentListener;
    private int stepNumber;
    private int stepCount;
    private Handler animHandler;
    private Runnable animRunnable;
    private CardView cv_chapter_selection, cv_scroll_toggle;
    private TextView tv_scroll_toggle;
    private ImageView iv_scroll_toggle;

    // Data variables
    private TodoStepsConfig todoStepsConfig;
    private boolean scrolledToBottom = false;

    public ToDoStepChapterSelectionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        threeFragmentListener = (ThreeFragmentListener) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("userSyllabusOrders", userSyllabusOrders);
        outState.putString("subject", subject);
        outState.putInt("stepNumber", stepNumber);
        outState.putInt("stepCount", stepCount);
        outState.putParcelable("miniVideo", miniVideoPlayerConfig);
    }

    public static ToDoStepChapterSelectionFragment newInstance(ArrayList<UserSyllabusOrder> userSyllabusOrders, String subject,
                                                               int stepNumber, int stepCount, MiniVideoPlayerConfig miniVideoPlayerConfig) {
        ToDoStepChapterSelectionFragment fragment = new ToDoStepChapterSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("userSyllabusOrders", userSyllabusOrders);
        args.putString("subject", subject);
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
            userSyllabusOrders = getArguments().getParcelableArrayList("userSyllabusOrders");
            subject = getArguments().getString("subject");
            stepNumber = getArguments().getInt("stepNumber");
            stepCount = getArguments().getInt("stepCount");
            miniVideoPlayerConfig = getArguments().getParcelable("miniVideo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_step_chapter_selection, container, false);
        rl_parent_layout = view.findViewById(R.id.rl_parent_layout);
        tv_header = view.findViewById(R.id.tv_header);
        tv_sub_header = view.findViewById(R.id.tv_sub_header);
        tv_body = view.findViewById(R.id.tv_body);
        cv_chapter_selection = view.findViewById(R.id.cv_chapter_selection);
        rv_chapter_selection = view.findViewById(R.id.rv_chapter_selection);
        cv_scroll_toggle = view.findViewById(R.id.cv_scroll_toggle);
        tv_scroll_toggle = view.findViewById(R.id.tv_scroll_toggle);
        iv_scroll_toggle = view.findViewById(R.id.iv_scroll_toggle);
        mini_video_player = view.findViewById(R.id.mini_video_player);
        try {
            setMiniVideoPlayer();
            setDynamicString();
            rearrangeOrder();
            startAnimation();
            startAnimationHandler();
        } catch (Exception ignored) {
        }
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
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_chapter_selection_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Gson gson = new Gson();
            todoStepsConfig = gson.fromJson(jsonStr, TodoStepsConfig.class);
            tv_sub_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getSubHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getSubHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getSubHeaderText().replace("{0}", StringUtils.isNotEmpty(subject) ? subject : "")) : todoStepsConfig.getSubHeaderText()) : getString(R.string.aap_hamse_jo_subject_padna_chahte_he_unhe_tick_kare)));
            tv_body.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getBodyText()) ? (CommonMethods.isValidJson(todoStepsConfig.getBodyText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getBodyText()) : todoStepsConfig.getBodyText()) : getString(R.string.jo_subject_nahi_chaiye_unka_tick_hataye)));
            tv_header.setText((todoStepsConfig != null && StringUtils.isNotEmpty(todoStepsConfig.getHeaderText()) ? (CommonMethods.isValidJson(todoStepsConfig.getHeaderText()) ? StyledTextBuilder.fromJson(requireContext(), todoStepsConfig.getHeaderText().replace("{0}", String.format(stepNumber + "/" + stepCount))) : todoStepsConfig.getHeaderText()) : getString(R.string.step_1_to_create_time_table)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        tv_header.startAnimation(fadeInFromRight);
        tv_sub_header.startAnimation(fadeInFromRight);
        tv_body.startAnimation(fadeInFromRight);
    }

    private void startAnimationHandler() {
        animHandler = new Handler();
        animRunnable = this::startRecyclerAnimation;
        animHandler.postDelayed(animRunnable, 200);
    }

    private void startRecyclerAnimation() {
        final Animation fadeInFromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_in);
        cv_chapter_selection.startAnimation(fadeInFromBottom);
        rv_chapter_selection.startAnimation(fadeInFromBottom);
    }

    private void rearrangeOrder() {

        toDoChapterSelectionAdapter = new ToDoChapterSelectionAdapter(getContext(), userSyllabusOrders, StringUtils.isNotEmpty(todoStepsConfig.getChapterCheckedText()) ? todoStepsConfig.getChapterCheckedText() : getString(R.string.todo_chapter_padhna_hain), StringUtils.isNotEmpty(todoStepsConfig.getChapterUnCheckedText()) ? todoStepsConfig.getChapterUnCheckedText() : getString(R.string.todo_chapter_nahi_padhna_hain), this);
        rv_chapter_selection.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        rv_chapter_selection.setAdapter(toDoChapterSelectionAdapter);

        cv_scroll_toggle.setOnClickListener(v -> {
            if (!scrolledToBottom) {
                rv_chapter_selection.smoothScrollToPosition(toDoChapterSelectionAdapter.getItemCount() - 1);
            } else {
                rv_chapter_selection.smoothScrollToPosition(0);
            }
        });

        rv_chapter_selection.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int first = lm.findFirstCompletelyVisibleItemPosition();
                    int last = lm.findLastCompletelyVisibleItemPosition();
                    int total = toDoChapterSelectionAdapter.getItemCount() - 1;

                    // Check if fully at bottom
                    if (last >= total) {
                        scrolledToBottom = true;
                        iv_scroll_toggle.setImageResource(R.drawable.baseline_arrow_upward_24);
                        tv_scroll_toggle.setText(R.string.uper_jayein);
                    }
                    // Check if fully at top
                    else if (first == 0) {
                        scrolledToBottom = false;
                        iv_scroll_toggle.setImageResource(R.drawable.baseline_arrow_downward_24);
                        tv_scroll_toggle.setText(R.string.neeche_jayein);
                    }
                }
            }
        });

        rv_chapter_selection.getViewTreeObserver().addOnGlobalLayoutListener(this::updateScrollToggleVisibility);
    }

    private void updateScrollToggleVisibility() {
        boolean canScrollUp = rv_chapter_selection.canScrollVertically(-1);
        boolean canScrollDown = rv_chapter_selection.canScrollVertically(1);
        boolean shouldShow = canScrollUp || canScrollDown;

        cv_scroll_toggle.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    public int getEnabledChapterCount() {
        int count = 0;
        for (UserSyllabusOrder userSyllabusOrder : userSyllabusOrders) {
            if (userSyllabusOrder.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    private int getEnabledSelectedChapterCount() {
        int count = 0;
        for (UserSyllabusOrder userSyllabusOrder : userSyllabusOrders) {
            if (userSyllabusOrder.isEnabled()) {
                if (userSyllabusOrder.isSelected()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void onChapterListChange(ArrayList<UserSyllabusOrder> userSyllabusOrders) {
        threeFragmentListener.onChapterListChange(userSyllabusOrders);
    }

    @Override
    public void onChapterSelection(int position, boolean isSelected) {
        try {
            UserSyllabusOrder userSyllabusOrder = userSyllabusOrders.get(position);
            if (getEnabledSelectedChapterCount() > 2 || !isSelected) {
                // condition select at least one subject
                userSyllabusOrder.setSelected(!userSyllabusOrder.isSelected());
                userSyllabusOrders.set(position, userSyllabusOrder);
                toDoChapterSelectionAdapter.notifyItemChanged(position);
                threeFragmentListener.onChapterListChange(userSyllabusOrders);
            } else {
                String message = getString(R.string.please_do_chapter_select_karna_zaroori_he);
                if (getEnabledChapterCount() <= 1)
                    message = getString(R.string.please_chapter_select_karna_zaroori_he);
                CommonMethods.showNegativeSnackbar(rl_parent_layout, message, 3000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean validateTodoSelection() {
        int enabledCount = getEnabledChapterCount();
        int selectedCount = getEnabledSelectedChapterCount();

        if (enabledCount > 1) {
            return selectedCount > 1;
        } else {
            return (enabledCount == 1 && selectedCount == 1) || enabledCount == 0;
        }
    }

    public interface ThreeFragmentListener {
        void onChapterListChange(ArrayList<UserSyllabusOrder> userSyllabusOrders);
    }

}