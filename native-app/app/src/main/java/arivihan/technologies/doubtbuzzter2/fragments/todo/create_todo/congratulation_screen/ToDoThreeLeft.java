package arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ToDoThreeLeft extends Fragment {

    private TextView name_text;
    private TextView subject_text;
    private TextView number_text;
    private TextView emoji_text;
    private String subject;
    private LottieAnimationView lottieAnimationView;
    private Handler handler;
    private Runnable runnable;
    private GoToNextSubjectListenerThree goToNextSubjectListenerThree;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("subject", subject);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        goToNextSubjectListenerThree= (GoToNextSubjectListenerThree) context;
    }

    public static ToDoThreeLeft newInstance(String subject) {
        ToDoThreeLeft fragment = new ToDoThreeLeft();
        Bundle args = new Bundle();
        args.putString("subject", subject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subject = getArguments().getString("subject");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_to_do_three_left, container, false);
        name_text = view.findViewById(R.id.name_text);
        emoji_text = view.findViewById(R.id.emoji_text);
        subject_text = view.findViewById(R.id.subject_text);
        number_text = view.findViewById(R.id.number_text);
        lottieAnimationView = view.findViewById(R.id.animation);
        lottieAnimationView.playAnimation();
        lottieAnimationView.loop(true);
        name_text.setText(String.format(getString(R.string.to_do_step_3), MyApplication.getStringData(PreferenceKeys.NAME.getKey())));
        subject_text.setText(String.format(getString(R.string.ka_bhi_Time_table_ban_gya),subject));
        number_text.setText(String.format(getString(R.string.Just_more_subject_left),3));
        startAnimation();
        return view;
    }

    private void startHandler(){
        handler = new Handler();
        runnable = () -> {
            if(goToNextSubjectListenerThree!=null)
                goToNextSubjectListenerThree.goToNextSubjectThree();
        };
        handler.postDelayed(runnable,4000);
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        subject_text.startAnimation(fadeInFromRight);
        name_text.startAnimation(fadeInFromRight);
        emoji_text.startAnimation(fadeInFromRight);
        lottieAnimationView.startAnimation(fadeInFromRight);
        number_text.startAnimation(fadeInFromRight);
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

    public interface GoToNextSubjectListenerThree{
        void goToNextSubjectThree();
    }
}