package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationDto implements Serializable {
    private int notificationId;
    private String messageBody;
    private String messageTitle;
    private String imageUrl;
    private String groupId;
    private String groupTitle;
    private String navType;
    private String className;
    private String params;
}
