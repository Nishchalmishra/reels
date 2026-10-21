package arivihan.technologies.doubtbuzzter2.model;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import arivihan.technologies.doubtbuzzter2.R;

public class SubscriptionRenewalReminderSheet extends BottomSheetDialogFragment {

    private static final String ARG_REMINDER = "arg_reminder";

    private ImageView close;
    private TextView title;
    private TextView subtitle;
    private ImageView poster;
    private CardView later;
    private CardView renew;

    private SubscriptionRenewalReminder subscriptionRenewalReminder;
    private BottomSheetListener bottomSheetListener;

    public SubscriptionRenewalReminderSheet() {
    }

    public static SubscriptionRenewalReminderSheet newInstance(
            SubscriptionRenewalReminder reminder,
            BottomSheetListener listener
    ) {
        SubscriptionRenewalReminderSheet sheet = new SubscriptionRenewalReminderSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder); // Ensure SubscriptionRenewalReminder implements Serializable
        sheet.setArguments(args);
        sheet.setBottomSheetListener(listener);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);

        if (getArguments() != null) {
            subscriptionRenewalReminder =
                    (SubscriptionRenewalReminder) getArguments().getSerializable(ARG_REMINDER);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.subscription_expiration_reminder, container, false);

        close = view.findViewById(R.id.close);
        title = view.findViewById(R.id.title);
        subtitle = view.findViewById(R.id.subtitle);
        poster = view.findViewById(R.id.poster);
        later = view.findViewById(R.id.later_card);
        renew = view.findViewById(R.id.renew_card);

        // Load poster if available
        if (subscriptionRenewalReminder != null && subscriptionRenewalReminder.poster != null) {
            Glide.with(requireContext())
                    .load(subscriptionRenewalReminder.poster)
                    .placeholder(R.drawable.white_logo)
                    .error(R.drawable.default_error)
                    .into(poster);
        }

        if (subscriptionRenewalReminder != null) {
            title.setText(HtmlCompat.fromHtml(subscriptionRenewalReminder.reminderTitle,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            subtitle.setText(HtmlCompat.fromHtml(subscriptionRenewalReminder.reminderText,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        close.setOnClickListener(v -> {
            if (bottomSheetListener != null) bottomSheetListener.onCloseClick();
            dismiss();
        });

        later.setOnClickListener(v -> {
            if (bottomSheetListener != null) bottomSheetListener.onLaterClick();
            dismiss();
        });

        renew.setOnClickListener(v -> {
            if (bottomSheetListener != null) bottomSheetListener.onRenewClick();
            dismiss();
        });

        return view;
    }

    public void setBottomSheetListener(BottomSheetListener listener) {
        this.bottomSheetListener = listener;
    }

    public interface BottomSheetListener {
        void onRenewClick();
        void onLaterClick();
        void onCloseClick();
    }
}
