package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ExploreActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.ToDoListMain;
import arivihan.technologies.doubtbuzzter2.activity.VernacularActivity;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class IntroSplashScreenFragment extends Fragment {

    String number = "";
    ImageView image;
    FrameLayout frame;
    CountDownTimer countDownTimer;
    boolean nextFrag = true;
    FragmentChangeListener listener;
    String deepLink;

    public IntroSplashScreenFragment() {

    }

    public IntroSplashScreenFragment(String deeplink) {
        this.deepLink = deeplink;
    }


    // not working for API > 11 here need to comment super code
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        nextFrag = false;
    }


    @Override
    public void onResume() {
        super.onResume();
        nextFrag = true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentChangeListener) context;
        } catch (ClassCastException e) {
            Log.e("Error in ", e.getMessage(), e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("launcherBug", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_intro_splash_screen, container, false);

        image = view.findViewById(R.id.splash_icon_unique);
        Animation scale = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        image.setAnimation(scale);
        frame = view.findViewById(R.id.frameSplash);

        MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), false);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        MyApplication.saveData(PreferenceKeys.SHOW_POP_UP.getKey(), true);
        number = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());



        return view;
    }

}