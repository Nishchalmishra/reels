package arivihan.technologies.doubtbuzzter2.activity.competitive;

import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.viewModel.CompetitiveHomeViewModel;

public class CompetitiveHomeActivity extends BaseActivity {

    public static final String TAG = CompetitiveHomeActivity.class.getSimpleName();

    private CompetitiveHomeViewModel viewModel;

    // UI Variables
    static BottomNavigationView bottomNavigationView;

    // Data Variables
    private long lastBackPressedTime = 0;
    private static final int BACK_PRESS_INTERVAL = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_competitive_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        viewModel = new ViewModelProvider(this).get(CompetitiveHomeViewModel.class);


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedClick();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void onBackPressedClick() {
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackPressedTime < BACK_PRESS_INTERVAL) {
                finishAffinity();
            } else {
                lastBackPressedTime = currentTime;
                Toast.makeText(this, getString(R.string.click_to_exit), Toast.LENGTH_SHORT).show();

                // haptic feedback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}