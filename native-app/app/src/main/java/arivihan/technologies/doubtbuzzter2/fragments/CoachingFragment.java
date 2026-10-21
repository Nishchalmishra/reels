package arivihan.technologies.doubtbuzzter2.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CoachingAdapter;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CoachingFragment extends Fragment implements CoachingAdapter.CoachingClickListener {

    private static final String ARG_EXAM_LIST = "exam_list";
    private static final String ARG_SELECTED_COACHING_ID = "selected_coaching_id";

    LottieAnimationView animationView;
    private RecyclerView coachingRecyclerView;
    private CoachingAdapter coachingAdapter;
    private CoachingClickListener coachingClickListener;
    private ArrayList<NameIdModel> coachingList = new ArrayList<>();
    private String selectedCoachingId;
    TextView step_text_1;
    CardView card_layout;

    public static CoachingFragment newInstance(ArrayList<NameIdModel> examList, String selectedCoachingId) {
        CoachingFragment fragment = new CoachingFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_EXAM_LIST, examList);
        args.putString(ARG_SELECTED_COACHING_ID, selectedCoachingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            coachingList = getArguments().getParcelableArrayList(ARG_EXAM_LIST);
            selectedCoachingId = getArguments().getString(ARG_SELECTED_COACHING_ID);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CoachingClickListener) {
            coachingClickListener = (CoachingClickListener) context;
        } else {
            throw new RuntimeException(context + " must implement CoachingClickListenerFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coaching, container, false);
        initViews(view);
        setFragmentText();
        setupRecyclerView();

        if (CollectionUtils.isEmpty(coachingList)) {
            fetchCoaching();
        } else {
            coachingAdapter.updateCoachings(coachingList, selectedCoachingId);
        }
        startAnimation();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void setFragmentText(){
        step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "CoachingFragment"));
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);
    }

    private void initViews(View view) {
        card_layout = view.findViewById(R.id.card_layout);
        coachingRecyclerView = view.findViewById(R.id.coaching_selection_rv);
        step_text_1 = view.findViewById(R.id.step_text_1);
        animationView = view.findViewById(R.id.animation);
        animationView.setSpeed(2);
    }

    private void fetchCoaching() {

        String coachingListJson = MyApplication.getFirebaseRemoteConfig().getString(getString(R.string.coaching_list) + "_" +
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Gson gson = new Gson();
        TypeToken<List<NameIdModel>> token = new TypeToken<>() {};
        coachingList.clear();
        coachingList = gson.fromJson(coachingListJson, token.getType());
        if (StringUtils.isNotEmpty(selectedCoachingId)) {
            for (NameIdModel nameIdModel : coachingList) {
                if (nameIdModel.getId().equalsIgnoreCase(selectedCoachingId)) {
                    nameIdModel.setSelected(true);
                }
            }
        }
        coachingAdapter.updateCoachings(coachingList, selectedCoachingId);
    }

    private void setupRecyclerView() {
        // Find the selected position based on course ID
        int selectedPosition = -1;
        if (selectedCoachingId != null) {
            for (int i = 0; i < coachingList.size(); i++) {
                if (coachingList.get(i).getId().equals(selectedCoachingId)) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        coachingAdapter = new CoachingAdapter(getContext(), coachingList, selectedCoachingId, this);
        coachingRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2, GridLayoutManager.VERTICAL, false));
        coachingRecyclerView.setAdapter(coachingAdapter);
    }

    @Override
    public void onCoachingClick(NameIdModel nameIdModel, int position) {
        selectedCoachingId = nameIdModel.getId(); // Update selected course ID
        coachingClickListener.onCoachingClicked(nameIdModel, position, coachingList);
    }

    public interface CoachingClickListener {
        void onCoachingClicked(NameIdModel nameIdModel,
                         int position, ArrayList<NameIdModel> examList);
    }
}
