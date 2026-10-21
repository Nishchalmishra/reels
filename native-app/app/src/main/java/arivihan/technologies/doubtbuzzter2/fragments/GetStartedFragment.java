package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ViewPagerAdapterSetStarted;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.LogInWIthMobileNumberFragment;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class GetStartedFragment extends Fragment {

    FrameLayout frameLayout;
    CardView start;
    ViewPager viewPager;
    ArrayList<String> arr = new ArrayList<>();
    ViewPagerAdapterSetStarted viewPagerAdapterSetStarted;
    FragmentChangeListener listener;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_started, container, false);

        frameLayout = view.findViewById(R.id.frame);
        viewPager = view.findViewById(R.id.view_pager);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragment_animation_come);
        frameLayout.startAnimation(animation);

        arr.add("All-in-One teacher for your exam preparation");
        arr.add("Clear all your doubts with just a click and 24X7");
        arr.add("Revise your concepts through Games");


        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);

        start = view.findViewById(R.id.start_card);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragmemt_animtaion_go);
                frameLayout.startAnimation(animation);
                listener.setFragment(new LogInWIthMobileNumberFragment());
            }
        });



        return view;
    }


}