package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;

public class UnlockActivity extends BaseActivity {

    public static final String TAG = UnlockActivity.class.getSimpleName();
    public static final String BUNDLE_CONDITION_TYPE = "condition_type";
    private ImageView topLeftImage;
    private ImageView bubbleTail;
    private TextView topText;
    private TextView dropperLineText;
    private ImageView middleImage;
    private MaterialCardView unlockCard, skipCard;
    private TextView skipText;
    private Map<String, Object> navigationParams = new HashMap<>();
    private String targetClassName, conditionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlock_subscription_screen);
        initViews();
        getUIComponentsData();
        setupClicks();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedClick(true, false);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void onBackPressedClick(boolean backPressed, boolean skipPressed) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("skip", skipPressed);
        resultIntent.putExtra("back", backPressed);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void getUIComponentsData() {
        String remoteKey=PreferenceKeys.UNLOCK_SUBSCRIPTION_SCREEN_DATA_V2.getKey()
                +"_"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String remoteJson = MyApplication.getFirebaseRemoteConfig()
                .getString(remoteKey);
        if(StringUtils.isEmpty(remoteJson)){
            remoteJson=MyApplication.getStringData(PreferenceKeys.UNLOCK_SUBSCRIPTION_SCREEN_DATA_V2.getKey());
        }
        // Always initialize to avoid null issues
        navigationParams = new HashMap<>();
        targetClassName = null;

        try {
            if (remoteJson == null || remoteJson.trim().isEmpty()) {
                return; // nothing to load
            }

            JSONObject root = new JSONObject(remoteJson);

            // -------- IMAGES --------
            if (root.has("images") && !root.isNull("images")) {
                JSONObject images = root.getJSONObject("images");

                if (images.has("topLeftImage")) {
                    Glide.with(this)
                            .load(images.getString("topLeftImage"))
                            .placeholder(R.drawable.well_done_image)
                            .error(R.drawable.well_done_image)
                            .into(topLeftImage);
                }

                if (images.has("middleImage")) {
                    Glide.with(this)
                            .load(images.getString("middleImage"))
                            .placeholder(R.drawable.benefits)
                            .error(R.drawable.benefits)
                            .into(middleImage);
                }
                if (images.has("bubbleTail")) {
                    Glide.with(this)
                            .load(images.getString("bubbleTail"))
                            .into(bubbleTail);
                }
            }

            // -------- TEXT --------
            if (root.has("dropperLineText")) {
                dropperLineText.setText(root.getString("dropperLineText"));
            }

            // -------- BUTTON --------
            if (root.has("button") && !root.isNull("button")) {
                JSONObject buttonObj = root.getJSONObject("button");

                if (buttonObj.has("text")) {
                    TextView unlockText = unlockCard.findViewById(R.id.unlock_text);
                    if (unlockText != null) {
                        unlockText.setText(buttonObj.getString("text"));
                    }
                }

                if (buttonObj.has("enabled")) {
                    boolean enabled = buttonObj.getBoolean("enabled");
                    unlockCard.setEnabled(enabled);
                }

                if (buttonObj.has("backgroundColor")) {
                    try {
                        String bgColor = buttonObj.getString("backgroundColor");
                        unlockCard.setCardBackgroundColor(Color.parseColor(bgColor));
                    } catch (IllegalArgumentException e) {
                        // invalid color from remote config, ignore
                    }
                }
            }

            // -------- SKIP --------
            if (root.has("skip") && !root.isNull("skip")) {
                JSONObject skipObj = root.getJSONObject("skip");

                if (skipObj.has("text")) {
                    skipText.setText(skipObj.getString("text"));
                }

                if (skipObj.has("visible")) {
                    boolean skipVisible = skipObj.getBoolean("visible");
                    skipText.setVisibility(skipVisible ? View.VISIBLE : View.GONE);
                }
            }

            // -------- CONDITIONS --------
            if (root.has("conditions") && !root.isNull("conditions")) {
                JSONObject conditionsObj = root.getJSONObject("conditions");

                conditionType = getIntent().getStringExtra(BUNDLE_CONDITION_TYPE);

                if (conditionType != null && conditionsObj.has(conditionType)) {
                    topText.setText(conditionsObj.getString(conditionType));
                } else {
                    topText.setText("");
                }
            }

            // -------- NAVIGATION CONFIG --------

            // className
            if (root.has("className") && !root.isNull("className")) {
                targetClassName = root.getString("className");
            }

            // params (safe for {}, missing, null)
            if (root.has("params") && !root.isNull("params")) {

                JSONObject paramsObj = root.getJSONObject("params");

                Iterator<String> keys = paramsObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = paramsObj.get(key);

                    if (value instanceof String ||
                            value instanceof Integer ||
                            value instanceof Boolean ||
                            value instanceof Double) {

                        navigationParams.put(key, value);

                    } else if (value instanceof JSONObject) {
                        navigationParams.put(key, jsonToMap((JSONObject) value));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Object> jsonToMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> keys = object.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = object.get(key);

            if (value instanceof JSONObject) {
                map.put(key, jsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.put(key, value.toString()); // or handle list if needed
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    private void initViews() {
        topLeftImage = findViewById(R.id.topLeftImage);
        bubbleTail = findViewById(R.id.bubbleTail);
        topText = findViewById(R.id.topText);
        dropperLineText = findViewById(R.id.dropperLineText);
        middleImage = findViewById(R.id.middleImage);
        unlockCard = findViewById(R.id.unlockCard);
        skipCard = findViewById(R.id.skipCard);
        skipText = findViewById(R.id.skipText);
    }

    private void setupClicks() {

        unlockCard.setOnClickListener(v -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("type", conditionType!=null?conditionType:"");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_completion_screen_join_now_clicked.getActionName(), params);
            } catch (Exception ignored){}

            if (targetClassName != null) {
                if (navigationParams == null) navigationParams = new HashMap<>();
                Object source = MyApplication.isCompetitiveProfile() ? "NEET_BENEFIT_SCREEN" : "UNLOCK_ACTIVITY";
                navigationParams.put(SubscriptionNavigationHelper.EXTRA_SOURCE, source);
                MyApplication.openActivity(
                        UnlockActivity.this,
                        targetClassName,
                        navigationParams
                );
                finish();
            }
        });

        skipCard.setOnClickListener(v -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("type", conditionType != null ? conditionType : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_completion_screen_skip_clicked.getActionName(), params);
            } catch (Exception ignored){}
            onBackPressedClick(false, true);
        });
    }

}
