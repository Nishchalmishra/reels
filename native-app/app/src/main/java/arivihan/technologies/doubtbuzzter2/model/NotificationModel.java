package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationModel implements Serializable {

    public String title;
    public String notificationMessage;
    public String imageUrl;
    private String receivedTime;
    private String deepLink;

}
