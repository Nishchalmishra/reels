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

import arivihan.technologies.doubtbuzzter2.R;


public class CreatingRoutineFragment extends Fragment {

    private LottieAnimationView lottieAnimationView;
    private CardView go_to_home;
    private GoToHomeListener goToHomeListener;
    TextView name_text;
    TextView subject_text;

    public CreatingRoutineFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        goToHomeListener= (GoToHomeListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_creating_routine, container, false);
        name_text = view.findViewById(R.id.name_text);
        subject_text = view.findViewById(R.id.subject_text);
        go_to_home = view.findViewById(R.id.go_to_home);
        go_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(goToHomeListener!=null)
                    goToHomeListener.goToHome();
            }
        });
        lottieAnimationView = view.findViewById(R.id.animation);
        lottieAnimationView.playAnimation();
        lottieAnimationView.loop(true);
        lottieAnimationView.setSpeed(3f);
        startAnimation();
        startRecyclerAnimation();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        lottieAnimationView.playAnimation();
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        subject_text.startAnimation(fadeInFromRight);
        name_text.startAnimation(fadeInFromRight);
        lottieAnimationView.startAnimation(fadeInFromRight);
    }

    private void startRecyclerAnimation() {
        final Animation fadeInFromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_in);
        go_to_home.startAnimation(fadeInFromBottom);
    }

    @Override
    public void onPause() {
        super.onPause();
        lottieAnimationView.cancelAnimation();
    }

    public interface GoToHomeListener{
        void goToHome();
    }
}