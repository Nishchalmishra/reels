package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class NotificationBody implements Serializable {
    private String text;
    private String topic;
    private String imageUrl;
    private String description;
    private String doubtId;
    private boolean askedDoubt;
}
