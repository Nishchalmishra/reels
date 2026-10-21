package arivihan.technologies.doubtbuzzter2.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.refer.ReferAndEarnActivity;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

import java.util.HashMap;

public class ParikshaAdhyayanReferralDialog extends Dialog {

    private static final String TAG = "PAReferralDialog";
    private static final String REMOTE_CONFIG_KEY = "pa_referral_dialog_config";

    public ParikshaAdhyayanReferralDialog(Context context) {
        super(context, R.style.AlertDialogTheme);
        setContentView(R.layout.dialog_pariksha_adhyayan_referral);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(true);
        initViews();
        applyRemoteConfig();
        setOnShowListener(d -> MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.pa_referral_dialog_viewed.getActionName(), new HashMap<>()));
    }

    private void initViews() {
        MaterialCardView ctaButton = findViewById(R.id.cv_pa_refer_cta);
        ctaButton.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.pa_referral_dialog_cta_clicked.getActionName(), new HashMap<>());
            dismiss();
            getContext().startActivity(new Intent(getContext(), ReferAndEarnActivity.class));
        });

        findViewById(R.id.iv_pa_referral_close).setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.pa_referral_dialog_close_clicked.getActionName(), new HashMap<>());
            dismiss();
        });
    }

    private void applyRemoteConfig() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isBlank(json)) return;

            JSONObject root = new JSONObject(json);
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isBlank(lang)) lang = "en";

            JSONObject langConfig = root.optJSONObject(lang);
            if (langConfig == null) langConfig = root.optJSONObject("en");
            if (langConfig == null) return;

            TextView tvTitle = findViewById(R.id.tv_pa_referral_title);
            TextView tvDescription = findViewById(R.id.tv_pa_referral_description);
            TextView tvCta = findViewById(R.id.tv_pa_refer_cta);
            ImageView ivIllustration = findViewById(R.id.iv_pa_referral_illustration);

            String title = langConfig.optString("title", "");
            if (StringUtils.isNotBlank(title)) tvTitle.setText(title);

            String description = langConfig.optString("description", "");
            if (StringUtils.isNotBlank(description)) tvDescription.setText(description);

            String buttonText = langConfig.optString("buttonText", "");
            if (StringUtils.isNotBlank(buttonText)) tvCta.setText(buttonText);

            String imageUrl = langConfig.optString("imageUrl", "");
            if (StringUtils.isNotBlank(imageUrl)) {
                Glide.with(getContext())
                        .load(imageUrl)
                        .error(R.drawable.img_girl_refer)
                        .into(ivIllustration);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply remote config", e);
        }
    }
}
