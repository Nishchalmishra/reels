package arivihan.technologies.doubtbuzzter2.common.feedback;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.testing.FakeReviewManager;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.review.AppReviewFeedbackAdapter;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackOption;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AppReviewDialog extends Dialog {

    private static final String TAG = AppReviewDialog.class.getSimpleName();
    private Context context;
    private OnAppReviewDialogClickListener listener;

    private CardView cv_secondary, cv_primary;
    private TextView tv_secondary_btn, tv_primary_btn, tv_title, tv_sub_title, tv_primary_text;
    private EditText et_feedback;
    private ImageView iv_star1, iv_star2, iv_star3, iv_star4, iv_star5, iv_close;
    private RecyclerView rv_feedback_options;

    //Data variable
    private int starSelectedCount;

    public AppReviewDialog(Context context, OnAppReviewDialogClickListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_app_review_star);
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(true);

        String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("app_review_pop_up_" +
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Log.d(TAG, "firebaseJsonStr: " + firebaseJsonStr);

        cv_secondary = findViewById(R.id.cv_secondary);
        cv_primary = findViewById(R.id.cv_primary);
        tv_secondary_btn = findViewById(R.id.tv_secondary_btn);
        tv_primary_btn = findViewById(R.id.tv_primary_btn);

        tv_title = findViewById(R.id.tv_title);
        tv_sub_title = findViewById(R.id.tv_sub_title);
        tv_primary_text = findViewById(R.id.tv_primary_text);

        et_feedback = findViewById(R.id.et_feedback);

        iv_star1 = findViewById(R.id.iv_star1);
        iv_star2 = findViewById(R.id.iv_star2);
        iv_star3 = findViewById(R.id.iv_star3);
        iv_star4 = findViewById(R.id.iv_star4);
        iv_star5 = findViewById(R.id.iv_star5);

        iv_close = findViewById(R.id.iv_close);

        rv_feedback_options = findViewById(R.id.rv_feedback_options);

        try {
            Gson gson = new Gson();
            FeedbackModel feedbackModel = gson.fromJson(firebaseJsonStr, FeedbackModel.class);

            tv_primary_btn.setText(feedbackModel.getPrimaryBtnText());
            tv_secondary_btn.setText(feedbackModel.getSecondaryBtnText());
            tv_title.setText(feedbackModel.getTitle());
            tv_sub_title.setText(feedbackModel.getSubtitle());
            tv_primary_text.setText(feedbackModel.getPrimarySubText());

            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            rv_feedback_options.setLayoutManager(layoutManager);
            rv_feedback_options.setAdapter(new AppReviewFeedbackAdapter(context, feedbackModel.getFeedbackOptions(), null));

            cv_secondary.setOnClickListener(v -> {

                ArrayList<String> selectedFeedbacks = new ArrayList<>();
                for (FeedbackOption feedbackOption : feedbackModel.getFeedbackOptions()) {
                    if (feedbackOption.isSelected()) {
                        selectedFeedbacks.add(feedbackOption.getMessage());
                    }
                }

                Map<String, String> params = new HashMap<>();
                params.put("star", String.valueOf(starSelectedCount));
                params.put("option_name", String.join(",", selectedFeedbacks));
                params.put("feedback", et_feedback.getText().toString());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.app_review_skip_click.getActionName(), params);

                dismiss();
                listener.onSecondaryButtonClick(false, starSelectedCount, et_feedback.getText().toString(), null);
            });

            cv_primary.setOnClickListener(v -> {

                ArrayList<String> selectedFeedbacks = new ArrayList<>();
                for (FeedbackOption feedbackOption : feedbackModel.getFeedbackOptions()) {
                    if (feedbackOption.isSelected()) {
                        selectedFeedbacks.add(feedbackOption.getMessage());
                    }
                }

                if (starSelectedCount==0) {
                    Toast.makeText(context, ContextCompat.getString(context, R.string.pehle_humein_apna_feedback_dein), Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("star", String.valueOf(starSelectedCount));
                params.put("option_name", String.join(",", selectedFeedbacks));
                params.put("feedback", et_feedback.getText().toString());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.app_review_submit_click.getActionName(), params);

                dismiss();
                listener.onPrimaryButtonClick(true, starSelectedCount, et_feedback.getText().toString().trim(), selectedFeedbacks);

            });

            iv_star1.setOnClickListener(v -> {
                setStars(1);
            });

            iv_star2.setOnClickListener(v -> {
                setStars(2);
            });

            iv_star3.setOnClickListener(v -> {
                setStars(3);
            });

            iv_star4.setOnClickListener(v -> {
                setStars(4);
            });

            iv_star5.setOnClickListener(v -> {
                setStars(5);
            });

            iv_close.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.app_review_close_btn_click.getActionName());

                dismiss();
            });

            setStars(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.app_review_visible.getActionName());

    }

    private void setStars(int rating) {
        starSelectedCount = rating;
        for (int i = 1; i <= rating; i++) {
            setImage(i, true);
        }

        for (int i = rating + 1; i <= 5; i++) {
            setImage(i, false);
        }
    }

    private void setImage(int position, boolean isFilled) {
        switch (position) {
            case 1 ->
                    iv_star1.setImageResource(isFilled ? R.drawable.img_teal_star_filled : R.drawable.img_teal_star_outline);
            case 2 ->
                    iv_star2.setImageResource(isFilled ? R.drawable.img_teal_star_filled : R.drawable.img_teal_star_outline);
            case 3 ->
                    iv_star3.setImageResource(isFilled ? R.drawable.img_teal_star_filled : R.drawable.img_teal_star_outline);
            case 4 ->
                    iv_star4.setImageResource(isFilled ? R.drawable.img_teal_star_filled : R.drawable.img_teal_star_outline);
            case 5 ->
                    iv_star5.setImageResource(isFilled ? R.drawable.img_teal_star_filled : R.drawable.img_teal_star_outline);
        }
    }

    public static void openPlayStore(Context context){
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.setPackage("com.android.vending");

        try {
           context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            context.startActivity(webIntent);
        }
    }

    public static void openAppInPlayStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.vending");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName());
            context.startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

//    public static void showInAppReview(Activity activity) {
////        ReviewManager manager = ReviewManagerFactory.create(activity);
//        ReviewManager manager = new FakeReviewManager(activity.getApplicationContext());
//
//        Task<ReviewInfo> request = manager.requestReviewFlow();
//        request.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                // We got the ReviewInfo object
//                ReviewInfo reviewInfo = task.getResult();
//                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
//                flow.addOnCompleteListener(flowTask -> {
//                    Log.d("Review", "In-app review launched");
//                });
//            } else {
//                Log.e("Review", "In-app review failed, opening Play Store");
//                openPlayStore(activity);
//            }
//        });
//    }
    public interface OnAppReviewDialogClickListener {
        void onPrimaryButtonClick(boolean status, int starCount,String feedback , Object object);
        void onSecondaryButtonClick(boolean status, int starCount, String feedback , Object object);
    }

}