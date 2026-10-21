package arivihan.technologies.doubtbuzzter2.model.subject;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubjectListWrapperResponse {

    @SerializedName("canAddOptionalSubject")
    private boolean canAddOptionalSubject;

    @SerializedName("remainingOptionalSubjectSlots")
    private int remainingOptionalSubjectSlots;

    @SerializedName("subjects")
    private List<SubjectResponse> subjects;
}
