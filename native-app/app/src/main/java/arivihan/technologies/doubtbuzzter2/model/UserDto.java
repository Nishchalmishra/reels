package arivihan.technologies.doubtbuzzter2.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.android.gms.common.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import arivihan.technologies.doubtbuzzter2.model.user.batch.subscription.SubscriptionInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@ToString
@Nullable
public class UserDto implements Serializable {

    private String userId;
    private String boardId;
    private String boardCode;
    private String boardName;
    private String courseName;
    private String className;
    private String username;
    private String email;
    private Long phoneNumber;
    private Long parentNumber;
    private String classId;
    private String courseId;
    private String secondaryCourseId;
    private String selectedCourseId;
    private String idUrl;
    private String address;
    private String myReferCode;
    private String referralCode;
    private String imageUrl;
    private Boolean active;
    private Boolean subscribed;
    private String currentCoaching;
    private Integer points;
    private Integer freeDoubt;
    private List<String> subjectIds;
    private List<NameAndIdDto> subjects;
    private Boolean syllabusGenerated;
    private Boolean generationInProgress;
    private Boolean askedDoubt;
    private int mockDemoVideoWatchCount;
    private boolean showMockVideo;
    private Boolean campaignSubscribed;
    private Integer boardTargetPercentage;
    private boolean rajasthanBoard;
    private String coachingId;
    private ArivihanLanguageEnum selectedLanguage;
    private boolean goToCoaching;
    private String displayName;
    private String registrationDate;

    private Double latitude;
    private Double longitude;
    private String gender;
    private Map<String, String> allUtmParams;
    private SwitchUserClassCourseRequestDto switchableCourseClassInfo;
    private String fcmToken;

    private SubscriptionInfo subscriptionInfo;
    private String parikshaAdhyayanAccess; // "ACCESS_GRANTED" | "ACCESS_DENIED"

    public String getSubjectSelected() {
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(subjectIds)) {
            for (String subjectId : subjectIds) {
                builder.append(subjectId).append(",");
            }
        }
        return builder.toString();
    }

//    @Override
//    public String toString() {
//        return "UserDto{" +
//                ", phoneNumber=" + phoneNumber +
//                ", selectedLanguage='" + selectedLanguage + '\'' +
//                ", selectedCourseId='" + selectedCourseId + '\'' +
//                ", secondaryCourseId='" + secondaryCourseId + '\'' +
//                ", classId='" + classId + '\'' +
//                ", boardCode='" + boardCode + '\'' +
//                ", courseId='" + courseId + '\'' +
//                ", subjects='" + getSubjectSelected() + '\'' +
//                ", rajasthanBoard='" + rajasthanBoard + '\'' +
//                '}';
//    }
}