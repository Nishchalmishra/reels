package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SECONDARY_COURSES_LIST;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ExamAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ExamFragment extends Fragment implements ExamAdapter.ExamClickListener {

    private static final String ARG_EXAM_LIST = "exam_list";
    private static final String ARG_SELECTED_COURSE_ID = PreferenceKeys.SELECTED_COURSE_ID.getKey();

    private ShimmerFrameLayout shimmerLayout;
    LottieAnimationView animationView;
    private ConstraintLayout contentLayout;
    private RecyclerView examsRecyclerView;
    private ExamAdapter examAdapter;
    private ExamClickListenerFromFragment examClickListener;

    private ArrayList<NameIdModel> examList = new ArrayList<>();
    private String selectedCourseId;
    TextView step_text_1;
    CardView card_layout;

    public static ExamFragment newInstance(ArrayList<NameIdModel> examList, String selectedCourseId) {
        ExamFragment fragment = new ExamFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_EXAM_LIST, examList);
        args.putString(ARG_SELECTED_COURSE_ID, selectedCourseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examList = getArguments().getParcelableArrayList(ARG_EXAM_LIST);
            selectedCourseId = getArguments().getString(ARG_SELECTED_COURSE_ID);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ExamClickListenerFromFragment) {
            examClickListener = (ExamClickListenerFromFragment) context;
        } else {
            throw new RuntimeException(context + " must implement CoachingClickListenerFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam, container, false);
        initViews(view);
        setFragmentText();
        setupRecyclerView();

        if (CollectionUtils.isEmpty(examList)) {
            selectedCourseId = null;
            showShimmer();
            fetchExams();
        } else {
            hideShimmer();
            examAdapter.updateExams(examList, selectedCourseId);
        }
        startAnimation();
        return view;
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);
    }

    @SuppressLint("SetTextI18n")
    private void setFragmentText() {
        step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "ExamFragment"));
    }

    private void initViews(View view) {
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        card_layout = view.findViewById(R.id.card_layout);
        contentLayout = view.findViewById(R.id.content_layout);
        examsRecyclerView = view.findViewById(R.id.exam_selection_rv);
        step_text_1 = view.findViewById(R.id.step_text_1);
        animationView = view.findViewById(R.id.animation);
        animationView.setSpeed(2);
    }


    private void showShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        contentLayout.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.setVisibility(View.GONE);
        shimmerLayout.stopShimmer();
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void fetchExams() {
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.CLASS_ID.getKey(), " ");

        MyJsonArrayRequest request = new MyJsonArrayRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(SECONDARY_COURSES_LIST, map),
                null,
                response -> {
                    TypeToken<List<NameIdModel>> token = new TypeToken<>() {
                    };
                    examList.addAll(new Gson().fromJson(response.toString(), token.getType()));
                    hideShimmer();
                    examAdapter.updateExams(examList, selectedCourseId);
                },
                error -> {
                    String message = error.getMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = getString(R.string.something_went_wrong);
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    hideShimmer();
                }
        );

        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void setupRecyclerView() {
        // Find the selected position based on course ID
        int selectedPosition = -1;
        if (selectedCourseId != null) {
            for (int i = 0; i < examList.size(); i++) {
                if (examList.get(i).getId().equals(selectedCourseId)) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        examAdapter = new ExamAdapter(getContext(), examList, selectedCourseId, this);
        examsRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2, GridLayoutManager.VERTICAL, false));
        examsRecyclerView.setAdapter(examAdapter);
    }

    @Override
    public void onExamClick(NameIdModel nameIdModel, int position) {
        selectedCourseId = nameIdModel.getId(); // Update selected course ID
        examClickListener.onExamClick(nameIdModel, position, examList);
    }

    public interface ExamClickListenerFromFragment {
        void onExamClick(NameIdModel nameIdModel,
                         int position, ArrayList<NameIdModel> examList);
    }
}
