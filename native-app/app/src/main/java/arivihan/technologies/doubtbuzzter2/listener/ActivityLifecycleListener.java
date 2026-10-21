package arivihan.technologies.doubtbuzzter2.listener;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.activity.ProPlanSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.QRCodePaymentActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionPaymentScreen;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = ActivityLifecycleListener.class.getSimpleName();
    private final Set<Class<? extends View>> eligibleViewClasses;
    private WeakReference<Activity> currentActivityRef;
    private FirebaseAnalytics firebaseAnalytics;

    public ActivityLifecycleListener(Application application) {
        eligibleViewClasses = new HashSet<>();
        initEligibleViewClasses();
        firebaseAnalytics = FirebaseAnalytics.getInstance(application);
    }

    private void initEligibleViewClasses() {
        eligibleViewClasses.add(Button.class);
        eligibleViewClasses.add(TextView.class);
        eligibleViewClasses.add(ImageView.class);
        eligibleViewClasses.add(CheckBox.class);
        eligibleViewClasses.add(RadioButton.class);
        eligibleViewClasses.add(EditText.class);
        eligibleViewClasses.add(Switch.class);
        eligibleViewClasses.add(ImageButton.class);
        eligibleViewClasses.add(SeekBar.class);
        eligibleViewClasses.add(Spinner.class);
        eligibleViewClasses.add(ProgressBar.class);
        eligibleViewClasses.add(CardView.class);
        eligibleViewClasses.add(ToggleButton.class);
        eligibleViewClasses.add(Chip.class);
        eligibleViewClasses.add(FloatingActionButton.class);
        eligibleViewClasses.add(RatingBar.class);
        eligibleViewClasses.add(WebView.class);
        eligibleViewClasses.add(RecyclerView.class);
    }


    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        logActivityEvent("onActivityPreCreated", activity);

        if (activity instanceof AppCompatActivity
                && !(activity instanceof ChapterListSubscribedActivity)) {
            if (activity instanceof ProPlanSubscriptionActivity) {
                ((AppCompatActivity) activity).getDelegate()
                        .setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                ((AppCompatActivity) activity).getDelegate()
                        .setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        // Configure the window before the activity inflates its first layout. The created
        // callback below repeats this call to attach the content inset listener once the root
        // view exists.
        if (!(activity instanceof QRCodePaymentActivity)
                && !(activity instanceof SubscriptionPaymentScreen)
                && !(activity instanceof CampaignSubscriptionActivity)) {
            EdgeToEdgeHelper.prepareWindow(activity);
        }

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        logActivityEvent("onActivityCreated", activity);

        //draw this screen behind the system bars
        if (!(activity instanceof SubscriptionPaymentScreen)
                && !(activity instanceof CampaignSubscriptionActivity)) {
            EdgeToEdgeHelper.apply(activity);
        }

        //show android logs
        if (BuildConfig.DEBUG) logActivityParams(activity, savedInstanceState);
    }

    private void logActivityParams(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        StringBuilder log = new StringBuilder();
        log.append("\n=== Activity Params: ").append(activity.getClass()).append(" ===");

        // Intent Extras
        Intent intent = activity.getIntent();
        if (intent != null) {
            log.append("\n-- Intent Action: ").append(intent.getAction());
            log.append("\n-- Intent Data: ").append(intent.getDataString());

            Bundle extras = intent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                log.append("\n-- Intent Extras:");
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    log.append("\n     ").append(key).append(" = ").append(value);
                }
            } else {
                log.append("\n-- Intent Extras: none");
            }
        } else {
            log.append("\n-- Intent: null");
        }

        // Saved Instance State
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            log.append("\n-- SavedInstanceState:");
            for (String key : savedInstanceState.keySet()) {
                Object value = savedInstanceState.get(key);
                log.append("\n     ").append(key).append(" = ").append(value);
            }
        } else {
            log.append("\n-- SavedInstanceState: null or empty");
        }

        log.append("\n===========================================");
        Log.d("ActivityParams", log.toString());
    }



    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        logActivityEvent("onActivityStarted", activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        logActivityEvent("onActivityResumed", activity);
        currentActivityRef = new WeakReference<>(activity);
        setGlobalClickListener(activity.findViewById(android.R.id.content));
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        logActivityEvent("onActivityPaused", activity);
        currentActivityRef = null;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        logActivityEvent("onActivityStopped", activity);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // Handle state saving if needed
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        logActivityEvent("onActivityDestroyed", activity);
    }

    private void logActivityEvent(String event, Activity activity) {
        Log.d(LOG_TAG, event + ": " + activity.getClass().getSimpleName());
    }

    private void setGlobalClickListener(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setGlobalClickListener(group.getChildAt(i));
            }
        } else if (isEligibleForClickLogging(view)) {
            setTouchListener(view);
        }
    }

    private boolean isEligibleForClickLogging(View view) {
        for (Class<? extends View> clazz : eligibleViewClasses) {
            if (clazz.isInstance(view)) {
                return true;
            }
        }
        return false;
    }

    private void setTouchListener(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                logViewClick(v);
            }
            return false; // Allow further handling of the event
        });
    }

    private void logViewClick(View view) {
        try {
            int viewId = view.getId();
            String resourceName = viewId != View.NO_ID ? view.getResources().getResourceEntryName(viewId) : "no_id";
            String activityName = currentActivityRef != null && currentActivityRef.get() != null
                    ? currentActivityRef.get().getClass().getSimpleName()
                    : "Unknown";
            Log.d(LOG_TAG, "Click in " + activityName + ": " + resourceName + " (ID: " + viewId + ")");

            // Log event to Firebase Analytics
            Bundle bundle = new Bundle();
            bundle.putString("activity_name", activityName);
            bundle.putString("view_name", resourceName);
            bundle.putString("view_id", String.valueOf(viewId));
            // Dispatched off the main thread: this runs on every tracked click, and the
            // Measurement SDK can block on a Binder call into Play Services.
            MyApplication.logFirebaseEventOffMainThread(resourceName, bundle);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while logging view click: ", e);
        }
    }
}
