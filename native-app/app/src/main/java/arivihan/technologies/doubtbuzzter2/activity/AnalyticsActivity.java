package arivihan.technologies.doubtbuzzter2.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.fragments.AnalyticFragment;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AnalyticsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.analytics_frame, new AnalyticFragment());
        fragmentTransaction.commitAllowingStateLoss();
    }
}