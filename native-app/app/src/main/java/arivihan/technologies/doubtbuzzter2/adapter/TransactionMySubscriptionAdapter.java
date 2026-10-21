package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.internal.StringUtil;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.TransactionModel;

public class TransactionMySubscriptionAdapter extends RecyclerView.Adapter<TransactionMySubscriptionAdapter.ViewHolder> {

    List<TransactionModel> model;
    private ItemOnClick itemOnClick;
    public TransactionMySubscriptionAdapter(List<TransactionModel> model, ItemOnClick itemOnClick) {
        this.model = model;
        this.itemOnClick = itemOnClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.transaction_layout,parent,false);
        return new TransactionMySubscriptionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (model == null || model.isEmpty() || position >= model.size()) {
            return;
        }
        holder.title.setText(model.get(position).getTitle());
        holder.subtitle.setText(model.get(position).getOrderNo());
        holder.liltitle.setText(model.get(position).getDate());
        holder.price.setText(model.get(position).getPrice());
        if (!StringUtil.isBlank(model.get(position).getPaymentStatus())) {
            String paymentStatus = model.get(position).getPaymentStatus();
            PaymentStatus paymentStatusEnum = PaymentStatus.valueOf(paymentStatus);
            holder.statusText.setText(paymentStatus);
            if (paymentStatusEnum != PaymentStatus.SUCCESS) {
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            }
        } else {
            if (!model.get(position).isOnGoing()) {
                holder.statusText.setText("Expired");
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            }
            if (model.get(position).isOnGoing()) {
                holder.statusText.setText("On-going");
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            }

        }

        holder.layout.setOnClickListener(view -> {
            itemOnClick.itemOnClick(model.get(position).getId());
        });
        if(StringUtil.isBlank(model.get(position).getInvoiceDownloadUrl())){
            holder.downloadInvoice.setVisibility(View.GONE);
        }else{
            holder.downloadInvoice.setVisibility(View.VISIBLE);
        }
        holder.downloadInvoice.setOnClickListener(v -> {
            itemOnClick.invoiceDownloadClick(model.get(position).getInvoiceDownloadUrl());
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title,subtitle,liltitle,price,statusText;
        private RelativeLayout layout;
        private LinearLayout downloadInvoice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            liltitle = itemView.findViewById(R.id.liltitle);
            price = itemView.findViewById(R.id.price);
            statusText = itemView.findViewById(R.id.subscription_status);
            layout = itemView.findViewById(R.id.layout);
            downloadInvoice = itemView.findViewById(R.id.download_invoice);
        }
    }

    public interface ItemOnClick {
        void itemOnClick(String id);
        void invoiceDownloadClick(String url);
    }
}
