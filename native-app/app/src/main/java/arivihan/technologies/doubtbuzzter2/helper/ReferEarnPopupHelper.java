package arivihan.technologies.doubtbuzzter2.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.refer.ReferEarnPopupDto;

/**
 * Manages the Refer & Earn popup lifecycle:
 * - Listens to Firebase RDB at users/refer-earn-popup/{userId}/
 * - Shows / updates a dialog when data arrives
 * - Deletes the RDB entry on dismiss
 * - Properly cleans up listener and dialog to prevent leaks
 */
public class ReferEarnPopupHelper {

    private static final String TAG = "ReferEarnPopupHelper";
    private static final String RDB_PATH_PREFIX = "users/refer-and-earn-popups/";

    private final Activity activity;
    private DatabaseReference dbRef;
    private ValueEventListener valueEventListener;
    private Dialog dialog;
    private boolean isListening;

    public ReferEarnPopupHelper(@NonNull Activity activity) {
        this.activity = activity;
    }

    // ── Public lifecycle methods ────────────────────────────────────────────────

    /**
     * Start listening to the RDB node. Call from Activity's post-setup (e.g. after onResume checks).
     */
    public void startListening() {
        if (isListening) return;

        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        if (StringUtils.isEmpty(userId)) return;

        String path = RDB_PATH_PREFIX + userId;
        dbRef = FirebaseDatabase.getInstance().getReference(path);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isActivityDead()) return;

                if (!snapshot.exists()) {
                    dismissDialogSilently();
                    return;
                }

                try {
                    Log.d(TAG, "---> Refer Earn Popup data received: " + snapshot.toString());
                    ReferEarnPopupDto data = snapshot.getValue(ReferEarnPopupDto.class);
                    if (data != null) {
                        // Firebase may fail to auto-deserialize List<String> subtitleItems
                        // Manual fallback: parse from snapshot children
                        if (data.subtitleItems == null || data.subtitleItems.isEmpty()) {
                            DataSnapshot itemsSnap = snapshot.child("subtitleItems");
                            if (itemsSnap.exists()) {
                                List<String> items = new ArrayList<>();
                                for (DataSnapshot child : itemsSnap.getChildren()) {
                                    String val = child.getValue(String.class);
                                    if (val != null) items.add(val);
                                }
                                if (!items.isEmpty()) {
                                    data.subtitleItems = items;
                                }
                            }
                        }
                        Log.d(TAG, "---> title=" + data.title
                                + ", subtitle=" + data.subtitle
                                + ", subtitleItems=" + (data.subtitleItems != null ? data.subtitleItems.size() : "null")
                                + ", buttons=" + (data.buttons != null ? data.buttons.size() : "null"));
                        showOrUpdatePopup(data);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse popup data", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "RDB listener cancelled: " + error.getMessage());
            }
        };

        dbRef.addValueEventListener(valueEventListener);
        isListening = true;
    }

    /**
     * Stop listening and clean up. Call from Activity's onDestroy().
     */
    public void stopListening() {
        if (dbRef != null && valueEventListener != null) {
            dbRef.removeEventListener(valueEventListener);
        }
        valueEventListener = null;
        isListening = false;
        dismissDialogSilently();
    }

    // ── Dialog management ───────────────────────────────────────────────────────

    private void showOrUpdatePopup(ReferEarnPopupDto data) {
        if (isActivityDead()) return;

        try {
            // If dialog is already showing, update its content
            if (dialog != null && dialog.isShowing()) {
                bindDialogContent(dialog, data);
                return;
            }

            // Create new dialog
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_refer_earn_popup);
            dialog.setCancelable(true);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // On dismiss (back press, outside touch) → delete RDB entry
            dialog.setOnDismissListener(d -> deleteRdbEntry());

            bindDialogContent(dialog, data);

            if (!isActivityDead()) {
                dialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "showOrUpdatePopup error", e);
        }
    }

    private void bindDialogContent(Dialog dialog, ReferEarnPopupDto data) {
        // Close button
        ImageView closeBtn = dialog.findViewById(R.id.iv_popup_close);
        closeBtn.setOnClickListener(v -> {
            try { dialog.dismiss(); } catch (Exception ignored) {}
        });

        // Image
        ImageView image = dialog.findViewById(R.id.iv_popup_image);
        if (StringUtils.isNotEmpty(data.imageUrl)) {
            image.setVisibility(View.VISIBLE);
            if (!isActivityDead()) {
                Glide.with(activity)
                        .load(data.imageUrl)
                        .into(image);
            }
        } else {
            image.setVisibility(View.GONE);
        }

        // Title
        TextView title = dialog.findViewById(R.id.tv_popup_title);
        if (StringUtils.isNotEmpty(data.title)) {
            title.setVisibility(View.VISIBLE);
            title.setText(decodeText(data.title));
        } else {
            title.setVisibility(View.GONE);
        }

        // Subtitle (single text)
        TextView subtitle = dialog.findViewById(R.id.tv_popup_subtitle);
        // Subtitle Items (bullet points)
        LinearLayout subtitleItemsContainer = dialog.findViewById(R.id.ll_popup_subtitle_items);

        if (data.subtitleItems != null && !data.subtitleItems.isEmpty()
                && !(StringUtils.isNotEmpty(data.subtitle) && data.subtitleItems.size() == 1)) {
            subtitle.setVisibility(View.GONE);
            subtitleItemsContainer.setVisibility(View.VISIBLE);
            subtitleItemsContainer.removeAllViews();
            for (String item : data.subtitleItems) {
                if (StringUtils.isNotEmpty(item)) {
                    subtitleItemsContainer.addView(createBulletRow(decodeText(item)));
                }
            }
        } else if (StringUtils.isNotEmpty(data.subtitle)) {
            // Show single subtitle text, hide bullet container
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(decodeText(data.subtitle));
            subtitleItemsContainer.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.GONE);
            subtitleItemsContainer.setVisibility(View.GONE);
        }

        // Buttons
        LinearLayout buttonsRow = dialog.findViewById(R.id.ll_popup_buttons);
        MaterialCardView cvSecondary = dialog.findViewById(R.id.cv_popup_secondary);
        MaterialCardView cvPrimary = dialog.findViewById(R.id.cv_popup_primary);
        TextView tvSecondary = dialog.findViewById(R.id.tv_popup_secondary);
        TextView tvPrimary = dialog.findViewById(R.id.tv_popup_primary);

        List<ReferEarnPopupDto.PopupButton> buttons = data.buttons;
        if (buttons == null || buttons.isEmpty()) {
            buttonsRow.setVisibility(View.GONE);
            return;
        }

        buttonsRow.setVisibility(View.VISIBLE);

        if (buttons.size() == 1) {
            // Single button — show as primary, hide secondary
            cvSecondary.setVisibility(View.GONE);
            cvPrimary.setVisibility(View.VISIBLE);
            setupButton(cvPrimary, tvPrimary, buttons.get(0));
        } else {
            // Two buttons
            cvSecondary.setVisibility(View.VISIBLE);
            cvPrimary.setVisibility(View.VISIBLE);
            setupButton(cvSecondary, tvSecondary, buttons.get(0));
            setupButton(cvPrimary, tvPrimary, buttons.get(1));
        }
    }

    private void setupButton(MaterialCardView card, TextView text,
                             ReferEarnPopupDto.PopupButton btnData) {
        text.setText(decodeText(btnData.buttonText));

        // Apply colors
        try {
//            if (StringUtils.isNotEmpty(btnData.bgColorCode)) {
//                card.setCardBackgroundColor(Color.parseColor(btnData.bgColorCode));
//            }
//            if (StringUtils.isNotEmpty(btnData.textColorCode)) {
//                text.setTextColor(Color.parseColor(btnData.textColorCode));
//            }
        } catch (Exception e) {
            Log.e(TAG, "Color parse error", e);
        }

        card.setOnClickListener(v -> {
            handleButtonAction(btnData);
            try { if (dialog != null) dialog.dismiss(); } catch (Exception ignored) {}
        });
    }

    // ── Button action handling ──────────────────────────────────────────────────

    private void handleButtonAction(ReferEarnPopupDto.PopupButton btnData) {
        if (isActivityDead()) return;
        if (btnData.actionType == null) return;

        try {
            switch (btnData.actionType) {
                case "OPEN_ACTIVITY":
                    openActivity(btnData);
                    break;
                case "OPEN_URL":
                    openUrl(btnData);
                    break;
                case "DISMISS":
                    // Dialog is already dismissed by the click handler
                    break;
                default:
                    Log.w(TAG, "Unknown actionType: " + btnData.actionType);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "handleButtonAction error", e);
        }
    }

    private void openActivity(ReferEarnPopupDto.PopupButton btnData) {
        try {
            if (StringUtils.isEmpty(btnData.className)) return;

            Class<?> targetClass = ActivityLauncher.resolveClass(btnData.className);
            Intent intent = new Intent(activity, targetClass);

            if (StringUtils.isNotEmpty(btnData.activityParams)) {
                try {
                    JSONObject params = new JSONObject(btnData.activityParams);
                    Iterator<String> keys = params.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object value = params.get(key);
                        if (value instanceof String) {
                            intent.putExtra(key, (String) value);
                        } else if (value instanceof Integer) {
                            intent.putExtra(key, (int) value);
                        } else if (value instanceof Boolean) {
                            intent.putExtra(key, (boolean) value);
                        }
                    }
                } catch (JSONException e) {
                    // activityParams is not JSON — pass it as a plain string extra
                    intent.putExtra("params", btnData.activityParams);
                    Log.w(TAG, "activityParams is not JSON, passed as plain string: " + btnData.activityParams);
                }
            }

            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "openActivity error", e);
        }
    }

    private void openUrl(ReferEarnPopupDto.PopupButton btnData) {
        try {
            if (StringUtils.isEmpty(btnData.activityParams)) return;
            JSONObject params = new JSONObject(btnData.activityParams);
            String url = params.optString("link", "");
            if (StringUtils.isNotEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "openUrl error", e);
        }
    }

    // ── RDB cleanup ─────────────────────────────────────────────────────────────

    private void deleteRdbEntry() {
        if (dbRef != null) {
            dbRef.removeValue()
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to delete RDB entry", e));
        }
    }

    // ── Subtitle bullet row builder ────────────────────────────────────────────

    private LinearLayout createBulletRow(CharSequence text) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.START);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = 8;
        row.setLayoutParams(rowLp);

        // Small teal circle dot
        View dot = new View(activity);
        int dotSize = (int) (8 * activity.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
        int dotTopMargin = (int) (6 * activity.getResources().getDisplayMetrics().density);
        dotLp.topMargin = dotTopMargin;
        dotLp.setMarginEnd((int) (10 * activity.getResources().getDisplayMetrics().density));
        dot.setLayoutParams(dotLp);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(activity.getResources().getColor(R.color.arivihan_secondary_color));
        dot.setBackground(circle);
        row.addView(dot);

        // Text
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(activity.getResources().getColor(R.color.text_gray));
        tv.setGravity(android.view.Gravity.START);
        try {
            tv.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.urbanist_semi_bold));
        } catch (Exception ignored) {}
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(tvLp);
        row.addView(tv);

        return row;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private void dismissDialogSilently() {
        try {
            if (dialog != null && dialog.isShowing()) {
                // Remove dismiss listener to avoid deleting RDB entry when we dismiss programmatically
                dialog.setOnDismissListener(null);
                dialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "dismissDialogSilently error", e);
        }
        dialog = null;
    }

    /**
     * Decodes text that may contain HTML entities (&#127881;) or
     * Java-style Unicode escapes (\\uD83C\\uDF89) into actual characters.
     */
    private CharSequence decodeText(String text) {
        if (text == null) return null;
        try {
            // Handle HTML entities like &#127881; or &#x1F389;
            if (text.contains("&#")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
                }
            }
            // Handle Java-style Unicode escapes like \\uD83C\\uDF89
            if (text.contains("\\u")) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                while (i < text.length()) {
                    if (i + 5 < text.length() && text.charAt(i) == '\\' && text.charAt(i + 1) == 'u') {
                        String hex = text.substring(i + 2, i + 6);
                        sb.append((char) Integer.parseInt(hex, 16));
                        i += 6;
                    } else {
                        sb.append(text.charAt(i));
                        i++;
                    }
                }
                return sb.toString();
            }
        } catch (Exception ignored) {
        }
        return text;
    }

    private boolean isActivityDead() {
        return activity.isFinishing() || activity.isDestroyed();
    }
}
