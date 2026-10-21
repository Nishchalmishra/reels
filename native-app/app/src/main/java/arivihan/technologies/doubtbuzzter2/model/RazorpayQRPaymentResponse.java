package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RazorpayQRPaymentResponse {
    private String entity;
    private int count;
    private List<Item> items;

    @Data
    public static class Item {
        public String id;
        public String entity;
        public Long amount;
        public String currency;
        public String status;
        public String order_id;
        public Object invoice_id;
        public boolean international;
        public String method;
        public Long amount_refunded;
        public Object refund_status;
        public Boolean captured;
        public String description;
        public Object card_id;
        public Object bank;
        public Object wallet;
        public String vpa;
        public Object email;
        public Object contact;
        public Notes notes;
        public int fee;
        public int tax;
        public Object error_code;
        public Object error_description;
        public Object error_source;
        public Object error_step;
        public Object error_reason;
        public AcquirerData acquirer_data;
        public int created_at;
        public Upi upi;
    }

    @Data
    public static class Upi {
        public String payer_account_type;
        public String vpa;
    }

    @Data
    public static class AcquirerData {
        private String rrn;
    }

    @Data
    private static class Notes {
        private String purpose;
        private String userId;
        private String orderId;
        private String planId;
        private String transactionId;
    }
}
