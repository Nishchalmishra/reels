package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.firebase.client.Firebase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AdditionalSettingsActivity extends BaseActivity {

    private static final String TAG = AdditionalSettingsActivity.class.getSimpleName();


    private View rootView;
    private ImageView ivBack;
    private CardView cv_privacy_policy, cv_teams_and_conditions, cv_account_delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additional_settings_screen);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_setting_screen_view.getActionName());
        rootView = findViewById(android.R.id.content);

        initViews();
        setupListeners();
    }


    //view
    private void initViews() {
        rootView = findViewById(android.R.id.content);
        ivBack = findViewById(R.id.iv_back);

        cv_privacy_policy = findViewById(R.id.cv_privacy_policy);
        cv_teams_and_conditions = findViewById(R.id.cv_teams_and_conditions);
        cv_account_delete = findViewById(R.id.cv_account_delete);
    }


    //clicks
    private void setupListeners() {
        ivBack.setOnClickListener(view ->{
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_setting_back_button_click.getActionName());
            getOnBackPressedDispatcher().onBackPressed();
        });

        cv_privacy_policy.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://arivihan.com/privacy-policy"));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_setting_privacy_policy_click.getActionName());
            startActivity(intent);
        });

        cv_teams_and_conditions.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://arivihan.com/t-and-c"));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_setting_terms_and_condition_click.getActionName());
            startActivity(intent);
        });

        cv_account_delete.setOnClickListener(v ->{
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_setting_account_delete_click.getActionName());
         showDeleteAccountDialog();
        });
    }



    public JSONObject getAccountDeleteJson() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("delete_account_config");
            if (StringUtils.isEmpty(json)) return null;

            JSONObject root = new JSONObject(json);
            return root.optJSONObject("delete_account");
        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
            return null;
        }
    }


    //show dialog
    public void showDeleteAccountDialog() {
        try {
            JSONObject popupData = getAccountDeleteJson();
            if (popupData == null) return;

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_account_delete);

            if (dialog.getWindow() != null) {
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            //title
            TextView tvTitle = dialog.findViewById(R.id.title);
            TextView tvSubTitle = dialog.findViewById(R.id.subtitle);

            CardView cvPrimary = dialog.findViewById(R.id.cv_primary);
            TextView tvPrimaryBtn = dialog.findViewById(R.id.tx_primary);
            CardView cvSecondary = dialog.findViewById(R.id.cv_secondary);
            TextView tvSecondaryBtn = dialog.findViewById(R.id.tx_secondary);

            ImageView centerImg = dialog.findViewById(R.id.center_img);
            centerImg.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_warning_delete));

            JSONObject titleObj = popupData.optJSONObject("title");
            tvTitle.setText(titleObj != null ? titleObj.optString(lang) : "");

            // Subtitle
            JSONObject subTitleObj = popupData.optJSONObject("sub_title");
            tvSubTitle.setText(subTitleObj != null ? subTitleObj.optString(lang) : "");

            JSONObject primaryBtnObj = popupData.optJSONObject("primary_btn");
            tvPrimaryBtn.setText(primaryBtnObj != null ? primaryBtnObj.optString(lang) : "");

            JSONObject secondaryBtnObj = popupData.optJSONObject("secondary_btn");
            tvSecondaryBtn.setText(secondaryBtnObj != null ? secondaryBtnObj.optString(lang) : "");

            ImageView ivClose = dialog.findViewById(R.id.iv_close);
            ivClose.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_dialog_close_btn_click.getActionName());
                dialog.dismiss();
            });


            LinearLayout buttonLayout = dialog.findViewById(R.id.button_layout);
            buttonLayout.setOrientation(LinearLayout.VERTICAL);

//            buttonLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Primary Button (Cancel)
            cvPrimary.setOnClickListener(v ->{
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_dialog_cancel_btn_click.getActionName());
                dialog.dismiss();
            });

            cvSecondary.setOnClickListener(v -> {
                dialog.dismiss();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_dialog_delete_btn_click.getActionName());
                deleteAccount();
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }
    }



//    private void updateUiState(Context context, CardView buttonView, TextView buttonText, boolean enable) {
//        buttonView.setEnabled(enable);
//        buttonView.setCardBackgroundColor(ContextCompat.getColor(context, enable ? R.color.btn_enabled_bg : R.color.btn_disabled_bg));
//        buttonText.setTextColor(ContextCompat.getColor(context, enable ? R.color.btn_enabled_text : R.color.btn_disabled_text));
//    }


    private void deleteAccount(){
        Intent intent = new Intent(this, NewDeleteAccountActivity.class);
        startActivity(intent);
    }



}