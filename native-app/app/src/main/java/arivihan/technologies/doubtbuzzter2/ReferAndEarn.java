package arivihan.technologies.doubtbuzzter2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import org.apache.commons.lang3.StringUtils;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ReferAndEarn extends BaseActivity {

    private static final int MY_REQUEST_CODE = 101;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog dialog;

    private String timeStamp;
    private TextView referralCode;
    private TextView referralScore, terms, rules;
    private String userValue;
    private String referralScoreValue;
    private String referralCodeValue;
    private String no1;
    private String no2;
    private String no3;
    private String no4;
    private String no5;
    LinearLayout back;
    TextView title;
    private Dialog dialog2;
    String Reference_Code = "";
    private CardView referAFriend;
    AppUpdateManager appUpdateManager;

//    private Button claimAchievementRefer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Reference_Code = getIntent().getStringExtra("Refer");
        if (MyApplication.getStringData(PreferenceKeys.REFER_CODE.getKey()) != null)
            Reference_Code = MyApplication.getStringData(PreferenceKeys.REFER_CODE.getKey());

        setContentView(R.layout.activity_refer_and_earn);
        referralCode = findViewById(R.id.referral_code_value);
        back = findViewById(R.id.back_arrow);
        title = findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.refer_earn));
        terms = findViewById(R.id.terms);
        referralScore = findViewById(R.id.referral_score_value);
        referAFriend = findViewById(R.id.button_refer_a_friend);
        userValue = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        appUpdateManager = AppUpdateManagerFactory.create(ReferAndEarn.this);

        dialog2 = new Dialog(ReferAndEarn.this);
        dialog2.setContentView(R.layout.custom_dialogue_type_activity);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        CardView confirm = (CardView) dialog2.findViewById(R.id.ask_points);
        CardView cancel = (CardView) dialog2.findViewById(R.id.revise_concept);
        TextView text = (TextView) dialog2.findViewById(R.id.text);
        CardView cut = (CardView) dialog2.findViewById(R.id.cancel);
        rules = findViewById(R.id.rules);

        TextView msg = (TextView) dialog2.findViewById(R.id.doubt_text);

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String appendText = lang;
        if ("en".equalsIgnoreCase(lang)) {
            appendText = "";
        }

        String referDescription = MyApplication.getFirebaseRemoteConfig().getString("terms_and_condition" + appendText);
        if (StringUtils.isEmpty(referDescription)) {
            referDescription = MyApplication.getFirebaseRemoteConfig().getString("terms_and_condition");
            msg.setText(referDescription.replace("\\n", "\n"));
        }
        String rulesText = MyApplication.getFirebaseRemoteConfig().getString("rules" + appendText);
        if (StringUtils.isEmpty(rulesText)) {
            rulesText = MyApplication.getFirebaseRemoteConfig().getString("rules");
        }
        if (StringUtils.isNotEmpty(rulesText)) {
            rules.setText(rulesText.replace("\\n", "\n"));
        }

        confirm.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        text.setText(getString(R.string.terms_and_condition));

        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
            }
        });

        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());


        terms.setOnClickListener(view -> {
            dialog2.show();
            try {
                Map<String, String> map = new HashMap<>();
                map.put("token", MyApplication.getFCMToken());
                db.collection("Admin").document(String.valueOf(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())))
                        .set(map, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("Notification", " Set"));
                Log.i("FCM TOKEN ", MyApplication.getFCMToken());
            } catch (Exception e) {
                Log.d("error", "error");
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Buy Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // getdata();

        referralScore.setText(MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()) + "");


        referralCode.setText(Reference_Code);

        referAFriend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.refer_message_part_one) + referralCodeValue + getString(R.string.refer_message_part_two) +
                        "\n" +
                        "https://play.google.com/store/apps/details?id=arivihan.technologies.doubtbuzzter2");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                HashMap<String, String> params = new HashMap<>();
                params.put("Reference_code", referralCodeValue);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.click_share_profile.getActionName(), params);
            }
        });

    }

    private void getdata() {
        db.collection("Student")
                .document(userValue).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            DocumentSnapshot document = task.getResult();

                            if (document.getData().get("ReferralScore") != null) {
                                referralScoreValue = document.getData().get("ReferralScore").toString();
                            } else {
                                referralScoreValue = "N/A";
                        /*Toast.makeText(getApplicationContext(),
                                "Fill Form & Complete Your Registration to Get Your Referral Code & Earn Free Doubts", Toast.LENGTH_SHORT)
                                .show();*/
                                Snackbar.make(referralCode, getString(R.string.referral_snackbar), 7000).show();

                            }

                            if (document.getData().get("Referral") != null) {
                                referralCodeValue = document.getData().get("Referral").toString();
                            } else {
                                referralCodeValue = "N/A";
                            }

                            referralCode.setText(referralCodeValue);
                            referralScore.setText(referralScoreValue);
                            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                            String appendText = lang;
                            if ("en".equalsIgnoreCase(lang)) {
                                appendText = "";
                            }
                            updateReferAndEarnPlan(appendText, true);


                        } else {
                            Toast.makeText(getApplicationContext(),
                                            "Error in fetching Data.. \nPlease Restart the App..", Toast.LENGTH_SHORT)
                                    .show();
                            referralCode.setText("xxxxxxx");
                            referralScore.setText("N/A");
                            dialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(),
                                                                "Error!!" + e.getMessage(), Toast.LENGTH_SHORT)
                                                        .show();

                                                if (dialog.isShowing()) {
                                                    dialog.dismiss();
                                                }


                                            }

                                        }

                );

    }


    public static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            IMMEDIATE,
                                            this,
                                            MY_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    void updateReferAndEarnPlan(String appendText, boolean callAgain) {


        db.collection("info")
                .document("ReferAndEarnStudentPlan" + appendText).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            DocumentSnapshot document = task.getResult();
                            if (document.getData().get("No1") != null) {
                                no1 = document.getData().get("No1").toString();
                            } else {
                                no1 = "";
                            }

                            if (document.getData().get("No2") != null) {
                                no2 = document.getData().get("No2").toString();
                            } else {
                                no2 = "";
                            }

                            if (document.getData().get("No3") != null) {
                                no3 = document.getData().get("No3").toString();
                            } else {
                                no3 = "";
                            }

                            if (document.getData().get("No4") != null) {
                                no4 = document.getData().get("No4").toString();
                            } else {
                                no4 = "";
                            }

                            if (document.getData().get("No5") != null) {
                                no5 = document.getData().get("No5").toString();
                            } else {
                                no5 = "";
                            }


                            dialog.dismiss();

                        } else {
                            if (!"en".equalsIgnoreCase(appendText) && callAgain) {
                                updateReferAndEarnPlan(appendText, false);
                            }
                            Toast.makeText(getApplicationContext(),
                                            "Error in fetching Data.. \nPlease Restart the App..", Toast.LENGTH_SHORT)
                                    .show();

                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (!"en".equalsIgnoreCase(appendText) && callAgain) {
                            updateReferAndEarnPlan(appendText, false);
                        }
                        Toast.makeText(getApplicationContext(),
                                        "Error!!" + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();

                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });

        dialog2 = new Dialog(ReferAndEarn.this);
        dialog2.setContentView(R.layout.custom_dialogue_type_activity);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        CardView use_points = (CardView) dialog2.findViewById(R.id.ask_points);
        CardView use_free_points = (CardView) dialog2.findViewById(R.id.revise_concept);
        use_points.setVisibility(View.GONE);
        use_free_points.setVisibility(View.GONE);
        TextView title = (TextView) dialog2.findViewById(R.id.text);
        TextView message = (TextView) dialog2.findViewById(R.id.doubt_text);


        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setText(R.string.terms);
                message.setText(no1 + "\n\n" + no2 + "\n\n" + no3 + "\n\n" + no4 + "\n" + no5);
                dialog2.show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

    }

}
