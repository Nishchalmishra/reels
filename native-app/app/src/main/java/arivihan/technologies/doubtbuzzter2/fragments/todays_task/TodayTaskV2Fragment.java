package arivihan.technologies.doubtbuzzter2.fragments.todays_task;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arivihan.technologies.doubtbuzzter2.R;

public class TodayTaskV2Fragment extends Fragment {

    public TodayTaskV2Fragment() {
        // Required empty public constructor
    }

    public static TodayTaskV2Fragment newInstance(String param1, String param2) {
        TodayTaskV2Fragment fragment = new TodayTaskV2Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_today_task_v2, container, false);
    }
}