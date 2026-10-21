package arivihan.technologies.doubtbuzzter2.fragments;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;

import arivihan.technologies.doubtbuzzter2.R;


public class RocketFragment extends Fragment {

    private LottieAnimationView lottieAnimationView;
    private ConstraintLayout parent_layout;
    private GoToTimeTableReadyScreenListener goToTimeTableReadyScreenListener;
    private Handler handler;
    private Runnable runnable;
    public RocketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        goToTimeTableReadyScreenListener= (GoToTimeTableReadyScreenListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rocket, container, false);
        parent_layout = view.findViewById(R.id.parent_layout);
        parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(goToTimeTableReadyScreenListener!=null)
                    goToTimeTableReadyScreenListener.goToTimeTableReadyScreen();
            }
        });
        lottieAnimationView = view.findViewById(R.id.animation);
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if(goToTimeTableReadyScreenListener!=null)
                    goToTimeTableReadyScreenListener.goToTimeTableReadyScreen();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
        return  view;
    }

    private void startHandler(){
        handler = new Handler();
        runnable = () -> {
            if(goToTimeTableReadyScreenListener!=null)
              goToTimeTableReadyScreenListener.goToTimeTableReadyScreen();
        };
        handler.postDelayed(runnable,4000);
    }

    @Override
    public void onResume() {
        super.onResume();
        lottieAnimationView.playAnimation();
        startHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
        lottieAnimationView.cancelAnimation();
    }

    public interface GoToTimeTableReadyScreenListener{
        void goToTimeTableReadyScreen();
    }
}