package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailTestAnalyticsModel implements Serializable {

    String message;
    String code;
    boolean success;
    List<TestAnalyticsData> data;

    @Override
    public String toString() {
        return "UserDetailTestAnalyticsModel{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", success=" + success +
                '}';
    }
}
