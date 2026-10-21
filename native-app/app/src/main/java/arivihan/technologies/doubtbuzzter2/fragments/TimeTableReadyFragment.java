package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;

public class TimeTableReadyFragment extends Fragment {
    private LottieAnimationView lottieAnimationView;
    private CardView got_to_routine;
    private GoToRoutineListener goToRoutineListener;
    private TextView subject_text;
    private TextView name_text;
    private Handler handler;
    private Runnable runnable;

    public TimeTableReadyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        goToRoutineListener= (GoToRoutineListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_time_table_ready, container, false);
        got_to_routine = view.findViewById(R.id.got_to_routine);
        name_text = view.findViewById(R.id.name_text);
        subject_text = view.findViewById(R.id.subject_text);
        got_to_routine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRoutineListener.goToRoutine();
            }
        });
        lottieAnimationView = view.findViewById(R.id.animation);
        lottieAnimationView.playAnimation();
        lottieAnimationView.loop(true);
        startAnimation();
        startRecyclerAnimation();
        MyApplication.getReferenceForUserRoutine().removeValue();
        return  view;
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        subject_text.startAnimation(fadeInFromRight);
        name_text.startAnimation(fadeInFromRight);
        lottieAnimationView.startAnimation(fadeInFromRight);
    }

    private void startRecyclerAnimation() {
        final Animation fadeInFromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_in);
        got_to_routine.startAnimation(fadeInFromBottom);
    }

    private void startHandler(){
        handler = new Handler();
        runnable = () -> {
            if(goToRoutineListener!=null)
                goToRoutineListener.goToRoutine();
        };
        handler.postDelayed(runnable,4000);
    }

    @Override
    public void onResume() {
        super.onResume();
        lottieAnimationView.playAnimation();
        lottieAnimationView.loop(true);
        startHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        lottieAnimationView.cancelAnimation();
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
    }

    public interface GoToRoutineListener{
        void goToRoutine();
    }
}