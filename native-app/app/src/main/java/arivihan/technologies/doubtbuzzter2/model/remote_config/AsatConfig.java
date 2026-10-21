package arivihan.technologies.doubtbuzzter2.model.remote_config;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsatConfig implements Serializable {
    private boolean expiryTimerHidden;
    private boolean couponHTMLTextHidden;
    private boolean bottomBuyOptionHidden;
}
