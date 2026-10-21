package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentInfo {
    private String mid;
    private String url;
    private String orderId;

    @Override
    public String toString() {
        return "PaymentInfo{" +
                "mid='" + mid + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
