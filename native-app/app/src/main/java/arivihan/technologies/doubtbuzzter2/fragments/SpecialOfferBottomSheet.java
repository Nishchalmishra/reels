package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.CouponDiscountType;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;

public class SpecialOfferBottomSheet extends BottomSheetDialogFragment {

    TextView validTimeText;
    TextView planName;
    TextView saveText;
    TextView perMonthPrice;
    TextView costPrice;
    TextView finalPrice;
    TextView discountPercent;
    TextView studentsRegistered;
    TextView selected_cost_price;
    TextView selected_final_price;
    TextView selected_plan_name;
    LottieAnimationView confetti1,confetti2;
    ConstraintLayout process_to_pay;
    PaymentListener paymentListener;
    MediaPlayer mediaPlayer;

    MaterialCardView checkBox;
    boolean tncChecked;
    TextView tnc;
    TextView privacy_policy;

    public SpecialOfferBottomSheet(PaymentListener paymentListener) {
        this.paymentListener = paymentListener;
    }

    public SpecialOfferBottomSheet() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }



    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.special_offer_dialog, container, false);
        validTimeText=view.findViewById(R.id.valid_time_text);
        planName=view.findViewById(R.id.plan_name);
        saveText=view.findViewById(R.id.saving_badge_text);
        perMonthPrice=view.findViewById(R.id.price_per_month);
        costPrice=view.findViewById(R.id.cost_price);
        finalPrice=view.findViewById(R.id.final_price);
        discountPercent=view.findViewById(R.id.extra_discount);
        studentsRegistered=view.findViewById(R.id.students);
        confetti1=view.findViewById(R.id.confetti_right);
        confetti2=view.findViewById(R.id.confetti_left);

        selected_cost_price=view.findViewById(R.id.selected_cost_price);
        selected_final_price=view.findViewById(R.id.selected_final_price);
        selected_plan_name=view.findViewById(R.id.selected_plan_name);
        process_to_pay=view.findViewById(R.id.process_to_pay);
        checkBox=view.findViewById(R.id.tnc_checkbox);
        startAnimAndSound();
        LinearLayout checkBoxLayout = view.findViewById(R.id.tnc_checkbox_layout);
        checkBoxLayout.setOnClickListener(v -> {
            tncChecked = !tncChecked;
            if (tncChecked) {
                checkBox.setBackground(getResources().getDrawable(R.drawable.checkmark));
                checkBox.setStrokeWidth(0);
                if (paymentListener != null) paymentListener.tncClicked();
            } else {
                checkBox.setBackground(getResources().getDrawable(R.color.white));
                checkBox.setStrokeWidth(4);
                checkBox.setStrokeColor(getResources().getColor(R.color.black));
            }
        });
        checkBox.setClickable(false);
        checkBox.setFocusable(false);
        checkBox.setFocusableInTouchMode(false);
//        checkBox.setOnClickListener(v -> {
//            tncChecked = !tncChecked;
//            if (tncChecked) {
//                checkBox.setBackground(getResources().getDrawable(R.drawable.checkmark));
//                checkBox.setStrokeWidth(0);
//                if (paymentListener != null) paymentListener.tncClicked();
//            } else {
//                checkBox.setBackground(getResources().getDrawable(R.color.white));
//                checkBox.setStrokeWidth(4);
//                checkBox.setStrokeColor(getResources().getColor(R.color.black));
//            }
//        });
        tnc=view.findViewById(R.id.tnc);
        Intent i = new Intent(Intent.ACTION_VIEW);
        tnc.setOnClickListener(v -> {
            String tncUrl = "https://drive.google.com/file/d/1tdcfPX1QaYbGG8EBmzKRM86xcSsGCyPz/view";
            i.setData(Uri.parse(tncUrl));
            startActivity(i);
        });
        privacy_policy = view.findViewById(R.id.privacy_policy);
        privacy_policy.setOnClickListener(v -> {
            String privacyPolicyUrl = "https://drive.google.com/file/d/11ITZu4AIr0EUD4oHqI7dEYd6vyYS1SZJ/view";
            i.setData(Uri.parse(privacyPolicyUrl));
            startActivity(i);
        });
        SubscriptionPlans subscriptionPlans= (SubscriptionPlans) getArguments().getSerializable("data");
        setDialogData(subscriptionPlans);
        return view;
    }

    public void setDialogData(SubscriptionPlans subscriptionPlans){
        if(subscriptionPlans!=null) {
            if (subscriptionPlans.getExtraDiscountType().equals(CouponDiscountType.PERCENTAGE))
                discountPercent.setText(String.format(getContext().getString(R.string.get_extra_percent_off), subscriptionPlans.getExtraDiscountValue()));
            else
                discountPercent.setText(String.format(getContext().getString(R.string.get_extra_amount_off), subscriptionPlans.getExtraDiscountValue()));
            validTimeText.setText(String.format(getContext().getString(R.string.valid_discount), subscriptionPlans.getValidForNextTime()));
            planName.setText(subscriptionPlans.getName());
            saveText.setText(String.format(getContext().getString(R.string.you_save), subscriptionPlans.getSavingBadgeText()));
            perMonthPrice.setText(String.format(getContext().getString(R.string.price_per_month), subscriptionPlans.getPerMonthPrice()));
            costPrice.setText(String.format(getContext().getString(R.string.price_format), subscriptionPlans.getCostPrice()));
            costPrice.setPaintFlags(costPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            finalPrice.setText(String.format(getContext().getString(R.string.price_format), subscriptionPlans.getFinalPrice()));
            studentsRegistered.setText(subscriptionPlans.getStudentsRegistered() + " ");
            selected_cost_price.setText(String.format(getContext().getString(R.string.price_format), subscriptionPlans.getCostPrice()));
            selected_cost_price.setPaintFlags(selected_cost_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            selected_final_price.setText(String.format(getContext().getString(R.string.price_format), subscriptionPlans.getFinalPrice()));
            selected_plan_name.setText(subscriptionPlans.getName());
            process_to_pay.setOnClickListener(v -> {
                if (tncChecked) {
                    this.dismiss();
                    if (paymentListener != null) {
                        Log.d("planpoup", "setDialogData: "+subscriptionPlans.getFinalPrice()+"_");
                        paymentListener.onProcessClick(Integer.parseInt(subscriptionPlans.getFinalPrice()), true);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.terms_condition), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    private void startAnimAndSound() {
        confetti1.playAnimation();
        confetti2.playAnimation();
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.ding);
        mediaPlayer.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public interface PaymentListener{
        void onProcessClick(int totalPrice, boolean isFromBottomSheet);
        void tncClicked();
    }
}

