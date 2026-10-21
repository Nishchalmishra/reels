package arivihan.technologies.doubtbuzzter2.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.internal.StringUtil;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.PlanLevel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionStatus;
import arivihan.technologies.doubtbuzzter2.model.TransactionModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MySubscriptionAdapter extends RecyclerView.Adapter<MySubscriptionAdapter.ViewHolder> {
    private List<TransactionModel> model;
    private Context context;
    private ItemOnClick itemOnClick;

    public MySubscriptionAdapter(List<TransactionModel> model, Context context, ItemOnClick itemOnClick) {
        this.model = model;
        this.context = context;
        this.itemOnClick = itemOnClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.my_subscription_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        TransactionModel transactionModel = model.get(position);
        PlanLevel planLevel = PlanLevel.fromString(transactionModel.getSubscriptionPlanLevel());
        if (PaymentStatus.FAILED.name().equals(transactionModel.getPaymentStatus())) {
            holder.subscription_type_card.setVisibility(View.INVISIBLE);
            holder.pending_text.setText(context.getString(R.string.Payment_Failed));
            holder.pending_text.setTextColor(context.getColor(R.color.locked_red));
            holder.pending_badge.setVisibility(View.VISIBLE);
            holder.pending_badge_image.setBackground(context.getDrawable(R.drawable.badge_red));
        } else if(PaymentStatus.PENDING.name().equals(transactionModel.getPaymentStatus())){
            holder.subscription_type_card.setVisibility(View.INVISIBLE);
            holder.pending_text.setText(context.getString(R.string.Payment_Pending));
            holder.pending_text.setTextColor(context.getColor(R.color.white));
            holder.pending_badge.setVisibility(View.VISIBLE);
            holder.pending_badge_image.setBackground(context.getDrawable(R.drawable.best_value_tag));
        }else {
            holder.subscription_type_card.setVisibility(View.VISIBLE);
            if (planLevel != null) {
                holder.subscription_type.setText("hi".equals(lang) || "hi-rIN".equals(lang)
                        ? planLevel.getHindiTitle()
                        : planLevel.getTitle());
            } else {
                holder.subscription_type.setVisibility(View.GONE); // Fallback if unknown PlanLevel
            }
            holder.pending_badge.setVisibility(View.GONE);
        }
        holder.subscription_name.setText(model.get(position).getTitle());
        if (PaymentStatus.FAILED.name().equals(transactionModel.getPaymentStatus()) ||
               PaymentStatus.PENDING.name().equals(transactionModel.getPaymentStatus())) {
            if (planLevel != null) {
                holder.subscription_type.setVisibility(View.VISIBLE);
                holder.subscription_type.setText("hi".equals(lang) || "hi-rIN".equals(lang)
                        ? planLevel.getHindiTitle()
                        : planLevel.getTitle());
            } else {
                holder.subscription_type.setVisibility(View.GONE);
            }
            holder.validity_text.setTextColor(context.getColor(R.color.subscription_blue));
            holder.validity_text_card.setCardBackgroundColor(context.getColor(R.color.my_subscriptions_lightest_blue));
            holder.buy_again.setVisibility(View.GONE);
        } else {
            if (SubscriptionStatus.EXPIRED.equals(transactionModel.getSubscriptionStatus())) {
                holder.validity_text.setText(String.format(context.getString(R.string.Expired_on),model.get(position).getDate()));
                holder.validity_text.setTextColor(context.getColor(R.color.my_subscriptions_dark_red));
                holder.validity_text_card.setCardBackgroundColor(context.getColor(R.color.mock_light_red));
                holder.buy_again.setVisibility(View.VISIBLE);
            } else {
                holder.validity_text.setText(String.format(context.getString(R.string.Valid_upto),model.get(position).getDate()));
                holder.validity_text.setTextColor(context.getColor(R.color.medium_marks_text));
                holder.validity_text_card.setCardBackgroundColor(context.getColor(R.color.medium_marks_card));
                holder.buy_again.setVisibility(View.GONE);
            }
        }
        holder.subscription_price.setText(model.get(position).getPrice());

        if (transactionModel.getPaymentStatus().equals(PaymentStatus.FAILED.name())) {
            holder.invoice_text.setTextColor(context.getColor(R.color.locked_red));
            holder.invoice_icon.setImageDrawable(context.getDrawable(R.drawable.download_outline_red));
            holder.course_benefits.setText(context.getString(R.string.Raise_a_Concern));
            holder.invoice_text.setText(context.getString(R.string.Invoice));
            holder.course_benefits.setTextColor(context.getColor(R.color.locked_red));
            holder.course_benefits_icon.setImageDrawable(context.getDrawable(R.drawable.red_right_v));
            holder.underline.setBackgroundColor(context.getColor(R.color.locked_red));
            holder.bottom_layout.setBackgroundColor(context.getColor(R.color.mock_light_red));
        } else if (transactionModel.getPaymentStatus().equals(PaymentStatus.PENDING.name())) {
            holder.invoice_text.setTextColor(context.getColor(R.color.subscription_blue));
            holder.invoice_text.setText(context.getString(R.string.recheck_payment_status));
            holder.invoice_icon.setImageDrawable(context.getDrawable(R.drawable.recheck));
            holder.course_benefits.setText(context.getString(R.string.Raise_a_Concern));
            holder.course_benefits.setTextColor(context.getColor(R.color.subscription_blue));
            holder.course_benefits_icon.setImageDrawable(context.getDrawable(R.drawable.blue_right_v));
            holder.underline.setBackgroundColor(context.getColor(R.color.subscription_blue));
            holder.bottom_layout.setBackgroundColor(context.getColor(R.color.my_subscriptions_lightest_blue));
        } else {
            holder.invoice_text.setTextColor(context.getColor(R.color.subscription_blue));
            holder.invoice_icon.setImageDrawable(context.getDrawable(R.drawable.download_invoice));
            holder.course_benefits.setText(context.getString(R.string.Course_Benefits));
            holder.invoice_text.setText(context.getString(R.string.Invoice));
            holder.course_benefits.setTextColor(context.getColor(R.color.subscription_blue));
            holder.course_benefits_icon.setImageDrawable(context.getDrawable(R.drawable.blue_right_v));
            holder.underline.setBackgroundColor(context.getColor(R.color.subscription_blue));
            holder.bottom_layout.setBackgroundColor(context.getColor(R.color.my_subscriptions_lightest_blue));
        }


        if (StringUtil.isBlank(model.get(position).getInvoiceDownloadUrl())) {
            holder.downloadInvoice.setVisibility(View.INVISIBLE);
        } else {
            holder.downloadInvoice.setVisibility(View.VISIBLE);
        }

        holder.buy_again.setOnClickListener(view -> {
            itemOnClick.buyAgainClick(transactionModel);
        });
        holder.susbcription_benefits.setOnClickListener(view -> {
            itemOnClick.courseBenefitsClick(transactionModel);
        });
        holder.downloadInvoice.setOnClickListener(view -> {
            if (PaymentStatus.PENDING.name().equals(transactionModel.getPaymentStatus())) {
                Log.d("PaymentCheck", "onBindViewHolder: "+transactionModel.getId()+"transactionModel"+transactionModel);
                itemOnClick.showPaymentCheckingDialog(transactionModel.getId(),transactionModel.getSubscriptionPlanId());
            }else {
                itemOnClick.invoiceDownloadClick(transactionModel.getInvoiceDownloadUrl());
            }
        });
        holder.itemView.setOnClickListener(view -> {
            Log.d("subcriptionclick", "onBindViewHolder: ");
            if (PaymentStatus.PENDING.name().equals(transactionModel.getPaymentStatus())) {
                Log.d("subcriptionclick", "onBindViewHolder: show dialog ");
               itemOnClick.showPaymentCheckingDialog(transactionModel.getId(), transactionModel.getSubscriptionPlanId());
            }else {
                itemOnClick.invoiceDownloadClick(transactionModel.getInvoiceDownloadUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView subscription_type, subscription_name, subscription_price,
                validity_text,invoice_text,course_benefits,pending_text;
        private LinearLayout downloadInvoice, susbcription_benefits, underline, buy_again,bottom_layout;
        private ConstraintLayout pending_badge;
        private CardView subscription_type_card, validity_text_card;
        private ImageView invoice_icon,course_benefits_icon,pending_badge_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subscription_type = itemView.findViewById(R.id.subscription_type);
            subscription_name = itemView.findViewById(R.id.subscription_name);
            subscription_price = itemView.findViewById(R.id.subscription_price);
            validity_text = itemView.findViewById(R.id.validity_text);
            downloadInvoice = itemView.findViewById(R.id.invoice_download);
            susbcription_benefits = itemView.findViewById(R.id.susbcription_benefits);
            underline = itemView.findViewById(R.id.underline);
            pending_badge = itemView.findViewById(R.id.pending_badge);
            pending_badge_image = itemView.findViewById(R.id.pending_badge_image);
            subscription_type_card = itemView.findViewById(R.id.subscription_type_card);
            validity_text_card = itemView.findViewById(R.id.validity_text_card);
            buy_again = itemView.findViewById(R.id.buy_again);
            invoice_text = itemView.findViewById(R.id.invoice_text);
            invoice_icon = itemView.findViewById(R.id.invoice_icon);
            course_benefits = itemView.findViewById(R.id.course_benefits);
            course_benefits_icon = itemView.findViewById(R.id.course_benefits_icon);
            bottom_layout = itemView.findViewById(R.id.bottom_layout);
            pending_text = itemView.findViewById(R.id.pending_text);
        }
    }

    public interface ItemOnClick {
        void buyAgainClick(TransactionModel transactionModel);
        void courseBenefitsClick(TransactionModel transactionModel);
        void invoiceDownloadClick(String url);
        void showPaymentCheckingDialog(String transactionId, String subscriptionPlanId);
    }
}
