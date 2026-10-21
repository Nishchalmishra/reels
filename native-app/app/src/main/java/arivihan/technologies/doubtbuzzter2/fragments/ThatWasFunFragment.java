package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.VernacularActivity;

public class ThatWasFunFragment extends Fragment {

    FrameLayout frameLayout;
    CardView back_to_feed,subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_that_was_fun, container, false);

        frameLayout = view.findViewById(R.id.framelayoutforfun);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragment_animation_come);
        frameLayout.startAnimation(animation);

        back_to_feed = view.findViewById(R.id.skip_subscription_plan);
        subscription = view.findViewById(R.id.subscription_plan);

        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragmemt_animtaion_go);
                frameLayout.startAnimation(animation);
                setFragment(new SubscriptionFragment());
            }
        });

        back_to_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VernacularActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.starting_frame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

}