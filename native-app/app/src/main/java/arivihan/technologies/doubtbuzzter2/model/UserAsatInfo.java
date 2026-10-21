package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAsatInfo implements Serializable {
    String id;
    String userId;
    String userName;
    String classId;
    String courseId;
    String updatedOn;
    boolean subscribed;
    boolean subscriptionPurchased;
    MarksheetInfo marksheetInfo;
    TestInfo testInfo;
    ResultInfo resultInfo;

    @Override
    public String toString() {
        return "UserAsatInfo{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", classId='" + classId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", updatedOn='" + updatedOn + '\'' +
                ", subscribed= "+ subscribed +
                ", subscriptionPurchased= "+ subscriptionPurchased +
                ", marksheetInfo=" + marksheetInfo +
                ", ResultInfo="+ resultInfo +
                ", TestInfo= "+ testInfo +
                '}';
    }
}
