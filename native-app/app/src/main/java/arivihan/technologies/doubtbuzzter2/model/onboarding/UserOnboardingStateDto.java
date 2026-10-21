package arivihan.technologies.doubtbuzzter2.model.onboarding;

import com.google.gson.annotations.SerializedName;

import arivihan.technologies.doubtbuzzter2.enums.onboarding.OnboardingScreenName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOnboardingStateDto {

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("selectionCode")
    private String selectionCode;

    @SerializedName("userName")
    private String userName;

    @SerializedName("languageCode")
    private String languageCode;

    @SerializedName("stepName")
    private OnboardingScreenName stepName;

    @SerializedName("examCode")
    private String examCode;

    @SerializedName("examId")
    private Integer examId;

    @SerializedName("gradeCode")
    private String gradeCode;

    @SerializedName("gradeId")
    private Integer gradeId;

    @SerializedName("streamCode")
    private String streamCode;

    @SerializedName("streamId")
    private Integer streamId;

    @SerializedName("academicYearCode")
    private String academicYearCode;

    @SerializedName("academicYearId")
    private Integer academicYearId;

    @SerializedName("neetGradeCode")
    private String neetGradeCode;

    @SerializedName("neetGradeLabel")
    private String neetGradeLabel;

    @SerializedName("onboardingCompleted")
    private Boolean onboardingCompleted;
}
