package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcelable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseTopicSubscription implements Serializable {
    private int id;
    private String userId;
    private String topic;
    private boolean subscribe;
    private String createdAt;
    private String updatedAt;
}
