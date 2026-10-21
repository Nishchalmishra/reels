package arivihan.technologies.doubtbuzzter2.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class RegisterCardView extends RelativeLayout   {

    private CardView mainCard, btnCard, closeCard;
    private RelativeLayout rvCustomHomeStatusMainLayout;
    private TextView tvTitle, tvSubTitle, btnTxt;
    private ImageView badgeImg;
    private MainDashBoardListener dashboardListener;

    public void setMainDashboardListener(MainDashBoardListener listener) {
        this.dashboardListener = listener;
    }

    public RegisterCardView(Context context) {
        super(context);
        init(context, null);
    }

    public RegisterCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public RegisterCardView(Context context, String jsonString) {
        super(context);
        init(context, jsonString);
    }

    @SuppressLint("MissingInflatedId")
    private void init(Context context, String jsonString) {
        // Inflate layout into this custom view
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.custom_home_status_secondary_layout, this, true);

        // Find all child views
        rvCustomHomeStatusMainLayout = rootView.findViewById(R.id.custom_home_status_main_layout);
        mainCard = rootView.findViewById(R.id.main_card);
        tvTitle = rootView.findViewById(R.id.tv_title);
        tvSubTitle = rootView.findViewById(R.id.tv_sub_title);
        btnTxt = rootView.findViewById(R.id.btn_txt);
        badgeImg = rootView.findViewById(R.id.img_home_status_view);
        closeCard = rootView.findViewById(R.id.close);
        btnCard = rootView.findViewById(R.id.btn_card);

        if (jsonString != null) applyJson(jsonString);

        btnCard.setOnClickListener(v -> {
            rvCustomHomeStatusMainLayout.setVisibility(GONE);
            dashboardListener.onDismiss(MyApplication.getStringData(PreferenceKeys.DIALOG_BANNER_TEXT.getKey())
                    ,MyApplication.getStringData(PreferenceKeys.DIALOG_BANNER_IMAGE.getKey()),
                    MyApplication.getLongData(PreferenceKeys.DIALOG_TIMEOUT.getKey()));
            MyApplication.deleteLiveSessionStatus();
            notifyDashboardListener();
            MyApplication.registerForLiveSession();
        });

        closeCard.setOnClickListener(v -> {
            rvCustomHomeStatusMainLayout.setVisibility(GONE);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_skipped_second_live_engagement_reminder.getActionName());
            notifyDashboardListener();
            MyApplication.deleteLiveSessionStatus();
        });
    }

    public void applyJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            if (json.has("title")) {
                tvTitle.setText(json.getString("title"));
            }
            if (json.has("subTitle")) {
                tvSubTitle.setText(json.getString("subTitle"));
            }
            if (json.has("buttonText")) {
                btnTxt.setText(json.getString("buttonText"));
            }
            if (json.has("badgeImageUrl")) {
                String imageUrl = json.getString("badgeImageUrl");

                Glide.with(getContext())
                        .load(imageUrl)
                        .into(badgeImg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifyDashboardListener() {
        if (dashboardListener != null) {
            dashboardListener.setAskDoubtMargin();
        }
    }

    public interface MainDashBoardListener {
        void setAskDoubtMargin();

        void onDismiss(String dialogBannerText, String dialogBannerImage, long dialogTimeout);
    }

}
