package arivihan.technologies.doubtbuzzter2.customView;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Iterator;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.CropQRTransformation;



public class SubscriptionFailedDialog extends Dialog {

    private enum State {LOADING, OPTIONS, CONTENT_READY, ERROR, DISMISSED}
    private State currentState = State.LOADING;


    //new
    public static final String TAG = "SubscriptionFailedDialog";

    String qrImageUrl;

    private boolean      shouldCropQR   = true;


    //view
    private RelativeLayout dialogContainer;
    private CardView cv_close;
    private ImageView QRCodePaymentImage;
    private TextView helpDeskTitle, helpDeskSubTitle, qrSectionTitle, qrSectionSubTitle;
    private ShimmerFrameLayout shimmerContainer, shimmerFrameLayout;
    private LinearLayout containerView;
    private LinearLayout bottomView;
    private MaterialCardView googlePayCard, phonePeCard;
    private MaterialCardView shareQR_Btn, otherPaymentOption, helpContactBtn;



    @SuppressLint("MissingInflatedId")
    public SubscriptionFailedDialog(@NonNull Context context, String imageUrl) {
        super(context);
        this.qrImageUrl = imageUrl;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.layout_subscription_failed_bottomsheet, null);
        setContentView(view);

        bindViews(view);
        configureWindow();
    }




    private void configureWindow() {
        Window window = getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = 25;

        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }





    private void bindViews(View view) {
        dialogContainer = view.findViewById(R.id.dialogContainer);

        cv_close = view.findViewById(R.id.cv_close);
        shimmerContainer = view.findViewById(R.id.shimmer_container);
        containerView = view.findViewById(R.id.container_view);
        bottomView = view.findViewById(R.id.layout_bottom_view);

        //text
        helpDeskTitle = view.findViewById(R.id.help_desk_title);
        helpDeskSubTitle = view.findViewById(R.id.help_desk_subtitle);
        qrSectionTitle = view.findViewById(R.id.qr_section_title);
        qrSectionSubTitle = view.findViewById(R.id.qr_section_subtitle);

        shimmerFrameLayout = findViewById(R.id.qr_code_shimmer);
        QRCodePaymentImage = view.findViewById(R.id.qr_code_payment_image);

        shareQR_Btn = view.findViewById(R.id.share_qr_btn);
        otherPaymentOption = view.findViewById(R.id.other_payment_option);
        helpContactBtn = view.findViewById(R.id.help_contact_btn);

        googlePayCard = view.findViewById(R.id.google_pay_card);
        phonePeCard = view.findViewById(R.id.phone_pe_card);

        cv_close.setOnClickListener(v -> {
            dismiss();
        });


        //set view
        setListenerAndQRImage();
        setViewText();
    }


    private void setListenerAndQRImage() {
        setLoading(true);

        // google pay
        googlePayCard.setOnClickListener(view -> {
            if (paymentActionListener != null) paymentActionListener.onGooglePayClick();
        });

        // phone pe
        phonePeCard.setOnClickListener(view -> {
            if (paymentActionListener != null) paymentActionListener.onPhonePeClick();
        });

        // share qr
        shareQR_Btn.setOnClickListener(view -> {
            if (paymentActionListener != null) paymentActionListener.onShareQrClick();
        });

        // other payment option
        otherPaymentOption.setOnClickListener(view -> {
            if (paymentActionListener != null) paymentActionListener.onOtherPaymentClick();
        });

        // help contact
        helpContactBtn.setOnClickListener(view -> {
            if (paymentActionListener != null) paymentActionListener.onHelpContactClick();
        });


        Log.d(TAG, ": qrImageUrl= " + qrImageUrl);
        showQRShimmer(true);

        GlideUrl url = new GlideUrl(qrImageUrl);
        Glide.with(getContext().getApplicationContext())
                .load(url)
                .apply(shouldCropQR
                        ? com.bumptech.glide.request.RequestOptions
                        .bitmapTransform(new CropQRTransformation())
                        : new com.bumptech.glide.request.RequestOptions())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        showQRShimmer(false);
                        return false;
                    }
                })
                .into(QRCodePaymentImage);




        //set lading false
        setLoading(false);

    }



    public void setViewText() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("subscription_failed_help_text");
            if (StringUtils.isEmpty(json)) return;

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            JSONObject root = new JSONObject(json);

            helpDeskTitle.setText(root.optJSONObject("help_info_title").optString(lang, ""));
            helpDeskSubTitle.setText(root.optJSONObject("help_info_subtitle").optString(lang, ""));
            qrSectionTitle.setText(root.optJSONObject("qr_dialog_title").optString(lang, ""));
            qrSectionSubTitle.setText(root.optJSONObject("qr_dialog_subtitle").optString(lang, ""));
        } catch (Exception e) {
            Log.e("RemoteConfig", "error = " + e.getMessage() );
        }
    }



    private void setLoading(boolean show){
        if (show){
            shimmerContainer.startShimmer();
            shimmerContainer.setVisibility(View.VISIBLE);
            containerView.setVisibility(View.GONE);
            bottomView.setVisibility(View.GONE);
        }
        else {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            containerView.setVisibility(View.VISIBLE);
            bottomView.setVisibility(View.VISIBLE);
        }
    }


    private void showQRShimmer(boolean show) {
        if (show) {
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            QRCodePaymentImage.setVisibility(View.GONE);
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            QRCodePaymentImage.setVisibility(View.VISIBLE);
        }
    }



    private void expandDialogAfterWebViewLoad() {
        currentState = State.CONTENT_READY;
        setCanceledOnTouchOutside(false);
        setCancelable(true);

        // Start height at 30% of screen (preview mode)
        int previewHeight = (int) (getScreenHeight() * 0.3f);
        setDialogHeight(previewHeight);

        // Fade + slide-in effect
        dialogContainer.setAlpha(0f);
        dialogContainer.setTranslationY(30);

        dialogContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Wait until layout is ready
        dialogContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                dialogContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (currentState != State.CONTENT_READY) return;

                // Measure height
                dialogContainer.measure(
                        View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                int measuredHeight = dialogContainer.getMeasuredHeight();
                int maxHeight = (int) (getScreenHeight() * 0.8f);

                Log.d("expandDialogAfterWebViewLoad", "Measured height: " + measuredHeight);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    animateHeightTransition(previewHeight, Math.min(measuredHeight, maxHeight));
                }, 1000);
            }
        });
    }


    private int getScreenHeight() {
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    private void animateHeightTransition(int fromHeight, int toHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(fromHeight, toHeight);
        animator.setDuration(400); // smooth animation
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedHeight = (Integer) animation.getAnimatedValue();
            setDialogHeight(animatedHeight);
        });

        animator.start();
    }

    private void setDialogHeight(int height) {
        Window window = getWindow();
        if (window == null) return;

        View decorView = window.getDecorView();
        if (decorView == null || !decorView.isAttachedToWindow()) return;

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }





    private PaymentActionListener paymentActionListener;

    public void setPaymentActionListener(
            PaymentActionListener listener
    ) {

        this.paymentActionListener = listener;
    }



    public interface PaymentActionListener {
        void onGooglePayClick();
        void onPhonePeClick();
        void onShareQrClick();
        void onOtherPaymentClick();
        void onHelpContactClick();
    }




}