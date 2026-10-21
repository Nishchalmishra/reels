package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlansDataModel implements Serializable {

    private String code;
    private String message;
    private boolean success;
    private List<SubscriptionPlanLevel> data;
}
