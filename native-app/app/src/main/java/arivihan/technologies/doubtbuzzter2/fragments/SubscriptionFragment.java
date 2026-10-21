package arivihan.technologies.doubtbuzzter2.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;

public class SubscriptionFragment extends Fragment {

    FrameLayout frameLayout;
    ScrollView scrollView;
    TextView year,month,month_small,price,save,btn_text;

    public SubscriptionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscription, container, false);

        frameLayout = view.findViewById(R.id.frameLayout);
        scrollView = view.findViewById(R.id.scrollView2);

        Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
        scrollView.startAnimation(animation1);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragment_animation_come);
        frameLayout.startAnimation(animation);

        year = view.findViewById(R.id.one_year);
        month = view.findViewById(R.id.six_months);
        month_small = view.findViewById(R.id.three_months);
        price = view.findViewById(R.id.price);
        save = view.findViewById(R.id.save);
        btn_text = view.findViewById(R.id.buy_text);

        year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                year.setTextColor(Color.BLACK);
                month.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                month_small.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                year.setTextSize(20);
                month.setTextSize(17);
                month_small.setTextSize(17);
                save.setText("Save 33%");
                price.setText("₹ 2499/monthly");
                btn_text.setText("Get 1 year - ₹ 2499/mo");
            }
        });
        month_small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                month_small.setTextColor(Color.BLACK);
                month.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                year.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                month_small.setTextSize(20);
                month.setTextSize(17);
                year.setTextSize(17);
                save.setText("");
                price.setText("₹ 799/monthly");
                btn_text.setText("Get 3 months - ₹ 799/mo");
            }
        });
        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                month.setTextColor(Color.BLACK);
                month_small.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                year.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_selected));
                month.setTextSize(20);
                month_small.setTextSize(17);
                year.setTextSize(17);
                save.setText("Save 20%");
                price.setText("₹ 1299/monthly");
                btn_text.setText("Get 6 months - ₹ 1299/mo");
            }
        });

        return view;
    }
}