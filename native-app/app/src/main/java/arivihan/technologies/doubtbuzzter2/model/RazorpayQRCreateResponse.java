package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class RazorpayQRCreateResponse {
    private String id;
    private String entity;
    private int created_at;
    private String name;
    private String usage;
    private String type;
    private String image_url;
    private int payment_amount;
    private String status;
    private String description;
    private boolean fixed_amount;
    private int payments_amount_received;
    private int payments_count_received;
    private Notes notes;
    private Object customer_id;
    private Object close_by;
    private ArrayList<Object> tax_invoice;

    @Data
    private class Notes {
        private String purpose;
        private String userId;
        private String orderId;
        private String planId;
        private String transactionId;
    }
}
